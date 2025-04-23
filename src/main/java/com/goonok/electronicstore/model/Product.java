package com.goonok.electronicstore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin; // Added
import jakarta.validation.constraints.Min;       // Added
import jakarta.validation.constraints.NotEmpty;  // Added
import jakarta.validation.constraints.NotNull;   // Added
import lombok.Data;
import lombok.EqualsAndHashCode; // Added
import lombok.ToString;         // Added
import org.hibernate.annotations.CreationTimestamp; // Added
import org.hibernate.annotations.UpdateTimestamp;   // Added

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList; // Added
import java.util.List;

@Data
@Entity
@Table(name = "products") // Added table name
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @NotEmpty(message = "Product name is required") // Added validation
    @Column(nullable = false)
    private String name;

    @NotEmpty(message = "Product description is required") // Added validation
    @Column(nullable = false, length = 10000)
    private String description;

    @Column(columnDefinition = "TEXT") // Use TEXT for potentially long specifications
    private String specifications;

    @NotNull(message = "Price is required") // Added validation
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0") // Added validation
    @Column(nullable = false, precision = 10, scale = 2) // Added precision/scale
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required") // Added validation
    @Min(value = 0, message = "Stock quantity cannot be negative") // Added validation
    @Column(nullable = false) // Added constraint
    private Integer stockQuantity;

    @Min(value = 0, message = "Minimum stock level cannot be negative") // Added validation
    private Integer minimumStockLevel; // Kept from your code

    @ManyToOne(fetch = FetchType.LAZY) // Added fetch type
    @JoinColumn(name = "category_id")
    @ToString.Exclude         // Added
    @EqualsAndHashCode.Exclude // Added
    private Category category; // Kept from your code (Ensure Category entity exists)

    @ManyToOne(fetch = FetchType.LAZY) // Added fetch type
    @JoinColumn(name = "brand_id")
    @ToString.Exclude         // Added
    @EqualsAndHashCode.Exclude // Added
    private Brand brand; // Kept from your code (Ensure Brand entity exists)

    @ManyToOne(fetch = FetchType.LAZY) // Added fetch type
    @JoinColumn(name = "warranty_id")
    @ToString.Exclude         // Added
    @EqualsAndHashCode.Exclude // Added
    private Warranty warranty; // Kept from your code (Ensure Warranty entity exists)

    @Column(length = 512) // Added length constraint for path
    private String imagePath; // Kept from your code (Stores path to image file)

    @Column(nullable = false)
    private boolean isFeatured = false;

    @Column(nullable = false)
    private boolean inStock = true; // Renamed from isAvailable

    @Column(nullable = false)
    private boolean isNewArrival = true; // Kept from your code

    @Column(updatable = false)
    @CreationTimestamp // Use annotation
    private LocalDateTime createdAt;

    @UpdateTimestamp // Use annotation
    private LocalDateTime updatedAt;

    // --- Relationships ---
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY) // Added details
    @ToString.Exclude         // Added
    @EqualsAndHashCode.Exclude // Added
    private List<Review> reviews = new ArrayList<>(); // Initialize

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY) // Set fetch type
    @ToString.Exclude         // Added
    @EqualsAndHashCode.Exclude // Added
    private List<OrderItem> orderItems = new ArrayList<>(); // Initialize

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY) // Added cascade, fetch
    @ToString.Exclude         // Added
    @EqualsAndHashCode.Exclude // Added
    private List<ProductStockTransaction> productStockTransactions = new ArrayList<>(); // Kept, Initialize

    // Consider if direct links to Admin/Discount are needed vs service logic
    @ManyToOne(fetch = FetchType.LAZY) // Added fetch type
    @JoinColumn(name = "admin_id")
    @ToString.Exclude         // Added
    @EqualsAndHashCode.Exclude // Added
    private Admin admin;  // Kept from your code (Ensure Admin entity exists)

    @ManyToOne(fetch = FetchType.LAZY) // Added fetch type
    @JoinColumn(name = "discount_id")
    @ToString.Exclude         // Added
    @EqualsAndHashCode.Exclude // Added
    private Discount discount;  // Kept from your code (Ensure Discount entity exists)

    // Lifecycle callback to keep inStock flag consistent
    @PrePersist
    @PreUpdate
    private void updateStockStatus() {
        this.inStock = this.stockQuantity != null && this.stockQuantity > 0;
    }

    // Removed manual @PrePersist/@PreUpdate for createdAt/updatedAt
}