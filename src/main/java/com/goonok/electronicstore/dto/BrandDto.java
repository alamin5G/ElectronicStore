package com.goonok.electronicstore.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BrandDto {

    private Long brandId; // Needed for updates

    @NotEmpty(message = "Brand name is required")
    @Size(min = 2, max = 100, message = "Brand name must be between 2 and 100 characters")
    private String name;

    @Size(max = 10000, message = "Description is too long") // Allow long descriptions
    private String description;

    // We'll handle the logo via MultipartFile in the controller/service,
    // but store the resulting path/filename here.
    @Size(max = 512, message = "Logo URL/path is too long")
    private String logoUrl; // Changed from logoUrl to reflect it might be a path or URL

}
