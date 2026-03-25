-- Create tables if not exist

CREATE TABLE IF NOT EXISTS biz_user (
  user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_name VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  nick_name VARCHAR(50),
  phonenumber VARCHAR(20),
  email VARCHAR(50),
  user_type INT DEFAULT 1 COMMENT '1=consumer, 2=merchant, 3=admin',
  status INT DEFAULT 0 COMMENT '0=active, 1=disabled',
  del_flag INT DEFAULT 0 COMMENT '0=active, 1=deleted',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS biz_user_profile (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE,
  carbon_points DECIMAL(10,2) DEFAULT 0,
  total_carbon_saved DECIMAL(10,2) DEFAULT 0,
  total_food_saved DECIMAL(10,2) DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS biz_merchant (
  merchant_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE,
  merchant_name VARCHAR(100),
  contact_phone VARCHAR(20),
  address VARCHAR(200),
  business_license VARCHAR(500),
  license_status INT DEFAULT 0 COMMENT '0=pending, 1=approved, 2=rejected',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS biz_product (
  product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  merchant_id BIGINT NOT NULL,
  category_id INT,
  product_name VARCHAR(100),
  description TEXT,
  original_price DECIMAL(10,2),
  discount_price DECIMAL(10,2),
  stock INT DEFAULT 0,
  image_url VARCHAR(500),
  status INT DEFAULT 1 COMMENT '1=active, 0=inactive',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS biz_order (
  order_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_no VARCHAR(50) UNIQUE,
  user_id BIGINT NOT NULL,
  merchant_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  product_name VARCHAR(100),
  quantity INT DEFAULT 1,
  total_amount DECIMAL(10,2),
  order_status INT DEFAULT 0 COMMENT '0=pending, 1=verified, 2=cancelled, 3=expired',
  verify_code VARCHAR(20),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_category (
  category_id INT AUTO_INCREMENT PRIMARY KEY,
  category_name VARCHAR(50),
  category_code VARCHAR(20),
  carbon_factor DECIMAL(5,2) DEFAULT 1.5,
  sort_order INT DEFAULT 0,
  status INT DEFAULT 1 COMMENT '1=active, 0=inactive'
);

CREATE TABLE IF NOT EXISTS sys_community (
  community_id INT AUTO_INCREMENT PRIMARY KEY,
  community_name VARCHAR(100),
  community_code VARCHAR(20) UNIQUE,
  province VARCHAR(50),
  city VARCHAR(50),
  district VARCHAR(50),
  address VARCHAR(200),
  status INT DEFAULT 1 COMMENT '1=active, 0=inactive'
);

-- Insert default users with BCrypt password 'admin'
-- BCrypt hash for 'admin': $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
INSERT IGNORE INTO biz_user (user_name, password, nick_name, user_type) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Administrator', 3),
('consumer', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Consumer User', 1),
('merchant', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Merchant User', 2);

INSERT IGNORE INTO biz_user_profile (user_id) SELECT user_id FROM biz_user WHERE user_name IN ('admin', 'consumer', 'merchant');

INSERT IGNORE INTO sys_category (category_name, category_code, carbon_factor, sort_order) VALUES
('Bakery', 'BAKERY', 2.5, 1),
('Dairy', 'DAIRY', 1.8, 2),
('Fruits', 'FRUITS', 1.2, 3),
('Vegetables', 'VEG', 1.0, 4),
('Meat', 'MEAT', 3.5, 5);

INSERT IGNORE INTO sys_community (community_name, community_code, province, city, district, address) VALUES
('阳光花园', 'SG001', '北京市', '北京市', '朝阳区', '朝阳区建国路88号'),
('绿城小区', 'GC002', '上海市', '上海市', '浦东新区', '浦东新区世纪大道100号');
