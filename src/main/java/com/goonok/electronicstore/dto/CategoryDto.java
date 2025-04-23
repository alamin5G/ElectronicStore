package com.goonok.electronicstore.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryDto {

    private Long categoryId; // For updates

    @NotEmpty(message = "Category name is required")
    @Size(min = 3, max = 100, message = "Category name must be between 3 and 100 characters")
    private String name;

    @Size(max = 10000, message = "Description is too long")
    private String description;

    // Timestamps (createdAt, updatedAt) are usually not included in DTOs for create/update
    // They are handled by the backend automatically.
}
