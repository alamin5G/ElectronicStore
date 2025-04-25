package com.goonok.electronicstore.repository;

import com.goonok.electronicstore.enums.OrderStatus;
import com.goonok.electronicstore.model.Order;
import com.goonok.electronicstore.dto.ChartDataPoint;
import com.goonok.electronicstore.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDateTime;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser_UserId(Long userId);

    /**
     * Finds all orders placed by a specific user, ordered by date descending.
     *
     * @param user     The user entity.
     * @param pageable Pagination information.
     * @return A page of the user's orders.
     */
    Page<Order> findByUserOrderByOrderDateDesc(User user, Pageable pageable);

    /**
     * Finds all orders placed by a specific user (non-paginated version if needed).
     *
     * @param user The user entity.
     * @return A list of the user's orders.
     */
    List<Order> findByUserOrderByOrderDateDesc(User user); // Non-paginated version

    Page<Order> findByStatusOrderByOrderDateDesc(OrderStatus status, Pageable pageable);

    List<Order> findByUser(User user);

    // Add other query methods if needed (e.g., find by status, find by date range)

    // Count total orders for a user
    Integer countByUser(User user);

    // Calculate total amount spent by user
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.user = :user")
    BigDecimal calculateTotalSpentByUser(@Param("user") User user);


    // Find most recent order by creation date
    Optional<Order> findTopByUserOrderByCreatedAtDesc(User user);

    boolean existsByTransactionId(String transactionId);


    // For dashboard stats
    @Query("SELECT COUNT(o) FROM Order o WHERE DATE(o.orderDate) = CURRENT_DATE")
    long countTodayOrders();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status != 'CANCELLED'")
    BigDecimal calculateTotalRevenue();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE DATE(o.orderDate) = CURRENT_DATE AND o.status != 'CANCELLED'")
    BigDecimal calculateTodayRevenue();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.paymentStatus = 'PENDING'")
    long countPendingPayments();



    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countOrdersByStatus(@Param("status") OrderStatus status);

    }