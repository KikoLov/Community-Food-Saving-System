package com.food.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.food.dto.LoginDTO;
import com.food.dto.RegisterDTO;
import com.food.entity.Merchant;
import com.food.entity.User;
import com.food.entity.UserProfile;
import com.food.mapper.MerchantMapper;
import com.food.mapper.UserMapper;
import com.food.mapper.UserProfileMapper;
import com.food.security.JwtUtil;
import com.food.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 认证服务
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final MerchantMapper merchantMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    /**
     * 登录
     */
    public String login(LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
        );
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return jwtUtil.generateToken(loginUser);
    }

    /**
     * 注册
     */
    @Transactional
    public void register(RegisterDTO registerDTO) {
        // 检查用户名是否存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserName, registerDTO.getUsername());
        if (userMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUserName(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setNickName(registerDTO.getNickName() != null ? registerDTO.getNickName() : registerDTO.getUsername());
        user.setPhonenumber(registerDTO.getPhonenumber());
        user.setEmail(registerDTO.getEmail());
        user.setUserType(registerDTO.getUserType());
        user.setStatus(0);
        user.setDelFlag(0);
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);

        // 创建用户资料
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(user.getUserId());
        userProfile.setCarbonPoints(BigDecimal.ZERO);
        userProfile.setTotalCarbonSaved(BigDecimal.ZERO);
        userProfile.setTotalFoodSaved(BigDecimal.ZERO);
        userProfile.setCreateTime(LocalDateTime.now());
        userProfileMapper.insert(userProfile);

        // 如果是商户，创建商户记录
        if (registerDTO.getUserType() == 2) {
            Merchant merchant = new Merchant();
            merchant.setUserId(user.getUserId());
            merchant.setMerchantName(registerDTO.getNickName() + "的店铺");
            merchant.setLicenseStatus(0); // 待审核
            merchant.setCreateTime(LocalDateTime.now());
            merchantMapper.insert(merchant);
        }
    }

    /**
     * 获取当前用户信息
     */
    public User getCurrentUser(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserName, username);
        return userMapper.selectOne(wrapper);
    }
}
