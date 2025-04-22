package com.goonok.electronicstore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "product_stock_transactions")
public class ProductStockTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Product product;

    @NotNull(message = "Quantity change cannot be null")
    @Column(nullable = false)
    private Integer quantityChange; // Positive for stock in, negative for stock out

    @NotNull(message = "Transaction type cannot be null")
    @Column(nullable = false, length = 50)
    private String transactionType; // e.g., "INITIAL_STOCK", "SALE", "RETURN", "ADJUSTMENT"

    @Column(columnDefinition = "TEXT")
    private String notes; // Optional notes about the transaction

    // Optional: Link to the order if it's a sale/return
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id") // Nullable, as not all transactions relate to an order
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Order order;

    // Optional: Link to the admin user who made an adjustment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_user_id") // Using User entity ID here
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User adminUser; // User who performed the action (if applicable)

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime transactionTimestamp;
}