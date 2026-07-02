package com.food.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.food.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单Mapper
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 查询用户的订单列表
     */
    @Select("""
        SELECT o.*, p.product_name, p.product_image
        FROM biz_order o
        LEFT JOIN biz_product p ON o.product_id = p.product_id AND (p.deleted IS NULL OR p.deleted = 0)
        WHERE o.user_id = #{userId}
          AND (o.deleted IS NULL OR o.deleted = 0)
        ORDER BY o.create_time DESC
        """)
    List<Order> selectOrderListByUser(@Param("userId") Long userId);

    /**
     * 查询商家的订单列表
     */
    @Select("""
        SELECT o.*, u.user_name as userName
        FROM biz_order o
        LEFT JOIN sys_user u ON o.user_id = u.user_id
        WHERE o.merchant_id = #{merchantId}
          AND (o.deleted IS NULL OR o.deleted = 0)
        ORDER BY o.create_time DESC
        """)
    List<Order> selectOrderListByMerchant(@Param("merchantId") Long merchantId);

    /**
     * 查询全平台订单列表（管理端）
     */
    @Select("""
        SELECT o.*, u.user_name as userName, p.product_name, p.product_image
        FROM biz_order o
        LEFT JOIN sys_user u ON o.user_id = u.user_id
        LEFT JOIN biz_product p ON o.product_id = p.product_id AND (p.deleted IS NULL OR p.deleted = 0)
        WHERE (o.deleted IS NULL OR o.deleted = 0)
        ORDER BY o.create_time DESC
        """)
    List<Order> selectAllOrders();

    /**
     * 根据核销码查询订单
     */
    @Select("""
        SELECT o.*, p.product_name, p.product_image
        FROM biz_order o
        LEFT JOIN biz_product p ON o.product_id = p.product_id AND (p.deleted IS NULL OR p.deleted = 0)
        WHERE o.verify_code = #{verifyCode}
          AND o.order_status = 0
          AND (o.deleted IS NULL OR o.deleted = 0)
        """)
    Order selectOrderByVerifyCode(@Param("verifyCode") String verifyCode);

    /**
     * 根据核销码查询订单(不限制状态，用于核销预览和错误细分)
     */
    @Select("""
        SELECT o.*, p.product_name, p.product_image
        FROM biz_order o
        LEFT JOIN biz_product p ON o.product_id = p.product_id AND (p.deleted IS NULL OR p.deleted = 0)
        WHERE o.verify_code = #{verifyCode}
          AND (o.deleted IS NULL OR o.deleted = 0)
        LIMIT 1
        """)
    Order selectOrderByVerifyCodeAnyStatus(@Param("verifyCode") String verifyCode);

    /**
     * 按用户和订单ID查询订单（用于幂等回放）
     */
    @Select("""
        SELECT o.*, p.product_name, p.product_image
        FROM biz_order o
        LEFT JOIN biz_product p ON o.product_id = p.product_id AND (p.deleted IS NULL OR p.deleted = 0)
        WHERE o.order_id = #{orderId}
          AND o.user_id = #{userId}
          AND (o.deleted IS NULL OR o.deleted = 0)
        LIMIT 1
        """)
    Order selectOrderByIdAndUser(@Param("orderId") Long orderId, @Param("userId") Long userId);

    @Select("""
            SELECT COALESCE(SUM(total_amount), 0) FROM biz_order
            WHERE merchant_id = #{merchantId} AND deleted = 0
              AND DATE(create_time) = CURDATE()
            """)
    BigDecimal sumTodaySalesByMerchant(@Param("merchantId") Long merchantId);

    @Select("""
            SELECT COUNT(*) FROM biz_order
            WHERE merchant_id = #{merchantId} AND deleted = 0
              AND DATE(create_time) = CURDATE()
            """)
    Long countTodayOrdersByMerchant(@Param("merchantId") Long merchantId);

    @Select("""
            SELECT COALESCE(SUM(total_amount), 0) FROM biz_order
            WHERE merchant_id = #{merchantId} AND deleted = 0
              AND YEAR(create_time) = YEAR(CURDATE())
              AND MONTH(create_time) = MONTH(CURDATE())
            """)
    BigDecimal sumMonthSalesByMerchant(@Param("merchantId") Long merchantId);

    @Select("""
            SELECT COUNT(*) FROM biz_order
            WHERE merchant_id = #{merchantId} AND deleted = 0
              AND YEAR(create_time) = YEAR(CURDATE())
              AND MONTH(create_time) = MONTH(CURDATE())
            """)
    Long countMonthOrdersByMerchant(@Param("merchantId") Long merchantId);

    @Select("""
            SELECT COUNT(*) FROM biz_order
            WHERE merchant_id = #{merchantId} AND deleted = 0 AND order_status = 0
            """)
    Long countPendingByMerchant(@Param("merchantId") Long merchantId);

    @Select("""
            SELECT COUNT(*) FROM biz_order
            WHERE merchant_id = #{merchantId} AND deleted = 0 AND order_status = 1
            """)
    Long countVerifiedByMerchant(@Param("merchantId") Long merchantId);

    /**
     * 用户已核销订单的碳减排合计（与 profile 不同步时以订单为准）
     */
    @Select("""
            SELECT IFNULL(SUM(o.carbon_saved), 0)
            FROM biz_order o
            WHERE o.user_id = #{userId}
              AND o.order_status = 1
              AND (o.deleted IS NULL OR o.deleted = 0)
            """)
    BigDecimal sumCarbonSavedVerifiedByUser(@Param("userId") Long userId);

    /**
     * 用户已核销订单的商品件数合计（作为累计挽救食品 kg 的展示口径，与核销入账逻辑一致）
     */
    @Select("""
            SELECT IFNULL(SUM(o.quantity), 0)
            FROM biz_order o
            WHERE o.user_id = #{userId}
              AND o.order_status = 1
              AND (o.deleted IS NULL OR o.deleted = 0)
            """)
    BigDecimal sumQuantityVerifiedByUser(@Param("userId") Long userId);

    /**
     * 已核销订单产生的碳积分合计：逐单 ROUND(实付×0.5,2) 再求和，与核销入账规则一致
     */
    @Select("""
            SELECT IFNULL(SUM(ROUND(COALESCE(o.total_amount, 0) * 0.5, 2)), 0)
            FROM biz_order o
            WHERE o.user_id = #{userId}
              AND o.order_status = 1
              AND (o.deleted IS NULL OR o.deleted = 0)
            """)
    BigDecimal sumCarbonPointsEarnedFromVerifiedOrders(@Param("userId") Long userId);
}
