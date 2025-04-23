package com.goonok.electronicstore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;       // Added
import jakarta.validation.constraints.PositiveOrZero; // Added
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
@Table(name = "warranties") // Added table name
public class Warranty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long warrantyId;

    @NotEmpty(message = "Warranty type/name is required") // Added validation
    @Column(nullable = false, length = 100)              // Added constraint
    private String type; // Added: e.g., "Manufacturer", "Seller"

    @Column(columnDefinition = "TEXT") // Added column definition
    private String description;

    @PositiveOrZero(message = "Duration must be zero or positive") // Added validation
    @Column(nullable = false)                                      // Added constraint
    private Integer durationMonths;

    @Column(columnDefinition = "TEXT")
    private String terms; // Detailed warranty terms and conditions

    @OneToMany(mappedBy = "warranty", fetch = FetchType.LAZY) // Added fetch type
    @ToString.Exclude         // Added
    @EqualsAndHashCode.Exclude // Added
    private List<Product> products = new ArrayList<>(); // Initialized List

    @Column(updatable = false) // Use annotation
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp // Use annotation
    private LocalDateTime updatedAt;
}