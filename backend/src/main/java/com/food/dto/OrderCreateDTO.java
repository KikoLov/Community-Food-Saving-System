package com.food.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

/**
 * 订单创建DTO
 */
@Data
public class OrderCreateDTO {

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /**
     * 数量
     */
    @NotNull(message = "数量不能为空")
    @Positive(message = "数量必须为正数")
    private Integer quantity;

    /**
     * 可选：碳积分商城兑换的优惠券码（下单自动抵扣）
     */
    private String couponCode;
}
