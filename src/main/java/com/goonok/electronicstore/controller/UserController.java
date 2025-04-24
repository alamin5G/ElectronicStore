package com.goonok.electronicstore.controller;

import com.goonok.electronicstore.dto.AddressDto; // Import
import com.goonok.electronicstore.dto.ChangePasswordDto;
import com.goonok.electronicstore.dto.UserProfileDto; // Import
import com.goonok.electronicstore.dto.UserProfileUpdateDto;
import com.goonok.electronicstore.exception.ResourceNotFoundException; // Import
import com.goonok.electronicstore.model.User; // Keep for password check
import com.goonok.electronicstore.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user") // Base path for user-specific actions
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;
    // Removed unused repositories (UserRepository, ProductService, etc.) - Service layer handles data access

    // --- Profile Viewing/Editing ---

    @GetMapping("/profile")
    public String userProfile(Model model, Authentication authentication) {
        String username = authentication.getName(); // Get email from principal
        log.info("Fetching profile for user: {}", username);
        try {
            UserProfileDto userProfileDto = userService.getUserProfileByEmail(username);
            model.addAttribute("userProfile", userProfileDto); // Use DTO
            model.addAttribute("pageTitle", "My Profile");
        } catch (ResourceNotFoundException e) {
            log.error("User not found for profile view: {}", username, e);
            model.addAttribute("errorMessage", "User profile could not be loaded.");
            // Optionally redirect to an error page or show message on profile template
        }
        return "user/profile"; // templates/user/profile.html
    }

    // *** UPDATED showEditProfileForm ***
    @GetMapping("/edit-profile")
    public String showEditProfileForm(Model model, Authentication authentication) {
        String username = authentication.getName();
        log.info("Showing edit profile form for user: {}", username);
        try {
            // Fetch DTO for display purposes
            UserProfileDto displayDto = userService.getUserProfileByEmail(username);

            // Create and populate the DTO specifically for the form update
            UserProfileUpdateDto updateDto = new UserProfileUpdateDto();
            updateDto.setId(displayDto.getUserId()); // Pass ID if needed
            updateDto.setName(displayDto.getName());
            // updateDto.setEmail(displayDto.getEmail()); // <<< REMOVED THIS LINE
            updateDto.setPhone(displayDto.getPhoneNumber());

            model.addAttribute("userProfileDisplay", displayDto); // DTO for displaying read-only info
            model.addAttribute("userProfileUpdateDto", updateDto); // DTO for form binding
            model.addAttribute("pageTitle", "Edit Profile");

        } catch (ResourceNotFoundException e) {
            log.error("User not found for edit profile form: {}", username, e);
            return "redirect:/user/profile?error=UserNotFound";
        }
        return "user/edit-profile";
    }
    // *** END UPDATED showEditProfileForm ***



    @PostMapping("/update-profile")
    public String processUpdateProfile(@Valid @ModelAttribute("userProfileUpdateDto") UserProfileUpdateDto userProfileUpdateDto,
                                       BindingResult bindingResult,
                                       Authentication authentication,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {

        String username = authentication.getName();
        log.info("Processing profile update for user: {}", username);

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors found while updating profile for {}: {}", username, bindingResult.getAllErrors());
            // Need to re-populate display info if returning to form
            try {
                // Fetch display DTO again to show read-only fields like email
                UserProfileDto displayDto = userService.getUserProfileByEmail(username);
                model.addAttribute("userProfileDisplay", displayDto);
            } catch (ResourceNotFoundException e) {
                log.error("User {} not found when repopulating edit form after error", username);
                redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
                return "redirect:/";
            }
            model.addAttribute("pageTitle", "Edit Profile");
            // The DTO with errors (userProfileUpdateDto) is already added by @ModelAttribute
            return "user/edit-profile";
        }

        try {
            userService.updateUserProfile(username, userProfileUpdateDto);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
            log.info("Profile updated successfully for user: {}", username);
            return "redirect:/user/profile";
        } catch (IllegalArgumentException e) {
            log.warn("Error updating profile for {}: {}", username, e.getMessage());
            if (e.getMessage().contains("Phone")) {
                bindingResult.rejectValue("phone", "error.userProfileUpdateDto", e.getMessage());
            } else {
                bindingResult.reject("error.userProfileUpdateDto", e.getMessage());
            }
            // Repopulate display info and return to form
            try {
                UserProfileDto displayDto = userService.getUserProfileByEmail(username);
                model.addAttribute("userProfileDisplay", displayDto);
            } catch (ResourceNotFoundException re) {
                log.error("User {} not found when repopulating edit form after error", username);
            }
            model.addAttribute("pageTitle", "Edit Profile");
            return "user/edit-profile";
        } catch (Exception e) {
            log.error("Unexpected error updating profile for {}: {}", username, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Could not update profile due to an unexpected error.");
            return "redirect:/user/profile";
        }
    }


    // --- Password Change (Keep as is) ---
    @GetMapping("/change-password")
    public String changePasswordForm(Model model) {
        model.addAttribute("changePasswordDto", new ChangePasswordDto());
        model.addAttribute("pageTitle", "Change Password");
        return "user/change-password";
    }

    @PostMapping("/update-password")
    public String updatePassword(@Valid @ModelAttribute("changePasswordDto") ChangePasswordDto changePasswordDto,
                                 BindingResult bindingResult,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes,
                                 Model model) { // Keep Model for returning to form

        String username = authentication.getName();
        Optional<User> userOptional = userService.getUserByEmail(username);

        if (!userOptional.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
            return "redirect:/login"; // Should not happen if authenticated, but good practice
        }
        User existingUser = userOptional.get();

        // Check custom validator/logic first
        if (!changePasswordDto.isPasswordsMatching()) {
            bindingResult.rejectValue("confirmPassword", "error.changePasswordDto", "New password and confirm password do not match");
        }
        if (!userService.checkIfValidOldPassword(existingUser, changePasswordDto.getOldPassword())) {
            bindingResult.rejectValue("oldPassword", "error.changePasswordDto", "Old password is incorrect");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Change Password");
            return "user/change-password";
        }

        try {
            userService.updatePassword(existingUser, changePasswordDto.getNewPassword());
            redirectAttributes.addFlashAttribute("successMessage", "Password updated successfully!");
            return "redirect:/user/profile";
        } catch (Exception e) {
            log.error("Error updating password for user {}: {}", username, e.getMessage(), e);
            model.addAttribute("errorMessage", "Could not update password.");
            model.addAttribute("pageTitle", "Change Password");
            return "user/change-password";
        }
    }

    // --- Address Book Management ---

    @GetMapping("/addresses")
    public String showAddressBook(Model model, Authentication authentication) {
        String username = authentication.getName();
        log.info("Fetching address book for user: {}", username);
        try {
            List<AddressDto> addresses = userService.getAddressesForUser(username);
            model.addAttribute("addresses", addresses);
            model.addAttribute("pageTitle", "My Address Book");
        } catch (ResourceNotFoundException e) {
            log.error("User not found for address book view: {}", username, e);
            model.addAttribute("errorMessage", "User profile could not be loaded.");
        }
        return "user/address-book"; // templates/user/address-book.html
    }

    @GetMapping("/addresses/add")
    public String showAddAddressForm(Model model) {
        log.info("Showing add address form");
        model.addAttribute("addressDto", new AddressDto());
        model.addAttribute("pageTitle", "Add New Address");
        model.addAttribute("formAction", "add"); // Indicate add mode
        return "user/address-form"; // templates/user/address-form.html
    }

    @PostMapping("/addresses/add")
    public String processAddAddress(@Valid @ModelAttribute("addressDto") AddressDto addressDto,
                                    BindingResult bindingResult,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {
        String username = authentication.getName();
        log.info("Processing add address for user: {}", username);

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors found while adding address for {}: {}", username, bindingResult.getAllErrors());
            model.addAttribute("pageTitle", "Add New Address");
            model.addAttribute("formAction", "add");
            return "user/address-form";
        }

        try {
            userService.addAddressToUser(username, addressDto);
            redirectAttributes.addFlashAttribute("successMessage", "Address added successfully!");
            return "redirect:/user/addresses";
        } catch (Exception e) {
            log.error("Error adding address for user {}: {}", username, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Could not add address: " + e.getMessage());
            // Optionally return to form with error
            model.addAttribute("pageTitle", "Add New Address");
            model.addAttribute("formAction", "add");
            model.addAttribute("addressDto", addressDto); // Keep data
            return "user/address-form";
        }
    }

    @GetMapping("/addresses/edit/{addressId}")
    public String showEditAddressForm(@PathVariable Long addressId,
                                      Model model,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        log.info("Showing edit address form for address ID {} and user: {}", addressId, username);
        try {
            AddressDto addressDto = userService.getAddressByIdAndUser(addressId, username);
            model.addAttribute("addressDto", addressDto);
            model.addAttribute("pageTitle", "Edit Address");
            model.addAttribute("formAction", "edit"); // Indicate edit mode
            return "user/address-form";
        } catch (ResourceNotFoundException e) {
            log.error("Address ID {} not found or does not belong to user {} for editing", addressId, username, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Address not found or access denied.");
            return "redirect:/user/addresses";
        }
    }

    @PostMapping("/addresses/edit/{addressId}")
    public String processEditAddress(@PathVariable Long addressId,
                                     @Valid @ModelAttribute("addressDto") AddressDto addressDto,
                                     BindingResult bindingResult,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {
        String username = authentication.getName();
        log.info("Processing edit address ID {} for user: {}", addressId, username);
        addressDto.setAddressId(addressId); // Ensure ID is set

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors found while editing address {} for {}: {}", addressId, username, bindingResult.getAllErrors());
            model.addAttribute("pageTitle", "Edit Address");
            model.addAttribute("formAction", "edit");
            model.addAttribute("addressDto", addressDto); // Keep data with errors
            return "user/address-form";
        }

        try {
            userService.updateAddressForUser(addressId, username, addressDto);
            redirectAttributes.addFlashAttribute("successMessage", "Address updated successfully!");
            return "redirect:/user/addresses";
        } catch (ResourceNotFoundException e) {
            log.error("Address ID {} not found or does not belong to user {} during update", addressId, username, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Address not found or access denied.");
            return "redirect:/user/addresses";
        } catch (Exception e) {
            log.error("Error updating address ID {} for user {}: {}", addressId, username, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Could not update address: " + e.getMessage());
            // Optionally return to form with error
            model.addAttribute("pageTitle", "Edit Address");
            model.addAttribute("formAction", "edit");
            model.addAttribute("addressDto", addressDto); // Keep data
            return "user/address-form";
        }
    }

    @PostMapping("/addresses/delete/{addressId}")
    public String deleteAddress(@PathVariable Long addressId,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        log.warn("Attempting to delete address ID {} for user: {}", addressId, username);
        try {
            userService.deleteUserAddress(addressId, username);
            redirectAttributes.addFlashAttribute("successMessage", "Address deleted successfully!");
        } catch (ResourceNotFoundException e) {
            log.error("Address ID {} not found or does not belong to user {} for deletion", addressId, username, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Address not found or access denied.");
        } catch (IllegalStateException e) { // Catch trying to delete last address
            log.error("Error deleting address ID {} for user {}: {}", addressId, username, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting address ID {} for user {}: {}", addressId, username, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Could not delete address.");
        }
        return "redirect:/user/addresses";
    }

    @PostMapping("/addresses/set-default/{addressId}")
    public String setDefaultAddress(@PathVariable Long addressId,
                                    @RequestParam String type, // "shipping" or "billing"
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        log.info("Setting default {} address to ID {} for user: {}", type, addressId, username);
        try {
            userService.setDefaultAddress(addressId, username, type);
            redirectAttributes.addFlashAttribute("successMessage", "Default " + type + " address updated successfully!");
        } catch (ResourceNotFoundException e) {
            log.error("Address ID {} not found or does not belong to user {} for setting default", addressId, username, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Address not found or access denied.");
        } catch (IllegalArgumentException e) {
            log.error("Invalid default type specified: {}", type, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid request.");
        } catch (Exception e) {
            log.error("Error setting default address ID {} for user {}: {}", addressId, username, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Could not set default address.");
        }
        return "redirect:/user/addresses";
    }

}
