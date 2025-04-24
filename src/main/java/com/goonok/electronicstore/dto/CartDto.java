package com.goonok.electronicstore.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class CartDto {
    private Long userId; // ID of the user owning the cart
    private List<CartItemDto> items = new ArrayList<>();
    private BigDecimal cartTotalPrice; // Calculated total for all items in the cart

}
