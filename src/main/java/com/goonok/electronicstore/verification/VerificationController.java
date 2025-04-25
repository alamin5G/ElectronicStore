package com.goonok.electronicstore.verification;

import com.goonok.electronicstore.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.goonok.electronicstore.service.interfaces.UserService;

import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping("/verify") // Matches /verify from email link
public class VerificationController {

    private static final Logger log = LoggerFactory.getLogger(VerificationController.class);
    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private UserService userService;

    // Account verification method, directly matching the /verify?token=... link
    @GetMapping
    public String verifyAccount(@RequestParam("token") String token, Model model, RedirectAttributes redirectAttributes) {
        // Existing implementation
        Optional<VerificationToken> verificationToken = tokenRepository.findByToken(token);

        long userId = verificationToken.map(value -> value.getUser().getUserId()).orElse(0L);

        Optional<User> user = Optional.ofNullable(userService.getUserById(userId));

        log.info("Verifying account for user {}", user.orElse(null));

        Long registeredNewUserId = 0L;

        if (user.isPresent()) {
             registeredNewUserId = user.get().getUserId();
        }

        if (verificationToken.isPresent() && verificationToken.get().isVerified()) {
            redirectAttributes.addFlashAttribute("successMessage", "You are already verified. Please Login with your credential.");
            return "redirect:/login";
        }

        if (verificationToken.isPresent() && verificationToken.get().getExpiryDate().isBefore(LocalDate.now()) && !verificationToken.get().isVerified()){
            model.addAttribute("error", "The verification token has expired.");
            model.addAttribute("userId", registeredNewUserId); // Add userId if needed for resending
            log.info("The verification token has expired.");
            return "verify/failure";
        }


        if (verificationToken.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Email Verification is failed due to Invalid verification token." +
                    "\n Please Register an account or verify your email address.");
            return "redirect:/register";
        }


        // Assuming the verifyToken method activates the user's account
        try {
            verificationService.verifyToken(token);
            model.addAttribute("successMessage", "Your account has been successfully verified. You can now log in.");
            return "verify/success";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to verify the account: " + e.getMessage());
            model.addAttribute("userId", registeredNewUserId);
            log.info("userId = " + registeredNewUserId);
            return "verify/failure";
        }
    }

    // Resend verification email using userId
    @GetMapping("/resend/{userId}")
    public String resendVerificationEmail(@PathVariable("userId") Long userId, Model model, RedirectAttributes redirectAttributes) {
        Optional<VerificationToken> verificationToken = tokenRepository.findUserById(userId);

        User user = userService.getUserById(userId);
        if(user.isVerified() && user.isEnabled()){
            redirectAttributes.addFlashAttribute("error", "You are already verified. We cannot re-send verification email again. Please login");
            return "redirect:/login";
        }
        try {
            userService.resendVerificationEmail(userId);
            model.addAttribute("successMessage", "A new verification email has been sent.");
            return "verify/resend-confirmation";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to resend verification email.");
            return "verify/failure";
        }
    }
    
    // NEW METHOD: Show the request verification form
    @GetMapping("/request")
    public String showRequestVerificationForm() {
        return "verify/request-verification";
    }
    
    // NEW METHOD: Process the request verification by email
    @PostMapping("/request")
    public String requestVerificationByEmail(@RequestParam("email") String email, 
                                            RedirectAttributes redirectAttributes) {
        log.info("Requesting verification email for: {}", email);
        
        // Find user by email
        Optional<User> userOptional = userService.getUserByEmail(email);
        
        if (userOptional.isEmpty()) {
            // Don't reveal that email doesn't exist - security best practice
            redirectAttributes.addFlashAttribute("successMessage", 
                "If your email exists in our system, a verification link has been sent.");
            return "redirect:/login";
        }
        
        User user = userOptional.get();
        
        // If already verified, don't send new email
        if (user.isVerified() && user.isEnabled()) {
            redirectAttributes.addFlashAttribute("infoMessage", 
                "Your account is already verified. You can login now.");
            return "redirect:/login";
        }
        
        // Resend verification email
        verificationService.resendVerificationToken(user);
        
        redirectAttributes.addFlashAttribute("successMessage", 
            "A new verification email has been sent to your email address.");
        return "redirect:/login";
    }
}