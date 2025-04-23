package com.goonok.electronicstore.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WarrantyDto {

    private Long warrantyId; // For updates

    @NotEmpty(message = "Warranty type is required")
    @Size(min = 3, max = 100, message = "Warranty type must be between 3 and 100 characters")
    private String type; // e.g., "Manufacturer", "Seller", "Extended"

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description; // Optional general description

    @NotNull(message = "Duration in months is required")
    @PositiveOrZero(message = "Duration must be zero or positive")
    private Integer durationMonths;

    @Size(max = 10000, message = "Terms description is too long") // Allow long terms
    private String terms; // Detailed warranty terms

    // Timestamps (createdAt, updatedAt) are handled by the backend.
}
