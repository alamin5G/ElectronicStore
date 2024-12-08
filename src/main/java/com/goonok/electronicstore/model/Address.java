package com.goonok.electronicstore.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;  // Each address is associated with one user

    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private Boolean isDefault;  // If true, this is the default address for the user

}
