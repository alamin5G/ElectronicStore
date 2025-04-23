package com.goonok.electronicstore.dto; // Assuming a dto package

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDto {

    private Long productId;

    @NotEmpty(message = "Product name cannot be empty")
    @Size(min = 3, max = 255, message = "Product name must be between 3 and 255 characters")
    private String name;

    @NotEmpty(message = "Product description cannot be empty")
    @Size(max = 10000, message = "Description is too long")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @Min(value = 0, message = "Minimum stock level cannot be negative")
    private Integer minimumStockLevel;

    @NotNull(message = "Category is required")
    private Long categoryId;
    private String categoryName; // <-- ADDED

    @NotNull(message = "Brand is required")
    private Long brandId;
    private String brandName; // <-- ADDED

    private Long warrantyId;
    // Optionally add warranty details like type/duration if needed in list view
    // private String warrantyType;
    // private Integer warrantyDurationMonths;

    private String specifications;

    @Size(max = 512, message = "Image path is too long")
    private String imagePath;

    private boolean isFeatured = false;

    private boolean isNewArrival = false;

    // Derived fields from entity (read-only in DTO context)
    private boolean inStock;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

}
