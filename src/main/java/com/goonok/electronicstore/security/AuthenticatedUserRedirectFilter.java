package com.goonok.electronicstore.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AuthenticatedUserRedirectFilter extends OncePerRequestFilter {

    private final List<String> protectedUrls;

    public AuthenticatedUserRedirectFilter(String... protectedUrls) {
        this.protectedUrls = Arrays.asList(protectedUrls);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String requestUrl = request.getRequestURI();

        // If the URL is one we want to check
        if (protectedUrls.contains(requestUrl)) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            // If user is authenticated (and not anonymous)
            if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
                if (auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))) {
                    response.sendRedirect("/admin/dashboard");
                    return;
                } else if (auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USER"))) {
                    response.sendRedirect("/user/profile");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}