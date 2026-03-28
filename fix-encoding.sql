-- 检查数据库字符集
SHOW VARIABLES LIKE 'character_set%';

-- 检查food_saving数据库的字符集
USE food_saving;
SHOW CREATE DATABASE food_saving;

-- 检查表字符集
SHOW TABLE STATUS;

-- 设置数据库字符集为UTF8MB4
ALTER DATABASE food_saving CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 转换表字符集
ALTER TABLE biz_merchant CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE biz_product CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE biz_order CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE biz_user CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE biz_user_profile CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE sys_category CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE sys_community CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;