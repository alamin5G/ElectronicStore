package com.goonok.electronicstore.initializer;

import com.goonok.electronicstore.model.Admin;
import com.goonok.electronicstore.model.User;
import com.goonok.electronicstore.model.Role;
import com.goonok.electronicstore.repository.AdminRepository;
import com.goonok.electronicstore.repository.UserRepository;
import com.goonok.electronicstore.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements ApplicationRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private AdminRepository adminRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Check if there are any admin users
        if (userRepository.count() == 0) {
            // If no users exist, create the first admin
            Role adminRole = roleRepository.findByRoleName("ADMIN");
            Role userRole = roleRepository.findByRoleName("USER");
            if (adminRole == null || userRole == null) {
                // Ensure the ADMIN role exists
                adminRole = new Role();
                adminRole.setRoleName("ADMIN");
                userRole = new Role();
                userRole.setRoleName("USER");
                roleRepository.save(userRole);
                roleRepository.save(adminRole);
            }

            // Create the first admin user
            User admin = new User();
            admin.setName("admin");
            admin.setEmail("admin@store.com");
            admin.setPassword("admin123");  // The password will be encoded automatically
            admin.getRoles().add(adminRole);
            admin.setCreatedAt(java.time.LocalDateTime.now());
            admin.setUpdatedAt(java.time.LocalDateTime.now());
            admin.setEnabled(true);
            admin.setVerified(true);
            admin.setPhoneNumber("01334567890");

            // Save the user first
            User savedUser = userRepository.save(admin);

            // Now create the Admin entity and associate it with the saved user and the role
            Admin myAdmin = new Admin();
            myAdmin.setUser(savedUser);  // Link the user
            myAdmin.setRole(adminRole);  // Assign the role
            myAdmin.setCreatedAt(java.time.LocalDateTime.now());
            myAdmin.setUpdatedAt(java.time.LocalDateTime.now());

            // Save the Admin entity
            adminRepository.save(myAdmin);

            System.out.println("First Admin user created with username: admin@store.com");
        }
    }
}
