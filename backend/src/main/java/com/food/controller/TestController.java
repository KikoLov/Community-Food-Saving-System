package com.food.controller;

import com.food.entity.User;
import com.food.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final DataSource dataSource;

    @PostMapping("/drop-all-tables")
    public Map<String, Object> dropAllTables() {
        Map<String, Object> result = new HashMap<>();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // Drop all tables
            stmt.execute("DROP TABLE IF EXISTS biz_carbon_log");
            stmt.execute("DROP TABLE IF EXISTS biz_order");
            stmt.execute("DROP TABLE IF EXISTS biz_product");
            stmt.execute("DROP TABLE IF EXISTS biz_cart");
            stmt.execute("DROP TABLE IF EXISTS biz_merchant");
            stmt.execute("DROP TABLE IF EXISTS biz_user_profile");
            stmt.execute("DROP TABLE IF EXISTS biz_user");
            stmt.execute("DROP TABLE IF EXISTS biz_category");
            stmt.execute("DROP TABLE IF EXISTS biz_community");
            stmt.execute("DROP TABLE IF EXISTS sys_user");
            stmt.execute("DROP TABLE IF EXISTS sys_community");
            stmt.execute("DROP TABLE IF EXISTS sys_category");
            stmt.execute("DROP TABLE IF EXISTS t_food_item");
            stmt.execute("DROP TABLE IF EXISTS t_user");

            result.put("code", 200);
            result.put("msg", "All tables dropped successfully");

        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "Error: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    @PostMapping("/init-db")
    public Map<String, Object> initDatabase() {
        Map<String, Object> result = new HashMap<>();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // Create tables
            stmt.execute("CREATE TABLE sys_user (" +
                "user_id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "user_name VARCHAR(50) NOT NULL UNIQUE," +
                "password VARCHAR(100) NOT NULL," +
                "nick_name VARCHAR(50)," +
                "user_type INT DEFAULT 1," +
                "phonenumber VARCHAR(20)," +
                "email VARCHAR(50)," +
                "avatar VARCHAR(255)," +
                "sex INT DEFAULT 0," +
                "status INT DEFAULT 0," +
                "del_flag INT DEFAULT 0," +
                "login_ip VARCHAR(50)," +
                "login_date DATETIME," +
                "create_by VARCHAR(50)," +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "update_by VARCHAR(50)," +
                "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "remark VARCHAR(500)" +
                ")");

            stmt.execute("CREATE TABLE biz_user_profile (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "user_id BIGINT NOT NULL UNIQUE," +
                "carbon_points DECIMAL(10,2) DEFAULT 0," +
                "total_carbon_saved DECIMAL(10,2) DEFAULT 0," +
                "total_food_saved DECIMAL(10,2) DEFAULT 0," +
                "create_by VARCHAR(50)," +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "update_by VARCHAR(50)," +
                "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "deleted INT DEFAULT 0" +
                ")");

            stmt.execute("CREATE TABLE biz_cart (" +
                "cart_id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "user_id BIGINT NOT NULL," +
                "product_id BIGINT NOT NULL," +
                "quantity INT DEFAULT 1," +
                "create_by VARCHAR(50)," +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "update_by VARCHAR(50)," +
                "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "deleted INT DEFAULT 0" +
                ")");

            stmt.execute("CREATE TABLE biz_merchant (" +
                "merchant_id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "user_id BIGINT NOT NULL UNIQUE," +
                "merchant_name VARCHAR(100)," +
                "contact_phone VARCHAR(20)," +
                "address VARCHAR(200)," +
                "business_license VARCHAR(500)," +
                "license_status INT DEFAULT 0," +
                "create_by VARCHAR(50)," +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "update_by VARCHAR(50)," +
                "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "deleted INT DEFAULT 0" +
                ")");

            stmt.execute("CREATE TABLE biz_product (" +
                "product_id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "merchant_id BIGINT NOT NULL," +
                "category_id INT," +
                "product_name VARCHAR(100)," +
                "description TEXT," +
                "original_price DECIMAL(10,2)," +
                "discount_price DECIMAL(10,2)," +
                "stock INT DEFAULT 0," +
                "expire_datetime DATETIME," +
                "image_url VARCHAR(500)," +
                "status INT DEFAULT 1," +
                "create_by VARCHAR(50)," +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "update_by VARCHAR(50)," +
                "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "deleted INT DEFAULT 0" +
                ")");

            stmt.execute("CREATE TABLE biz_order (" +
                "order_id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "order_no VARCHAR(50) UNIQUE," +
                "user_id BIGINT NOT NULL," +
                "merchant_id BIGINT NOT NULL," +
                "product_id BIGINT NOT NULL," +
                "product_name VARCHAR(100)," +
                "quantity INT DEFAULT 1," +
                "total_amount DECIMAL(10,2)," +
                "order_status INT DEFAULT 0," +
                "verify_code VARCHAR(20)," +
                "create_by VARCHAR(50)," +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "update_by VARCHAR(50)," +
                "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "deleted INT DEFAULT 0" +
                ")");

            stmt.execute("CREATE TABLE sys_category (" +
                "category_id INT AUTO_INCREMENT PRIMARY KEY," +
                "category_name VARCHAR(50)," +
                "category_code VARCHAR(20)," +
                "parent_id INT DEFAULT 0," +
                "carbon_factor DECIMAL(5,2) DEFAULT 1.5," +
                "sort_order INT DEFAULT 0," +
                "status INT DEFAULT 1," +
                "create_by VARCHAR(50)," +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "update_by VARCHAR(50)," +
                "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "deleted INT DEFAULT 0" +
                ")");

            stmt.execute("CREATE TABLE sys_community (" +
                "community_id INT AUTO_INCREMENT PRIMARY KEY," +
                "community_name VARCHAR(100)," +
                "community_code VARCHAR(20) UNIQUE," +
                "province VARCHAR(50)," +
                "city VARCHAR(50)," +
                "district VARCHAR(50)," +
                "address VARCHAR(200)," +
                "status INT DEFAULT 1," +
                "create_by VARCHAR(50)," +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "update_by VARCHAR(50)," +
                "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "deleted INT DEFAULT 0" +
                ")");

            stmt.execute("CREATE TABLE biz_carbon_log (" +
                "log_id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "user_id BIGINT NOT NULL," +
                "order_id BIGINT," +
                "carbon_points DECIMAL(10,2) DEFAULT 0," +
                "carbon_saved DECIMAL(10,2) DEFAULT 0," +
                "log_type INT DEFAULT 1," +
                "description VARCHAR(500)," +
                "user_name VARCHAR(50)," +
                "create_by VARCHAR(50)," +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "update_by VARCHAR(50)," +
                "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "deleted INT DEFAULT 0" +
                ")");

            // Insert default users
            String adminPassword = passwordEncoder.encode("admin");
            stmt.execute("INSERT INTO sys_user (user_name, password, nick_name, user_type, status, del_flag) VALUES " +
                "('admin', '" + adminPassword + "', 'Administrator', 3, 0, 0)," +
                "('consumer', '" + adminPassword + "', 'Consumer User', 1, 0, 0)," +
                "('merchant', '" + adminPassword + "', 'Merchant User', 2, 0, 0)");

            stmt.execute("INSERT INTO biz_user_profile (user_id) SELECT user_id FROM sys_user WHERE user_name IN ('admin', 'consumer', 'merchant')");

            // Insert merchant for merchant user
            stmt.execute("INSERT INTO biz_merchant (user_id, merchant_name, license_status) SELECT user_id, CONCAT(nick_name, '的店铺'), 0 FROM sys_user WHERE user_name = 'merchant'");

            // Insert sample products
            stmt.execute("INSERT INTO biz_product (merchant_id, category_id, product_name, description, original_price, discount_price, stock, expire_datetime, image_url, status) VALUES " +
                "(1, 1, '新鲜面包', '当天烘焙的新鲜面包，晚上8点后5折', 20.00, 10.00, 10, DATE_ADD(NOW(), INTERVAL 1 DAY), 'https://via.placeholder.com/300', 1)," +
                "(1, 2, '鲜牛奶', '当日鲜奶，当天必须售完', 15.00, 8.00, 20, DATE_ADD(NOW(), INTERVAL 1 DAY), 'https://via.placeholder.com/300', 1)," +
                "(1, 3, '新鲜水果', '各种新鲜水果，当日特价', 30.00, 15.00, 15, DATE_ADD(NOW(), INTERVAL 1 DAY), 'https://via.placeholder.com/300', 1)");

            // Insert sample categories
            stmt.execute("INSERT INTO sys_category (category_name, category_code, carbon_factor, sort_order) VALUES " +
                "('烘焙', 'BAKERY', 2.5, 1)," +
                "('乳制品', 'DAIRY', 1.8, 2)," +
                "('水果', 'FRUITS', 1.2, 3)," +
                "('蔬菜', 'VEG', 1.0, 4)," +
                "('肉类', 'MEAT', 3.5, 5)");

            // Insert sample communities
            stmt.execute("INSERT INTO sys_community (community_name, community_code, province, city, district, address) VALUES " +
                "('阳光花园', 'SG001', '北京市', '北京市', '朝阳区', '朝阳区建国路88号')," +
                "('绿城小区', 'GC002', '上海市', '上海市', '浦东新区', '浦东新区世纪大道100号')");

            result.put("code", 200);
            result.put("msg", "Database initialized successfully");
            result.put("data", "All tables created with sample data");

        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "Error: " + e.getMessage());
            result.put("error", e.getClass().getName());
            e.printStackTrace();
        }

        return result;
    }
}
