package com.food.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.food.dto.OrderCreateDTO;
import com.food.entity.*;
import com.food.mapper.*;
import com.food.util.DemoTextNormalizeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 订单服务
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final UserProfileMapper userProfileMapper;
    private final CarbonLogMapper carbonLogMapper;
    private final ProductService productService;
    private final UserCouponService userCouponService;

    /**
     * 创建订单
     */
    @Transactional
    public Order createOrder(Long userId, OrderCreateDTO orderDTO) {
        // 查询商品
        Product product = productMapper.selectById(orderDTO.getProductId());
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        if (product.getStatus() != 1) {
            throw new RuntimeException("商品已下架或售罄");
        }
        if (product.getExpireDatetime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("商品已过期");
        }

        BigDecimal subtotal = product.getDiscountPrice().multiply(new BigDecimal(orderDTO.getQuantity()));

        Long userCouponId = null;
        BigDecimal discount = BigDecimal.ZERO;
        String couponSnapshot = null;
        if (orderDTO.getCouponCode() != null && !orderDTO.getCouponCode().isBlank()) {
            var uc = userCouponService.validateForOrder(userId, orderDTO.getCouponCode(), subtotal, product.getMerchantId());
            discount = userCouponService.computeDiscount(uc, subtotal);
            userCouponId = uc.getCouponId();
            couponSnapshot = uc.getCouponCode();
        }
        BigDecimal payable = subtotal.subtract(discount);

        UserProfile userProfile = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId)
        );
        if (userProfile == null) {
            userProfile = new UserProfile();
            userProfile.setUserId(userId);
            userProfile.setCarbonPoints(BigDecimal.ZERO);
            userProfile.setTotalCarbonSaved(BigDecimal.ZERO);
            userProfile.setTotalFoodSaved(BigDecimal.ZERO);
            userProfile.setWalletBalance(new BigDecimal("200.00"));
            userProfileMapper.insert(userProfile);
        } else if (userProfile.getWalletBalance() == null) {
            userProfile.setWalletBalance(new BigDecimal("200.00"));
            userProfileMapper.updateById(userProfile);
        }
        if (userProfile.getWalletBalance().compareTo(payable) < 0) {
            throw new RuntimeException("余额不足，当前余额: " + userProfile.getWalletBalance());
        }

        // 扣减库存
        boolean success = productService.decreaseStock(product.getProductId(), orderDTO.getQuantity());
        if (!success) {
            throw new RuntimeException("库存不足");
        }

        try {
            // 扣减用户钱包余额（实付）
            userProfile.setWalletBalance(userProfile.getWalletBalance().subtract(payable));
            userProfile.setUpdateTime(LocalDateTime.now());
            userProfileMapper.updateById(userProfile);

            // 创建订单
            Order order = new Order();
            order.setOrderNo(generateOrderNo());
            order.setUserId(userId);
            order.setMerchantId(product.getMerchantId());
            order.setProductId(product.getProductId());
            order.setProductName(DemoTextNormalizeUtil.normalizeProductName(product.getProductName()));
            order.setProductImage(product.getProductImage());
            order.setQuantity(orderDTO.getQuantity());
            order.setOriginalAmount(subtotal);
            order.setDiscountAmount(discount);
            order.setTotalAmount(payable);
            order.setUserCouponId(userCouponId);
            order.setCouponCode(couponSnapshot);
            order.setVerifyCode(generateVerifyCode());
            order.setOrderStatus(0); // 待核销
            order.setCreateTime(LocalDateTime.now());

            // 计算碳减排量 (使用默认碳因子 1.5)
            BigDecimal carbonSaved = new BigDecimal(orderDTO.getQuantity()).multiply(new BigDecimal("1.5"))
                    .divide(new BigDecimal("1000"), 4, RoundingMode.HALF_UP); // 假设每件商品1kg
            order.setCarbonSaved(carbonSaved);

            orderMapper.insert(order);

            if (userCouponId != null) {
                userCouponService.markUsed(userCouponId, order.getOrderId());
            }

            return order;
        } catch (Exception e) {
            // 回滚库存
            productService.rollbackStock(product.getProductId(), orderDTO.getQuantity());
            throw new RuntimeException("创建订单失败: " + e.getMessage());
        }
    }

    /**
     * 核销前预览订单并校验
     */
    public Order previewOrderForVerify(String verifyCode, Long merchantId) {
        Order order = orderMapper.selectOrderByVerifyCodeAnyStatus(verifyCode);
        validateVerifyOrder(order, merchantId);
        return order;
    }

    /**
     * 核销订单
     */
    @Transactional
    public Order verifyOrder(String verifyCode, Long merchantId) {
        // 查询并校验订单（带精细错误提示）
        Order order = orderMapper.selectOrderByVerifyCodeAnyStatus(verifyCode);
        validateVerifyOrder(order, merchantId);

        // 更新订单状态
        order.setOrderStatus(1); // 已核销
        order.setVerifyTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 给用户增加低碳积分
        UserProfile userProfile = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, order.getUserId())
        );
        if (userProfile != null) {
            // 碳积分 = 碳减排量 * 10
            BigDecimal carbonPoints = order.getCarbonSaved().multiply(new BigDecimal("10"));

            userProfile.setCarbonPoints(userProfile.getCarbonPoints().add(carbonPoints));
            userProfile.setTotalCarbonSaved(userProfile.getTotalCarbonSaved().add(order.getCarbonSaved()));
            userProfile.setTotalFoodSaved(userProfile.getTotalFoodSaved().add(new BigDecimal(order.getQuantity())));
            userProfile.setUpdateTime(LocalDateTime.now());
            userProfileMapper.updateById(userProfile);

            // 记录低碳日志
            CarbonLog carbonLog = new CarbonLog();
            carbonLog.setUserId(order.getUserId());
            carbonLog.setOrderId(order.getOrderId());
            carbonLog.setCarbonPoints(carbonPoints);
            carbonLog.setCarbonSaved(order.getCarbonSaved());
            carbonLog.setLogType(1); // 订单获得
            carbonLog.setDescription("订单核销获得碳积分");
            carbonLog.setCreateTime(LocalDateTime.now());
            carbonLogMapper.insert(carbonLog);
        }

        return order;
    }

    private void validateVerifyOrder(Order order, Long merchantId) {
        if (order == null) {
            throw new RuntimeException("核销码不存在，请确认后重试");
        }
        if (!order.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("该核销码不属于您的店铺，无法核销");
        }
        if (order.getOrderStatus() == null) {
            throw new RuntimeException("订单状态异常，请联系管理员");
        }
        if (order.getOrderStatus() == 1) {
            throw new RuntimeException("该订单已核销，请勿重复核销");
        }
        if (order.getOrderStatus() == 2) {
            throw new RuntimeException("该订单已取消，不能核销");
        }
        if (order.getOrderStatus() == 3) {
            throw new RuntimeException("该订单已过期，不能核销");
        }
        if (order.getOrderStatus() != 0) {
            throw new RuntimeException("订单状态异常，无法核销");
        }
    }

    /**
     * 获取用户订单列表
     */
    public List<Order> getUserOrders(Long userId) {
        return enrichOrdersWithProductSnapshot(orderMapper.selectOrderListByUser(userId));
    }

    /**
     * 按订单ID和用户ID获取订单（用于幂等回放）
     */
    public Order getUserOrderById(Long userId, Long orderId) {
        return enrichOrderWithProductSnapshot(orderMapper.selectOrderByIdAndUser(orderId, userId));
    }

    /**
     * 获取商家订单列表
     */
    public List<Order> getMerchantOrders(Long merchantId) {
        return enrichOrdersWithProductSnapshot(orderMapper.selectOrderListByMerchant(merchantId));
    }

    /**
     * 获取全平台订单列表（管理端）
     */
    public List<Order> getAllOrders() {
        return enrichOrdersWithProductSnapshot(orderMapper.selectAllOrders());
    }

    /**
     * 取消订单
     */
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权限操作");
        }
        if (order.getOrderStatus() != 0) {
            throw new RuntimeException("订单状态异常，无法取消");
        }

        // 回滚库存
        productService.rollbackStock(order.getProductId(), order.getQuantity());

        // 退还钱包（实付金额）
        UserProfile profile = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId)
        );
        if (profile != null && order.getTotalAmount() != null) {
            BigDecimal bal = profile.getWalletBalance() != null ? profile.getWalletBalance() : BigDecimal.ZERO;
            profile.setWalletBalance(bal.add(order.getTotalAmount()));
            profile.setUpdateTime(LocalDateTime.now());
            userProfileMapper.updateById(profile);
        }

        // 退回优惠券
        userCouponService.restoreIfOrderCancelled(order.getUserCouponId());

        // 更新订单状态
        order.setOrderStatus(2); // 已取消
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    /**
     * 生成订单编号
     */
    private String generateOrderNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "ORD" + date + random;
    }

    /**
     * 生成6位核销码
     */
    private String generateVerifyCode() {
        return String.format("%06d", new Random().nextInt(1000000));
    }

    /**
     * 若历史订单缺少商品名称/图片，则自动用当前商品信息补齐并回写，便于前端演示展示
     */
    private List<Order> enrichOrdersWithProductSnapshot(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return orders;
        }
        List<Order> result = new ArrayList<>(orders.size());
        for (Order order : orders) {
            result.add(enrichOrderWithProductSnapshot(order));
        }
        return result;
    }

    private Order enrichOrderWithProductSnapshot(Order order) {
        if (order == null) {
            return null;
        }
        boolean missingName = order.getProductName() == null || order.getProductName().isBlank();
        boolean missingImage = order.getProductImage() == null || order.getProductImage().isBlank();
        if (!missingName && !missingImage) {
            return order;
        }
        Product product = productMapper.selectById(order.getProductId());
        boolean changed = false;
        if (product != null) {
            if (missingName && product.getProductName() != null && !product.getProductName().isBlank()) {
                order.setProductName(product.getProductName());
                changed = true;
            }
            if (missingImage && product.getProductImage() != null && !product.getProductImage().isBlank()) {
                order.setProductImage(product.getProductImage());
                changed = true;
            }
        }
        if (missingImage && (order.getProductImage() == null || order.getProductImage().isBlank())) {
            String name = product != null && product.getProductName() != null && !product.getProductName().isBlank()
                    ? product.getProductName()
                    : order.getProductName();
            String desc = product != null ? product.getDescription() : null;
            // 仅用于返回给前端展示，避免把超长 DataURI 写入数据库导致截断报错
            order.setProductImage(buildAiLikePlaceholder(name, desc));
        }
        if (changed) {
            Order patch = new Order();
            patch.setOrderId(order.getOrderId());
            patch.setProductName(order.getProductName());
            patch.setProductImage(order.getProductImage());
            patch.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(patch);
        }
        return DemoTextNormalizeUtil.normalizeOrder(order);
    }

    /**
     * 生成AI风格占位图（Data URI），用于历史演示订单缺图兜底
     */
    private String buildAiLikePlaceholder(String productName, String productDesc) {
        String title = productName == null || productName.isBlank() ? "Food Item" : productName;
        if (title.length() > 12) {
            title = title.substring(0, 12);
        }
        String safeTitle = title
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
        String semanticText = ((productName == null ? "" : productName) + " " + (productDesc == null ? "" : productDesc)).toLowerCase();
        String c1 = "#e8f5e9";
        String c2 = "#81c784";
        String tag = "Food";
        String icon = "🍱";
        if (semanticText.contains("bread") || semanticText.contains("cake") || semanticText.contains("bakery")
                || semanticText.contains("面包") || semanticText.contains("蛋糕") || semanticText.contains("烘焙")) {
            c1 = "#fff3e0";
            c2 = "#ffb74d";
            tag = "Bakery";
            icon = "🥐";
        } else if (semanticText.contains("milk") || semanticText.contains("yogurt") || semanticText.contains("dairy")
                || semanticText.contains("牛奶") || semanticText.contains("酸奶") || semanticText.contains("乳")) {
            c1 = "#e3f2fd";
            c2 = "#90caf9";
            tag = "Dairy";
            icon = "🥛";
        } else if (semanticText.contains("drink") || semanticText.contains("tea") || semanticText.contains("coffee")
                || semanticText.contains("饮料") || semanticText.contains("果汁") || semanticText.contains("茶") || semanticText.contains("咖啡")) {
            c1 = "#f3e5f5";
            c2 = "#ce93d8";
            tag = "Drink";
            icon = "🧃";
        } else if (semanticText.contains("fruit") || semanticText.contains("vegetable") || semanticText.contains("fresh")
                || semanticText.contains("水果") || semanticText.contains("蔬菜") || semanticText.contains("生鲜")) {
            c1 = "#e8f5e9";
            c2 = "#66bb6a";
            tag = "Fresh";
            icon = "🍎";
        } else if (semanticText.contains("rice") || semanticText.contains("noodle") || semanticText.contains("meal")
                || semanticText.contains("套餐") || semanticText.contains("米饭") || semanticText.contains("面")) {
            c1 = "#f1f8e9";
            c2 = "#9ccc65";
            tag = "Meal";
            icon = "🍛";
        }
        String svg = "<svg xmlns='http://www.w3.org/2000/svg' width='320' height='180'>"
                + "<defs><linearGradient id='g' x1='0' y1='0' x2='1' y2='1'>"
                + "<stop offset='0%' stop-color='" + c1 + "'/>"
                + "<stop offset='100%' stop-color='" + c2 + "'/>"
                + "</linearGradient></defs>"
                + "<rect width='320' height='180' rx='16' fill='url(#g)'/>"
                + "<rect x='18' y='18' width='284' height='96' rx='12' fill='rgba(255,255,255,0.55)'/>"
                + "<circle cx='160' cy='66' r='28' fill='rgba(255,255,255,0.68)'/>"
                + "<text x='160' y='75' font-size='28' text-anchor='middle'>" + icon + "</text>"
                + "<text x='28' y='145' font-size='20' font-family='Arial' fill='#1f2937'>" + safeTitle + "</text>"
                + "<text x='28' y='166' font-size='14' font-family='Arial' fill='#4b5563'>" + tag + " AI Style</text>"
                + "</svg>";
        return "data:image/svg+xml;utf8," + URLEncoder.encode(svg, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
