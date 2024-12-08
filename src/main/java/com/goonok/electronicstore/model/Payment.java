package com.goonok.electronicstore.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;  // Correct relationship mapping with Order entity

    private String paymentMethod;
    private String paymentStatus;
    private LocalDateTime paymentDate;
    private Double paymentAmount;
    private String transactionId;
    private String paymentGatewayResponseCode;
    private String paymentGatewayMessage;

    // Getters and setters
}

