package com.food.dto;

import lombok.Data;

import java.util.List;

/**
 * 购物车结算请求
 */
@Data
public class CartCheckoutDTO {
    /**
     * 需要结算的购物车ID列表；为空时默认结算全部
     */
    private List<Long> cartIds;
}
