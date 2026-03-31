-- 双演示商户 + 长期商品种子（幂等）
USE food_saving;

-- merchant123 的 bcrypt，可用于 merchant1/merchant2 演示账号
SET @merchant_pwd_hash = '$2a$10$00GeLjJUzduSFwJsHenBLudWZvBJQbiSCeUD8g/8iwh7S9LPMe7mi';

-- 1) 两个演示社区（按你的演示口径：绿城小区 / 阳光花园）
-- 以系统当前可见（deleted=0）的 DEMO 编码社区为准，避免绑到历史已删除社区ID
UPDATE sys_community
SET community_name = '绿城小区',
    province = '北京市',
    city = '北京市',
    district = '朝阳区',
    address = '朝阳区绿城路100号',
    status = 1,
    deleted = 0,
    update_time = NOW()
WHERE community_code = 'GC_DEMO_001';

UPDATE sys_community
SET community_name = '阳光花园',
    province = '北京市',
    city = '北京市',
    district = '海淀区',
    address = '海淀区阳光路200号',
    status = 1,
    deleted = 0,
    update_time = NOW()
WHERE community_code = 'YG_DEMO_001';

SET @community_a = (SELECT community_id FROM sys_community WHERE community_code = 'GC_DEMO_001' AND deleted = 0 LIMIT 1);
SET @community_b = (SELECT community_id FROM sys_community WHERE community_code = 'YG_DEMO_001' AND deleted = 0 LIMIT 1);

-- 2) 两个演示商家账号
INSERT INTO sys_user (user_name, nick_name, user_type, password, phonenumber, email, status, del_flag, create_time, update_time)
VALUES
  ('merchant1', '演示商家一号', 2, @merchant_pwd_hash, '13800001001', 'merchant1@demo.local', 0, 0, NOW(), NOW()),
  ('merchant2', '演示商家二号', 2, @merchant_pwd_hash, '13800001002', 'merchant2@demo.local', 0, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  nick_name = VALUES(nick_name),
  user_type = VALUES(user_type),
  password = VALUES(password),
  phonenumber = VALUES(phonenumber),
  email = VALUES(email),
  status = 0,
  del_flag = 0,
  update_time = NOW();

SET @merchant1_user_id = (SELECT user_id FROM sys_user WHERE user_name = 'merchant1' LIMIT 1);
SET @merchant2_user_id = (SELECT user_id FROM sys_user WHERE user_name = 'merchant2' LIMIT 1);

-- 3) 分类
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

-- 4) 商户档案（重建，避免历史脏数据）
DELETE FROM biz_merchant WHERE user_id IN (@merchant1_user_id, @merchant2_user_id);

INSERT INTO biz_merchant (
  user_id, merchant_name, contact_phone, address, business_license, license_status,
  opening_hours, description, community_id, deleted, create_time, update_time
)
VALUES
  (@merchant1_user_id, '演示商户-低碳便利店A', '13800001001', '朝阳区绿城路100号底商A', '/licenses/demo1.png', 1, '08:00-22:00', '绑定绿城小区', @community_a, 0, NOW(), NOW()),
  (@merchant2_user_id, '演示商户-低碳便利店B', '13800001002', '海淀区阳光路200号底商B', '/licenses/demo2.png', 1, '08:00-22:00', '绑定阳光花园', @community_b, 0, NOW(), NOW());

SET @merchant1_id = (SELECT merchant_id FROM biz_merchant WHERE user_id = @merchant1_user_id ORDER BY merchant_id DESC LIMIT 1);
SET @merchant2_id = (SELECT merchant_id FROM biz_merchant WHERE user_id = @merchant2_user_id ORDER BY merchant_id DESC LIMIT 1);

DELETE FROM biz_merchant_community WHERE merchant_id IN (@merchant1_id, @merchant2_id);
INSERT INTO biz_merchant_community (merchant_id, community_id, deleted, create_time)
VALUES
  (@merchant1_id, @community_a, 0, NOW()),
  (@merchant2_id, @community_b, 0, NOW());

-- 5) 两家商户都放长期演示商品（约 2 年后过期）
DELETE FROM biz_product
WHERE merchant_id IN (@merchant1_id, @merchant2_id)
  AND product_name IN ('演示面包A', '演示果汁A', '演示面包B', '演示果汁B');

INSERT INTO biz_product (
  merchant_id, category_id, product_name, product_image, original_price, discount_price, stock, unit,
  expire_date, expire_datetime, description, warning_hours, status, deleted, create_time, update_time
)
VALUES
  (@merchant1_id, @bakery_id, '演示面包A', '/images/demo-bread.png', 10.00, 5.00, 80, '袋',
   DATE_ADD(CURDATE(), INTERVAL 730 DAY), DATE_ADD(NOW(), INTERVAL 730 DAY), '商户A演示商品（长期有效）', 24, 1, 0, NOW(), NOW()),
  (@merchant1_id, @drink_id, '演示果汁A', '/images/demo-juice.png', 8.00, 4.00, 100, '瓶',
   DATE_ADD(CURDATE(), INTERVAL 730 DAY), DATE_ADD(NOW(), INTERVAL 730 DAY), '商户A演示商品（长期有效）', 24, 1, 0, NOW(), NOW()),
  (@merchant2_id, @bakery_id, '演示面包B', '/images/demo-bread.png', 11.00, 6.00, 70, '袋',
   DATE_ADD(CURDATE(), INTERVAL 730 DAY), DATE_ADD(NOW(), INTERVAL 730 DAY), '商户B演示商品（长期有效）', 24, 1, 0, NOW(), NOW()),
  (@merchant2_id, @drink_id, '演示果汁B', '/images/demo-juice.png', 9.00, 4.50, 90, '瓶',
   DATE_ADD(CURDATE(), INTERVAL 730 DAY), DATE_ADD(NOW(), INTERVAL 730 DAY), '商户B演示商品（长期有效）', 24, 1, 0, NOW(), NOW());

SELECT 'dual merchants seed completed' AS message,
       @merchant1_user_id AS merchant1_user_id,
       @merchant2_user_id AS merchant2_user_id,
       @merchant1_id AS merchant1_id,
       @merchant2_id AS merchant2_id,
       @community_a AS community_a,
       @community_b AS community_b;
