package com.food.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 商家评分摘要
 */
@Data
@AllArgsConstructor
public class MerchantRatingSummaryDTO {
    private Long merchantId;
    private Integer totalReviews;
    private Double avgRating;
    private Double goodRate;
}

