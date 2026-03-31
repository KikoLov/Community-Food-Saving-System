package com.food.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.food.dto.LoginDTO;
import com.food.dto.RegisterDTO;
import com.food.entity.Merchant;
import com.food.entity.User;
import com.food.entity.UserProfile;
import com.food.entity.Community;
import com.food.mapper.CommunityMapper;
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
import java.util.Objects;

/**
 * 认证服务
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final MerchantMapper merchantMapper;
    private final CommunityMapper communityMapper;
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
            Long communityId = registerDTO.getMerchantCommunityId();
            String communityCode = registerDTO.getMerchantCommunityCode();
            if (communityId == null && (communityCode == null || communityCode.isBlank())) {
                throw new RuntimeException("商户注册必须选择所属社区");
            }

            Community community = null;
            if (communityCode != null && !communityCode.isBlank()) {
                LambdaQueryWrapper<Community> byCodeWrapper = new LambdaQueryWrapper<>();
                byCodeWrapper.eq(Community::getCommunityCode, communityCode)
                        .eq(Community::getStatus, 1)
                        .eq(Community::getDeleted, 0)
                        .last("LIMIT 1");
                community = communityMapper.selectOne(byCodeWrapper);
            }
            if (community == null && communityId != null) {
                LambdaQueryWrapper<Community> byIdWrapper = new LambdaQueryWrapper<>();
                byIdWrapper.eq(Community::getCommunityId, communityId)
                        .eq(Community::getStatus, 1)
                        .eq(Community::getDeleted, 0)
                        .last("LIMIT 1");
                community = communityMapper.selectOne(byIdWrapper);
            }
            if (community == null) {
                throw new RuntimeException("所选社区不存在或未启用，请联系管理员");
            }
            if (communityCode != null && !communityCode.isBlank()
                    && communityId != null && !Objects.equals(community.getCommunityId(), communityId)) {
                // 前端传入的ID与code不一致时，以code定位结果为准，防止绑错社区。
                communityId = community.getCommunityId();
            } else {
                communityId = community.getCommunityId();
            }

            Merchant merchant = new Merchant();
            merchant.setUserId(user.getUserId());
            merchant.setMerchantName(registerDTO.getNickName() + "的店铺");
            merchant.setCommunityId(communityId);
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
