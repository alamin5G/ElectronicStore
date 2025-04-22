package com.goonok.electronicstore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty; // Added
import lombok.Data;
import lombok.EqualsAndHashCode; // Added
import lombok.ToString;         // Added
import org.hibernate.annotations.CreationTimestamp; // Added
import org.hibernate.annotations.UpdateTimestamp;   // Added

import java.time.LocalDateTime;
import java.util.HashSet;       // Added
// Removed unused List import
import java.util.Set;

@Data
@Entity
@Table(name = "brands") // Added table name
public class Brand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long brandId;

    @NotEmpty(message = "Brand name is required") // Added validation
    @Column(nullable = false, unique = true, length = 100) // Added length
    private String name;

    @Column(columnDefinition = "TEXT") // Added column definition
    private String description;

    @Column(length = 512) // Added: URL for brand logo
    private String logoUrl;

    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude         // Added
    @EqualsAndHashCode.Exclude // Added
    private Set<Product> products = new HashSet<>(); // Initialized Set

    @Column(updatable = false) // Added
    @CreationTimestamp         // Added
    private LocalDateTime createdAt;

    @UpdateTimestamp           // Added
    private LocalDateTime updatedAt;
}