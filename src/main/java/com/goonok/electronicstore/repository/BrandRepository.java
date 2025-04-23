package com.goonok.electronicstore.repository;

import com.goonok.electronicstore.model.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    Brand findBrandsByName(String name);
    Brand findByBrandId(Long brandId);

    Brand findBrandsByNameContainingIgnoreCase(String name);
    Brand findBrandsByDescription(String brandDescription);

    // Optional: Add custom query methods if needed later, e.g., find by name
    Optional<Brand> findByNameIgnoreCase(String name);
}
