package com.goonok.electronicstore.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable; // Implement Serializable for session storage
import java.math.BigDecimal;

/**
 * DTO to hold state during the multi-step checkout process.
 * Typically stored in the HTTP session.
 */
@Data
public class CheckoutDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L; // Good practice for Serializable

    private AddressDto selectedShippingAddress;
    private String selectedPaymentMethod; // e.g., "COD", "BKASH", "NAGAD"

    // You might add other fields later, like selected shipping method if applicable
    private String selectedShippingMethod;
    private BigDecimal shippingCost;

    // Store cart details at the start of checkout to prevent changes during the process? (Optional)
    private CartDto checkoutCart;
}
