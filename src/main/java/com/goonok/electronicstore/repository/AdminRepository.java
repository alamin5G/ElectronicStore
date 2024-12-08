package com.goonok.electronicstore.repository;

import com.goonok.electronicstore.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AdminRepository extends JpaRepository<Admin, Long> {

    Admin findByAdminId(Long adminId);


    void deleteByAdminId(Long adminId);
}
