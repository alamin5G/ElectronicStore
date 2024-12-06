package com.goonok.electronicstore.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class SecurityService {

    private static final Logger log = LoggerFactory.getLogger(SecurityService.class);


    public String findLoggedInUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }


    public Collection<? extends GrantedAuthority> findLoggedInUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Collections.emptyList();
        }
        return authentication.getAuthorities();
    }


    public String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            log.info("Authentication found: {}", authentication.getName());
            if (authentication.isAuthenticated()) {
                log.info("User is authenticated: {}", authentication.getName());
                Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            } else {
                log.warn("User is not authenticated");
            }
        } else {
            log.warn("No authentication found in security context");
        }
        return null; // If no role found
    }

}

