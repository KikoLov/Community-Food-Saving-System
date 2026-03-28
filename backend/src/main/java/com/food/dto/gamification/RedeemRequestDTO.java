package com.food.dto.gamification;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RedeemRequestDTO {
    @NotBlank
    private String itemCode;
}
