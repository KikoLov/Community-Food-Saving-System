-- 碳积分游戏化：兑换记录、用户优惠券、订单优惠字段
-- 数据库名请按环境修改（如 food_saving）

USE food_saving;

-- 1) 兑换记录（与后端 GamificationService 自动建表结构一致，可手工执行）
CREATE TABLE IF NOT EXISTS biz_carbon_redemption (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='碳积分兑换记录';

-- 2) 用户优惠券（可下单抵扣）
CREATE TABLE IF NOT EXISTS biz_user_coupon (
  coupon_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '券ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  redemption_id BIGINT NULL COMMENT '关联兑换记录',
  coupon_code VARCHAR(64) NOT NULL COMMENT '券码（唯一）',
  source_item_code VARCHAR(64) NOT NULL COMMENT '目录项如 coupon_platform_10',
  category VARCHAR(32) NOT NULL COMMENT 'COUPON_PLATFORM / COUPON_MERCHANT',
  min_amount DECIMAL(10,2) NOT NULL COMMENT '满额门槛',
  discount_amount DECIMAL(10,2) NOT NULL COMMENT '减免金额',
  merchant_scope_id BIGINT NULL COMMENT 'NULL 表示全平台；非空则仅该商户可用',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0未使用 1已使用',
  used_order_id BIGINT NULL COMMENT '使用该券的订单',
  create_by VARCHAR(64) DEFAULT '',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_by VARCHAR(64) DEFAULT '',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0,
  UNIQUE KEY uk_coupon_code (coupon_code),
  INDEX idx_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券（碳积分兑换）';

-- 3) 订单表增加优惠字段（若不存在则执行）
SET @db = DATABASE();

SET @sql = (
  SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_order' AND COLUMN_NAME = 'user_coupon_id') = 0,
    'ALTER TABLE biz_order ADD COLUMN user_coupon_id BIGINT NULL COMMENT ''使用的优惠券ID'' AFTER total_amount',
    'SELECT 1'
  )
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (
  SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_order' AND COLUMN_NAME = 'original_amount') = 0,
    'ALTER TABLE biz_order ADD COLUMN original_amount DECIMAL(10,2) NULL COMMENT ''优惠前小计'' AFTER user_coupon_id',
    'SELECT 1'
  )
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (
  SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_order' AND COLUMN_NAME = 'discount_amount') = 0,
    'ALTER TABLE biz_order ADD COLUMN discount_amount DECIMAL(10,2) NULL DEFAULT 0 COMMENT ''本单优惠金额'' AFTER original_amount',
    'SELECT 1'
  )
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (
  SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_order' AND COLUMN_NAME = 'coupon_code') = 0,
    'ALTER TABLE biz_order ADD COLUMN coupon_code VARCHAR(64) NULL COMMENT ''券码快照'' AFTER discount_amount',
    'SELECT 1'
  )
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
