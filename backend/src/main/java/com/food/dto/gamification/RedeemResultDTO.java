package com.food.dto.gamification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedeemResultDTO {
    private BigDecimal balanceAfter;
    private String message;
    private RedemptionItemDTO redemption;
}
