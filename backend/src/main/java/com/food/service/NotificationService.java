package com.food.service;

import com.food.dto.NotificationItemDTO;
import com.food.entity.Order;
import com.food.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 站内提醒服务（基于现有业务数据动态生成）
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final OrderService orderService;
    private final MerchantService merchantService;

    public List<NotificationItemDTO> getNotifications(LoginUser loginUser) {
        Integer userType = loginUser.getUserType();
        if (userType == null) {
            return List.of();
        }
        if (userType == 1) {
            return buildConsumerNotifications(loginUser.getUserId());
        }
        if (userType == 2) {
            Long merchantId = loginUser.getMerchantId();
            if (merchantId == null) {
                var merchant = merchantService.getMerchantByUserId(loginUser.getUserId());
                merchantId = merchant != null ? merchant.getMerchantId() : null;
            }
            if (merchantId == null) {
                return List.of();
            }
            return buildMerchantNotifications(merchantId);
        }
        if (userType == 3) {
            return buildAdminNotifications();
        }
        return List.of();
    }

    public int getUnreadCount(LoginUser loginUser) {
        return getNotifications(loginUser).size();
    }

    private List<NotificationItemDTO> buildConsumerNotifications(Long userId) {
        List<Order> orders = orderService.getUserOrders(userId);
        List<NotificationItemDTO> list = new ArrayList<>();
        for (Order o : orders) {
            if (o.getOrderStatus() != null && o.getOrderStatus() != 0) {
                NotificationItemDTO item = new NotificationItemDTO();
                item.setId("consumer-order-" + o.getOrderId());
                item.setTitle("订单状态更新");
                item.setContent("订单 " + o.getOrderNo() + " 当前状态：" + orderStatusText(o.getOrderStatus()));
                item.setLevel(o.getOrderStatus() == 1 ? "success" : (o.getOrderStatus() == 3 ? "danger" : "warning"));
                item.setTargetPath("/consumer/orders");
                item.setCreateTime(o.getUpdateTime() != null ? o.getUpdateTime() : o.getCreateTime());
                list.add(item);
            }
        }
        list.sort(Comparator.comparing(NotificationItemDTO::getCreateTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return list.size() > 20 ? list.subList(0, 20) : list;
    }

    private List<NotificationItemDTO> buildMerchantNotifications(Long merchantId) {
        List<Order> orders = orderService.getMerchantOrders(merchantId);
        List<NotificationItemDTO> list = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        long pendingCount = orders.stream().filter(x -> x.getOrderStatus() != null && x.getOrderStatus() == 0).count();
        if (pendingCount > 0) {
            NotificationItemDTO summary = new NotificationItemDTO();
            summary.setId("merchant-pending-summary");
            summary.setTitle("待核销订单提醒");
            summary.setContent("当前有 " + pendingCount + " 笔待核销订单，请尽快处理。");
            summary.setLevel("warning");
            summary.setTargetPath("/merchant/orders");
            summary.setCreateTime(now);
            list.add(summary);
        }

        for (Order o : orders) {
            if (o.getOrderStatus() != null && o.getOrderStatus() == 0) {
                NotificationItemDTO item = new NotificationItemDTO();
                item.setId("merchant-order-" + o.getOrderId());
                item.setTitle("新订单待核销");
                item.setContent("订单 " + o.getOrderNo() + "（" + o.getProductName() + " x " + o.getQuantity() + "）待核销");
                item.setLevel("info");
                item.setTargetPath("/merchant/verify");
                item.setCreateTime(o.getCreateTime());
                list.add(item);
            }
        }

        list.sort(Comparator.comparing(NotificationItemDTO::getCreateTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return list.size() > 20 ? list.subList(0, 20) : list;
    }

    private List<NotificationItemDTO> buildAdminNotifications() {
        List<Order> orders = orderService.getAllOrders();
        LocalDateTime now = LocalDateTime.now();
        long pendingVerify = orders.stream().filter(x -> x.getOrderStatus() != null && x.getOrderStatus() == 0).count();
        long canceled = orders.stream().filter(x -> x.getOrderStatus() != null && x.getOrderStatus() == 2).count();
        long expired = orders.stream().filter(x -> x.getOrderStatus() != null && x.getOrderStatus() == 3).count();

        List<NotificationItemDTO> list = new ArrayList<>();
        if (pendingVerify > 0) {
            NotificationItemDTO item = new NotificationItemDTO();
            item.setId("admin-pending");
            item.setTitle("平台待核销提醒");
            item.setContent("当前全平台待核销订单 " + pendingVerify + " 笔。");
            item.setLevel("warning");
            item.setTargetPath("/admin/orders");
            item.setCreateTime(now);
            list.add(item);
        }
        if (canceled > 0) {
            NotificationItemDTO item = new NotificationItemDTO();
            item.setId("admin-canceled");
            item.setTitle("取消订单监控");
            item.setContent("当前取消订单累计 " + canceled + " 笔。");
            item.setLevel("info");
            item.setTargetPath("/admin/orders");
            item.setCreateTime(now.minusMinutes(1));
            list.add(item);
        }
        if (expired > 0) {
            NotificationItemDTO item = new NotificationItemDTO();
            item.setId("admin-expired");
            item.setTitle("过期订单提醒");
            item.setContent("当前过期订单累计 " + expired + " 笔，请关注库存和核销效率。");
            item.setLevel("danger");
            item.setTargetPath("/admin/orders");
            item.setCreateTime(now.minusMinutes(2));
            list.add(item);
        }
        return list;
    }

    private String orderStatusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待核销";
            case 1 -> "已核销";
            case 2 -> "已取消";
            case 3 -> "已过期";
            default -> "未知";
        };
    }
}
