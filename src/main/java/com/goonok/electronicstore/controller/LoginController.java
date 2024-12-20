package com.goonok.electronicstore.controller;

import com.goonok.electronicstore.model.Product;
import com.goonok.electronicstore.model.User;
import com.goonok.electronicstore.repository.UserRepository;
import com.goonok.electronicstore.service.GreetingService;
import com.goonok.electronicstore.service.ProductService;
import com.goonok.electronicstore.service.SecurityService;
import com.goonok.electronicstore.service.ShoppingCartService;
import jakarta.servlet.http.HttpSession;
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
    @Autowired
    private ShoppingCartService shoppingCartService;

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error, Model model) {

        if (error != null) {
            model.addAttribute("error", "Invalid username or password. Please try again.");
        }

        model.addAttribute("pageTitle", "Login");
        return "login"; // No need to add an empty User object
    }

    /*@GetMapping("/login/success")
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
                redirectAttributes.addFlashAttribute("successMessage", greetingService.greet(LocalTime.now()) + ", " + loginUserName + "!" );
                log.info("role admin redirected to admin/dashboard");
                return "redirect:/admin/dashboard";
            } else if (role.equals("[ROLE_USER]")) {
                redirectAttributes.addFlashAttribute("successMessage", greetingService.greet(LocalTime.now()) + ", " + loginUserName + "!" );
                log.info("role admin redirected to user/profile");
                return "redirect:/user/profile";
            }
        }
        log.info("redirect to login");
        model.addAttribute("error", "There was error. Please try again or contact us!");
        return "login"; // Fallback
    }*/

    @GetMapping("/login/success")
    public String loginRedirect(HttpSession session, RedirectAttributes redirectAttributes) {
        String loginUserName = securityService.findLoggedInUsername();
        Optional<User> userOptional = userRepository.findByEmailIgnoreCase(loginUserName);

        if (userOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Login failed. Please try again.");
            return "redirect:/login";
        }

        User loggedInUser = userOptional.get();

        // Check if there's a pending cart item
        Long pendingProductId = (Long) session.getAttribute("pendingCartProductId");
        Integer pendingQuantity = (Integer) session.getAttribute("pendingCartQuantity");

        if (pendingProductId != null && pendingQuantity != null) {
            // Process the pending cart item
            Product product = productService.getProductById(pendingProductId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            shoppingCartService.addOrUpdateCartItem(loggedInUser, product, pendingQuantity);

            // Clear the session attributes
            session.removeAttribute("pendingCartProductId");
            session.removeAttribute("pendingCartQuantity");

            redirectAttributes.addFlashAttribute("successMessage", "Product added to cart successfully!");
        }

        // Redirect based on user role
        String role = securityService.findLoggedInUserRoles().toString();
        if ("[ROLE_ADMIN]".equals(role)) {
            return "redirect:/admin/dashboard";
        } else if ("[ROLE_USER]".equals(role)) {
            return "redirect:/cart";
        }

        redirectAttributes.addFlashAttribute("error", "Unexpected role. Please contact support.");
        return "redirect:/";
    }

}
