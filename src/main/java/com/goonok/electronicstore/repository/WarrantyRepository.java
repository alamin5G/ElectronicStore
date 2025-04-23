package com.goonok.electronicstore.repository;

import com.goonok.electronicstore.model.Warranty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // Optional but good practice
public interface WarrantyRepository extends JpaRepository<Warranty, Long> {

    // Optional: Find by type to check for duplicates if needed
    Optional<Warranty> findByTypeIgnoreCase(String type);

}