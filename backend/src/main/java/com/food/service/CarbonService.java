package com.food.service;

import com.food.entity.CarbonLog;
import com.food.entity.UserProfile;
import com.food.mapper.CarbonLogMapper;
import com.food.mapper.UserProfileMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
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
    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void ensureWalletBalanceColumn() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_user_profile' AND COLUMN_NAME = 'wallet_balance'",
                Integer.class
        );
        if (count == null || count == 0) {
            jdbcTemplate.execute("ALTER TABLE biz_user_profile ADD COLUMN wallet_balance DECIMAL(10,2) NOT NULL DEFAULT 200.00 COMMENT '钱包余额'");
        }
        // 老数据兜底：顾客资料首次初始化为200，其余角色初始化为0
        jdbcTemplate.execute("""
                UPDATE biz_user_profile up
                LEFT JOIN sys_user su ON su.user_id = up.user_id
                SET up.wallet_balance = CASE WHEN su.user_type = 1 THEN 200.00 ELSE 0.00 END
                WHERE up.wallet_balance IS NULL
                """);
    }

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
