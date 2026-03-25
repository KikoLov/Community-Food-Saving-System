package com.food.controller;

import com.food.common.Result;
import com.food.entity.Category;
import com.food.entity.Community;
import com.food.entity.Merchant;
import com.food.entity.OperationLog;
import com.food.entity.Order;
import com.food.security.LoginUser;
import com.food.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 获取所有商户列表
     */
    @GetMapping("/merchants")
    public Result<List<Merchant>> getMerchants() {
        List<Merchant> merchants = merchantService.getAllMerchants();
        return Result.success(merchants);
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
}
