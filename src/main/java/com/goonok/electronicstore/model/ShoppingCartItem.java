package com.goonok.electronicstore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min; // Added
import jakarta.validation.constraints.NotNull; // Added
import lombok.Data;
import lombok.EqualsAndHashCode; // Added
import lombok.ToString;         // Added
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal; // Added

@Data
@Entity
@Table(name = "shopping_cart_items") // Renamed table
public class ShoppingCartItem { // Renamed class

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartItemId; // Renamed from cartId

    @ManyToOne(fetch = FetchType.LAZY) // Confirmed ManyToOne, set fetch LAZY
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude         // Added
    @EqualsAndHashCode.Exclude // Added
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) // Set fetch LAZY
    @JoinColumn(name = "product_id", nullable = false) // Added nullable=false
    @ToString.Exclude         // Added
    @EqualsAndHashCode.Exclude // Added
    private Product product;

    @Min(value = 1, message = "Quantity must be at least 1") // Added validation
    @Column(nullable = false)                             // Added nullable=false
    private Integer quantity; // Changed from int to Integer

    @NotNull(message="Price per item cannot be null") // Added
    @Column(nullable = false, precision = 10, scale = 2) // Added field and constraints
    private BigDecimal pricePerItem; // Added: Price at time of adding

    // Inside ShoppingCartItem class

    @Column(nullable = false, updatable = false) // Ensure created date isn't updated
    @CreationTimestamp
    private java.time.LocalDateTime createdAt;

    @UpdateTimestamp
    private java.time.LocalDateTime updatedAt;
}