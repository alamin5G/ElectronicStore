package com.goonok.electronicstore.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * DTO representing a single item within a placed order.
 */
@Data
public class OrderItemDto {

    private Long orderItemId;
    private Long productId;
    private String productName; // Denormalized for easy display
    private String productImagePath; // Denormalized
    private int quantity;
    private BigDecimal pricePerItem; // Price at the time of order
    private BigDecimal itemTotalPrice; // quantity * pricePerItem
}
