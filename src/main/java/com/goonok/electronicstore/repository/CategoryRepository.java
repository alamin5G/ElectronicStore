package com.goonok.electronicstore.repository;

import com.goonok.electronicstore.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Find a category by name, ignoring case (useful for validation)
    Optional<Category> findByNameIgnoreCase(String name);

}
