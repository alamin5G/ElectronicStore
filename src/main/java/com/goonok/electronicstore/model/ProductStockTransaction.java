package com.goonok.electronicstore.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class ProductStockTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private String transactionType; // Sale, Return, Restock
    private Integer quantityChange;
    private LocalDateTime transactionDate;

    // Getters and setters
}

