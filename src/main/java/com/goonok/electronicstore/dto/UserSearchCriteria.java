package com.goonok.electronicstore.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class UserSearchCriteria {
    // Basic user information filters
    private String nameContains;
    private String emailContains;
    private String phoneContains;

    // Account status filters
    private Boolean enabled;
    private Boolean verified;

    // Role-based filters
    private String hasRole;

    // Date range filters
    private LocalDateTime createdDateStart;
    private LocalDateTime createdDateEnd;
    private LocalDateTime lastLoginStart;
    private LocalDateTime lastLoginEnd;

    // Order-related filters
    private Integer minTotalOrders;
    private Integer maxTotalOrders;
    private BigDecimal minTotalSpent;
    private BigDecimal maxTotalSpent;

    // Customer tier filters
    private String customerTier;

    // Activity filters
    private Integer minLoginCount;
    private Integer maxLoginCount;

    // Cart status filters
    private Boolean hasActiveCart;
    private BigDecimal minCartValue;

    // Subscription status
    private Boolean hasActiveSubscription;
}