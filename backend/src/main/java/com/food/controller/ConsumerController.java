package com.food.controller;

import com.food.common.Result;
import com.food.dto.CartCheckoutDTO;
import com.food.dto.MerchantRatingSummaryDTO;
import com.food.dto.OrderCreateDTO;
import com.food.dto.ReviewCreateDTO;
import com.food.entity.*;
import com.food.exception.RateLimitException;
import com.food.security.LoginUser;
import com.food.service.*;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 居民端控制器
 */
@RestController
@RequestMapping("/api/consumer")
@RequiredArgsConstructor
public class ConsumerController {

    private final ProductService productService;
    private final CartService cartService;
    private final OrderService orderService;
    private final CarbonService carbonService;
    private final CommunityService communityService;
    private final RateLimitService rateLimitService;
    private final IdempotencyService idempotencyService;
    private final ReviewService reviewService;
    private final UserCouponService userCouponService;

    /**
     * 获取商品列表(按社区)
     */
    @GetMapping("/products")
    public Result<List<Product>> getProducts(@RequestParam(required = false) Long communityId) {
        if (communityId == null) {
            return Result.error("请先选择社区");
        }
        List<Product> products = productService.getProductsByCommunity(communityId);
        return Result.success(products);
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/products/{id}")
    public Result<Product> getProductDetail(@PathVariable("id") Long productId) {
        Product product = productService.getProductById(productId);
        return Result.success(product);
    }

    /**
     * 添加到购物车
     */
    @PostMapping("/cart/add")
    public Result<Cart> addToCart(Authentication authentication,
                                   @RequestParam Long productId,
                                   @RequestParam(defaultValue = "1") Integer quantity) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Cart cart = cartService.addToCart(loginUser.getUserId(), productId, quantity);
        return Result.success(cart);
    }

    /**
     * 获取购物车列表
     */
    @GetMapping("/cart")
    public Result<List<Cart>> getCart(Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<Cart> cartList = cartService.getCartList(loginUser.getUserId());
        return Result.success(cartList);
    }

