package com.goonok.electronicstore.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long discountId;

    private String discountType;
    private Double discountValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String applicableOn;

    @OneToMany(mappedBy = "discount")
    private List<Order> orders;

    @OneToMany(mappedBy = "discount")
    private List<Product> products;

    // Getters and setters
}

