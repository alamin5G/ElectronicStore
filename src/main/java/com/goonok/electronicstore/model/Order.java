package com.goonok.electronicstore.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "`order`") // Escaping the table name 'order' to avoid conflict with the reserved keyword
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    private Double totalPrice;
    private String shippingAddress;
    private String status;
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

    // Getters and setters
}
