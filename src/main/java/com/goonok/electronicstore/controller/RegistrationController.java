package com.goonok.electronicstore.controller;

import com.goonok.electronicstore.model.User;
import com.goonok.electronicstore.service.UserService;
import com.goonok.electronicstore.verification.VerificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegistrationController {

    @Autowired
    private UserService userService;

    @Autowired
    private VerificationService verificationService;

    // Show the registration form
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "registration";
    }

    // Handle user registration
    @PostMapping("/register")
    public String registerUser(@Valid User user, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Please fix the errors in the form.");
            return "registration";
        }



        try {
            // Register the user, assign the USER role, and send the verification email
            userService.registerUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please check your email to verify your account.");
            return "redirect:/login";  // Redirect to login after successful registration
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred during registration. Please try again." + e.getMessage());
            return "registration";
        }
    }

    // Handle email verification token

}
