package com.food.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_order")
public class Order extends BaseEntity {

    /**
     * 订单ID
     */
    @TableId(type = IdType.AUTO)
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 买家用户ID
     */
    private Long userId;

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称 (冗余)
     */
    private String productName;

    /**
     * 商品图片 (冗余)
     */
    private String productImage;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 使用的用户优惠券ID（biz_user_coupon）
     */
    private Long userCouponId;

    /**
     * 优惠前小计（未用券时通常等于 totalAmount）
     */
    private BigDecimal originalAmount;

    /**
     * 本单优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 券码快照
     */
    private String couponCode;

    /**
     * 6位核销码
     */
    private String verifyCode;

    /**
     * 订单状态: 0-待核销 1-已核销 2-已取消 3-已过期 4-已退款
     */
    private Integer orderStatus;

    /**
     * 退款申请: 0-无 1-待审 2-已拒 3-已完成
     */
    private Integer refundApplyStatus;

    /**
     * 顾客申请退款时间
     */
    private LocalDateTime refundApplyTime;

    /**
     * 商家拒绝退款理由
     */
    private String refundRejectReason;

    /**
     * 商家处理退款时间
     */
    private LocalDateTime refundAuditTime;

    /**
     * 本次订单碳减排量(kg CO2)
     */
    private BigDecimal carbonSaved;

    /**
     * 核销时间
     */
    private LocalDateTime verifyTime;

    /**
     * 冗余字段：买家用户名
     */
    @TableField(exist = false)
    private String userName;

    /**
     * 冗余字段：商户名称
     */
    @TableField(exist = false)
    private String merchantName;

    /**
     * 是否盲盒商品: 0-普通商品 1-盲盒商品
     */
    private Integer surpriseBag;

    /**
     * 盲盒名义价值（元）
     */
    private java.math.BigDecimal bagValue;
}
