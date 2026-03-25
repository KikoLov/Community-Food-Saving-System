package com.food.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.food.entity.User;
import com.food.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {
    
    private final UserMapper userMapper;
    
    @GetMapping("/users")
    public Map<String, Object> getAllUsers() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<User> users = userMapper.selectList(new LambdaQueryWrapper<>());
            result.put("code", 200);
            result.put("msg", "Success");
            result.put("data", users);
            return result;
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
            result.put("error", e.getClass().getName());
            return result;
        }
    }

    @GetMapping("/user/{username}")
    public Map<String, Object> getUser(@PathVariable String username) {
        Map<String, Object> result = new HashMap<>();
        try {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUserName, username);
            User user = userMapper.selectOne(wrapper);

            if (user != null) {
                result.put("code", 200);
                result.put("msg", "User found");
                Map<String, Object> userData = new HashMap<>();
                userData.put("userId", user.getUserId());
                userData.put("userName", user.getUserName());
                userData.put("nickName", user.getNickName());
                userData.put("userType", user.getUserType());
                userData.put("status", user.getStatus());
                userData.put("delFlag", user.getDelFlag());
                result.put("data", userData);
            } else {
                result.put("code", 404);
                result.put("msg", "User not found");
                result.put("data", null);
            }
            return result;
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
            result.put("error", e.getClass().getName());
            return result;
        }
    }
}
