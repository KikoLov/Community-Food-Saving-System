package com.food.dto;

import lombok.Data;

/**
 * 居民端社区商家卡片信息
 */
@Data
public class CommunityMerchantDTO {
    private Long merchantId;
    private String merchantName;
    private Long communityId;
    private Integer productCount;
}
