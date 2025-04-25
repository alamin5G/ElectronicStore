package com.goonok.electronicstore.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

@Data
public class AdminUserViewDto {
    // Existing fields
    private Long userId;
    private String name;
    private String email;
    private String phoneNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isEnabled;
    private boolean isVerified;
    private Set<String> roleNames = new HashSet<>();

    // New fields for order analytics
    private Integer totalOrders;
    private LocalDateTime lastOrderDate;
    private BigDecimal totalSpent;
    private Integer totalProductsPurchased;
    private String lastOrderStatus;

    // Account activity metrics
    private LocalDateTime lastLoginDate;
    private Integer loginCount;
    private String lastLoginIP;

    // Address information
    private Integer totalAddresses;
    private String defaultShippingAddress;
    private String defaultBillingAddress;

    // Cart information
    private Integer currentCartItems;
    private BigDecimal currentCartValue;

    // Customer segment/status
    private String customerTier; // e.g., "BRONZE", "SILVER", "GOLD" based on spending
    private boolean hasActiveSubscription;
}