package com.food.controller;

import com.food.common.Result;
import com.food.dto.MerchantAdminStatsDTO;
import com.food.entity.Category;
import com.food.entity.Community;
import com.food.entity.Merchant;
import com.food.entity.OperationLog;
import com.food.entity.Order;
import com.food.security.LoginUser;
import com.food.service.*;
import com.food.util.DemoTextNormalizeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 管理端控制器
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final MerchantService merchantService;
    private final CommunityService communityService;
    private final CategoryService categoryService;
    private final CarbonService carbonService;
    private final OrderService orderService;
    private final OperationLogService operationLogService;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 修复数据库编码问题（临时方法）
     */
    @PostMapping("/fix-encoding")
    public Result<Void> fixEncoding() {
        try {
            // 设置数据库字符集
            jdbcTemplate.execute("ALTER DATABASE food_saving CHARACTER SET utf8 COLLATE utf8_general_ci");

            // 转换所有表字符集
            String[] tables = {
                "biz_merchant", "biz_product", "biz_order", "biz_user",
                "biz_user_profile", "sys_category", "sys_community"
            };

            for (String table : tables) {
                jdbcTemplate.execute("ALTER TABLE " + table + " CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci");
            }

            return Result.success();
        } catch (Exception e) {
            return Result.error("修复编码失败: " + e.getMessage());
        }
    }

    /**
     * 审核商户资质
     */
    @PutMapping("/merchant/{id}/audit")
    public Result<Void> auditMerchant(Authentication authentication, @PathVariable("id") Long merchantId, @RequestParam Integer status) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        merchantService.auditMerchant(merchantId, status);
        operationLogService.logOperation(
                loginUser,
                "AUDIT_MERCHANT",
                "MERCHANT",
                merchantId,
                null,
                "管理员审核商户，状态=" + status
        );
        return Result.success();
    }

    /**
     * 获取全部商户列表（管理端）
     */
    @GetMapping("/merchants")
    public Result<List<Merchant>> listMerchants() {
        List<Merchant> list = merchantService.getAllMerchants();
        if (list != null) {
            for (Merchant m : list) {
                DemoTextNormalizeUtil.normalizeMerchant(m);
            }
        }
        return Result.success(list);
    }

    /**
     * 商户经营统计（管理端详情）
     */
    @GetMapping("/merchants/{id}/stats")
    public Result<MerchantAdminStatsDTO> merchantStats(@PathVariable("id") Long merchantId) {
        return Result.success(merchantService.getMerchantAdminStats(merchantId));
    }

    /**
     * 删除商户（无订单；非 merchant1/merchant2 演示主体）
     */
    @DeleteMapping("/merchants/{id}")
    public Result<Void> deleteMerchantAdmin(Authentication authentication,
                                           @PathVariable("id") Long merchantId,
                                           @RequestParam(value = "force", defaultValue = "false") boolean force) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        merchantService.deleteMerchantForAdmin(merchantId, force);
        operationLogService.logOperation(
                loginUser,
                "DELETE_MERCHANT",
                "MERCHANT",
                merchantId,
                null,
                "管理员删除商户" + (force ? "（含下属订单/商品）" : "")
        );
        return Result.success();
    }

    /**
     * 一键清理：名称疑似乱码且无任何订单的商户（保留 merchant1/merchant2）
     */
    @PostMapping("/merchants/prune-garbled")
    public Result<Map<String, Object>> pruneGarbledMerchants(Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        int n = merchantService.pruneGarbledMerchantsWithoutOrders();
        operationLogService.logOperation(
                loginUser,
                "PRUNE_MERCHANT",
                "SYSTEM",
                null,
                null,
                "清理乱码空商户，删除数量=" + n
        );
        return Result.success(Map.of(
                "removed", n,
                "message", "已清理 " + n + " 个无订单乱码商户"
        ));
    }

    /**
     * 获取社区列表
     */
    @GetMapping("/communities")
    public Result<List<Community>> getCommunities() {
        List<Community> communities = communityService.getAllCommunitiesForAdmin();
        return Result.success(communities);
    }

    /**
     * 添加社区
     */
    @PostMapping("/communities")
    public Result<Community> addCommunity(Authentication authentication, @RequestBody Community community) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Community added = communityService.addCommunity(community);
        operationLogService.logOperation(
                loginUser,
                "ADD_COMMUNITY",
                "COMMUNITY",
                added.getCommunityId(),
                added.getCommunityName(),
                "管理员新增社区"
        );
        return Result.success(added);
    }

    /**
     * 更新社区
     */
    @PutMapping("/communities/{id}")
    public Result<Community> updateCommunity(Authentication authentication,
                                             @PathVariable("id") Long communityId,
                                             @RequestBody Community community) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        community.setCommunityId(communityId);
        Community updated = communityService.updateCommunity(community);
        operationLogService.logOperation(
                loginUser,
                "UPDATE_COMMUNITY",
                "COMMUNITY",
                communityId,
                updated.getCommunityName(),
                "管理员更新社区"
        );
        return Result.success(updated);
    }

    /**
     * 删除社区
     */
    @DeleteMapping("/communities/{id}")
    public Result<Void> deleteCommunity(Authentication authentication, @PathVariable("id") Long communityId) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        communityService.deleteCommunity(communityId);
        operationLogService.logOperation(
                loginUser,
                "DELETE_COMMUNITY",
                "COMMUNITY",
                communityId,
                null,
                "管理员删除社区"
        );
        return Result.success();
    }

    /**
     * 获取分类列表
     */
    @GetMapping("/categories")
    public Result<List<Category>> getCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }

    /**
     * 添加分类
     */
    @PostMapping("/categories")
    public Result<Category> addCategory(Authentication authentication, @RequestBody Category category) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Category added = categoryService.addCategory(category);
        operationLogService.logOperation(
                loginUser,
                "ADD_CATEGORY",
                "CATEGORY",
                added.getCategoryId(),
                added.getCategoryName(),
                "管理员新增分类"
        );
        return Result.success(added);
    }

    /**
     * 更新分类
     */
    @PutMapping("/categories/{id}")
    public Result<Category> updateCategory(Authentication authentication,
                                           @PathVariable("id") Long categoryId,
                                           @RequestBody Category category) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        category.setCategoryId(categoryId);
        Category updated = categoryService.updateCategory(category);
        operationLogService.logOperation(
                loginUser,
                "UPDATE_CATEGORY",
                "CATEGORY",
                categoryId,
                updated.getCategoryName(),
                "管理员更新分类"
        );
        return Result.success(updated);
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(Authentication authentication, @PathVariable("id") Long categoryId) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        categoryService.deleteCategory(categoryId);
        operationLogService.logOperation(
                loginUser,
                "DELETE_CATEGORY",
                "CATEGORY",
                categoryId,
                null,
                "管理员删除分类"
        );
        return Result.success();
    }

    /**
     * 获取全局统计数据
     */
    @GetMapping("/dashboard/stats")
    public Result<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = carbonService.getPlatformStats();
        return Result.success(stats);
    }

    /**
     * 获取图表数据
     */
    @GetMapping("/dashboard/chart")
    public Result<Map<String, Object>> getChartData() {
        // 这里可以返回ECharts需要的图表数据
        // 实际项目中可以根据日期范围查询订单数据生成图表
        return Result.success(Map.of(
                "xAxis", List.of("1月", "2月", "3月", "4月", "5月", "6月"),
                "carbonData", List.of(120, 200, 150, 280, 210, 320),
                "foodData", List.of(80, 120, 90, 160, 110, 180)
        ));
    }

    /**
     * 获取所有订单(管理端)
     */
    @GetMapping("/orders")
    public Result<List<Order>> getAllOrders() {
        return Result.success(orderService.getAllOrders());
    }

    /**
     * 获取操作审计日志
     */
    @GetMapping("/operation-logs")
    public Result<List<OperationLog>> getOperationLogs() {
        return Result.success(operationLogService.listLogs());
    }

    /**
     * 修复演示数据中文乱码（仅管理员）
     * 说明：
     * 1) 将关键表字符集转换为 utf8mb4
     * 2) 修复绿城小区、阳光花园社区名称
     * 3) 修复 merchant1/merchant2 商户信息与其商品中文名称
     */
    @PostMapping("/fix-demo-garbled-text")
    public Result<Map<String, Object>> fixDemoGarbledText(Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<String> steps = new ArrayList<>();

        jdbcTemplate.execute("ALTER TABLE sys_community CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        steps.add("sys_community charset -> utf8mb4");
        jdbcTemplate.execute("ALTER TABLE biz_merchant CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        steps.add("biz_merchant charset -> utf8mb4");
        jdbcTemplate.execute("ALTER TABLE biz_product CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        steps.add("biz_product charset -> utf8mb4");

        int c3 = jdbcTemplate.update(
                "UPDATE sys_community SET " +
                        "community_name=CONVERT(0xE7BBBFE59F8EE5B08FE58CBA USING utf8mb4), " +
                        "province=CONVERT(0xE4B88AE6B5B7E5B882 USING utf8mb4), " +
                        "city=CONVERT(0xE4B88AE6B5B7E5B882 USING utf8mb4), " +
                        "district=CONVERT(0xE6B5A6E4B89CE696B0E58CBA USING utf8mb4), " +
                        "address=CONVERT(0xE6B5A6E4B89CE696B0E58CBAE4B896E7BAAAE5A4A7E98193313030E58FB7 USING utf8mb4), " +
                        "status=1 WHERE community_id=3"
        );
        int c4 = jdbcTemplate.update(
                "UPDATE sys_community SET " +
                        "community_name=CONVERT(0xE998B3E58589E88AB1E59BAD USING utf8mb4), " +
                        "province=CONVERT(0xE58C97E4BAACE5B882 USING utf8mb4), " +
                        "city=CONVERT(0xE58C97E4BAACE5B882 USING utf8mb4), " +
                        "district=CONVERT(0xE69C9DE998B3E58CBA USING utf8mb4), " +
                        "address=CONVERT(0xE69C9DE998B3E58CBAE5BBBAE59BBDE8B7AF3838E58FB7 USING utf8mb4), " +
                        "status=1 WHERE community_id=4"
        );
        steps.add("community rows updated: c3=" + c3 + ", c4=" + c4);

        jdbcTemplate.update(
                "UPDATE biz_merchant SET " +
                        "merchant_name=CONVERT(0xE7BBBFE59F8EE5B08FE58CBAE7A4BEE58CBAE4BEBFE588A9E5BA97 USING utf8mb4), " +
                        "address=CONVERT(0xE6B5A6E4B89CE696B0E58CBAE4B896E7BAAAE5A4A7E98193313030E58FB7 USING utf8mb4), " +
                        "description='demo store' " +
                        "WHERE user_id=(SELECT user_id FROM sys_user WHERE user_name='merchant1' LIMIT 1)"
        );
        jdbcTemplate.update(
                "UPDATE biz_merchant SET " +
                        "merchant_name=CONVERT(0xE998B3E58589E88AB1E59BADE7A4BEE58CBAE4BEBFE588A9E5BA97 USING utf8mb4), " +
                        "address=CONVERT(0xE69C9DE998B3E58CBAE5BBBAE59BBDE8B7AF3838E58FB7 USING utf8mb4), " +
                        "description='demo store' " +
                        "WHERE user_id=(SELECT user_id FROM sys_user WHERE user_name='merchant2' LIMIT 1)"
        );
        steps.add("merchant profile text updated");

        List<Long> m7Ids = jdbcTemplate.queryForList(
                "SELECT product_id FROM biz_product WHERE merchant_id=7 ORDER BY product_id ASC",
                Long.class
        );
        List<Long> m8Ids = jdbcTemplate.queryForList(
                "SELECT product_id FROM biz_product WHERE merchant_id=8 ORDER BY product_id ASC",
                Long.class
        );
        if (m7Ids.size() >= 1) {
            jdbcTemplate.update("UPDATE biz_product SET product_name=CONVERT(0xE7BBBFE59F8EE696B0E9B29CE7899BE5A5B6 USING utf8mb4), description='demo product' WHERE product_id=?",
                    m7Ids.get(0));
        }
        if (m7Ids.size() >= 2) {
            jdbcTemplate.update("UPDATE biz_product SET product_name=CONVERT(0xE7BBBFE59F8EE585A8E9BAA6E99DA2E58C85 USING utf8mb4), description='demo product' WHERE product_id=?",
                    m7Ids.get(1));
        }
        if (m7Ids.size() >= 3) {
            jdbcTemplate.update("UPDATE biz_product SET product_name=CONVERT(0xE7BBBFE59F8EE88BB9E69E9CE69E9CE58887 USING utf8mb4), description='demo product' WHERE product_id=?",
                    m7Ids.get(2));
        }

        if (m8Ids.size() >= 1) {
            jdbcTemplate.update("UPDATE biz_product SET product_name=CONVERT(0xE998B3E58589E58E9FE591B3E985B8E5A5B6 USING utf8mb4), description='demo product' WHERE product_id=?",
                    m8Ids.get(0));
        }
        if (m8Ids.size() >= 2) {
            jdbcTemplate.update("UPDATE biz_product SET product_name=CONVERT(0xE998B3E58589E89B8BE7B395E58DB7 USING utf8mb4), description='demo product' WHERE product_id=?",
                    m8Ids.get(1));
        }
        if (m8Ids.size() >= 3) {
            jdbcTemplate.update("UPDATE biz_product SET product_name=CONVERT(0xE998B3E58589E9B29CE6A999E6B181 USING utf8mb4), description='demo product' WHERE product_id=?",
                    m8Ids.get(2));
        }
        steps.add("product text updated for merchant_id=7/8");

        // 对已写入的“UTF-8字节被按latin1解释”的脏值做回转修复
        jdbcTemplate.execute(
                "UPDATE sys_community SET " +
                        "community_name = CONVERT(BINARY(CONVERT(community_name USING latin1)) USING utf8mb4), " +
                        "province = CONVERT(BINARY(CONVERT(province USING latin1)) USING utf8mb4), " +
                        "city = CONVERT(BINARY(CONVERT(city USING latin1)) USING utf8mb4), " +
                        "district = CONVERT(BINARY(CONVERT(district USING latin1)) USING utf8mb4), " +
                        "address = CONVERT(BINARY(CONVERT(address USING latin1)) USING utf8mb4) " +
                        "WHERE community_id IN (3,4)"
        );
        jdbcTemplate.execute(
                "UPDATE biz_merchant SET " +
                        "merchant_name = CONVERT(BINARY(CONVERT(merchant_name USING latin1)) USING utf8mb4), " +
                        "address = CONVERT(BINARY(CONVERT(address USING latin1)) USING utf8mb4), " +
                        "description = CONVERT(BINARY(CONVERT(description USING latin1)) USING utf8mb4) " +
                        "WHERE merchant_id IN (7,8)"
        );
        steps.add("mojibake transcoding repaired (latin1 -> utf8mb4)");

        operationLogService.logOperation(
                loginUser,
                "FIX_GARBLED_TEXT",
                "SYSTEM",
                null,
                "DEMO_DATA",
                "管理员执行演示数据乱码修复"
        );

        return Result.success(Map.of(
                "message", "乱码修复完成",
                "steps", steps
        ));
    }
}
