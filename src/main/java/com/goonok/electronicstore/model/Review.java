package com.goonok.electronicstore.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer rating;
    private String reviewText;
    private Boolean isReported;
    private String responseText;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "admin_id")  // New relationship with Admin entity
    private Admin admin;  // Each product is associated with one admin


    // Getters and setters
}
