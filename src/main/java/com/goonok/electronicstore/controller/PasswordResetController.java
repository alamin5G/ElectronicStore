package com.goonok.electronicstore.controller;

import com.goonok.electronicstore.dto.PasswordResetDto;
import com.goonok.electronicstore.service.interfaces.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PasswordResetController {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetController.class);

    @Autowired
    private UserService userService;

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "password/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        log.info("Processing forgot password request for email: {}", email);

        userService.createPasswordResetTokenForUser(email.trim().toLowerCase());

        // Always show success message regardless of whether email exists (security)
        redirectAttributes.addFlashAttribute("successMessage",
                "If your email exists in our system, you will receive instructions to reset your password shortly.");

        return "redirect:/login";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        boolean isValidToken = userService.validatePasswordResetToken(token);

        if (!isValidToken) {
            model.addAttribute("errorMessage",
                    "The password reset link is invalid or has expired. Please request a new one.");
            return "password/invalid-token";
        }

        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setToken(token);
        model.addAttribute("passwordResetDto", passwordResetDto);

        return "password/reset-password";
    }


    @PostMapping("/reset-password")
    public String processResetPassword(@Valid @ModelAttribute("passwordResetDto") PasswordResetDto passwordResetDto,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {

        log.info("Processing password reset for token: {}", passwordResetDto.getToken());

        // Add password validation
        if (passwordResetDto.getPassword() != null && passwordResetDto.getPassword().length() < 8) {
            bindingResult.rejectValue("password", "error.passwordResetDto",
                    "Password must be at least 8 characters long");
        }

        // Check if passwords match
        if (!passwordResetDto.isPasswordsMatching()) {
            bindingResult.rejectValue("confirmPassword", "error.passwordResetDto",
                    "Passwords don't match");
        }

        // If validation errors exist, return to the form
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors in password reset: {}", bindingResult.getAllErrors());
            return "password/reset-password";
        }

        try {
            userService.resetPassword(passwordResetDto.getToken(), passwordResetDto.getPassword());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Your password has been reset successfully. You can now login with your new password.");
            return "redirect:/login";
        } catch (Exception e) {
            log.error("Error resetting password: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "There was an error resetting your password. Please try again.");
            return "password/reset-password";
        }
    }
}