package com.goonok.electronicstore.controller;

import com.goonok.electronicstore.dto.AdminUserViewDto;
import com.goonok.electronicstore.dto.ChangePasswordDto;
import com.goonok.electronicstore.dto.UserProfileUpdateDto;
import com.goonok.electronicstore.exception.ResourceNotFoundException;
import com.goonok.electronicstore.service.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProfileController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public String adminProfile(Model model, Authentication authentication) {
        String email = authentication.getName();
        log.info("Fetching admin profile for: {}", email);

        try {
            AdminUserViewDto adminProfile = userService.getUserByIdForAdmin(
                    userService.getUserProfileByEmail(email).getUserId());

            model.addAttribute("adminProfile", adminProfile);
            model.addAttribute("pageTitle", "Admin Profile");
        } catch (ResourceNotFoundException e) {
            log.error("Admin profile not found for: {}", email, e);
            model.addAttribute("errorMessage", "Admin profile could not be loaded.");
        }

        return "admin/profile";
    }

    @GetMapping("/edit-profile")
    public String showEditProfileForm(Model model, Authentication authentication) {
        String email = authentication.getName();
        log.info("Showing edit form for admin: {}", email);

        try {
            // Get current profile data
            AdminUserViewDto adminProfile = userService.getUserByIdForAdmin(
                    userService.getUserProfileByEmail(email).getUserId());

            // Create DTO for form binding
            UserProfileUpdateDto updateDto = new UserProfileUpdateDto();
            updateDto.setId(adminProfile.getUserId());
            updateDto.setName(adminProfile.getName());
            updateDto.setPhone(adminProfile.getPhoneNumber());

            model.addAttribute("userProfileUpdateDto", updateDto);
            model.addAttribute("userProfileDisplay", adminProfile);
            model.addAttribute("pageTitle", "Edit Admin Profile");
        } catch (ResourceNotFoundException e) {
            log.error("Admin profile not found for editing: {}", email, e);
            model.addAttribute("errorMessage", "Admin profile could not be loaded for editing.");
        }

        return "admin/edit-profile";
    }

    @PostMapping("/update-profile")
    public String processUpdateProfile(
            @Valid @ModelAttribute("userProfileUpdateDto") UserProfileUpdateDto updateDto,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Admin Profile");
            return "admin/edit-profile";
        }

        String email = authentication.getName();
        log.info("Updating admin profile for: {}", email);

        try {
            userService.updateUserProfile(email, updateDto);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        } catch (Exception e) {
            log.error("Error updating admin profile: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update profile: " + e.getMessage());
        }

        return "redirect:/admin/profile";
    }

    @GetMapping("/change-password")
    public String changePasswordForm(Model model) {
        model.addAttribute("changePasswordDto", new ChangePasswordDto());
        model.addAttribute("pageTitle", "Change Admin Password");
        return "admin/change-password";
    }

    @PostMapping("/update-password")
    public String updatePassword(
            @Valid @ModelAttribute("changePasswordDto") ChangePasswordDto changePasswordDto,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Change Admin Password");
            return "admin/change-password";
        }

        String email = authentication.getName();
        log.info("Changing password for admin: {}", email);

        try {
            // Get current user
            var user = userService.getUserByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Validate old password
            if (!userService.checkIfValidOldPassword(user, changePasswordDto.getOldPassword())) {
                model.addAttribute("errorMessage", "Current password is incorrect");
                model.addAttribute("pageTitle", "Change Admin Password");
                return "admin/change-password";
            }

            // Check if new password matches confirmation
            if (!changePasswordDto.getNewPassword().equals(changePasswordDto.getConfirmPassword())) {
                model.addAttribute("errorMessage", "New password and confirmation do not match");
                model.addAttribute("pageTitle", "Change Admin Password");
                return "admin/change-password";
            }

            // Update password
            userService.updatePassword(user, changePasswordDto.getNewPassword());
            redirectAttributes.addFlashAttribute("successMessage", "Password updated successfully!");

        } catch (ResourceNotFoundException e) {
            log.error("Admin not found for password change: {}", email, e);
            redirectAttributes.addFlashAttribute("errorMessage", "User not found");
        } catch (Exception e) {
            log.error("Error changing admin password: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update password: " + e.getMessage());
        }

        return "redirect:/admin/profile";
    }
}