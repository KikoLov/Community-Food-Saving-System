package com.food.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 用户优惠券（碳积分商城兑换，下单抵扣）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_user_coupon")
public class UserCoupon extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long couponId;

    private Long userId;

    private Long redemptionId;

    private String couponCode;

    private String sourceItemCode;

    private String category;

    private BigDecimal minAmount;

    private BigDecimal discountAmount;

    /**
     * 非空时仅该商户订单可用；NULL 表示不限制商户（平台券或全合作商户）
     */
    private Long merchantScopeId;

    /**
     * 0 未使用 1 已使用
     */
    private Integer status;

    private Long usedOrderId;
}