    /**
     * 更新购物车数量
     */
    @PutMapping("/cart/{id}")
    public Result<Cart> updateCart(Authentication authentication,
                                   @PathVariable("id") Long cartId,
                                   @RequestParam Integer quantity) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Cart cart = cartService.updateCartQuantity(cartId, loginUser.getUserId(), quantity);
        return Result.success(cart);
    }

    /**
     * 删除购物车商品
     */
    @DeleteMapping("/cart/{id}")
    public Result<Void> deleteCart(Authentication authentication, @PathVariable("id") Long cartId) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        cartService.deleteCartItem(cartId, loginUser.getUserId());
        return Result.success();
    }

    /**
     * 购物车结算
     */
    @PostMapping("/cart/checkout")
    public Result<List<Order>> checkoutCart(Authentication authentication,
                                            @RequestBody(required = false) CartCheckoutDTO dto) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<Long> cartIds = dto == null ? null : dto.getCartIds();
        String couponCode = dto == null ? null : dto.getCouponCode();
        List<Order> orders = cartService.checkoutCart(loginUser.getUserId(), cartIds, couponCode);
        return Result.success(orders);
    }

    /**
     * 未使用的优惠券（结算时可选用）
     */
    @GetMapping("/coupons")
    public Result<List<UserCoupon>> myCoupons(Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return Result.success(userCouponService.listUnusedForUser(loginUser.getUserId()));
    }

    /**
     * 创建订单
     */
    @PostMapping("/order/create")
    public Result<Order> createOrder(Authentication authentication,
                                     @Valid @RequestBody OrderCreateDTO orderDTO,
                                     HttpServletRequest request) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        String key = "consumer:create-order:" + loginUser.getUserId();
        if (!rateLimitService.allow(key, 5, 10)) {
            throw new RateLimitException("下单操作过于频繁，请稍后重试");
        }
        String idempotencyHeader = request.getHeader("X-Idempotency-Key");
        String idempotencyKey = (idempotencyHeader != null && !idempotencyHeader.isBlank())
                ? ("order:idem:" + loginUser.getUserId() + ":" + idempotencyHeader)
                : buildIdempotencyKey(loginUser.getUserId(), orderDTO);
        Long existedOrderId = idempotencyService.checkDone(idempotencyKey);
        if (existedOrderId != null) {
            Order existed = orderService.getUserOrderById(loginUser.getUserId(), existedOrderId);
            if (existed != null) {
                return Result.success(existed);
            }
        }
        if (!idempotencyService.tryBegin(idempotencyKey)) {
            if (idempotencyService.isProcessing(idempotencyKey)) {
                throw new RuntimeException("订单正在处理中，请勿重复提交");
            }
            Long doneId = idempotencyService.checkDone(idempotencyKey);
            if (doneId != null) {
                Order done = orderService.getUserOrderById(loginUser.getUserId(), doneId);
                if (done != null) {
                    return Result.success(done);
                }
            }
        }
        try {
            Order order = orderService.createOrder(loginUser.getUserId(), orderDTO);
            idempotencyService.complete(idempotencyKey, order.getOrderId());
            return Result.success(order);
        } catch (RuntimeException ex) {
            idempotencyService.clear(idempotencyKey);
            throw ex;
        }
    }

    private String buildIdempotencyKey(Long userId, OrderCreateDTO orderDTO) {
        long bucket = System.currentTimeMillis() / 8000L; // 8秒窗口，防止连续重复点击
        return "order:" + userId + ":" + orderDTO.getProductId() + ":" + orderDTO.getQuantity() + ":" + bucket;
    }

    /**
     * 获取我的订单列表
     */
    @GetMapping("/orders")
    public Result<List<Order>> myOrders(Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<Order> orders = orderService.getUserOrders(loginUser.getUserId());
        return Result.success(orders);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/order/{id}")
    public Result<Order> getOrderDetail(Authentication authentication, @PathVariable("id") Long orderId) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<Order> orders = orderService.getUserOrders(loginUser.getUserId());
        Order order = orders.stream().filter(o -> o.getOrderId().equals(orderId)).findFirst().orElse(null);
        return Result.success(order);
    }

    /**
     * 取消订单
     */
    @PostMapping("/order/{id}/cancel")
    public Result<Void> cancelOrder(Authentication authentication, @PathVariable("id") Long orderId) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        orderService.cancelOrder(orderId, loginUser.getUserId());
        return Result.success();
    }

    /**
     * 获取低碳中心信息
     */
    @GetMapping("/carbon")
    public Result<Map<String, Object>> carbonCenter(Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        UserProfile profile = carbonService.getUserCarbonInfo(loginUser.getUserId());
        List<CarbonLog> logs = carbonService.getUserCarbonLogs(loginUser.getUserId());
        return Result.success(Map.of(
                "profile", profile != null ? profile : new UserProfile(),
                "logs", logs
        ));
    }

    /**
     * 绑定社区
     */
    @PostMapping("/community/bind")
    public Result<Void> bindCommunity(Authentication authentication, @RequestParam Long communityId) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        carbonService.bindCommunity(loginUser.getUserId(), communityId);
        return Result.success();
    }

    /**
     * 获取社区列表
     */
    @GetMapping("/communities")
    public Result<List<Community>> communities() {
        List<Community> communities = communityService.getAllCommunities();
        return Result.success(communities);
    }

    /**
     * 上传评价图片
     */
    @PostMapping("/reviews/upload-image")
    public Result<String> uploadReviewImage(@RequestParam MultipartFile file) {
        return Result.success(reviewService.uploadReviewImage(file));
    }

    /**
     * 提交评价
     */
    @PostMapping("/reviews")
    public Result<Review> createReview(Authentication authentication, @Valid @RequestBody ReviewCreateDTO dto) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Review review = reviewService.createReview(loginUser.getUserId(), dto);
        return Result.success(review);
    }

    /**
     * 我的评价列表
     */
    @GetMapping("/reviews/my")
    public Result<List<Review>> myReviews(Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return Result.success(reviewService.getUserReviews(loginUser.getUserId()));
    }

    /**
     * 商家评分摘要（好评率）
     */
    @GetMapping("/reviews/merchant-summary")
    public Result<MerchantRatingSummaryDTO> merchantSummary(@RequestParam Long merchantId) {
        return Result.success(reviewService.getMerchantSummary(merchantId));
    }

    /**
     * 商家最近评价（用于商品页展示）
     */
    @GetMapping("/reviews/merchant-latest")
    public Result<List<Review>> merchantLatest(@RequestParam Long merchantId,
                                               @RequestParam(defaultValue = "5") Integer limit) {
        return Result.success(reviewService.getLatestMerchantReviews(merchantId, limit));
    }
}
