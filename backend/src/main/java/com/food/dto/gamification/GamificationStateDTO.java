package com.food.dto.gamification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GamificationStateDTO {
    private BigDecimal carbonCoins;
    private long treesPlanted;
    private List<RedemptionItemDTO> badges;
    private List<RedemptionItemDTO> coupons;
    private List<RedemptionItemDTO> recentRedemptions;
    /** 已拥有的徽章 code，便于前端置灰 */
    private Map<String, Boolean> ownedBadgeCodes = new HashMap<>();
}
