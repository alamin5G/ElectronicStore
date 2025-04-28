package com.goonok.electronicstore.config;


import com.goonok.electronicstore.security.AuthenticatedUserRedirectFilter;
import com.goonok.electronicstore.security.CustomLogoutSuccessHandler;
import com.goonok.electronicstore.security.CustomAuthenticationSuccessHandler;
import com.goonok.electronicstore.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    private CustomAuthenticationSuccessHandler authenticationSuccessHandler;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return new CustomLogoutSuccessHandler();
    }

    String[] publicUrl = {
            "/", "/login", "/register", "/verify/**",
            "/specialAccessForAdmin", // Review if this should be public
            "/images/**", // Keep if you have other static images here
            "/css/**",
            "/js/**", // Add if you have custom JS files
            "/uploads/**", // Keep if used for other uploads

            // --- ADD THESE LINES ---
            "/product-images/**", // Allow access to product images
            "/brand-logos/**",    // Allow access to brand logos
            // ---------------------

            "/about", "/services", "/contact", "/latest-news",
            "/layout/**", // Be careful with layout, might expose too much? Usually CSS/JS is enough.
            "/products/**", // Public product browsing URLs
            // "/category/**", // Usually accessed via /products?categoryId=...
            // "/brand/**", // Usually accessed via /products?brandId=...
            "/search/**",
            // Public parts of cart/checkout/order/payment if applicable (e.g., viewing cart)
            "/cart/**", // Might need more specific rules later
            "/payment",  // Might need more specific rules later
            "/forgot-password", "/reset-password",
            // Review other paths like /checkout/**, /order/** - should they be fully public?
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(publicUrl).permitAll() // Publicly accessible URLs
                        .requestMatchers("/admin/**", "/admin/products/**", "/admin/warranties/**").hasRole("ADMIN") // Admin access only
                        .requestMatchers("/user/**", "/verify-warranty", "/cart/**", "/checkout/**", "/order/**", "/payment/**").hasRole("USER") // User access only
                        .anyRequest().authenticated() // All other URLs require authentication
                )
                .formLogin(form -> form
                        .loginPage("/login") // Custom login page
                        .loginProcessingUrl("/login") // URL for form submission
                        .usernameParameter("email") // Username parameter in the form
                        .failureUrl("/login?error=true") // Redirect to this URL on login failure
                        .successHandler(authenticationSuccessHandler) // Use the custom success handler
                        .permitAll() // Allow everyone to access the login page
                )
                .logout(logout -> logout
                        .logoutUrl("/logout") // URL to trigger logout
                        .logoutSuccessHandler(logoutSuccessHandler())
                        .permitAll()
                )
                .userDetailsService(customUserDetailsService);

        // Add the custom filter to handle authenticated users trying to access protected URLs
        http.addFilterBefore(
                new AuthenticatedUserRedirectFilter("/login", "/register", "/specialAccessForAdmin"),
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

}