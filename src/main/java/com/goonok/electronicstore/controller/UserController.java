package com.goonok.electronicstore.controller;


import com.goonok.electronicstore.dto.ChangePasswordDto;
import com.goonok.electronicstore.dto.UserProfileUpdateDto;
import com.goonok.electronicstore.model.Product;
import com.goonok.electronicstore.model.User;
import com.goonok.electronicstore.repository.UserRepository;
import com.goonok.electronicstore.service.BrandService;
import com.goonok.electronicstore.service.CategoryService;
import com.goonok.electronicstore.service.ProductService;
import com.goonok.electronicstore.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;

    @Autowired
    private View error;


    @Autowired
    private ValidatorFactory validatorFactory;

    @Autowired
    private Validator validator;


    // Display User Profile
    @GetMapping("/profile")
    public String userProfile(Model model, Authentication authentication) {
        String username = authentication.getName();
        log.info("We have gotten the username from authentication.getName() function: "+username);
        Optional<User> userOptional = userService.getUserByEmail(username);

        if (userOptional.isPresent()) {
            model.addAttribute("user", userOptional.get());
        } else {
            // Handle the case where the user is not found
            model.addAttribute("errorMessage", "User not found");
            return "user/error";
        }

        return "user/profile";
    }

    // Display Edit Profile Form
    @GetMapping("/edit-profile")
    public String editProfile(Model model, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOptional = userService.getUserByEmail(username);

        if (userOptional.isPresent()) {
            model.addAttribute("userProfileUpdateDto", userOptional.get());
        } else {
            model.addAttribute("error", "User not found");
            return "user/error";
        }

        return "user/edit-profile";
    }

    // Handle Profile Update
    @PostMapping("/update-profile")
    public String updateProfile(@Valid @ModelAttribute("userProfileUpdateDto") UserProfileUpdateDto userProfileUpdateDto, BindingResult result, Model model,
                                Authentication authentication, RedirectAttributes redirectAttributes) {

        // Skip password validation if not changing the password

        String currentUsername = authentication.getName();
        log.info("We have gotten the username from authentication.getName() function: "+currentUsername);
        Optional<User> existingUserOpt = userService.getUserByEmail(currentUsername);
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();

            // Set the correct user ID
            userProfileUpdateDto.setId(existingUser.getUserId());

            boolean b = userRepository.findByEmail(userProfileUpdateDto.getEmail()).isPresent();
            // Check if the email or phone number already exists
            if ( b && !userProfileUpdateDto.getEmail().equals(existingUser.getEmail())) {
                result.rejectValue("email", "error.user", "This email is already in use");
            }

            if (userRepository.findByPhoneNumber(userProfileUpdateDto.getPhone()).isPresent() && !userProfileUpdateDto.getPhone().equals(existingUser.getPhoneNumber())) {
                result.rejectValue("phone", "error.user", "This phone number is already in use");
            }

            if (result.hasErrors()) {
                log.info("Binding Result Error: " + result.getAllErrors().toString());
                return "user/edit-profile";
            }


            // Save the updated user data
            userService.updateUser(userProfileUpdateDto);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully");
            return "redirect:/user/profile";
        } else {
            model.addAttribute("errorMessage", "User not found");
            return "user/error";
        }
    }


    // Display Update Password Form
    @GetMapping("/change-password")
    public String changePasswordForm(Model model) {
        model.addAttribute("changePasswordDto", new ChangePasswordDto());
        return "user/change-password";
    }

    // Handle Password Update
    @PostMapping("/update-password")
    public String updatePassword(@Valid ChangePasswordDto changePasswordDto, BindingResult bindingResult, Authentication authentication, Model model,
                                 RedirectAttributes redirectAttributes) {

        String username = authentication.getName();
        Optional<User> userOptional = userService.getUserByEmail(username);

        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();


            if (bindingResult.hasErrors()) {
                return "user/change-password";
            }

            if (!changePasswordDto.isPasswordsMatching()) {
                bindingResult.rejectValue("passwordsMatching", "error.passwordsMatching", "New password and confirm password do not match");
                return "user/change-password";
            }

            if (!userService.checkIfValidOldPassword(existingUser, changePasswordDto.getOldPassword())) {
                bindingResult.rejectValue("oldPassword", "error.oldPassword", "Old password is incorrect");
                return "user/change-password";
            }

            userService.updatePassword(existingUser, changePasswordDto.getNewPassword());
            redirectAttributes.addFlashAttribute("successMessage", "Password updated successfully");
            return "redirect:/user/profile";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found");
            return "/user/error";
        }
    }

    @GetMapping("/products")
    public String listProductsForUsers(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String priceRange,
            Model model) {

        List<Product> products = productService.getFilteredProducts(categoryId, brandId, priceRange);
        model.addAttribute("products", products);

        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("brands", brandService.getAllBrands());

        return "product/user/list"; // New user template
    }

}
