package com.goonok.electronicstore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin; // Added
import jakarta.validation.constraints.Min;       // Added
import jakarta.validation.constraints.NotNull;   // Added
import lombok.Data;
import lombok.EqualsAndHashCode; // Added
import lombok.ToString;         // Added
import org.hibernate.annotations.CreationTimestamp; // Added
import org.hibernate.annotations.UpdateTimestamp;   // Added

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "order_items") // Added table name
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY) // Added fetch type
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude         // Added
    @EqualsAndHashCode.Exclude // Added
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY) // Added fetch type
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude         // Added
    @EqualsAndHashCode.Exclude // Added
    private Product product;

    @NotNull(message = "Quantity is required") // Added validation
    @Min(value = 1, message = "Quantity must be at least 1") // Added validation
    @Column(nullable = false) // Added constraint
    private Integer quantity;

    @NotNull(message = "Price per item is required") // Added validation
    @DecimalMin(value = "0.0", message = "Price per item must be non-negative") // Added validation
    @Column(nullable = false, precision = 10, scale = 2) // Added constraints
    private BigDecimal pricePerItem; // Renamed from priceAtTime

    @Column(updatable = false)
    @CreationTimestamp // Use annotation
    private LocalDateTime createdAt;

    @UpdateTimestamp // Use annotation
    private LocalDateTime updatedAt;

    // Removed manual @PrePersist/@PreUpdate for timestamps
}