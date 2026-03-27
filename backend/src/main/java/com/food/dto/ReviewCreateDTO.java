package com.food.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 顾客提交评价
 */
@Data
public class ReviewCreateDTO {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最少1星")
    @Max(value = 5, message = "评分最多5星")
    private Integer rating;

    private String content;

    private String imageUrl;
}

