package com.food.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 核销DTO
 */
@Data
public class VerifyCodeDTO {

    /**
     * 核销码
     */
    @NotBlank(message = "核销码不能为空")
    @Size(min = 6, max = 6, message = "核销码为6位数字")
    private String verifyCode;

    /**
     * 商户ID
     */
    private Long merchantId;
}
