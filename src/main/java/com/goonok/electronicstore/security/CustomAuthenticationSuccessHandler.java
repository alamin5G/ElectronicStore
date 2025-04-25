package com.goonok.electronicstore.security;

import com.goonok.electronicstore.model.User;
import com.goonok.electronicstore.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final UserRepository userRepository;

    public CustomAuthenticationSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
        // Exclude these URLs from the saved request redirect
        setAlwaysUseDefaultTargetUrl(false);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            user.setLastLoginDate(LocalDateTime.now());
            Integer currentCount = user.getLoginCount();
            user.setLoginCount(currentCount == null ? 1 : currentCount + 1);
            user.setLastLoginIp(request.getRemoteAddr());
            userRepository.save(user);
        }

        // Check if the request was for a protected URL that we want to override
        String requestUrl = request.getRequestURI();
        if (requestUrl.equals("/login") || requestUrl.equals("/register") || 
            requestUrl.equals("/specialAccessForAdmin")) {
            
            // Redirect based on role
            if (authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                response.sendRedirect("/admin/dashboard");
            } else {
                response.sendRedirect("/user/profile");
            }
        } else {
            // For other URLs, use the parent class behavior which will redirect to the originally requested URL
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }
}