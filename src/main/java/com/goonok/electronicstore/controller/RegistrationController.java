package com.goonok.electronicstore.controller;


import com.goonok.electronicstore.model.User;
import com.goonok.electronicstore.repository.UserRepository;
import com.goonok.electronicstore.services.UserService;
import com.goonok.electronicstore.services.VerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class RegistrationController {

    private static final Logger log = LoggerFactory.getLogger(RegistrationController.class);
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "registration";
    }

    @PostMapping("/register")
    public String processRegistrationForm(@Valid @ModelAttribute("user") User user, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        boolean isUserExist = userRepository.findByUsername(user.getUsername()).isPresent();
        boolean isEmailExist = userRepository.findByEmail(user.getEmail()).isPresent();
        boolean isPhoneExist = userRepository.findByPhone(user.getPhone()).isPresent();


        if (isUserExist) {
            bindingResult.rejectValue("username","error.username", "This username is already in use");
        }

        if (isEmailExist) {
            bindingResult.rejectValue("email","error.email", "This email is already in use");
        }

        if (isPhoneExist) {

            bindingResult.rejectValue("phone","error.phone", "This phone is already in use");
        }

        if (bindingResult.hasErrors() || isEmailExist || isPhoneExist || isUserExist) {
            log.info( "Binding Result Error: " + bindingResult.getAllErrors().toString());
            return "registration";
        }

        try {
            log.info("Registering user: " + user.getUsername());
            log.info("Email: " + user.getEmail());
            log.info("Phone: " + user.getPhone());
            log.info("Password: " + user.getPassword());
            log.info("Full Name: " + user.getFullName());

            /*if (user.getRoles() == null) {
                log.info("roles is null before save the user in the Registration controller");
                user.setRoles(new HashSet<>()); // Initialize roles if null
            }*/
            userService.createUser(user);
            log.info("User created: " + user.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "A verification email has been sent to your email address. Please verify within 24 hours. And Login");
            log.info("Verification email: " + user.getEmail());
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred during registration. Please try again.");
            log.info("Got an error: " + e.getMessage());
            return "registration";
        }
    }


    /*
     * Only when we need to register an account
     * for admin
     * we will hit this link /specialAccessForAdmin
     * */

    @GetMapping("/specialAccessForAdmin")
    public String showRegistrationFormForAdmin(Model model) {
        model.addAttribute("user", new User());
        return "registration-admin-form";
    }

    @PostMapping("/specialAccessForAdmin")
    public String registerAdmin(@Valid @ModelAttribute("user") User user, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        boolean isAdminExist = userRepository.findByUsername(user.getUsername()).isPresent();
        boolean isEmailExist = userRepository.findByEmail(user.getEmail()).isPresent();
        boolean isPhoneExist = userRepository.findByPhone(user.getPhone()).isPresent();


        if (isAdminExist) {
            bindingResult.rejectValue("username","error.username", "This username is already in use");
        }

        if (isEmailExist) {
            bindingResult.rejectValue("email","error.email", "This email is already in use");
        }

        if (isPhoneExist) {

            bindingResult.rejectValue("phone","error.phone", "This phone is already in use");
        }

        if (bindingResult.hasErrors() || isEmailExist || isPhoneExist || isAdminExist) {
            log.info( "Binding Result Error: " + bindingResult.getAllErrors().toString());
            return "registration-admin-form";
        }

        try {
            userService.createAdmin(user);
            redirectAttributes.addFlashAttribute("successMessage", "Admin registration is success.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred during registration. Please try again.");
            return "registration-admin-form";
        }
    }

    // This method is now handled in VerificationController
    @GetMapping("/resend-token")
    public String resendVerificationToken(@RequestParam("userId") Integer userId, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> user = userService.getUserById(userId);
        if (user.isPresent() && user.get().isVerified() && user.get().isEnabled()) {
            redirectAttributes.addFlashAttribute("successMessage", "You have been verified successfully. Please Login.");
            return "redirect:/login";
        }

        try {
            userService.resendVerificationEmail(userId);
            model.addAttribute("successMessage", "A new verification email has been sent to your email address. Please verify within 24 hours. And Login.");
            return "verify/resend-confirmation";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to resend verification email.");
            return "verify/failure";
        }
    }
}
