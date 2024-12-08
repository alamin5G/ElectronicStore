package com.goonok.electronicstore.repository;

import com.goonok.electronicstore.model.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    Brand findBrandsByName(String name);
    Brand findByBrandId(Long brandId);

    Brand findBrandsByNameContainingIgnoreCase(String name);
    Brand findBrandsByDescription(String brandDescription);
}
