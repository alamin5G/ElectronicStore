package com.goonok.electronicstore.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 10000)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;
    private Integer stockQuantity;
    private Integer minimumStockLevel;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne
    @JoinColumn(name = "warranty_id")
    private Warranty warranty;

    //private String imageUrl;
    private String imagePath;
    @Column(nullable = false)
    private boolean isFeatured = false; // Default to false
    private boolean isAvailable = true; // Default to true
    private boolean isNewArrival = true; // Default to false

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "product")
    private List<Review> reviews;

    @OneToMany(mappedBy = "product")
    private List<OrderItem> orderItems;

    @OneToMany(mappedBy = "product")
    private List<ProductStockTransaction> productStockTransactions;


    @ManyToOne
    @JoinColumn(name = "admin_id")  // New relationship with Admin entity
    private Admin admin;  // Each product is associated with one admin

    @ManyToOne
    @JoinColumn(name = "discount_id")
    private Discount discount;  // New relationship with Discount entity
    // Getters and setters

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}


