package com.goonok.electronicstore.config;


import com.goonok.electronicstore.model.Role;
import com.goonok.electronicstore.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class InitialDataLoader {

    @Autowired
    private RoleRepository roleRepository;

    @Bean
    public CommandLineRunner loadInitialRoles() {
        return args -> {
            if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
                Role admin = new Role();
                admin.setName("ROLE_ADMIN");
                log.info("Role admin created successfully");
                roleRepository.save(admin);

            }

            if (roleRepository.findByName("ROLE_USER").isEmpty()) {
                Role user = new Role();
                user.setName("ROLE_USER");
                log.info("Role user created successfully");
                roleRepository.save(user);

            }

            log.info("Roles loaded successfully");

        };
    }
}
