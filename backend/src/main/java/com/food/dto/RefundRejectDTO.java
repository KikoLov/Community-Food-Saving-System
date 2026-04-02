package com.food.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商家拒绝退款
 */
@Data
public class RefundRejectDTO {

    @NotBlank(message = "请填写拒绝理由")
    @Size(max = 500, message = "理由不超过500字")
    private String reason;
}
