package com.goonok.electronicstore.model;

import com.goonok.electronicstore.enums.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "`order`") // Escaping the table name 'order' to avoid conflict with the reserved keyword
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @NotNull
    private BigDecimal totalPrice;
    private String shippingAddress;

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // e.g., PENDING, SHIPPED, DELIVERED
    @Column(nullable = false)
    private String paymentType = "Cash on Delivery"; // Default to COD

    private String paymentStatus;
    private String trackingNumber;

    private LocalDateTime orderDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private Admin admin;

    @OneToOne(mappedBy = "order")  // One-to-One relationship for payments
    private Payment payment;  // Only one payment per order

    @ManyToOne
    @JoinColumn(name = "discount_id")
    private Discount discount;  // New relationship with Discount entity

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    // Getters and setters
}
