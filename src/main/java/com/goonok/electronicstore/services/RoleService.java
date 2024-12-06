package com.goonok.electronicstore.services;


import com.goonok.electronicstore.model.Role;
import com.goonok.electronicstore.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    // Get all roles
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    // Save a new role
    public void saveRole(Role role) {
        roleRepository.save(role);
    }

    // Get role by ID
    public Role getRoleById(int id) {
        return roleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid role ID: " + id));
    }

    // Update an existing role
    public void updateRole(int id, Role roleDetails) {
        Role role = getRoleById(id);
        role.setName(roleDetails.getName());
        roleRepository.save(role);
    }

    // Delete a role
    public void deleteRole(int id) {
        roleRepository.deleteById(id);
    }
}
