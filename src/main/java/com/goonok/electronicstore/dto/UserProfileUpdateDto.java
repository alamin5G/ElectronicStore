package com.goonok.electronicstore.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileUpdateDto {

    private Long id; // Changed from long to Long, added based on user's example

    @NotEmpty(message = "Name is required")
    // Updated Size constraint based on user's example
    @Size(min = 3, max = 50, message = "Full Name must be between 3 and 50 characters")
    private String name;


    // Renamed field to 'phone' and updated constraints based on user's example
    @NotEmpty(message = "phone is required")
    @Size(min = 11, max = 11, message = "Phone number can not be less or more than 11 digits")
    @Pattern(regexp = "^(01[3-9]\\d{8})$", message = "Phone number must be a valid 11-digit Bangladeshi number starting with 01") // Using BD specific pattern
    private String phone; // Renamed from phoneNumber

    // Password updates should be handled via a separate DTO and endpoint
    // for security (requiring old password confirmation).
}
