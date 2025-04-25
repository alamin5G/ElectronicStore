package com.goonok.electronicstore.service.interfaces;

import com.goonok.electronicstore.dto.CheckoutDto;
import com.goonok.electronicstore.dto.OrderDto;
import com.goonok.electronicstore.dto.PaymentSubmissionResponse;
import com.goonok.electronicstore.dto.PaymentDetailsSubmissionDto;
import com.goonok.electronicstore.enums.OrderStatus; // Import OrderStatus
import com.goonok.electronicstore.exception.ResourceNotFoundException; // Import if used in throws clause
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.goonok.electronicstore.service.interfaces.OrderService;
import org.springframework.transaction.annotation.Transactional; // Import if needed

public interface OrderService {

    // --- User Methods ---

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


    // --- Admin Methods ---

    /**
     * Retrieves all orders placed in the system, with pagination. (For Admin)
     * @param pageable Pagination and sorting information.
     * @return A Page of OrderDto objects.
     */
    @Transactional(readOnly = true) // Annotations typically go on implementation, but can be here
    Page<OrderDto> getAllOrders(Pageable pageable);

    /**
     * Retrieves all orders with a specific status, with pagination. (For Admin Filtering)
     * @param status The OrderStatus to filter by. If null, should ideally fetch all.
     * @param pageable Pagination and sorting information.
     * @return A Page of OrderDto objects matching the status or all if status is null.
     */
    @Transactional(readOnly = true)
    Page<OrderDto> getAllOrders(OrderStatus status, Pageable pageable); // Added overload for status filter

    /**
     * Retrieves a specific order by its ID. (For Admin - no user check)
     * @param orderId The ID of the order.
     * @return The OrderDto.
     * @throws ResourceNotFoundException if order not found.
     */
    @Transactional(readOnly = true)
    OrderDto getOrderById(Long orderId);

    /**
     * Updates the status of a specific order. (For Admin)
     * @param orderId The ID of the order to update.
     * @param newStatus The new OrderStatus.
     * @return The updated OrderDto.
     * @throws ResourceNotFoundException if order not found.
     * @throws IllegalArgumentException if the status transition is invalid.
     */
    @Transactional
    OrderDto updateOrderStatus(Long orderId, OrderStatus newStatus);

    /**
     * Cancels a specific order by setting its status to CANCELLED. (For Admin)
     * @param orderId The ID of the order to cancel.
     * @return The updated OrderDto with CANCELLED status.
     * @throws ResourceNotFoundException if order not found.
     * @throws IllegalArgumentException if the order cannot be cancelled (e.g., already delivered).
     */
    @Transactional
    OrderDto cancelOrder(Long orderId); // Added cancel method

    /**
     * Updates the payment details (status and transaction ID) for a specific order. (For Admin)
     * @param orderId The ID of the order to update.
     * @param paymentStatus The new payment status (e.g., "COMPLETED", "FAILED").
     * @param transactionId Optional transaction ID or reference number.
     * @return The updated OrderDto.
     * @throws ResourceNotFoundException if order not found.
     */
    @Transactional // Added
    OrderDto updatePaymentDetails(Long orderId, String paymentStatus, String transactionId); // <-- ADDED METHOD


    PaymentSubmissionResponse submitPaymentDetails(Long orderId, PaymentDetailsSubmissionDto paymentDetails);

}
