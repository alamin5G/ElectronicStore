package com.goonok.electronicstore.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * Represents a single item within the shopping cart.
 */
@Data
public class CartItemDto {

    private Long cartItemId; // ID of the ShoppingCartItem entity
    private Long productId;
    private String productName;
    private String productImagePath; // To display the image
    private int quantity;
    private BigDecimal pricePerItem; // Price of the product when added/in cart
    private BigDecimal itemTotalPrice; // Calculated: quantity * pricePerItem
    private Integer productStockQuantity;

    // Constructor, Getters, Setters provided by Lombok @Data
}