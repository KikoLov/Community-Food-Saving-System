package com.food.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 商品DTO - 用于创建/更新商品
 */
@Data
public class ProductDTO {

    /**
     * 商品ID (更新时使用)
     */
    private Long productId;

    /**
     * 商户ID
     */
    @NotNull(message = "商户ID不能为空")
    private Long merchantId;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 商品名称
     */
    @NotBlank(message = "商品名称不能为空")
    private String productName;

    /**
     * 商品图片URL
     */
    private String productImage;

    /**
     * 原价
     */
    @NotNull(message = "原价不能为空")
    @Positive(message = "原价必须为正数")
    private BigDecimal originalPrice;

    /**
     * 折扣价
     */
    @NotNull(message = "折扣价不能为空")
    @Positive(message = "折扣价必须为正数")
    private BigDecimal discountPrice;

    /**
     * 库存数量
     */
    @NotNull(message = "库存不能为空")
    @Positive(message = "库存必须为正数")
    private Integer stock;

    /**
     * 单位
     */
    private String unit;

    /**
     * 过期日期
     */
    @NotNull(message = "过期日期不能为空")
    private LocalDate expireDate;

    /**
     * 过期时间(精确到时分)
     */
    @NotNull(message = "过期时间不能为空")
    private LocalDateTime expireDatetime;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 预警小时数
     */
    private Integer warningHours;

    /**
     * 状态: 0-下架 1-上架
     */
    private Integer status;
}
