package com.goonok.electronicstore.service;

import com.goonok.electronicstore.model.User;
import com.goonok.electronicstore.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@Transactional
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);
    @Autowired
    private UserRepository userRepository;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));

/* this line of code is working.. so, don't delete this

        System.out.println("========================================");
        System.out.println("User found: " + user.getUsername());
        System.out.println("Password: " + user.getPassword());
        System.out.println("========================================");


        System.out.println("User roles: Before Listing the roles: " + user.getRoles());
        // Convert roles to GrantedAuthority
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());

        System.out.println("User roles after listing the role: " + user.getRoles());
        System.out.println("Authorities: " + authorities);

        authorities.forEach(auth -> log.info("Assigned Authority: " + auth.getAuthority()));

        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);

*/

        // Explicitly initialize the roles and users collection if needed (if you use @Transactional it doesn't need)
        //user.getRoles().forEach(role -> Hibernate.initialize(role.getUsers()));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                true, true, true, //this line of true is required if you want to verify the user and then access to login
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                        .collect(Collectors.toList())
        );
    }

}
