package com.goonok.electronicstore.repository;



import com.goonok.electronicstore.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    // You can add custom queries here if needed
}

