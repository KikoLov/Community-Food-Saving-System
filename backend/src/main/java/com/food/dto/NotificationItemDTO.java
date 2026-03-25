package com.food.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 站内提醒项
 */
@Data
public class NotificationItemDTO {
    private String id;
    private String title;
    private String content;
    private String level; // info / warning / danger / success
    private String targetPath;
    private LocalDateTime createTime;
}
