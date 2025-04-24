package com.goonok.electronicstore.service;

import com.goonok.electronicstore.dto.CheckoutDto;
import com.goonok.electronicstore.dto.OrderDto;
import com.goonok.electronicstore.enums.OrderStatus;
import com.goonok.electronicstore.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface OrderService {

    /**
     * Places a new order based on the user's cart and checkout details.
     * This involves creating Order/OrderItems, reducing stock, clearing cart.
     *
     * @param userEmail   The email of the logged-in user.
     * @param checkoutDto DTO containing selected shipping address and payment method.
     * @return The created OrderDto.
     */
    OrderDto placeOrder(String userEmail, CheckoutDto checkoutDto);

    /**
     * Retrieves a specific order by its ID, ensuring it belongs to the user.
     *
     * @param userEmail The email of the logged-in user.
     * @param orderId   The ID of the order to retrieve.
     * @return The OrderDto.
     * @throws ResourceNotFoundException if order not found or doesn't belong to user.
     */
    OrderDto getOrderByIdForUser(String userEmail, Long orderId);

    /**
     * Retrieves all orders for a specific user with pagination.
     *
     * @param userEmail The email of the logged-in user.
     * @param pageable  Pagination information.
     * @return A Page of OrderDto objects.
     */
    Page<OrderDto> getOrdersForUser(String userEmail, Pageable pageable);

    @Transactional(readOnly = true)
    Page<OrderDto> getAllOrders(Pageable pageable);

    @Transactional(readOnly = true)
    OrderDto getOrderById(Long orderId);

    @Transactional
    OrderDto updateOrderStatus(Long orderId, OrderStatus newStatus);

    // Add methods for admin order management later (e.g., updateStatus, getAllOrders)

}
