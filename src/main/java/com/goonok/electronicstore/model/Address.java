package com.goonok.electronicstore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty; // Added
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;   // Added
import lombok.Data;
import lombok.EqualsAndHashCode; // Added
import lombok.ToString;         // Added

@Data
@Entity
@Table(name = "addresses") // Explicit table name
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @ManyToOne(fetch = FetchType.LAZY) // Changed to LAZY
    @JoinColumn(name = "user_id", nullable = false) // Added nullable = false
    @ToString.Exclude         // Added
    @EqualsAndHashCode.Exclude // Added
    private User user;


    // *** ADDED Recipient Name ***
    @NotEmpty(message = "Recipient name is required")
    @Size(min = 3, max = 100, message = "Recipient name must be between 3 and 100 characters")
    @Column(nullable = false, length = 100)
    private String recipientName;
    // **************************

    // *** ADDED Recipient Phone ***
    @NotEmpty(message = "Recipient phone number is required")
    @Size(min = 11, max = 11, message = "Phone number must be 11 digits")
    @Pattern(regexp = "^(01[3-9]\\d{8})$", message = "Phone number must be a valid 11-digit Bangladeshi number starting with 01")
    @Column(nullable = false, length = 15) // Increased length slightly for potential formatting
    private String recipientPhone;
    // ***************************


    @NotEmpty(message = "Street address is required") // Added
    @Column(nullable = false)                        // Added
    private String street; // Renamed from addressLine1

    // Removed addressLine2 for simplicity, add back if needed

    @NotEmpty(message = "City is required")     // Added
    @Column(nullable = false, length = 50)      // Added
    private String city;

    // Removed state and country for simplicity, add back if needed
    @NotEmpty(message = "State is required")    // Added
    @Column(nullable = false, length = 15)      // Added
    private String state;

    private String country = "Bangladesh"; // Added;

    @NotEmpty(message = "Postal code is required") // Added
    @Column(nullable = false, length = 10)       // Added
    private String postalCode; // Renamed from zipCode


    @Column(nullable = false)
    private boolean isDefaultShipping = false; // Renamed and split from isDefault

    @Column(nullable = false)
    private boolean isDefaultBilling = false; // Added

}