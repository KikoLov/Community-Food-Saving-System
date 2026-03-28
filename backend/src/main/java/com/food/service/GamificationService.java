package com.food.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.food.dto.gamification.*;
import com.food.entity.CarbonLog;
import com.food.entity.CarbonRedemption;
import com.food.entity.UserProfile;
import com.food.mapper.CarbonLogMapper;
import com.food.mapper.CarbonRedemptionMapper;
import com.food.mapper.UserProfileMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 碳积分游戏化：兑换虚拟树、徽章、优惠券（参考蚂蚁森林能量兑换思路）
 */
@Service
@RequiredArgsConstructor
public class GamificationService {

    private final UserProfileMapper userProfileMapper;
    private final CarbonLogMapper carbonLogMapper;
    private final CarbonRedemptionMapper carbonRedemptionMapper;
    private final JdbcTemplate jdbcTemplate;
    private final UserCouponService userCouponService;

    private static final List<GamificationCatalogItemDTO> CATALOG = List.of(
            new GamificationCatalogItemDTO("virtual_tree", "TREE", "虚拟树苗", "在「我的森林」种下一棵演示树，为地球加一点绿", new BigDecimal("30"), "🌳"),
            new GamificationCatalogItemDTO("badge_green", "BADGE", "徽章「绿色先锋」", "平台环保成就徽章，展示在个人中心（每人限兑 1 次）", new BigDecimal("80"), "🎖️"),
            new GamificationCatalogItemDTO("badge_earth", "BADGE", "徽章「地球卫士」", "高阶环保徽章（每人限兑 1 次）", new BigDecimal("180"), "🌍"),
            new GamificationCatalogItemDTO("coupon_platform_10", "COUPON_PLATFORM", "平台券 · 满10减2", "购物车结算时选用，满足门槛自动抵扣实付", new BigDecimal("50"), "🎫"),
            new GamificationCatalogItemDTO("coupon_merchant_15", "COUPON_MERCHANT", "合作商户券 · 满15减3", "购物车结算时选用，满足门槛自动抵扣实付", new BigDecimal("100"), "🏪")
    );

