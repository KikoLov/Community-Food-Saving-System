-- Demo seed data (idempotent)
-- Compatible with schema in docs/sql/init.sql
USE food_saving;

-- BCrypt hash for plaintext password: admin123 / consumer123 / merchant123
SET @admin_pwd_hash = '$2a$10$bTD4oE8ah6FO055Hf53f5u8zpqnnqNW6Xo1ebnLt.e6lejjJy6nZG';
SET @consumer_pwd_hash = '$2a$10$gRkWbSnJaQUHx8KVERWYuO7OkeMY7N6eg5fiYulycNetPCLmsupNC';
SET @merchant_pwd_hash = '$2a$10$00GeLjJUzduSFwJsHenBLudWZvBJQbiSCeUD8g/8iwh7S9LPMe7mi';

-- 1) Users
INSERT INTO sys_user (user_name, nick_name, user_type, password, phonenumber, email, status, del_flag, create_time, update_time)
VALUES ('admin', '系统管理员', 3, @admin_pwd_hash, '13800000001', 'admin@demo.local', 0, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  nick_name = VALUES(nick_name),
  user_type = VALUES(user_type),
  password = VALUES(password),
  phonenumber = VALUES(phonenumber),
  email = VALUES(email),
  status = 0,
  del_flag = 0,
  update_time = NOW();

INSERT INTO sys_user (user_name, nick_name, user_type, password, phonenumber, email, status, del_flag, create_time, update_time)
VALUES ('merchant', '演示商家', 2, @merchant_pwd_hash, '13800000002', 'merchant@demo.local', 0, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  nick_name = VALUES(nick_name),
  user_type = VALUES(user_type),
  password = VALUES(password),
  phonenumber = VALUES(phonenumber),
  email = VALUES(email),
  status = 0,
  del_flag = 0,
  update_time = NOW();

INSERT INTO sys_user (user_name, nick_name, user_type, password, phonenumber, email, status, del_flag, create_time, update_time)
VALUES ('consumer', '演示居民', 1, @consumer_pwd_hash, '13800000003', 'consumer@demo.local', 0, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  nick_name = VALUES(nick_name),
  user_type = VALUES(user_type),
  password = VALUES(password),
  phonenumber = VALUES(phonenumber),
  email = VALUES(email),
  status = 0,
  del_flag = 0,
  update_time = NOW();

SET @admin_user_id = (SELECT user_id FROM sys_user WHERE user_name = 'admin' LIMIT 1);
SET @merchant_user_id = (SELECT user_id FROM sys_user WHERE user_name = 'merchant' LIMIT 1);
SET @consumer_user_id = (SELECT user_id FROM sys_user WHERE user_name = 'consumer' LIMIT 1);

-- 2) Communities
INSERT INTO biz_community (community_name, community_code, province, city, district, address, status, deleted, create_time, update_time)
VALUES ('演示社区A', 'DEMO_A', '北京市', '北京市', '朝阳区', '朝阳区演示路100号', 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  community_name = VALUES(community_name),
  province = VALUES(province),
  city = VALUES(city),
  district = VALUES(district),
  address = VALUES(address),
  status = 1,
  deleted = 0,
  update_time = NOW();

SET @community_id = (SELECT community_id FROM biz_community WHERE community_code = 'DEMO_A' LIMIT 1);

-- 3) Categories
INSERT INTO biz_category (category_name, category_code, parent_id, sort_order, carbon_factor, status, deleted, create_time, update_time)
VALUES
  ('演示-烘焙', 'DEMO_BAKERY', 0, 1, 2.5, 1, 0, NOW(), NOW()),
  ('演示-饮料', 'DEMO_DRINK', 0, 2, 1.0, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  category_name = VALUES(category_name),
  parent_id = VALUES(parent_id),
  sort_order = VALUES(sort_order),
  carbon_factor = VALUES(carbon_factor),
  status = 1,
  deleted = 0,
  update_time = NOW();

SET @bakery_id = (SELECT category_id FROM biz_category WHERE category_code = 'DEMO_BAKERY' LIMIT 1);
SET @drink_id = (SELECT category_id FROM biz_category WHERE category_code = 'DEMO_DRINK' LIMIT 1);

-- 4) User profile
INSERT INTO biz_user_profile (user_id, community_id, carbon_points, total_carbon_saved, total_food_saved, deleted, create_time, update_time)
VALUES (@consumer_user_id, @community_id, 0, 0, 0, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  community_id = VALUES(community_id),
  carbon_points = VALUES(carbon_points),
  total_carbon_saved = VALUES(total_carbon_saved),
  total_food_saved = VALUES(total_food_saved),
  deleted = 0,
  update_time = NOW();

-- 5) Merchant profile
DELETE FROM biz_merchant WHERE user_id = @merchant_user_id;
INSERT INTO biz_merchant (
  user_id, merchant_name, contact_phone, address, business_license, license_status,
  opening_hours, description, community_id, deleted, create_time, update_time
) VALUES (
  @merchant_user_id, '演示商户-低碳便利店', '13800000002', '朝阳区演示路100号底商',
  '/licenses/demo.png', 1, '08:00-22:00', '用于联调的演示商户', @community_id, 0, NOW(), NOW()
);

SET @merchant_id = (SELECT merchant_id FROM biz_merchant WHERE user_id = @merchant_user_id ORDER BY merchant_id DESC LIMIT 1);

-- 6) Merchant-community mapping
DELETE FROM biz_merchant_community WHERE merchant_id = @merchant_id AND community_id = @community_id;
INSERT INTO biz_merchant_community (merchant_id, community_id, deleted, create_time)
VALUES (@merchant_id, @community_id, 0, NOW());

-- 7) Demo products
DELETE FROM biz_product WHERE merchant_id = @merchant_id AND product_name IN ('演示面包', '演示果汁');

