package com.food.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.food.dto.gamification.GamificationCatalogItemDTO;
import com.food.entity.UserCoupon;
import com.food.mapper.UserCouponMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户优惠券：发放、校验、核销、取消订单退回
 */
@Service
@RequiredArgsConstructor
public class UserCouponService {

    private final UserCouponMapper userCouponMapper;
    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void ensureSchema() {
        Integer t = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_user_coupon'",
                Integer.class
        );
        if (t == null || t == 0) {
            jdbcTemplate.execute("""
                    CREATE TABLE biz_user_coupon (
                      coupon_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      user_id BIGINT NOT NULL,
                      redemption_id BIGINT NULL,
                      coupon_code VARCHAR(64) NOT NULL,
                      source_item_code VARCHAR(64) NOT NULL,
                      category VARCHAR(32) NOT NULL,
                      min_amount DECIMAL(10,2) NOT NULL,
                      discount_amount DECIMAL(10,2) NOT NULL,
                      merchant_scope_id BIGINT NULL,
                      status TINYINT NOT NULL DEFAULT 0,
                      used_order_id BIGINT NULL,
                      create_by VARCHAR(64) DEFAULT '',
                      create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                      update_by VARCHAR(64) DEFAULT '',
                      update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                      deleted TINYINT DEFAULT 0,
                      UNIQUE KEY uk_coupon_code (coupon_code),
                      INDEX idx_user_status (user_id, status)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券'
                    """);
        }
        addOrderColumnIfMissing("user_coupon_id", "BIGINT NULL COMMENT '使用的优惠券ID'");
        addOrderColumnIfMissing("original_amount", "DECIMAL(10,2) NULL COMMENT '优惠前小计'");
        addOrderColumnIfMissing("discount_amount", "DECIMAL(10,2) NULL DEFAULT 0 COMMENT '本单优惠金额'");
        addOrderColumnIfMissing("coupon_code", "VARCHAR(64) NULL COMMENT '券码快照'");
    }

    private void addOrderColumnIfMissing(String column, String ddl) {
        Integer c = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_order' AND COLUMN_NAME = ?",
                Integer.class,
                column
        );
        if (c == null || c == 0) {
            jdbcTemplate.execute("ALTER TABLE biz_order ADD COLUMN " + column + " " + ddl);
        }
    }

    /**
     * 兑换成功后写入可下单使用的券
     */
    @Transactional
    public void issueFromRedemption(Long redemptionId, Long userId, GamificationCatalogItemDTO item, String couponCode) {
        BigDecimal min;
        BigDecimal discount;
        Long merchantScope = null;
        switch (item.getCode()) {
            case "coupon_platform_10" -> {
                min = new BigDecimal("10");
                discount = new BigDecimal("2");
            }
            case "coupon_merchant_15" -> {
                min = new BigDecimal("15");
                discount = new BigDecimal("3");
            }
            default -> throw new IllegalStateException("非优惠券目录项");
        }
        UserCoupon uc = new UserCoupon();
        uc.setUserId(userId);
        uc.setRedemptionId(redemptionId);
        uc.setCouponCode(couponCode);
        uc.setSourceItemCode(item.getCode());
        uc.setCategory(item.getCategory());
        uc.setMinAmount(min);
        uc.setDiscountAmount(discount);
        uc.setMerchantScopeId(merchantScope);
        uc.setStatus(0);
        userCouponMapper.insert(uc);
    }

    /**
     * 下单前校验：归属、门槛、商户范围；返回实体供扣减金额使用
     */
    public UserCoupon validateForOrder(Long userId, String couponCode, BigDecimal orderSubtotal, Long productMerchantId) {
        if (couponCode == null || couponCode.isBlank()) {
            return null;
        }
        UserCoupon uc = userCouponMapper.selectOne(
                new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getCouponCode, couponCode.trim())
                        .eq(UserCoupon::getUserId, userId)
                        .eq(UserCoupon::getStatus, 0)
        );
        if (uc == null) {
            throw new RuntimeException("优惠券无效或已使用");
        }
        if (orderSubtotal.compareTo(uc.getMinAmount()) < 0) {
            throw new RuntimeException("订单金额未满 " + uc.getMinAmount().stripTrailingZeros().toPlainString() + " 元，无法使用该券");
        }
        if (uc.getMerchantScopeId() != null && !uc.getMerchantScopeId().equals(productMerchantId)) {
            throw new RuntimeException("该券仅限指定商户使用");
        }
        return uc;
    }

    public BigDecimal computeDiscount(UserCoupon uc, BigDecimal subtotal) {
        BigDecimal d = uc.getDiscountAmount();
        if (d.compareTo(subtotal) > 0) {
            return subtotal;
        }
        return d;
    }

    @Transactional
    public void markUsed(Long couponId, Long orderId) {
        UserCoupon uc = userCouponMapper.selectById(couponId);
        if (uc == null || uc.getStatus() == null || uc.getStatus() != 0) {
            throw new RuntimeException("优惠券状态异常");
        }
        uc.setStatus(1);
        uc.setUsedOrderId(orderId);
        userCouponMapper.updateById(uc);
    }

    @Transactional
    public List<UserCoupon> listUnusedForUser(Long userId) {
        return userCouponMapper.selectUnusedByUser(userId);
    }

    @Transactional
    public void restoreIfOrderCancelled(Long couponId) {
        if (couponId == null) {
            return;
        }
        UserCoupon uc = userCouponMapper.selectById(couponId);
        if (uc != null && uc.getStatus() != null && uc.getStatus() == 1) {
            uc.setStatus(0);
            uc.setUsedOrderId(null);
            userCouponMapper.updateById(uc);
        }
    }
}
