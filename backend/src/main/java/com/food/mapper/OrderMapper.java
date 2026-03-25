package com.food.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.food.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
        LEFT JOIN biz_product p ON o.product_id = p.product_id
        WHERE o.user_id = #{userId}
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
        LEFT JOIN biz_product p ON o.product_id = p.product_id
        ORDER BY o.create_time DESC
        """)
    List<Order> selectAllOrders();

    /**
     * 根据核销码查询订单
     */
    @Select("""
        SELECT o.*, p.product_name, p.product_image
        FROM biz_order o
        LEFT JOIN biz_product p ON o.product_id = p.product_id
        WHERE o.verify_code = #{verifyCode}
          AND o.order_status = 0
        """)
    Order selectOrderByVerifyCode(@Param("verifyCode") String verifyCode);

    /**
     * 根据核销码查询订单(不限制状态，用于核销预览和错误细分)
     */
    @Select("""
        SELECT o.*, p.product_name, p.product_image
        FROM biz_order o
        LEFT JOIN biz_product p ON o.product_id = p.product_id
        WHERE o.verify_code = #{verifyCode}
        LIMIT 1
        """)
    Order selectOrderByVerifyCodeAnyStatus(@Param("verifyCode") String verifyCode);

    /**
     * 按用户和订单ID查询订单（用于幂等回放）
     */
    @Select("""
        SELECT o.*, p.product_name, p.product_image
        FROM biz_order o
        LEFT JOIN biz_product p ON o.product_id = p.product_id
        WHERE o.order_id = #{orderId}
          AND o.user_id = #{userId}
        LIMIT 1
        """)
    Order selectOrderByIdAndUser(@Param("orderId") Long orderId, @Param("userId") Long userId);
}
