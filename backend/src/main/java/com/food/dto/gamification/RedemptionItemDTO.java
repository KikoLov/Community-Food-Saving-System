package com.food.dto.gamification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedemptionItemDTO {
    private Long redemptionId;
    private String itemCode;
    private String category;
    private String title;
    private BigDecimal pointsCost;
    private String detail;
    private LocalDateTime createTime;
}
