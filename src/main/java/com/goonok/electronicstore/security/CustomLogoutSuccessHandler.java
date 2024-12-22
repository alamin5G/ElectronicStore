package com.goonok.electronicstore.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomLogoutSuccessHandler.class);

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        // Add your custom logout logic here
        if (authentication != null) {
            String username = authentication.getName();
            // Perform any necessary actions (e.g., logging, invalidating tokens, etc.)
            System.out.println("User " + username + " has logged out successfully.");

            // Send a notification (for demonstration purposes, this is just a log)
            sendLogoutNotification(username);

            // Redirect to a custom logout page
            response.sendRedirect("/?logout");
            log.info("After logout " + username + " has redirected to the index page from the CustomLogoutSuccessHandler");
            //Clearing session attributes
            request.getSession().invalidate();
        }else{

            // Redirect to the homepage or login page after logout
            response.sendRedirect("/?logout");
            //Clearing session attributes
            request.getSession().invalidate();
            log.info("User logged out successfully. from the customLogoutSuccessHandler");
        }


    }

    private void sendLogoutNotification(String username) {
        // Implement the logic to send an email or any other notification
        // This is just a placeholder for demonstration
        System.out.println("Notification: User " + username + " has logged out.");
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return new CustomLogoutSuccessHandler();
    }
}
