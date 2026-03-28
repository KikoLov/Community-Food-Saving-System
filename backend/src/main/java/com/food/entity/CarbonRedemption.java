package com.food.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 碳积分兑换记录（虚拟树、徽章、优惠券等）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_carbon_redemption")
public class CarbonRedemption extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long redemptionId;

    private Long userId;

    /**
     * 与目录项 code 一致，如 virtual_tree、badge_green
     */
    private String itemCode;

    /**
     * TREE / BADGE / COUPON_PLATFORM / COUPON_MERCHANT
     */
    private String category;

    private BigDecimal pointsCost;

    private String title;

    /**
     * 优惠券码或 JSON 说明
     */
    private String detail;
}
