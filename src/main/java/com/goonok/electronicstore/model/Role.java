package com.goonok.electronicstore.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode; // Added
import lombok.ToString;         // Added

import java.util.HashSet; // Changed from ArrayList
import java.util.Set;     // Changed from List

@Data
@Entity
@Table(name = "roles") // Explicit table name
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleId;

    @Column(nullable = false, unique = true, length = 50) // Added constraints
    private String roleName;

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY) // Set fetch to LAZY
    @ToString.Exclude         // Added
    @EqualsAndHashCode.Exclude // Added
    private Set<User> users = new HashSet<>(); // Changed to Set

}