package com.goonok.electronicstore.security;

import com.goonok.electronicstore.model.User;
import com.goonok.electronicstore.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    public CustomAuthenticationSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
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

        // Redirect based on role
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            response.sendRedirect("/admin/dashboard");
        } else {
            response.sendRedirect("/user/profile");
        }
    }
}