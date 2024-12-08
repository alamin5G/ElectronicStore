package com.goonok.electronicstore.controller;

import com.goonok.electronicstore.model.User;
import com.goonok.electronicstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    // Show the login form
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // Return the login.html view
    }

    // Handle login form submission
    @GetMapping("/login/success")
    public String loginSuccess(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            if (auth.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ADMIN"))) {
                return "redirect:/admin/dashboard"; // Admin redirect
            } else if (auth.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("USER"))) {
                return "redirect:/"; // User redirect
            }
        }
        return "redirect:/login?error=true"; // Fallback in case of error
    }

    // Handle login failure
    @GetMapping("/login?error=true")
    public String loginError(Model model) {
        model.addAttribute("error", "Invalid credentials. Please try again.");
        return "login"; // Return to login page with error message
    }
}
