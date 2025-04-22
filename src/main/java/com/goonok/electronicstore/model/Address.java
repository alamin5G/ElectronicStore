package com.goonok.electronicstore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty; // Added
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

    @NotEmpty(message = "Street address is required") // Added
    @Column(nullable = false)                        // Added
    private String street; // Renamed from addressLine1

    // Removed addressLine2 for simplicity, add back if needed

    @NotEmpty(message = "City is required")     // Added
    @Column(nullable = false, length = 50)      // Added
    private String city;

    // Removed state and country for simplicity, add back if needed

    @NotEmpty(message = "Postal code is required") // Added
    @Column(nullable = false, length = 10)       // Added
    private String postalCode; // Renamed from zipCode


    @Column(nullable = false)
    private boolean isDefaultShipping = false; // Renamed and split from isDefault

    @Column(nullable = false)
    private boolean isDefaultBilling = false; // Added

}