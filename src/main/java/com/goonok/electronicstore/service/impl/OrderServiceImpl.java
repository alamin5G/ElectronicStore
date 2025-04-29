package com.goonok.electronicstore.service.impl; // Example implementation package

import com.goonok.electronicstore.dto.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.goonok.electronicstore.enums.OrderStatus;
import com.goonok.electronicstore.enums.PaymentStatus;
import com.goonok.electronicstore.exception.ResourceNotFoundException;
import com.goonok.electronicstore.model.*;
import com.goonok.electronicstore.repository.*;
import com.goonok.electronicstore.service.CartService;
import com.goonok.electronicstore.service.interfaces.OrderService;
import com.goonok.electronicstore.verification.EmailService;
import lombok.extern.slf4j.Slf4j;
import com.goonok.electronicstore.enums.PaymentMethod;
import java.time.LocalDateTime;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CartService cartService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private EmailService emailService;
    @Autowired
    private OrderRepository orderRepository;

    @Override
    @Transactional
    public OrderDto placeOrder(String userEmail, CheckoutDto checkoutDto) {
        log.info("Placing order for user: {}", userEmail);

        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        // 1. Validate Checkout DTO
        if (checkoutDto.getSelectedShippingAddress() == null) {
            throw new IllegalArgumentException("Shipping address must be selected.");
        }
        if (checkoutDto.getSelectedPaymentMethod() == null || checkoutDto.getSelectedPaymentMethod().isBlank()) {
            throw new IllegalArgumentException("Payment method must be selected.");
        }
        AddressDto shippingAddress = checkoutDto.getSelectedShippingAddress();

        // 2. Get current cart details (use the snapshot if available)
        CartDto currentCart = checkoutDto.getCheckoutCart(); // Use cart from checkout DTO
        if (currentCart == null || currentCart.getItems() == null || currentCart.getItems().isEmpty()) {
            log.warn("Cart details missing in checkout DTO for user {}. Refetching.", userEmail);
            currentCart = cartService.getCartForUser(userEmail);
            if (currentCart.getItems() == null || currentCart.getItems().isEmpty()) {
                throw new IllegalStateException("Cannot place order with an empty cart.");
            }
        }


        // 3. Create and Populate Order Entity
        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber(generateOrderNumber());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        // Set Shipping Details using selected address information
        order.setShippingName(shippingAddress.getRecipientName()); // Use recipient name from selected address
        order.setShippingPhone(shippingAddress.getRecipientPhone()); // Use recipient phone from selected address
        order.setShippingStreet(shippingAddress.getStreet());
        order.setShippingCity(shippingAddress.getCity());
        order.setShippingPostalCode(shippingAddress.getPostalCode());
        order.setShippingState(shippingAddress.getState());
        order.setShippingCountry(shippingAddress.getCountry());
        order.setShippingMethod(checkoutDto.getSelectedShippingMethod() != null ? 
                           checkoutDto.getSelectedShippingMethod() : "Standard");

        // Set Payment Details
        try {
            order.setPaymentMethod(PaymentMethod.valueOf(checkoutDto.getSelectedPaymentMethod().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid payment method: " + checkoutDto.getSelectedPaymentMethod());
        }


        // After setting payment method, add this code block
        if (PaymentMethod.COD.equals(order.getPaymentMethod())) {
            order.setTransactionId("COD-" + generateOrderNumber()); // Generate a unique COD transaction ID
            order.setPaymentStatus(PaymentStatus.PENDING);
        } else if (PaymentMethod.valueOf(checkoutDto.getSelectedPaymentMethod()) == PaymentMethod.BKASH ||
                PaymentMethod.valueOf(checkoutDto.getSelectedPaymentMethod()) == PaymentMethod.NAGAD) {
            String txnId = checkoutDto.getTransactionId();
            // Check if transaction ID already exists
            if (orderRepository.existsByTransactionId(txnId)) {
                throw new IllegalStateException("Transaction ID already used in another order");
            }
            order.setTransactionId(checkoutDto.getTransactionId());
            order.setPaymentNotes(checkoutDto.getPaymentNotes());
            order.setPaymentSubmissionDate(LocalDateTime.now());
            order.setStatus(OrderStatus.PENDING);
            order.setPaymentStatus(PaymentStatus.AWAITING_VERIFICATION);
        }

        // Set payment details if mobile payment
        // For mobile payments, validate transaction ID
       /* if (PaymentMethod.valueOf(checkoutDto.getSelectedPaymentMethod()) == PaymentMethod.BKASH ||
                PaymentMethod.valueOf(checkoutDto.getSelectedPaymentMethod()) == PaymentMethod.NAGAD) {

            String txnId = checkoutDto.getTransactionId();
            // Check if transaction ID already exists
            if (orderRepository.existsByTransactionId(txnId)) {
                throw new IllegalStateException("Transaction ID already used in another order");
            }

            order.setTransactionId(checkoutDto.getTransactionId());
            order.setPaymentNotes(checkoutDto.getPaymentNotes());
            order.setPaymentSubmissionDate(LocalDateTime.now());
            order.setStatus(OrderStatus.PENDING);
            order.setPaymentStatus(PaymentStatus.AWAITING_VERIFICATION);
        } else if (PaymentMethod.COD.equals(order.getPaymentMethod())) {
            order.setPaymentStatus(PaymentStatus.PENDING);
        } else {
            order.setStatus(OrderStatus.PENDING);
            order.setPaymentStatus(PaymentStatus.AWAITING_VERIFICATION);
        }*/

        // Calculate totals
        BigDecimal subtotal = currentCart.getCartTotalPrice();
        BigDecimal shippingCost = checkoutDto.getShippingCost() != null ? checkoutDto.getShippingCost() : calculateShippingCost(shippingAddress);
        BigDecimal taxAmount = calculateTax(subtotal);
        BigDecimal totalAmount = subtotal.add(shippingCost).add(taxAmount);

        order.setShippingCost(shippingCost);
        order.setTaxAmount(taxAmount);
        order.setTotalAmount(totalAmount);

        // 4. Create OrderItem Entities and Update Stock
        final AtomicReference<Boolean> stockIssue = new AtomicReference<>(false);
        List<OrderItem> orderItems = currentCart.getItems().stream().map(cartItemDto -> {
            Long productId = cartItemDto.getProductId();
            int quantity = cartItemDto.getQuantity();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "ID", productId + " (during order placement)"));

            // ** Critical Stock Check **
            if (product.getStockQuantity() == null || product.getStockQuantity() < quantity) { // Added null check
                log.error("Stock issue for product ID {}. Requested: {}, Available: {}", productId, quantity, product.getStockQuantity());
                stockIssue.set(true);
                throw new IllegalStateException("Insufficient stock for product: " + product.getName());
            }

            // ** Decrease Stock **
            product.setStockQuantity(product.getStockQuantity() - quantity);
            productRepository.save(product);

            // Create OrderItem
            OrderItem orderItem = new OrderItem();
            // orderItem.setOrder(order); // Link will be set by JPA via @ManyToOne relationship if cascade is correct
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setPricePerItem(cartItemDto.getPricePerItem()); // Use price from cart item

            return orderItem;

        }).collect(Collectors.toList());

        if (stockIssue.get()) {
            throw new IllegalStateException("Order cannot be placed due to insufficient stock for one or more items.");
        }

        // 5. Save Order and OrderItems
        // Set items on Order *before* saving if Order is the owning side with CascadeType.ALL
        order.setOrderItems(orderItems);
        // Set the inverse reference on each item manually *if needed* (depends on cascade/owning side)
        for(OrderItem item : orderItems) {
            item.setOrder(order);
        }
        Order savedOrder = orderRepository.save(order); // Save order (should cascade to items)

        // 6. Clear the user's shopping cart
        cartService.clearCart(userEmail);

        // 7. TODO: Send Order Confirmation Email
        //sendOrderConfirmationEmail(savedOrder);

        log.info("Order placed successfully: Order Number {}", savedOrder.getOrderNumber());

        // 8. Map saved order to DTO and return
        return mapOrderToDto(savedOrder);
    }

    // getOrderByIdForUser, getOrdersForUser methods remain the same...
    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderByIdForUser(String userEmail, Long orderId) {
        log.debug("Fetching order ID {} for user {}", orderId, userEmail);
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "ID", orderId));
        if (!order.getUser().getUserId().equals(user.getUserId())) {
            log.warn("User {} attempted to access order ID {} which does not belong to them.", userEmail, orderId);
            throw new ResourceNotFoundException("Order", "ID", orderId);
        }
        return mapOrderToDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> getOrdersForUser(String userEmail, Pageable pageable) {
        log.debug("Fetching orders for user {} with pageable {}", userEmail, pageable);
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));
        Page<Order> orderPage = orderRepository.findByUserOrderByOrderDateDesc(user, pageable);
        return orderPage.map(this::mapOrderToDto);
    }

    // --- Admin Methods ---

    @Transactional(readOnly = true)
    @Override
    public Page<OrderDto> getAllOrders(Pageable pageable) {
        log.info("Admin fetching all orders with pageable: {}", pageable);
        // Use the sorted version if available in repository, otherwise fallback
        // Page<Order> orderPage = orderRepository.findAllByOrderByOrderDateDesc(pageable);
        Page<Order> orderPage = orderRepository.findAll(pageable); // Using standard findAll
        return orderPage.map(this::mapOrderToDto);
    }

    // *** IMPLEMENTED getAllOrders with status filter ***
    @Transactional(readOnly = true)
    @Override
    public Page<OrderDto> getAllOrders(OrderStatus status, Pageable pageable) {
        log.info("Admin fetching orders with status {} and pageable: {}", status, pageable);
        Page<Order> orderPage;
        if (status != null) {
            // Assumes findByStatusOrderByOrderDateDesc exists in OrderRepository
            orderPage = orderRepository.findByStatusOrderByOrderDateDesc(status, pageable);
        } else {
            // Fallback to getting all orders if status is null (should ideally not happen if controller logic is correct)
            // orderPage = orderRepository.findAllByOrderByOrderDateDesc(pageable);
            orderPage = orderRepository.findAll(pageable);
        }
        return orderPage.map(this::mapOrderToDto);
    }

    @Transactional(readOnly = true)
    @Override
    public OrderDto getOrderById(Long orderId) {
        log.info("Admin fetching order by ID: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "ID", orderId));
        return mapOrderToDto(order);
    }

    @Override
    @jakarta.transaction.Transactional
    public OrderDto updateOrderStatus(Long orderId, OrderStatus newStatus) {
        log.warn("Admin attempting to update status for order ID {} to {}", orderId, newStatus);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "ID", orderId));

        OrderStatus currentStatus = order.getStatus();
        if (currentStatus == OrderStatus.DELIVERED || currentStatus == OrderStatus.CANCELLED) {
            log.error("Attempted to update status of already completed/cancelled order ID: {}", orderId);
            throw new IllegalArgumentException("Order status cannot be updated once it is DELIVERED or CANCELLED.");
        }
        // Add more transition validation if needed

        order.setStatus(newStatus);
        // TODO: Side effects (tracking, payment update for COD, email)

        Order updatedOrder = orderRepository.save(order);
        log.info("Order status updated successfully for order ID {} to {}", orderId, newStatus);
        return mapOrderToDto(updatedOrder);
    }



    // *** IMPLEMENTED cancelOrder ***
    /**
     * Cancels an order by setting its status to CANCELLED.
     * @param orderId The ID of the order to cancel.
     * @return The updated OrderDto.
     */
    @Transactional
    @Override
    public OrderDto cancelOrder(Long orderId) {
        log.warn("Admin attempting to CANCEL order ID {}", orderId);
        // Use the existing update method, ensuring CANCELLED is a valid transition
        // The updateOrderStatus method already contains checks for DELIVERED/CANCELLED states.
        // TODO: Add logic for restocking items here if cancellation is allowed after processing/shipping?
        return updateOrderStatus(orderId, OrderStatus.CANCELLED);
    }

    @Override
    @Transactional
    public OrderDto updatePaymentDetails(Long orderId, String paymentStatus, String transactionId) {
        log.info("Admin attempting to update payment details for order ID {} to Status: {}, TxnID: {}", orderId, paymentStatus, transactionId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "ID", orderId));

        try {
            PaymentStatus newStatus = PaymentStatus.valueOf(paymentStatus.toUpperCase());
            order.setPaymentStatus(newStatus);
            order.setTransactionId(StringUtils.hasText(transactionId) ? transactionId.trim() : null);

            Order updatedOrder = orderRepository.save(order);
            log.info("Payment details updated successfully for order ID {}", orderId);

            return mapOrderToDto(updatedOrder);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid payment status: " + paymentStatus);
        } catch (Exception e){
            throw new IllegalArgumentException("Error on updating payment details");
        }

        // TODO: Trigger any side effects? (e.g., notify user, update related systems)

    }


    @Override
    public PaymentSubmissionResponse submitPaymentDetails(Long orderId, PaymentDetailsSubmissionDto paymentDetails) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Validate order state
        if (!order.getStatus().equals(OrderStatus.PENDING_PAYMENT)) {
            throw new InvalidOperationException("Payment details can only be submitted for orders awaiting payment");
        }

        // Update order with payment details
        order.setTransactionId(paymentDetails.getTransactionId());
        order.setPaymentMethod(PaymentMethod.valueOf(paymentDetails.getPaymentMethod()));
        order.setPaymentNotes(paymentDetails.getNotes());
        order.setStatus(OrderStatus.PENDING_PAYMENT);  // Use existing enum value
        order.setPaymentSubmissionDate(LocalDateTime.now());

        orderRepository.save(order);

        return new PaymentSubmissionResponse(
                true,
                "Payment details submitted successfully. Our team will verify the payment shortly.",
                order.getStatus().toString()
        );
    }



    @Override
    @Transactional(readOnly = true)
    public long countTotalOrders() {
        return orderRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countOrdersByStatus(OrderStatus status) {
        return orderRepository.countOrdersByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalRevenue() {
        return orderRepository.calculateTotalRevenue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getRecentOrders(int limit) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "orderDate"));
        return orderRepository.findAll(pageable)
                .stream()
                .map(this::mapOrderToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countTodayOrders() {
        return orderRepository.countTodayOrders();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateTodayRevenue() {
        return orderRepository.calculateTodayRevenue();
    }

    @Override
    @Transactional(readOnly = true)
    public long countPendingPayments() {
        return orderRepository.countPendingPayments();
    }




    // --- Helper Methods ---
    private String generateOrderNumber() { return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase(); }
    private BigDecimal calculateShippingCost(AddressDto shippingAddress) { return new BigDecimal("50.00"); } // Placeholder
    private BigDecimal calculateTax(BigDecimal subtotal) { return BigDecimal.ZERO; } // Placeholder

    //send email method
    private void sendOrderConfirmationEmail(Order order){
        User user = order.getUser();
        String orderNumber = order.getOrderNumber();
        String subject = "Order Confirmation - " + orderNumber;
        String message = "Dear " + order.getShippingName() + ",\n\n" +
                "Thank you for your order! Your order number is " + orderNumber + ".\n" +
                "We will process your order and notify you when it's shipped.\n\n" +
                "Order Details:\n" +
                "Order Number: " + orderNumber + "\n" +
                "Order Date: " + order.getOrderDate() + "\n" +
                "Total Amount: " + order.getTotalAmount() + "\n" +
                "Shipping Address:\n" +
                order.getShippingStreet() + ", " + order.getShippingCity() + ", " + order.getShippingPostalCode() + ", " + order.getShippingCountry() + "\n\n" +
                "Payment Method: " + order.getPaymentMethod() + "\n" +
                "Payment Status: " + order.getPaymentStatus() + "\n\n" +
                "Thank you for shopping with us!\n\n" +
                "Best regards,\n" +
                "The Electronic Store Team";

        emailService.sendOrderConfirmationEmail(user, subject, message);
    };

    // Updated mapping helpers to handle potential nulls
    private OrderDto mapOrderToDto(Order order) {
        if (order == null) return null;

        OrderDto orderDto = modelMapper.map(order, OrderDto.class);

        if (order.getOrderItems() != null) {
            orderDto.setOrderItems(order.getOrderItems().stream()
                    .map(this::mapOrderItemToDto)
                    .collect(Collectors.toList()));
        } else {
            orderDto.setOrderItems(new ArrayList<>());
        }
        if (order.getUser() != null) {
            orderDto.setUserId(order.getUser().getUserId());
        }
        // Explicitly map shipping fields if names differ or for clarity
        orderDto.setShippingName(order.getShippingName());
        orderDto.setShippingPhone(order.getShippingPhone());
        orderDto.setShippingStreet(order.getShippingStreet());
        orderDto.setShippingCity(order.getShippingCity());
        orderDto.setShippingPostalCode(order.getShippingPostalCode());
        // Map state/country if they exist
        orderDto.setShippingState(order.getShippingState());
        orderDto.setShippingCountry(order.getShippingCountry());
        orderDto.setShippingMethod(order.getShippingMethod());
        // Map payment fields
        orderDto.setPaymentStatus(order.getPaymentStatus());
        orderDto.setTransactionId(order.getTransactionId());
        orderDto.setPaymentMethod(String.valueOf(order.getPaymentMethod()));

        return orderDto;
    }

    private OrderItemDto mapOrderItemToDto(OrderItem orderItem) {
        if (orderItem == null) return null;
        OrderItemDto itemDto = modelMapper.map(orderItem, OrderItemDto.class);
        if (itemDto.getPricePerItem() != null && itemDto.getQuantity() > 0) {
            itemDto.setItemTotalPrice(itemDto.getPricePerItem().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
        } else {
            itemDto.setItemTotalPrice(BigDecimal.ZERO);
        }
        if (orderItem.getProduct() != null) {
            itemDto.setProductId(orderItem.getProduct().getProductId());
            itemDto.setProductName(orderItem.getProduct().getName());
            itemDto.setProductImagePath(orderItem.getProduct().getImagePath());
        } else {
            itemDto.setProductName("Product Unavailable");
        }
        return itemDto;
    }



}