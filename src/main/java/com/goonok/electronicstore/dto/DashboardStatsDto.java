package com.goonok.electronicstore.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DashboardStatsDto {

    // Existing fields
    private long totalUsers;
    private long totalProducts;
    private long totalOrders;
    private long pendingOrders;
    private long shippedOrders;
    private long deliveredOrders;
    private BigDecimal totalSell;
    private long lowStockProducts;
    private long todayOrders;
    private BigDecimal todayRevenue;
    private long pendingPayments;
    private int newUsersToday;
    private List<ChartDataPoint> last7DaysOrders;
    private List<ChartDataPoint> last7DaysRevenue;

    // New fields for contact messages
    private Long totalMessages;
    private Long unreadMessages;
    private Long newMessagesToday;
}