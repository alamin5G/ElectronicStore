package com.goonok.electronicstore.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "admin")
    private List<Product> products;

    @OneToMany(mappedBy = "admin")
    private List<Order> orders;

    @OneToMany(mappedBy = "admin")
    private List<Review> reviews;

    // Getters and setters
}

