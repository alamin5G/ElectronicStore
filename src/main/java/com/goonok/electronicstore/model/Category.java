package com.goonok.electronicstore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty; // Added
import lombok.Data;
import lombok.EqualsAndHashCode; // Added
import lombok.ToString;         // Added
import org.hibernate.annotations.CreationTimestamp; // Added
import org.hibernate.annotations.UpdateTimestamp;   // Added

import java.time.LocalDateTime; // Added
import java.util.HashSet;       // Added
import java.util.Set;

@Data
@Entity
@Table(name = "categories") // Added table name
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @NotEmpty(message = "Category name is required") // Added validation
    @Column(nullable = false, unique = true, length = 100) // Added length
    private String name;

    @Column(columnDefinition = "TEXT") // Added column definition
    private String description;

    // FetchType.LAZY is generally good default
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude         // Added
    @EqualsAndHashCode.Exclude // Added
    private Set<Product> products = new HashSet<>(); // Initialized Set

    @Column(updatable = false) // Added
    @CreationTimestamp         // Added
    private LocalDateTime createdAt;

    @UpdateTimestamp           // Added
    private LocalDateTime updatedAt;
}