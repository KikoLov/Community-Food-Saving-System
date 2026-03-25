package com.food.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_order")
public class Order extends BaseEntity {

    /**
     * 订单ID
     */
    @TableId(type = IdType.AUTO)
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 买家用户ID
     */
    private Long userId;

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称 (冗余)
     */
    private String productName;

    /**
     * 商品图片 (冗余)
     */
    private String productImage;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 6位核销码
     */
    private String verifyCode;

    /**
     * 订单状态: 0-待核销 1-已核销 2-已取消 3-已过期
     */
    private Integer orderStatus;

    /**
     * 本次订单碳减排量(kg CO2)
     */
    private BigDecimal carbonSaved;

    /**
     * 核销时间
     */
    private LocalDateTime verifyTime;

    /**
     * 冗余字段：买家用户名
     */
    private String userName;

    /**
     * 冗余字段：商户名称
     */
    private String merchantName;
}
