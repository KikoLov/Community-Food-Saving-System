package com.food.controller;

import com.food.common.Result;
import com.food.dto.LoginDTO;
import com.food.dto.RegisterDTO;
import com.food.entity.User;
import com.food.exception.RateLimitException;
import com.food.service.AuthService;
import com.food.service.RateLimitService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RateLimitService rateLimitService;

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String key = "auth:login:" + ip + ":" + loginDTO.getUsername();
        if (!rateLimitService.allow(key, 8, 60)) {
            throw new RateLimitException("登录过于频繁，请稍后再试");
        }
        String token = authService.login(loginDTO);
        return Result.success(token);
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO registerDTO) {
        authService.register(registerDTO);
        return Result.success();
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<User> getUserInfo(Authentication authentication) {
        User user = authService.getCurrentUser(authentication.getName());
        user.setPassword(null); // 隐藏密码
        return Result.success(user);
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        // JWT无状态，客户端删除token即可
        return Result.success();
    }
}
