package com.food.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据库修复��制器
 */
@RestController
@RequestMapping("/api/test/fix")
public class DatabaseFixController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/merchant-table")
    public Map<String, Object> fixMerchantTable() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 添加缺失的字段
            String[] sqlStatements = {
                "ALTER TABLE biz_merchant ADD COLUMN opening_hours VARCHAR(100) COMMENT '营业时间'",
                "ALTER TABLE biz_merchant ADD COLUMN description TEXT COMMENT '店铺描述'",
                "ALTER TABLE biz_merchant ADD COLUMN community_id BIGINT COMMENT '社区ID'",
                "ALTER TABLE biz_merchant ADD COLUMN create_by VARCHAR(50) COMMENT '创建者'",
                "ALTER TABLE biz_merchant ADD COLUMN update_by VARCHAR(50) COMMENT '更新者'",
                "ALTER TABLE biz_merchant ADD COLUMN deleted INT DEFAULT 0 COMMENT '删除标志'"
            };

            StringBuilder messages = new StringBuilder();
            for (String sql : sqlStatements) {
                try {
                    jdbcTemplate.execute(sql);
                    messages.append("✓ ").append(sql).append("\n");
                } catch (Exception e) {
                    // 字段可能已存在，忽略错误
                    messages.append("⊘ ").append(e.getMessage()).append("\n");
                }
            }

            result.put("code", 200);
            result.put("msg", "Merchant table fixed successfully");
            result.put("data", messages.toString());

        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "Error fixing merchant table: " + e.getMessage());
            result.put("data", null);
        }

        return result;
    }
}
