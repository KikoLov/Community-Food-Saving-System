package com.food.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 订单评价表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_review")
public class Review extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long reviewId;

    private Long orderId;

    private Long userId;

    private Long merchantId;

    private Long productId;

    private String productName;

    /**
     * 评分 1-5
     */
    private Integer rating;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 评价图片URL（可选，单张）
     */
    private String imageUrl;

    /**
     * 商家回复
     */
    private String replyContent;

    private LocalDateTime replyTime;

    @TableField(exist = false)
    private String userName;
}

