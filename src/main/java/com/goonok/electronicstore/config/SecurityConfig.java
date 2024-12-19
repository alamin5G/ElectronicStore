package com.goonok.electronicstore.config;


import com.goonok.electronicstore.security.CustomLogoutSuccessHandler;
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

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return new CustomLogoutSuccessHandler();
    }

    String[] publicUrl = { "/", "/login", "/register", "/verify/**", "/specialAccessForAdmin", "/images/**", "/product/**", "/css/**", "/read-news",
            "/uploads/**",
            "/about", "/services", "/contact", "/latest-news" , "/layout/**", "/products/**", "/category/**", "/brand/**", "/search/**", "/cart/**",
            "/checkout/**", "/order/**", "/payment" };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(publicUrl).permitAll() // Publicly accessible URLs
                        .requestMatchers("/admin/**", "/customers/**", "/jewellers/**", "/items/**").hasRole("ADMIN") // Admin access only
                        .requestMatchers("/user/**").hasRole("USER") // User access only
                        .anyRequest().authenticated() // All other URLs require authentication
                )
                .formLogin(form -> form
                        .loginPage("/login") // Custom login page
                        .loginProcessingUrl("/login") // URL for form submission
                        .usernameParameter("email") // Username parameter in the form
                        .failureUrl("/login?error=true") // Redirect to this URL on login failure
                        .defaultSuccessUrl("/login/success", true) // Redirect after successful login
                        .permitAll() // Allow everyone to access the login page
                )
                .logout(logout -> logout
                        .logoutUrl("/logout") // URL to trigger logout
                        .logoutSuccessHandler(logoutSuccessHandler())
                        .permitAll()
                )
                .userDetailsService(customUserDetailsService);

        // Custom filter to redirect authenticated users from /login, /register, /specialAccessForAdmin
        http.addFilterBefore(new RedirectAuthenticatedUserFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Custom filter to redirect authenticated users
    public static class RedirectAuthenticatedUserFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {

            String[] protectedUrls = {"/login", "/register", "/specialAccessForAdmin"};
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
                for (String url : protectedUrls) {
                    if (request.getRequestURI().equals(url)) {
                        if (auth.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))) {
                            response.sendRedirect("/admin/dashboard");
                        } else if (auth.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USER"))) {
                            response.sendRedirect("/user/profile");
                        }
                        return;
                    }
                }
            }

            filterChain.doFilter(request, response);
        }
    }
}
