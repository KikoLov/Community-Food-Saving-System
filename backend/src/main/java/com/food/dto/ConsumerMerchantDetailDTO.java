package com.food.dto;

import com.food.entity.Merchant;
import com.food.entity.Product;
import lombok.Data;

import java.util.List;

/**
 * 居民端商家详情
 */
@Data
public class ConsumerMerchantDetailDTO {
    private Merchant merchant;
    private List<Product> products;
}
