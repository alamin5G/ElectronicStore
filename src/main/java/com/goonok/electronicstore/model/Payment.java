package com.goonok.electronicstore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;  // Added
import jakarta.validation.constraints.Size;    // Added
import lombok.Data;
import lombok.EqualsAndHashCode; // Added
import lombok.ToString;         // Added
import org.hibernate.annotations.CreationTimestamp; // Added
import org.hibernate.annotations.UpdateTimestamp;   // Added

import java.math.BigDecimal; // Changed from Double
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "payments") // Added table name
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @OneToOne(fetch = FetchType.LAZY) // Added fetch type
    @JoinColumn(name = "order_id", nullable = false, unique = true) // Added nullable=false, unique=true
    @ToString.Exclude         // Added
    @EqualsAndHashCode.Exclude // Added
    private Order order;

    @NotNull(message = "Payment method is required") // Added validation
    @Column(nullable = false, length = 50)
    private String paymentMethod; // e.g., "COD", "BKASH", "NAGAD", "CARD_GATEWAY"

    @NotNull(message = "Payment status is required") // Added validation
    @Column(nullable = false, length = 50)
    private String paymentStatus; // e.g., "PENDING", "AWAITING_VERIFICATION", "COMPLETED", "FAILED", "REFUNDED"

    @Column(nullable = true) // Payment might be completed later than creation
    private LocalDateTime paymentDate; // When the payment was actually confirmed/completed

    @NotNull(message = "Payment amount is required") // Added validation
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal paymentAmount; // Changed from Double to BigDecimal for accuracy

    // --- Fields relevant for Manual/Mobile Payments ---
    @Size(max = 100, message = "Reference ID cannot exceed 100 characters") // Added validation
    @Column(length = 100)
    private String manualPaymentReference; // Field to store bKash/Nagad TrxID provided by user

    @Column(columnDefinition = "TEXT")
    private String adminNotes; // Optional notes by admin during verification

    // --- Fields relevant for Automated Gateways (nullable for manual) ---
    @Column(length = 100) // Made nullable
    private String transactionId; // Gateway's transaction ID

    @Column(length = 50) // Made nullable
    private String paymentGatewayResponseCode;

    @Column(columnDefinition = "TEXT") // Made nullable
    private String paymentGatewayMessage;

    // --- Timestamps ---
    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt; // When the payment record was created

    @UpdateTimestamp
    private LocalDateTime updatedAt; // When the payment record was last updated
}