package com.goonok.electronicstore.controller;

import com.goonok.electronicstore.dto.AdminUserViewDto;
import com.goonok.electronicstore.dto.DashboardStatsDto;
import com.goonok.electronicstore.dto.OrderDto;
import com.goonok.electronicstore.enums.OrderStatus;
import com.goonok.electronicstore.service.interfaces.ContactService;
import com.goonok.electronicstore.service.interfaces.OrderService;
import com.goonok.electronicstore.service.interfaces.ProductService;
import com.goonok.electronicstore.service.interfaces.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.goonok.electronicstore.dto.DashboardStatsDto;
import com.goonok.electronicstore.enums.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private static final int LOW_STOCK_THRESHOLD = 35; // Adjust this value as needed
    private static final int LAST_7_DAYS_PERIOD = 7;
    private static final int LAST_30_DAYS_PERIOD = 30;
    private static final int LAST_90_DAYS_PERIOD = 90;
    private static final int LAST_180_DAYS_PERIOD = 180;
    private static final int LAST_365_DAYS_PERIOD = 365;
    private static final int LEAST_VIEW_LIMIT = 5;


    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ContactService contactService;

    @GetMapping
    public String showDashboard(Model model) {
        try {
            // Basic Stats
            DashboardStatsDto stats = DashboardStatsDto.builder()
                    .totalUsers(userService.countTotalUsers())
                    .totalProducts(productService.countTotalProducts())
                    .totalOrders(orderService.countTotalOrders())
                    .pendingOrders(orderService.countOrdersByStatus(OrderStatus.PENDING))
                    .shippedOrders(orderService.countOrdersByStatus(OrderStatus.SHIPPED))
                    .deliveredOrders(orderService.countOrdersByStatus(OrderStatus.DELIVERED))
                    .totalSell(orderService.calculateTotalRevenue())
                    .lowStockProducts(productService.countLowStockProducts(LOW_STOCK_THRESHOLD))
                    // New stats
                    .todayOrders(orderService.countTodayOrders())
                    .todayRevenue(orderService.calculateTodayRevenue())
                    .pendingPayments(orderService.countPendingPayments())
                    .newUsersToday(userService.countNewUsersToday())
                    // Contact message stats
                    .totalMessages(contactService.countTotalMessages())
                    .unreadMessages(contactService.countUnreadMessages())
                    .newMessagesToday(contactService.countNewMessagesToday())
                    .build();

            model.addAttribute("stats", stats);

            // Recent Activity
            model.addAttribute("recentOrders", orderService.getRecentOrders(LEAST_VIEW_LIMIT));
            model.addAttribute("lowStockProducts", productService.getLowStockProductsList(LOW_STOCK_THRESHOLD));
            model.addAttribute("recentUsers", userService.getRecentUsers(LEAST_VIEW_LIMIT));
            // Add recent messages
            model.addAttribute("recentMessages", contactService.getRecentMessages(LEAST_VIEW_LIMIT));

        } catch (Exception e) {
            log.error("Error loading dashboard data", e);
            handleDashboardError(model);
        }

        return "admin/dashboard";
    }




    private void handleDashboardError(Model model) {
        model.addAttribute("error", "Failed to load dashboard data");
        model.addAttribute("stats", DashboardStatsDto.builder()
                .totalUsers(0)
                .totalProducts(0)
                .totalOrders(0)
                .pendingOrders(0)
                .shippedOrders(0)
                .deliveredOrders(0)
                .totalSell(BigDecimal.ZERO)
                .lowStockProducts(0)
                .todayOrders(0)
                .todayRevenue(BigDecimal.ZERO)
                .pendingPayments(0)
                .newUsersToday(0)
                .last7DaysOrders(Collections.emptyList())
                .last7DaysRevenue(Collections.emptyList())
                // Add default values for contact message stats
                .totalMessages(0L)
                .unreadMessages(0L)
                .newMessagesToday(0L)
                .build());
    }
}
