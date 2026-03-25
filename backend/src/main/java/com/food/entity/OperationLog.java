package com.food.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作审计日志
 */
@Data
@TableName("biz_operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long actorUserId;

    private String actorUsername;

    private String actorRole;

    private String actionType;

    private String targetType;

    private Long targetId;

    private String targetName;

    private String detail;

    private LocalDateTime createTime;
}
