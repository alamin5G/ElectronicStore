package com.goonok.electronicstore.model;

import com.goonok.electronicstore.util.DiscountType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*; // Added
import lombok.Data;
import lombok.EqualsAndHashCode; // Added
import lombok.ToString;         // Added
import org.hibernate.annotations.CreationTimestamp; // Added
import org.hibernate.annotations.UpdateTimestamp;   // Added

import java.math.BigDecimal; // Changed from Double
import java.time.LocalDateTime;
import java.util.ArrayList; // Added
import java.util.List;

@Data
@Entity
@Table(name = "discounts") // Added table name
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long discountId;

    @NotEmpty(message = "Discount code is required")          // Added validation
    @Column(nullable = false, unique = true, length = 50)     // Added constraints
    private String code; // Added: e.g., "SUMMER10"

    @Column(columnDefinition = "TEXT") // Added
    private String description; // Added

    @NotNull(message = "Discount type is required") // Added validation
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiscountType discountType; // Changed String to Enum

    @NotNull(message = "Discount value is required") // Added validation
    @DecimalMin(value = "0.0", message = "Discount value must be non-negative") // Added validation
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal value; // Changed Double to BigDecimal

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // Removed applicableOn field

    @Min(value = 0, message = "Usage limit cannot be negative") // Added
    private Integer usageLimit; // Added Optional: Max overall uses

    @Column(nullable = false) // Added
    private boolean isActive = true; // Added Flag

    // Kept relationships from your code
    @OneToMany(mappedBy = "discount", fetch = FetchType.LAZY) // Added fetch type
    @ToString.Exclude         // Added
    @EqualsAndHashCode.Exclude // Added
    private List<Order> orders = new ArrayList<>(); // Initialized List

    @OneToMany(mappedBy = "discount", fetch = FetchType.LAZY) // Added fetch type
    @ToString.Exclude         // Added
    @EqualsAndHashCode.Exclude // Added
    private List<Product> products = new ArrayList<>(); // Initialized List

    @Column(updatable = false)
    @CreationTimestamp // Use annotation
    private LocalDateTime createdAt;

    @UpdateTimestamp // Use annotation
    private LocalDateTime updatedAt;

}