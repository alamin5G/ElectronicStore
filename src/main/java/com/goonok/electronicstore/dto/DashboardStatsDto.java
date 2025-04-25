package com.goonok.electronicstore.dto;

import lombok.Builder; // Using Builder pattern for easy construction
import lombok.Data;
import com.goonok.electronicstore.dto.ChartDataPoint;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO to hold statistics for display on the Admin Dashboard.
 */
@Data
@Builder // Allows easy creation: DashboardStatsDto.builder().totalUsers(10).build()
public class DashboardStatsDto {

    private long totalUsers;
    private long totalProducts;
    private long totalOrders;
    private long pendingOrders;
    private long shippedOrders;
    private long deliveredOrders;
    private BigDecimal totalRevenue; // Sum of totalAmount for DELIVERED orders? Or all orders? Decide business logic.
    private long lowStockProducts; // Count of products below minimum stock level
    // New fields
    private long todayOrders;
    private BigDecimal todayRevenue;
    private long pendingPayments;
    private long newUsersToday;
    private List<ChartDataPoint> last7DaysOrders;
    private List<ChartDataPoint> last7DaysRevenue;
}
