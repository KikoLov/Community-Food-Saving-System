package com.food.controller;

import com.food.common.Result;
import com.food.dto.NotificationItemDTO;
import com.food.security.LoginUser;
import com.food.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 站内提醒控制器
 */
@RestController
@RequestMapping("/api/notify")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/list")
    public Result<List<NotificationItemDTO>> list(Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return Result.success(notificationService.getNotifications(loginUser));
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Integer>> unreadCount(Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        int count = notificationService.getUnreadCount(loginUser);
        return Result.success(Map.of("count", count));
    }
}
