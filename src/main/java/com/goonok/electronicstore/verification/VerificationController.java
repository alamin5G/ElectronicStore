package com.goonok.electronicstore.verification;

import com.goonok.electronicstore.model.User;
import com.goonok.electronicstore.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        Optional<VerificationToken> verificationToken = tokenRepository.findByToken(token);

        int userId = verificationToken.map(value -> value.getUser().getId()).orElse(0);

        Optional<User> user = userService.getUserById(userId);

        log.info("Verifying account for user {}", user);

        int registeredNewUserId = 0;

        if (user.isPresent()) {
             registeredNewUserId = user.get().getId();
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

    // Resend verification email
    @GetMapping("/resend/{userId}")
    public String resendVerificationEmail(@PathVariable("userId") Integer userId, Model model, RedirectAttributes redirectAttributes) {
        Optional<VerificationToken> verificationToken = tokenRepository.findByUserId(userId);

        User user = userService.getUserById(userId).get();
        if(user.isVerified() && user.isEnabled()){
            redirectAttributes.addFlashAttribute("error", " You are already verified, We cannot re-send verification email again. Please login");
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
}
