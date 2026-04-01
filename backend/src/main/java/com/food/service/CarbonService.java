package com.food.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.food.entity.CarbonLog;
import com.food.entity.User;
import com.food.entity.UserProfile;
import com.food.mapper.CarbonLogMapper;
import com.food.mapper.OrderMapper;
import com.food.mapper.UserMapper;
import com.food.mapper.UserProfileMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
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
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
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
     * 获取用户低碳信息（并写回资料表，保证与订单/流水一致）
     * <ul>
     *   <li>累计碳减排、累计挽救食品：已核销订单汇总</li>
     *   <li>碳积分：Σ(逐单 ROUND(实付×5,2)) − Σ(兑换流水 log_type=2 的扣减分值)，与核销规则及商城兑换一致</li>
     * </ul>
     */
    public UserProfile getUserCarbonInfo(Long userId) {
        BigDecimal orderCarbon = nz(orderMapper.sumCarbonSavedVerifiedByUser(userId));
        BigDecimal orderFoodQty = nz(orderMapper.sumQuantityVerifiedByUser(userId));
        BigDecimal earnedPoints = nz(orderMapper.sumCarbonPointsEarnedFromVerifiedOrders(userId));
        BigDecimal redeemedPoints = nz(carbonLogMapper.sumRedeemCarbonPointsByUser(userId));

        BigDecimal carbonPoints = earnedPoints.subtract(redeemedPoints).setScale(2, RoundingMode.HALF_UP);
        if (carbonPoints.compareTo(BigDecimal.ZERO) < 0) {
            carbonPoints = BigDecimal.ZERO;
        }

        UserProfile profile = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId)
        );

        if (profile == null) {
            UserProfile insert = new UserProfile();
            insert.setUserId(userId);
            insert.setCarbonPoints(carbonPoints);
            insert.setTotalCarbonSaved(orderCarbon);
            insert.setTotalFoodSaved(orderFoodQty);
            insert.setWalletBalance(defaultWalletForUser(userId));
            insert.setCreateTime(LocalDateTime.now());
            userProfileMapper.insert(insert);
            return userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId));
        }

        profile.setCarbonPoints(carbonPoints);
        profile.setTotalCarbonSaved(orderCarbon);
        profile.setTotalFoodSaved(orderFoodQty);

        UserProfile patch = new UserProfile();
        patch.setProfileId(profile.getProfileId());
        patch.setCarbonPoints(carbonPoints);
        patch.setTotalCarbonSaved(orderCarbon);
        patch.setTotalFoodSaved(orderFoodQty);
        patch.setUpdateTime(LocalDateTime.now());
        userProfileMapper.updateById(patch);

        return profile;
    }

    private static BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private BigDecimal defaultWalletForUser(Long userId) {
        User u = userMapper.selectById(userId);
        if (u != null && u.getUserType() != null && u.getUserType() == 1) {
            return new BigDecimal("200.00");
        }
        return BigDecimal.ZERO;
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
