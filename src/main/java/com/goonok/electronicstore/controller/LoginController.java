package com.goonok.electronicstore.controller;

import com.goonok.electronicstore.model.User;
import com.goonok.electronicstore.repository.UserRepository;
import com.goonok.electronicstore.service.GreetingService;
import com.goonok.electronicstore.service.interfaces.ProductService;
import com.goonok.electronicstore.service.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalTime;
import java.util.Optional;

@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    private SecurityService securityService;

    @Autowired
    private GreetingService greetingService;

    @Autowired
    private ProductService productService;

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error, Model model) {

        if (error != null) {
            model.addAttribute("error", "Invalid username or password. Please try again.");
        }

        model.addAttribute("pageTitle", "Login");
        return "login"; // No need to add an empty User object
    }

    @GetMapping("/login/success")
    public String loginRedirect(RedirectAttributes redirectAttributes, Model model) {
        String loginUserName = securityService.findLoggedInUsername();
        System.out.println("Current Login User : " + loginUserName  );

        Optional<User> user = userRepository.findByEmailIgnoreCase(loginUserName);

        String role = securityService.findLoggedInUserRoles().toString();

        log.info("role : " + role);
        log.info("login success");
        if (role != null) {
            log.info("role not null: " + role);
            if (role.equals("[ROLE_ADMIN]")) {
                //set the greetings
                redirectAttributes.addFlashAttribute("successMessage", greetingService.greet(LocalTime.now()) + ", " + user.get().getName() + "!" );
                log.info("role admin redirected to admin/dashboard");
                return "redirect:/admin/dashboard";
            } else if (role.equals("[ROLE_USER]")) {
                redirectAttributes.addFlashAttribute("successMessage", greetingService.greet(LocalTime.now()) + ", " + user.get().getName() + "!" );
                log.info("role admin redirected to user/profile");
                return "redirect:/user/profile";
            }
        }
        log.info("redirect to login");
        model.addAttribute("error", "There was error. Please try again or contact us!");
        return "login"; // Fallback
    }


}
