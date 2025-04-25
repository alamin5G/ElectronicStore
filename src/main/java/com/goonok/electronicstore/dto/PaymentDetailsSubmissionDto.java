package com.goonok.electronicstore.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PaymentDetailsSubmissionDto {
    @NotEmpty(message = "Transaction ID is required")
    @Size(min = 5, max = 50, message = "Transaction ID must be between 5 and 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Transaction ID must contain only letters and numbers")
    private String transactionId;

    @NotEmpty(message = "Payment method is required")
    private String paymentMethod; // "BKASH" or "NAGAD"

    private String notes;
}