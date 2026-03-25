package com.food.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 低碳日志表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_carbon_log")
public class CarbonLog extends BaseEntity {

    /**
     * 日志ID
     */
    @TableId(type = IdType.AUTO)
    private Long logId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 关联订单ID
     */
    private Long orderId;

    /**
     * 本次获得积分
     */
    private BigDecimal carbonPoints;

    /**
     * 本次碳减排量(kg)
     */
    private BigDecimal carbonSaved;

    /**
     * 日志类型: 1-订单获得 2-积分扣减
     */
    private Integer logType;

    /**
     * 描述
     */
    private String description;

    /**
     * 冗余字段：用户名
     */
    private String userName;
}
