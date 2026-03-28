package com.food.dto.gamification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GamificationCatalogItemDTO {
    private String code;
    private String category;
    private String title;
    private String subtitle;
    private BigDecimal cost;
    /** emoji */
    private String icon;
}
