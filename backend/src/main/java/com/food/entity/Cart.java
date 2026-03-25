package com.food.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 购物车表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_cart")
public class Cart extends BaseEntity {

    /**
     * 购物车ID
     */
    @TableId(type = IdType.AUTO)
    private Long cartId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 冗余字段：商品名称
     */
    private String productName;

    /**
     * 冗余字段：商品图片
     */
    private String productImage;

    /**
     * 冗余字段：折扣价
     */
    private java.math.BigDecimal discountPrice;

    /**
     * 冗余字段：过期时间
     */
    private java.time.LocalDateTime expireDatetime;

    /**
     * 冗余字段：库存
     */
    private Integer stock;
}
