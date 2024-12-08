package com.goonok.electronicstore.repository;

import com.goonok.electronicstore.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
