package com.food.service;

import com.food.entity.CarbonLog;
import com.food.entity.UserProfile;
import com.food.mapper.CarbonLogMapper;
import com.food.mapper.UserProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 低碳服务
 */
@Service
@RequiredArgsConstructor
public class CarbonService {

    private final UserProfileMapper userProfileMapper;
    private final CarbonLogMapper carbonLogMapper;

    /**
     * 获取用户低碳信息
     */
    public UserProfile getUserCarbonInfo(Long userId) {
        UserProfile profile = userProfileMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserProfile>()
                        .eq(UserProfile::getUserId, userId)
        );
        return profile;
    }

    /**
     * 获取用户低碳日志
     */
    public List<CarbonLog> getUserCarbonLogs(Long userId) {
        return carbonLogMapper.selectCarbonLogListByUser(userId);
    }

    /**
     * 获取全平台统计数据
     */
    public Map<String, Object> getPlatformStats() {
        Map<String, Object> stats = new HashMap<>();

        BigDecimal totalCarbon = carbonLogMapper.selectTotalCarbonSaved();
        BigDecimal totalFood = carbonLogMapper.selectTotalFoodSaved();
        BigDecimal totalAmount = carbonLogMapper.selectTotalTransactionAmount();
        Long totalOrders = carbonLogMapper.selectTotalOrderCount();

        stats.put("totalCarbonSaved", totalCarbon != null ? totalCarbon : BigDecimal.ZERO);
        stats.put("totalFoodSaved", totalFood != null ? totalFood : BigDecimal.ZERO);
        stats.put("totalTransactionAmount", totalAmount != null ? totalAmount : BigDecimal.ZERO);
        stats.put("totalOrderCount", totalOrders != null ? totalOrders : 0L);

        return stats;
    }

    /**
     * 绑定社区
     */
    public void bindCommunity(Long userId, Long communityId) {
        UserProfile profile = userProfileMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserProfile>()
                        .eq(UserProfile::getUserId, userId)
        );
        if (profile == null) {
            throw new RuntimeException("用户资料不存在");
        }
        profile.setCommunityId(communityId);
        profile.setUpdateTime(java.time.LocalDateTime.now());
        userProfileMapper.updateById(profile);
    }
}
