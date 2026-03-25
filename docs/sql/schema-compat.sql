-- Schema compatibility patch for mixed/legacy installations
USE food_saving;

-- biz_user_profile: add community_id if missing
SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE biz_user_profile ADD COLUMN community_id BIGINT NULL',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'biz_user_profile'
    AND COLUMN_NAME = 'community_id'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- biz_product: add product_image if missing
SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE biz_product ADD COLUMN product_image VARCHAR(500) NULL',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'biz_product'
    AND COLUMN_NAME = 'product_image'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- biz_product: add unit if missing
SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE biz_product ADD COLUMN unit VARCHAR(20) DEFAULT ''件''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'biz_product'
    AND COLUMN_NAME = 'unit'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- biz_product: add expire_date if missing
SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE biz_product ADD COLUMN expire_date DATE NULL',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'biz_product'
    AND COLUMN_NAME = 'expire_date'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- biz_product: add warning_hours if missing
SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE biz_product ADD COLUMN warning_hours INT DEFAULT 24',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'biz_product'
    AND COLUMN_NAME = 'warning_hours'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- biz_order: add product_image if missing
SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE biz_order ADD COLUMN product_image VARCHAR(500) NULL',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'biz_order'
    AND COLUMN_NAME = 'product_image'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- biz_order: add carbon_saved if missing
SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE biz_order ADD COLUMN carbon_saved DECIMAL(10,4) DEFAULT 0',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'biz_order'
    AND COLUMN_NAME = 'carbon_saved'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- biz_order: add verify_time if missing
SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE biz_order ADD COLUMN verify_time DATETIME NULL',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'biz_order'
    AND COLUMN_NAME = 'verify_time'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- copy old image_url into product_image when present
SET @has_image_url = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'biz_product'
    AND COLUMN_NAME = 'image_url'
);

SET @copy_sql = IF(
  @has_image_url > 0,
  'UPDATE biz_product SET product_image = COALESCE(product_image, image_url) WHERE product_image IS NULL OR product_image = ''''',
  'SELECT 1'
);
PREPARE stmt2 FROM @copy_sql;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;