INSERT INTO biz_product (
  merchant_id, category_id, product_name, product_image, original_price, discount_price, stock, unit,
  expire_date, expire_datetime, description, warning_hours, status, deleted, create_time, update_time
) VALUES
  (@merchant_id, @bakery_id, '演示面包', '/images/demo-bread.png', 10.00, 5.00, 40, '袋',
   DATE_ADD(CURDATE(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 36 HOUR), '演示商品：次日到期烘焙', 24, 1, 0, NOW(), NOW()),
  (@merchant_id, @drink_id, '演示果汁', '/images/demo-juice.png', 8.00, 4.00, 60, '瓶',
   DATE_ADD(CURDATE(), INTERVAL 4 DAY), DATE_ADD(NOW(), INTERVAL 72 HOUR), '演示商品：临期饮料', 24, 1, 0, NOW(), NOW());

-- 8) Optional demo pending order (for merchant verify flow)
SET @demo_product_id = (
  SELECT product_id FROM biz_product
  WHERE merchant_id = @merchant_id AND product_name = '演示面包'
  ORDER BY product_id DESC LIMIT 1
);

INSERT INTO biz_order (
  order_no, user_id, merchant_id, product_id, product_name, product_image,
  quantity, total_amount, verify_code, order_status, carbon_saved, deleted, create_time, update_time
)
SELECT
  CONCAT('DEMO', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s')),
  @consumer_user_id, @merchant_id, @demo_product_id, '演示面包', '/images/demo-bread.png',
  1, 5.00, LPAD(FLOOR(RAND()*1000000), 6, '0'), 0, 0.0015, 0, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM biz_order WHERE user_id = @consumer_user_id AND merchant_id = @merchant_id AND order_status = 0
);

SELECT 'demo seed completed' AS message,
       @admin_user_id AS admin_user_id,
       @merchant_user_id AS merchant_user_id,
       @consumer_user_id AS consumer_user_id,
       @merchant_id AS merchant_id,
       @community_id AS community_id;
