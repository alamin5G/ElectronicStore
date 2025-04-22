package com.goonok.electronicstore.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode; // Added
import lombok.ToString;         // Added
import org.hibernate.annotations.CreationTimestamp; // Added
import org.hibernate.annotations.UpdateTimestamp;   // Added

import java.time.LocalDateTime;
import java.util.ArrayList; // Added
import java.util.List;

@Data
@Entity
@Table(name = "admins") // Added table name
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminId;

    // Link to the primary User account for this admin
    @OneToOne(fetch = FetchType.LAZY) // Changed to OneToOne assuming one Admin profile per User
    @JoinColumn(name = "user_id", unique = true) // Should likely be unique
    @ToString.Exclude         // Added
    @EqualsAndHashCode.Exclude // Added
    private User user; // Link to User entity

    // This seems redundant if User already has Roles. Consider removing.
    @ManyToOne(fetch = FetchType.LAZY) // Added fetch type
    @JoinColumn(name = "role_id") // Assuming this references the ADMIN role ID
    @ToString.Exclude         // Added
    @EqualsAndHashCode.Exclude // Added
    private Role role;

    // Add any *admin-specific* profile fields here if needed (e.g., department, internal ID)

    @Column(updatable = false)
    @CreationTimestamp // Use annotation
    private LocalDateTime createdAt;

    @UpdateTimestamp // Use annotation
    private LocalDateTime updatedAt;

    // --- Relationships indicating management/ownership ---
    @OneToMany(mappedBy = "admin", fetch = FetchType.LAZY) // Added fetch type
    @ToString.Exclude         // Added
    @EqualsAndHashCode.Exclude // Added
    private List<Product> products = new ArrayList<>(); // Initialized List

    @OneToMany(mappedBy = "admin", fetch = FetchType.LAZY) // Added fetch type
    @ToString.Exclude         // Added
    @EqualsAndHashCode.Exclude // Added
    private List<Order> orders = new ArrayList<>(); // Initialized List

    @OneToMany(mappedBy = "admin", fetch = FetchType.LAZY) // Added fetch type
    @ToString.Exclude         // Added
    @EqualsAndHashCode.Exclude // Added
    private List<Review> reviews = new ArrayList<>(); // Initialized List
}