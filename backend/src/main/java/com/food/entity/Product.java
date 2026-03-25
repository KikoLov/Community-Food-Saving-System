package com.food.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 商品信息表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_product")
public class Product extends BaseEntity {

    /**
     * 商品ID
     */
    @TableId(type = IdType.AUTO)
    private Long productId;

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品图片URL
     */
    private String productImage;

    /**
     * 原价
     */
    private BigDecimal originalPrice;

    /**
     * 折扣价
     */
    private BigDecimal discountPrice;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 单位
     */
    private String unit;

    /**
     * 过期日期
     */
    private LocalDate expireDate;

    /**
     * 过期时间(精确到时分)
     */
    private LocalDateTime expireDatetime;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 预警小时数(距离过期多久开始预警)
     */
    private Integer warningHours;

    /**
     * 状态: 0-下架 1-上架 2-已售罄
     */
    private Integer status;

    /**
     * 冗余字段：分类名称（用于列表展示）
     */
    @TableField(exist = false)
    private String categoryName;

    /**
     * 冗余字段：商户名称（用于列表展示）
     */
    @TableField(exist = false)
    private String merchantName;
}
