package com.goonok.electronicstore.model; // Assuming this package is correct

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode; // Recommended with Lombok @Data and relationships
import lombok.ToString; // Recommended with Lombok @Data and relationships
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet; // Using Set for roles
import java.util.List;
import java.util.Set; // Using Set for roles

@Data
@Entity
@Table(name = "users") // Explicitly naming the table
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @NotEmpty(message = "Full Name is required")
    @Size(min = 3, max = 50, message = "Full Name must be between 3 and 50 characters")
    @Column(nullable = false, length = 50) // Added DB constraint consistency
    private String name; // Changed 'name' to 'fullName' to match requirements [cite: 2]

    @Email(message = "Invalid email format")
    @NotEmpty(message = "Email is required")
    @Column(nullable = false, unique = true) // Added DB constraint consistency
    private String email;

    @NotEmpty(message = "Password is required")
    // Password strength validation is usually handled in DTO/Service, not here.
    @Column(nullable = false) // Added DB constraint consistency
    private String password; // Remember to store the HASHED password [cite: 50]

    // Phone number is optional per requirements[cite: 2], but validated if present.
    // Made column nullable.
    @Size(min = 11, max = 11, message = "Phone number must be 11 digits if provided")
    @Pattern(regexp = "^(01[3-9]\\d{8})$", message = "Phone number must be a valid Bangladeshi number starting with 01")
    @Column(length = 11) // Optional field in DB
    private String phoneNumber;

    @Column(updatable = false)
    @CreationTimestamp // Automatically set on creation
    private LocalDateTime createdAt;

    @UpdateTimestamp // Automatically set on update/modification
    private LocalDateTime updatedAt;

    // --- Relationships ---

    // Address relationship - looks good. Lazy fetch is suitable.
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude // Avoid infinite loops in toString
    @EqualsAndHashCode.Exclude // Avoid infinite loops in equals/hashCode
    private List<Address> addresses = new ArrayList<>();

    // Order relationship - Lazy fetch is good. Cascade type depends on business rules
    // (e.g., deleting user shouldn't delete orders usually).
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Order> orders = new ArrayList<>();

    // Review relationship - Added based on your code. Lazy fetch is good.
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Review> reviews = new ArrayList<>();

    // ShoppingCart relationship - Added based on your code. Cascade ALL makes sense here.
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ShoppingCartItem> shoppingCartItems = new ArrayList<>();

    // Role relationship - Changed to Set for uniqueness. EAGER fetch often okay for roles.
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Role> roles = new HashSet<>(); // Using Set for uniqueness

    // --- Flags --- Added based on your code, good for activation flows

    @Column(nullable = false)
    private boolean isEnabled = false;  // Default is false until enabled (e.g., after verification)

    @Column(nullable = false)
    private boolean isVerified = false; // Default is false until email verification

    // --- Helper methods for relationships (optional but good practice) ---
    public void addRole(Role role) {
        this.roles.add(role);
        role.getUsers().add(this); // Assuming Role entity has a Set<User> users field
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
        role.getUsers().remove(this); // Assuming Role entity has a Set<User> users field
    }

    public void addAddress(Address address) {
        this.addresses.add(address);
        address.setUser(this);
    }

    public void removeAddress(Address address) {
        this.addresses.remove(address);
        address.setUser(null);
    }

    // Add similar helpers for Order, Review, ShoppingCart if needed
}