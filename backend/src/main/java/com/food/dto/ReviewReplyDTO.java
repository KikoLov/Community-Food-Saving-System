package com.food.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 商家回复评价
 */
@Data
public class ReviewReplyDTO {

    @NotBlank(message = "回复内容不能为空")
    private String replyContent;
}