    @PostConstruct
    public void ensureRedemptionTable() {
        Integer n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_carbon_redemption'",
                Integer.class
        );
        if (n == null || n == 0) {
            jdbcTemplate.execute("""
                    CREATE TABLE biz_carbon_redemption (
                      redemption_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '兑换ID',
                      user_id BIGINT NOT NULL COMMENT '用户ID',
                      item_code VARCHAR(64) NOT NULL COMMENT '目录项编码',
                      category VARCHAR(32) NOT NULL COMMENT 'TREE/BADGE/COUPON_PLATFORM/COUPON_MERCHANT',
                      points_cost DECIMAL(10,2) NOT NULL COMMENT '消耗碳积分',
                      title VARCHAR(255) NOT NULL COMMENT '标题',
                      detail TEXT COMMENT '券码或说明',
                      create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
                      create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                      update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
                      update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                      deleted TINYINT DEFAULT 0 COMMENT '删除标志',
                      INDEX idx_user_id (user_id),
                      INDEX idx_user_item (user_id, item_code)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='碳积分兑换记录'
                    """);
        }
    }

    public List<GamificationCatalogItemDTO> getCatalog() {
        return CATALOG;
    }

    public GamificationStateDTO getState(Long userId) {
        UserProfile profile = ensureProfile(userId);
        BigDecimal coins = profile.getCarbonPoints() != null ? profile.getCarbonPoints() : BigDecimal.ZERO;
        long trees = carbonRedemptionMapper.countTreesByUser(userId);

        List<CarbonRedemption> all = carbonRedemptionMapper.selectList(
                new LambdaQueryWrapper<CarbonRedemption>()
                        .eq(CarbonRedemption::getUserId, userId)
                        .orderByDesc(CarbonRedemption::getCreateTime)
        );
        List<RedemptionItemDTO> badges = all.stream()
                .filter(r -> "BADGE".equals(r.getCategory()))
                .map(this::toItem)
                .collect(Collectors.toList());
        List<RedemptionItemDTO> coupons = all.stream()
                .filter(r -> r.getCategory() != null && r.getCategory().startsWith("COUPON"))
                .map(this::toItem)
                .collect(Collectors.toList());
        List<RedemptionItemDTO> recent = all.stream().limit(12).map(this::toItem).collect(Collectors.toList());

        Map<String, Boolean> owned = new HashMap<>();
        for (GamificationCatalogItemDTO c : CATALOG) {
            if (!"BADGE".equals(c.getCategory())) {
                continue;
            }
            owned.put(c.getCode(), carbonRedemptionMapper.countBadgeByUserAndCode(userId, c.getCode()) > 0);
        }

        GamificationStateDTO dto = new GamificationStateDTO();
        dto.setCarbonCoins(coins);
        dto.setTreesPlanted(trees);
        dto.setBadges(badges);
        dto.setCoupons(coupons);
        dto.setRecentRedemptions(recent);
        dto.setOwnedBadgeCodes(owned);
        return dto;
    }

    private RedemptionItemDTO toItem(CarbonRedemption r) {
        return new RedemptionItemDTO(
                r.getRedemptionId(),
                r.getItemCode(),
                r.getCategory(),
                r.getTitle(),
                r.getPointsCost(),
                r.getDetail(),
                r.getCreateTime()
        );
    }

    @Transactional
    public RedeemResultDTO redeem(Long userId, String itemCode) {
        GamificationCatalogItemDTO item = CATALOG.stream()
                .filter(c -> c.getCode().equals(itemCode))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("无效的兑换项"));

        if ("BADGE".equals(item.getCategory())) {
            if (carbonRedemptionMapper.countBadgeByUserAndCode(userId, itemCode) > 0) {
                throw new RuntimeException("该徽章已兑换过，每人限 1 枚");
            }
        }

        UserProfile profile = ensureProfile(userId);
        BigDecimal balance = profile.getCarbonPoints() != null ? profile.getCarbonPoints() : BigDecimal.ZERO;
        if (balance.compareTo(item.getCost()) < 0) {
            throw new RuntimeException("碳积分不足，当前余额：" + balance.stripTrailingZeros().toPlainString());
        }

        String couponCodeForWallet = null;
        if ("COUPON_PLATFORM".equals(item.getCategory()) || "COUPON_MERCHANT".equals(item.getCategory())) {
            couponCodeForWallet = genCouponCode(
                    "COUPON_MERCHANT".equals(item.getCategory()) ? "MCH" : "ECO", userId);
        }
        String detail = buildDetail(item, couponCodeForWallet);
        CarbonRedemption row = new CarbonRedemption();
        row.setUserId(userId);
        row.setItemCode(item.getCode());
        row.setCategory(item.getCategory());
        row.setPointsCost(item.getCost());
        row.setTitle(item.getTitle());
        row.setDetail(detail);
        row.setCreateTime(LocalDateTime.now());
        carbonRedemptionMapper.insert(row);

        if (couponCodeForWallet != null) {
            userCouponService.issueFromRedemption(row.getRedemptionId(), userId, item, couponCodeForWallet);
        }

        BigDecimal after = balance.subtract(item.getCost());
        profile.setCarbonPoints(after);
        profile.setUpdateTime(LocalDateTime.now());
        userProfileMapper.updateById(profile);

        CarbonLog log = new CarbonLog();
        log.setUserId(userId);
        log.setOrderId(null);
        log.setCarbonPoints(item.getCost());
        log.setCarbonSaved(BigDecimal.ZERO);
        log.setLogType(2);
        log.setDescription("碳积分兑换：" + item.getTitle());
        log.setCreateTime(LocalDateTime.now());
        carbonLogMapper.insert(log);

        RedeemResultDTO res = new RedeemResultDTO();
        res.setBalanceAfter(after);
        res.setMessage("兑换成功");
        res.setRedemption(toItem(row));
        return res;
    }

    private String buildDetail(GamificationCatalogItemDTO item, String couponCode) {
        return switch (item.getCategory()) {
            case "TREE" -> "已在「我的森林」种下 1 棵虚拟树";
            case "BADGE" -> "徽章已解锁，可在本页「我的徽章」查看";
            case "COUPON_PLATFORM" -> "券码：" + couponCode + "（满10减2，结算时自动抵扣）";
            case "COUPON_MERCHANT" -> "券码：" + couponCode + "（满15减3，结算时自动抵扣）";
            default -> "";
        };
    }

    private static String genCouponCode(String prefix, Long userId) {
        long tail = (System.nanoTime() % 1_000_000L + 1_000_000L) % 1_000_000L;
        return prefix + "-" + userId + "-" + String.format("%06d", tail);
    }

    private UserProfile ensureProfile(Long userId) {
        UserProfile p = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId)
        );
        if (p == null) {
            p = new UserProfile();
            p.setUserId(userId);
            p.setCarbonPoints(BigDecimal.ZERO);
            p.setTotalCarbonSaved(BigDecimal.ZERO);
            p.setTotalFoodSaved(BigDecimal.ZERO);
            p.setWalletBalance(new BigDecimal("200.00"));
            userProfileMapper.insert(p);
        } else if (p.getCarbonPoints() == null) {
            p.setCarbonPoints(BigDecimal.ZERO);
            userProfileMapper.updateById(p);
        }
        return p;
    }
}
