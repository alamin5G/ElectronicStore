package com.goonok.electronicstore.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ManyToAny;

import java.time.LocalDateTime;

@Data
@Entity

public class ShoppingCart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartId;

    @ManyToOne // Change from @OneToOne to @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) // Allow multiple cart items for the same user
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer quantity;


    private LocalDateTime cartExpiryTimestamp;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        cartExpiryTimestamp = LocalDateTime.now().plusDays(30); // Default to 1 day
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
