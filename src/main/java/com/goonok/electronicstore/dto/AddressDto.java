package com.goonok.electronicstore.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressDto {

    private Long addressId; // Needed for updates/deletes

    // userId is usually inferred from the logged-in user, not directly in the DTO for add/update
    // private Long userId;

    @NotEmpty(message = "Street address is required")
    @Size(max = 255, message = "Street address cannot exceed 255 characters")
    private String street;

    @NotEmpty(message = "City is required")
    @Size(max = 50, message = "City name cannot exceed 50 characters")
    private String city;

    @NotEmpty(message = "Postal code is required")
    @Size(max = 10, message = "Postal code cannot exceed 10 characters")
    private String postalCode;

    // Add other fields like state, country if you included them in the Address entity

    private boolean isDefaultShipping = false;
    private boolean isDefaultBilling = false;

}
