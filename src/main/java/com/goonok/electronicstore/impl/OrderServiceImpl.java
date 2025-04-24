package com.goonok.electronicstore.impl; // Example implementation package

import com.goonok.electronicstore.dto.*;
import com.goonok.electronicstore.enums.OrderStatus; // Assuming enum exists
import com.goonok.electronicstore.exception.ResourceNotFoundException;
import com.goonok.electronicstore.model.*;
import com.goonok.electronicstore.repository.*;
import com.goonok.electronicstore.service.CartService;
import com.goonok.electronicstore.service.OrderService;
// import com.goonok.electronicstore.service.ProductService; // Not directly needed if using ProductRepository
import com.goonok.electronicstore.verification.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
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
        order.setPaymentMethod(checkoutDto.getSelectedPaymentMethod());
        if ("COD".equalsIgnoreCase(checkoutDto.getSelectedPaymentMethod())) {
            order.setPaymentStatus("PENDING");
        } else {
            order.setPaymentStatus("AWAITING_VERIFICATION");
        }

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
        Page<Order> orderPage = orderRepository.findAll(pageable); // Use built-in findAll
        return orderPage.map(this::mapOrderToDto); // Reuse mapping helper
    }

    @Transactional(readOnly = true)
    @Override
    public OrderDto getOrderById(Long orderId) {
        log.info("Admin fetching order by ID: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "ID", orderId));
        return mapOrderToDto(order);
    }

    @Transactional
    @Override
    public OrderDto updateOrderStatus(Long orderId, OrderStatus newStatus) {
        log.warn("Admin attempting to update status for order ID {} to {}", orderId, newStatus);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "ID", orderId));

        // Optional: Add logic to validate status transitions (e.g., cannot go from DELIVERED back to PENDING)
        OrderStatus currentStatus = order.getStatus();
        if (currentStatus == OrderStatus.DELIVERED || currentStatus == OrderStatus.CANCELLED) {
            log.error("Attempted to update status of already completed/cancelled order ID: {}", orderId);
            throw new IllegalArgumentException("Order status cannot be updated once it is DELIVERED or CANCELLED.");
        }
        // Add more specific transition rules if needed

        order.setStatus(newStatus);
        // TODO: Add logic for side effects of status change
        // e.g., if status is SHIPPED, maybe generate tracking number?
        // e.g., if status is DELIVERED, update payment status for COD?
        // e.g., if status is CANCELLED, potentially restock items? (Complex!)

        Order updatedOrder = orderRepository.save(order);
        log.info("Order status updated successfully for order ID {} to {}", orderId, newStatus);

        // TODO: Send status update notification email to user?

        return mapOrderToDto(updatedOrder);
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