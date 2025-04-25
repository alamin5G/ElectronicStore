package com.goonok.electronicstore.dto;

import com.goonok.electronicstore.enums.OrderStatus; // Assuming you have this enum
import com.goonok.electronicstore.enums.PaymentStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO representing a placed order for display purposes.
 */
@Data
public class OrderDto {

    private Long orderId;
    private String orderNumber;
    private LocalDateTime orderDate;
    private OrderStatus status; // Use Enum
    private BigDecimal totalAmount;
    private BigDecimal shippingCost;
    private BigDecimal taxAmount; // Optional

    // Shipping Details (denormalized)
    private String shippingStreet;
    private String shippingCity;
    private String shippingPostalCode;
    private String shippingState;
    private String shippingCountry;

    // Add name/phone if stored on order
    private String shippingName;
    private String shippingPhone;

    private String shippingMethod;
    private PaymentStatus paymentStatus; // Change from String to PaymentStatus
    private String transactionId; // Gateway or manual reference
    private String trackingNumber;
    private String paymentMethod;

    private Long userId; // ID of the user who placed the order
    private String userName; // <-- ADDED
    private String userEmail;    // <-- ADDED
    private List<OrderItemDto> orderItems = new ArrayList<>();
}