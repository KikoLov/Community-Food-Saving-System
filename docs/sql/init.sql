-- =============================================
-- 社区临期食品低碳减损系统 - 数据库初始化脚本
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS food_saving DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE food_saving;

-- =============================================
-- 1. 系统基础表 (扩展RuoYi框架)
-- =============================================

-- 用户表 (扩展)
CREATE TABLE IF NOT EXISTS sys_user (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    user_name VARCHAR(30) NOT NULL UNIQUE COMMENT '用户名',
    nick_name VARCHAR(30) COMMENT '昵称',
    user_type TINYINT DEFAULT 1 COMMENT '用户类型: 1-居民 2-商户 3-管理员',
    email VARCHAR(50) COMMENT '邮箱',
    phonenumber VARCHAR(11) COMMENT '手机号码',
    sex TINYINT DEFAULT 0 COMMENT '性别: 0-未知 1-男 2-女',
    avatar VARCHAR(100) COMMENT '头像',
    password VARCHAR(100) DEFAULT '' COMMENT '密码',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-正常 1-停用',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标志: 0-存在 1-删除',
    login_ip VARCHAR(128) COMMENT '最后登录IP',
    login_date DATETIME COMMENT '最后登录时间',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) COMMENT '备注',
    INDEX idx_user_name (user_name),
    INDEX idx_phonenumber (phonenumber),
    INDEX idx_user_type (user_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- =============================================
-- 2. 业务表
-- =============================================

-- 社区信息表
CREATE TABLE IF NOT EXISTS biz_community (
    community_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '社区ID',
    community_name VARCHAR(100) NOT NULL COMMENT '社区名称',
    community_code VARCHAR(50) NOT NULL UNIQUE COMMENT '社区编码',
    province VARCHAR(50) COMMENT '省份',
    city VARCHAR(50) COMMENT '城市',
    district VARCHAR(50) COMMENT '区县',
    address VARCHAR(255) COMMENT '详细地址',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志: 0-存在 1-删除',
    INDEX idx_community_code (community_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区信息表';

-- 商户信息表
CREATE TABLE IF NOT EXISTS biz_merchant (
    merchant_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商户ID',
    user_id BIGINT NOT NULL COMMENT '关联sys_user的ID',
    merchant_name VARCHAR(100) NOT NULL COMMENT '商户名称',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    address VARCHAR(255) COMMENT '详细地址',
    business_license VARCHAR(255) COMMENT '营业执照图片URL',
    license_status TINYINT DEFAULT 0 COMMENT '资质状态: 0-待审核 1-已通过 2-已拒绝',
    opening_hours VARCHAR(100) COMMENT '营业时间，如: 08:00-22:00',
    description TEXT COMMENT '店铺描述',
    community_id BIGINT COMMENT '主要服务社区ID',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志: 0-存在 1-删除',
    INDEX idx_user_id (user_id),
    INDEX idx_community_id (community_id),
    INDEX idx_license_status (license_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户信息表';

-- 食品分类表
CREATE TABLE IF NOT EXISTS biz_category (
    category_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    category_name VARCHAR(50) NOT NULL COMMENT '分类名称',
    category_code VARCHAR(50) NOT NULL UNIQUE COMMENT '分类编码',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    carbon_factor DOUBLE DEFAULT 1.5 COMMENT '碳排放因子 (kg CO2/kg食品)',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志: 0-存在 1-删除',
    INDEX idx_category_code (category_code),
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食品分类表';

-- 商品信息表
CREATE TABLE IF NOT EXISTS biz_product (
    product_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
    merchant_id BIGINT NOT NULL COMMENT '商户ID',
    category_id BIGINT COMMENT '分类ID',
    product_name VARCHAR(100) NOT NULL COMMENT '商品名称',
    product_image VARCHAR(500) COMMENT '商品图片URL',
    original_price DECIMAL(10,2) NOT NULL COMMENT '原价',
    discount_price DECIMAL(10,2) NOT NULL COMMENT '折扣价',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    unit VARCHAR(20) DEFAULT '件' COMMENT '单位',
    expire_date DATE NOT NULL COMMENT '过期日期',
    expire_datetime DATETIME NOT NULL COMMENT '过期时间(精确到时分)',
    description TEXT COMMENT '商品描述',
    warning_hours INT DEFAULT 24 COMMENT '预警小时数(距离过期多久开始预警)',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-下架 1-上架 2-已售罄',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志: 0-存在 1-删除',
    INDEX idx_merchant_id (merchant_id),
    INDEX idx_category_id (category_id),
    INDEX idx_status (status),
    INDEX idx_expire_datetime (expire_datetime),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品信息表';

-- 订单表
CREATE TABLE IF NOT EXISTS biz_order (
    order_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '买家用户ID',
    merchant_id BIGINT NOT NULL COMMENT '商户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(100) COMMENT '商品名称(冗余)',
    product_image VARCHAR(500) COMMENT '商品图片(冗余)',
    quantity INT NOT NULL DEFAULT 1 COMMENT '购买数量',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    verify_code VARCHAR(6) NOT NULL UNIQUE COMMENT '6位核销码',
    order_status TINYINT DEFAULT 0 COMMENT '订单状态: 0-待核销 1-已核销 2-已取消 3-已过期',
    carbon_saved DECIMAL(10,4) DEFAULT 0 COMMENT '本次订单碳减排量(kg CO2)',
    verify_time DATETIME COMMENT '核销时间',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志: 0-存在 1-删除',
    INDEX idx_user_id (user_id),
    INDEX idx_merchant_id (merchant_id),
    INDEX idx_order_no (order_no),
    INDEX idx_verify_code (verify_code),
    INDEX idx_order_status (order_status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 用户资料扩展表
CREATE TABLE IF NOT EXISTS biz_user_profile (
    profile_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '资料ID',
    user_id BIGINT NOT NULL UNIQUE COMMENT '关联sys_user',
    community_id BIGINT COMMENT '所属社区ID',
    carbon_points DECIMAL(10,2) DEFAULT 0 COMMENT '低碳积分',
    total_carbon_saved DECIMAL(10,4) DEFAULT 0 COMMENT '累计碳减排量(kg CO2)',
    total_food_saved DECIMAL(10,2) DEFAULT 0 COMMENT '累计挽救食品重量(kg)',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志: 0-存在 1-删除',
    INDEX idx_user_id (user_id),
    INDEX idx_community_id (community_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户资料扩展表';

-- 购物车表
CREATE TABLE IF NOT EXISTS biz_cart (
    cart_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '购物车ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志: 0-存在 1-删除',
    UNIQUE KEY uk_user_product (user_id, product_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 低碳日志表
CREATE TABLE IF NOT EXISTS biz_carbon_log (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    order_id BIGINT COMMENT '关联订单ID',
    carbon_points DECIMAL(10,2) NOT NULL COMMENT '本次获得积分',
    carbon_saved DECIMAL(10,4) NOT NULL COMMENT '本次碳减排量(kg)',
    log_type TINYINT NOT NULL COMMENT '日志类型: 1-订单获得 2-积分扣减',
    description VARCHAR(255) COMMENT '描述',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志: 0-存在 1-删除',
    INDEX idx_user_id (user_id),
    INDEX idx_order_id (order_id),
    INDEX idx_log_type (log_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='低碳日志表';

-- 商户-社区关联表
CREATE TABLE IF NOT EXISTS biz_merchant_community (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    merchant_id BIGINT NOT NULL COMMENT '商户ID',
    community_id BIGINT NOT NULL COMMENT '社区ID',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志',
    UNIQUE KEY uk_merchant_community (merchant_id, community_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户-社区关联表';

-- 操作审计日志表
CREATE TABLE IF NOT EXISTS biz_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    actor_user_id BIGINT NOT NULL COMMENT '操作者用户ID',
    actor_username VARCHAR(64) NOT NULL COMMENT '操作者用户名',
    actor_role VARCHAR(32) NOT NULL COMMENT '操作者角色',
    action_type VARCHAR(64) NOT NULL COMMENT '操作类型',
    target_type VARCHAR(64) NOT NULL COMMENT '目标类型',
    target_id BIGINT COMMENT '目标ID',
    target_name VARCHAR(255) COMMENT '目标名称',
    detail VARCHAR(500) COMMENT '操作详情',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_actor_user_id (actor_user_id),
    INDEX idx_action_type (action_type),
    INDEX idx_target_type (target_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作审计日志';

-- =============================================
-- 3. 初始化数据
-- =============================================

-- 插入管理员用户 (密码: admin123)
INSERT INTO sys_user (user_name, nick_name, user_type, password, status, create_time) VALUES
('admin', '系统管理员', 3, '$2a$10$bTD4oE8ah6FO055Hf53f5u8zpqnnqNW6Xo1ebnLt.e6lejjJy6nZG', 0, NOW());

-- 插入社区数据
INSERT INTO biz_community (community_name, community_code, province, city, district, address, status) VALUES
('阳光社区', 'YG001', '北京市', '北京市', '朝阳区', '朝阳区阳光路1号', 1),
('绿城社区', 'LC001', '上海市', '上海市', '浦东新区', '浦东新区绿城大道2号', 1),
('和谐社区', 'HX001', '广州市', '广州市', '天河区', '天河区和谐街3号', 1),
('幸福社区', 'XF001', '深圳市', '深圳市', '南山区', '南山区幸福路4号', 1);

-- 插入食品分类数据
INSERT INTO biz_category (category_name, category_code, parent_id, sort_order, carbon_factor, status) VALUES
('烘焙食品', 'BAKERY', 0, 1, 2.5, 1),
('生鲜果蔬', 'FRESH', 0, 2, 0.5, 1),
('乳制品', 'DAIRY', 0, 3, 3.2, 1),
('肉类', 'MEAT', 0, 4, 7.0, 1),
('饮料', 'DRINK', 0, 5, 1.0, 1),
('其他', 'OTHER', 0, 99, 1.5, 1);

-- 插入面包子分类
INSERT INTO biz_category (category_name, category_code, parent_id, sort_order, carbon_factor, status) VALUES
('面包', 'BREAD', 1, 1, 2.5, 1),
('蛋糕', 'CAKE', 1, 2, 2.8, 1),
('饼干', 'COOKIE', 1, 3, 2.2, 1);

-- 插入牛奶子分类
INSERT INTO biz_category (category_name, category_code, parent_id, sort_order, carbon_factor, status) VALUES
('纯牛奶', 'MILK', 3, 1, 3.0, 1),
('酸奶', 'YOGURT', 3, 2, 3.2, 1);

-- =============================================
-- 4. 测试数据
-- =============================================

-- 插入测试居民用户 (密码: consumer123)
INSERT INTO sys_user (user_name, nick_name, user_type, password, phonenumber, status, create_time) VALUES
('consumer', '测试居民', 1, '$2a$10$gRkWbSnJaQUHx8KVERWYuO7OkeMY7N6eg5fiYulycNetPCLmsupNC', '13800138001', 0, NOW());

-- 插入测试商户用户 (密码: merchant123)
INSERT INTO sys_user (user_name, nick_name, user_type, password, phonenumber, status, create_time) VALUES
('merchant', '王老板', 2, '$2a$10$00GeLjJUzduSFwJsHenBLudWZvBJQbiSCeUD8g/8iwh7S9LPMe7mi', '13900139001', 0, NOW());

-- 为商户创建商户记录
INSERT INTO biz_merchant (user_id, merchant_name, contact_phone, address, license_status, community_id, create_time) VALUES
(3, '阳光便利店', '13900139001', '朝阳区阳光路1号底商', 1, 1, NOW());

-- 为居民创建用户资料
INSERT INTO biz_user_profile (user_id, community_id, carbon_points, total_carbon_saved, total_food_saved, create_time) VALUES
(1, 1, 0, 0, 0, NOW()),
(2, 2, 0, 0, 0, NOW());

-- 插入测试商品数据
INSERT INTO biz_product (merchant_id, category_id, product_name, product_image, original_price, discount_price, stock, unit, expire_date, expire_datetime, description, warning_hours, status, create_time) VALUES
(1, 1, '新鲜面包片', '/images/bread.jpg', 8.00, 4.00, 50, '袋', DATE_ADD(CURDATE(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 48 HOUR), '当天现烤面包片，口感松软', 24, 1, NOW()),
(1, 2, '有机苹果', '/images/apple.jpg', 12.00, 6.00, 30, '斤', DATE_ADD(CURDATE(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 72 HOUR), '新鲜有机苹果，甜脆可口', 24, 1, NOW()),
(1, 3, '纯牛奶', '/images/milk.jpg', 6.00, 3.50, 100, '盒', DATE_ADD(CURDATE(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 120 HOUR), '新鲜纯牛奶，营养丰富', 24, 1, NOW()),
(1, 5, '橙汁饮料', '/images/orange_juice.jpg', 5.00, 2.50, 80, '瓶', DATE_ADD(CURDATE(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 168 HOUR), '鲜榨橙汁，天然健康', 24, 1, NOW());

-- 插入测试订单
INSERT INTO biz_order (order_no, user_id, merchant_id, product_id, product_name, product_image, quantity, total_amount, verify_code, order_status, carbon_saved, create_time) VALUES
('ORD202603150001', 1, 1, 1, '新鲜面包片', '/images/bread.jpg', 2, 8.00, '123456', 1, 0.0050, NOW()),
('ORD202603150002', 2, 1, 2, '有机苹果', '/images/apple.jpg', 1, 6.00, '234567', 1, 0.0005, NOW());

-- 插入低碳日志
INSERT INTO biz_carbon_log (user_id, order_id, carbon_points, carbon_saved, log_type, description, create_time) VALUES
(1, 1, 0.50, 0.0050, 1, '订单核销获得碳积分', NOW()),
(2, 2, 0.05, 0.0005, 1, '订单核销获得碳积分', NOW());

-- 更新用户积分
UPDATE biz_user_profile SET carbon_points = 0.50, total_carbon_saved = 0.0050, total_food_saved = 2.00 WHERE user_id = 1;
UPDATE biz_user_profile SET carbon_points = 0.05, total_carbon_saved = 0.0005, total_food_saved = 1.00 WHERE user_id = 2;

-- =============================================
-- 5. 视图和存储过程 (可选)
-- =============================================

-- 查看各社区商品数量
CREATE OR REPLACE VIEW view_community_products AS
SELECT c.community_id, c.community_name, COUNT(p.product_id) as product_count
FROM biz_community c
LEFT JOIN biz_merchant m ON c.community_id = m.community_id
LEFT JOIN biz_product p ON m.merchant_id = p.merchant_id AND p.status = 1
WHERE c.status = 1
GROUP BY c.community_id, c.community_name;

-- 查看商户订单统计
CREATE OR REPLACE VIEW view_merchant_stats AS
SELECT m.merchant_id, m.merchant_name,
       COUNT(DISTINCT o.order_id) as order_count,
       SUM(o.total_amount) as total_sales,
       SUM(o.carbon_saved) as total_carbon_saved
FROM biz_merchant m
LEFT JOIN biz_order o ON m.merchant_id = o.merchant_id AND o.order_status = 1
GROUP BY m.merchant_id, m.merchant_name;

-- =============================================
-- 完成
-- =============================================
SELECT '数据库初始化完成!' as message;
