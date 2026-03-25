package com.food.controller;

import com.food.common.Result;
import com.food.dto.ProductDTO;
import com.food.dto.VerifyCodeDTO;
import com.food.entity.Merchant;
import com.food.entity.Order;
import com.food.entity.Product;
import com.food.exception.RateLimitException;
import com.food.security.LoginUser;
import com.food.service.MerchantService;
import com.food.service.OperationLogService;
import com.food.service.OrderService;
import com.food.service.ProductService;
import com.food.service.CategoryService;
import com.food.service.RateLimitService;
import com.food.entity.Category;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 商家端控制器
 */
@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;
    private final ProductService productService;
    private final OrderService orderService;
    private final CategoryService categoryService;
    private final OperationLogService operationLogService;
    private final RateLimitService rateLimitService;

    /**
     * 获取商户资料
     */
    @GetMapping("/profile")
    public Result<Merchant> getProfile(Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Merchant merchant = merchantService.getMerchantByUserId(loginUser.getUserId());
        return Result.success(merchant);
    }

    /**
     * 更新商户资料
     */
    @PutMapping("/profile")
    public Result<Merchant> updateProfile(Authentication authentication, @RequestBody Merchant merchant) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Merchant updated = merchantService.updateMerchant(loginUser.getUserId(), merchant);
        return Result.success(updated);
    }

    /**
     * 上传营业执照
     */
    @PostMapping("/license/upload")
    public Result<String> uploadLicense(Authentication authentication, @RequestParam MultipartFile file) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        String url = merchantService.uploadLicense(loginUser.getUserId(), file);
        return Result.success(url);
    }

    /**
     * 上传商品图片
     */
    @PostMapping("/products/upload-image")
    public Result<String> uploadProductImage(Authentication authentication, @RequestParam MultipartFile file) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        String url = merchantService.uploadProductImage(loginUser.getUserId(), file);
        return Result.success(url);
    }

    /**
     * 删除商品图片（前端手动移除时调用）
     */
    @DeleteMapping("/products/delete-image")
    public Result<Boolean> deleteProductImage(Authentication authentication, @RequestParam String imageUrl) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Merchant merchant = merchantService.getMerchantByUserId(loginUser.getUserId());
        if (merchant == null) {
            return Result.error("请先完善商户信息");
        }
        boolean ok = merchantService.deleteUploadedProductImage(imageUrl);
        return Result.success(ok);
    }

    /**
     * 提交资质审核
     */
    @PostMapping("/license/submit")
    public Result<Void> submitLicense(Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        merchantService.submitLicenseAudit(loginUser.getUserId());
        return Result.success();
    }

    /**
     * 添加商品
     */
    @PostMapping("/products")
    public Result<Product> addProduct(Authentication authentication, @Valid @RequestBody ProductDTO productDTO) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Merchant merchant = merchantService.getMerchantByUserId(loginUser.getUserId());
        if (merchant == null) {
            return Result.error("请先完善商户信息");
        }
        productDTO.setMerchantId(merchant.getMerchantId());
        Product product = productService.addProduct(productDTO);
        operationLogService.logOperation(
                loginUser,
                "ADD_PRODUCT",
                "PRODUCT",
                product.getProductId(),
                product.getProductName(),
                "商家新增商品"
        );
        return Result.success(product);
    }

    /**
     * 更新商品
     */
    @PutMapping("/products/{id}")
    public Result<Product> updateProduct(Authentication authentication,
                                          @PathVariable("id") Long productId,
                                          @Valid @RequestBody ProductDTO productDTO) {
        productDTO.setProductId(productId);
        Product product = productService.updateProduct(productDTO);
        return Result.success(product);
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/products/{id}")
    public Result<Void> deleteProduct(Authentication authentication, @PathVariable("id") Long productId) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Product existed = productService.getProductById(productId);
        productService.deleteProduct(productId);
        if (existed != null) {
            merchantService.deleteUploadedProductImage(existed.getProductImage());
        }
        operationLogService.logOperation(
                loginUser,
                "DELETE_PRODUCT",
                "PRODUCT",
                productId,
                existed != null ? existed.getProductName() : null,
                "商家删除商品"
        );
        return Result.success();
    }

    /**
     * 批量上/下架商品
     */
    @PutMapping("/products/batch-status")
    public Result<Integer> batchUpdateProductStatus(Authentication authentication,
                                                    @RequestParam List<Long> productIds,
                                                    @RequestParam Integer status) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Merchant merchant = merchantService.getMerchantByUserId(loginUser.getUserId());
        if (merchant == null) {
            return Result.error("请先完善商户信息");
        }
        int affected = productService.batchUpdateStatus(merchant.getMerchantId(), productIds, status);
        operationLogService.logOperation(
                loginUser,
                "BATCH_UPDATE_PRODUCT_STATUS",
                "PRODUCT",
                null,
                null,
                "批量更新商品状态，状态=" + status + "，数量=" + affected
        );
        return Result.success(affected);
    }

    /**
     * 批量删除商品
     */
    @DeleteMapping("/products/batch")
    public Result<Integer> batchDeleteProducts(Authentication authentication,
                                               @RequestParam List<Long> productIds) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Merchant merchant = merchantService.getMerchantByUserId(loginUser.getUserId());
        if (merchant == null) {
            return Result.error("请先完善商户信息");
        }
        List<Product> products = productService.getMerchantProducts(merchant.getMerchantId(), 1, 5000).getRecords();
        products.stream()
                .filter(p -> productIds.contains(p.getProductId()))
                .forEach(p -> merchantService.deleteUploadedProductImage(p.getProductImage()));
        int affected = productService.batchDelete(merchant.getMerchantId(), productIds);
        operationLogService.logOperation(
                loginUser,
                "BATCH_DELETE_PRODUCT",
                "PRODUCT",
                null,
                null,
                "批量删除商品，数量=" + affected
        );
        return Result.success(affected);
    }

    /**
     * 获取商品列表
     */
    @GetMapping("/products")
    public Result<List<Product>> getProducts(Authentication authentication,
                                               @RequestParam(defaultValue = "1") int pageNum,
                                               @RequestParam(defaultValue = "10") int pageSize) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Merchant merchant = merchantService.getMerchantByUserId(loginUser.getUserId());
        if (merchant == null) {
            return Result.success(List.of());
        }
        List<Product> products = productService.getMerchantProducts(merchant.getMerchantId(), pageNum, pageSize).getRecords();
        return Result.success(products);
    }

    /**
     * 获取预警商品列表
     */
    @GetMapping("/products/warning")
    public Result<List<Product>> getWarningProducts(Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Merchant merchant = merchantService.getMerchantByUserId(loginUser.getUserId());
        if (merchant == null) {
            return Result.success(List.of());
        }
        List<Product> products = productService.getWarningProducts(merchant.getMerchantId());
        return Result.success(products);
    }

    /**
     * 批量导入商品
     */
    @PostMapping("/products/import")
    public Result<Map<String, Object>> importProducts(Authentication authentication,
                                                        @RequestParam MultipartFile file) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Merchant merchant = merchantService.getMerchantByUserId(loginUser.getUserId());
        if (merchant == null) {
            return Result.error("请先完善商户信息");
        }
        Map<String, Object> result = merchantService.importProducts(merchant.getMerchantId(), file);
        return Result.success(result);
    }

    /**
     * 获取订单列表
     */
    @GetMapping("/orders")
    public Result<List<Order>> getOrders(Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Merchant merchant = merchantService.getMerchantByUserId(loginUser.getUserId());
        if (merchant == null) {
            return Result.success(List.of());
        }
        List<Order> orders = orderService.getMerchantOrders(merchant.getMerchantId());
        return Result.success(orders);
    }

    /**
     * 核销前预览订单
     */
    @PostMapping("/order/preview")
    public Result<Order> previewVerifyOrder(Authentication authentication,
                                            @Valid @RequestBody VerifyCodeDTO verifyCodeDTO,
                                            HttpServletRequest request) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        String key = "merchant:preview:" + loginUser.getUserId();
        if (!rateLimitService.allow(key, 12, 30)) {
            throw new RateLimitException("核销预览操作过于频繁，请稍后再试");
        }
        Merchant merchant = merchantService.getMerchantByUserId(loginUser.getUserId());
        if (merchant == null) {
            return Result.error("请先完善商户信息");
        }
        Order order = orderService.previewOrderForVerify(verifyCodeDTO.getVerifyCode(), merchant.getMerchantId());
        return Result.success(order);
    }

    /**
     * 核销订单
     */
    @PostMapping("/order/verify")
    public Result<Order> verifyOrder(Authentication authentication,
                                     @Valid @RequestBody VerifyCodeDTO verifyCodeDTO,
                                     HttpServletRequest request) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        String key = "merchant:verify:" + loginUser.getUserId();
        if (!rateLimitService.allow(key, 8, 30)) {
            throw new RateLimitException("核销操作过于频繁，请稍后再试");
        }
        Merchant merchant = merchantService.getMerchantByUserId(loginUser.getUserId());
        if (merchant == null) {
            return Result.error("请先完善商户信息");
        }
        verifyCodeDTO.setMerchantId(merchant.getMerchantId());
        Order order = orderService.verifyOrder(verifyCodeDTO.getVerifyCode(), merchant.getMerchantId());
        operationLogService.logOperation(
                loginUser,
                "VERIFY_ORDER",
                "ORDER",
                order.getOrderId(),
                order.getOrderNo(),
                "商家核销订单，核销码: " + verifyCodeDTO.getVerifyCode()
        );
        return Result.success(order);
    }

    /**
     * 获取商家统计数据
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Merchant merchant = merchantService.getMerchantByUserId(loginUser.getUserId());
        if (merchant == null) {
            return Result.error("请先完善商户信息");
        }
        List<Order> orders = orderService.getMerchantOrders(merchant.getMerchantId());
        int productCount = productService.getMerchantProducts(merchant.getMerchantId(), 1, 2000).getRecords().size();

        long pendingOrderCount = orders.stream().filter(o -> o.getOrderStatus() != null && o.getOrderStatus() == 0).count();
        long verifiedOrderCount = orders.stream().filter(o -> o.getOrderStatus() != null && o.getOrderStatus() == 1).count();
        long todayOrderCount = orders.stream()
                .filter(o -> o.getCreateTime() != null && o.getCreateTime().toLocalDate().equals(LocalDate.now()))
                .count();
        BigDecimal totalCarbonSaved = orders.stream()
                .map(o -> o.getCarbonSaved() != null ? o.getCarbonSaved() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAmount = orders.stream()
                .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long denominator = orders.stream()
                .filter(o -> o.getOrderStatus() != null && (o.getOrderStatus() == 0 || o.getOrderStatus() == 1))
                .count();
        BigDecimal verifyRate = denominator == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(verifiedOrderCount * 100.0 / denominator).setScale(2, RoundingMode.HALF_UP);

        List<Map<String, Object>> orderTrend7d = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            long dayOrderCount = orders.stream()
                    .filter(o -> o.getCreateTime() != null && o.getCreateTime().toLocalDate().equals(day))
                    .count();
            long dayVerifiedCount = orders.stream()
                    .filter(o -> o.getVerifyTime() != null && o.getVerifyTime().toLocalDate().equals(day))
                    .count();
            BigDecimal dayAmount = orders.stream()
                    .filter(o -> o.getCreateTime() != null && o.getCreateTime().toLocalDate().equals(day))
                    .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", day.toString());
            point.put("orderCount", dayOrderCount);
            point.put("verifiedCount", dayVerifiedCount);
            point.put("amount", dayAmount);
            orderTrend7d.add(point);
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("productCount", productCount);
        stats.put("pendingOrderCount", pendingOrderCount);
        stats.put("verifiedOrderCount", verifiedOrderCount);
        stats.put("todayOrderCount", todayOrderCount);
        stats.put("totalCarbonSaved", totalCarbonSaved);
        stats.put("totalAmount", totalAmount);
        stats.put("verifyRate", verifyRate);
        stats.put("orderTrend7d", orderTrend7d);
        return Result.success(stats);
    }

    /**
     * 获取可用商品分类（商家端）
     */
    @GetMapping("/categories")
    public Result<List<Category>> getCategories() {
        return Result.success(categoryService.getAllCategories());
    }
}
