package com.goonok.electronicstore.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressDto {

    private Long addressId; // Needed for updates/deletes

    @NotEmpty(message = "Recipient name is required")
    @Size(min = 3, max = 100, message = "Recipient name must be between 3 and 100 characters")
    private String recipientName;

    @NotEmpty(message = "Recipient phone number is required")
    @Size(min = 11, max = 11, message = "Phone number must be 11 digits")
    @Pattern(regexp = "^(01[3-9]\\d{8})$", message = "Phone number must be a valid 11-digit Bangladeshi number starting with 01")
    private String recipientPhone;


    @NotEmpty(message = "Street address is required")
    @Size(max = 255, message = "Street address cannot exceed 255 characters")
    private String street;

    @NotEmpty(message = "City is required")
    @Size(max = 50, message = "City name cannot exceed 50 characters")
    private String city;

    // *** ADDED State Field ***
    @NotEmpty(message = "State/Division is required")
    @Size(max = 50, message = "State/Division name cannot exceed 50 characters")
    private String state;
    // ***********************

    // *** ADDED Country Field ***
    @NotEmpty(message = "Country is required")
    @Size(max = 50, message = "Country name cannot exceed 50 characters")
    private String country = "Bangladesh";

    @NotEmpty(message = "Postal code is required")
    @Size(max = 10, message = "Postal code cannot exceed 10 characters")
    private String postalCode;

    // Add other fields like state, country if you included them in the Address entity

    private boolean isDefaultShipping = false;
    private boolean isDefaultBilling = false;

}
