package com.goonok.electronicstore.controller;

import com.goonok.electronicstore.model.Product;
import com.goonok.electronicstore.model.User;
import com.goonok.electronicstore.repository.UserRepository;
import com.goonok.electronicstore.service.ProductService;
import com.goonok.electronicstore.service.ShoppingCartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/cart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String showCart(Model model, Principal principal, HttpSession session) {
        if (principal != null) {
            User currentUser = getUserFromPrincipal(principal);
            model.addAttribute("cartItems", shoppingCartService.getCartItemsByUser(currentUser));
        } else {
            String sessionId = session.getId();
            model.addAttribute("cartItems", shoppingCartService.getCartItemsBySessionId(sessionId));
        }
        return "cart/list";
    }

    @PostMapping("/add/{productId}")
    public String addToCart(@PathVariable Long productId, @RequestParam Integer quantity,
                            Principal principal, HttpSession session, RedirectAttributes redirectAttributes) {
        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Validate stock quantity
        if (product.getStockQuantity() < quantity) {
            redirectAttributes.addFlashAttribute("error", "Not enough stock available!");
            return "redirect:/products"; // Redirect to products page or handle error appropriately
        }

        if (principal == null) {
            // Store product ID and quantity in session for guest users
            session.setAttribute("pendingCartProductId", productId);
            session.setAttribute("pendingCartQuantity", quantity);

            // Redirect to login page with a return URL
            return "redirect:/login/?redirect=/cart/add/" + productId;
        } else {
            // Process cart for logged-in users
            User currentUser = getUserFromPrincipal(principal);
            shoppingCartService.addOrUpdateCartItem(currentUser, product, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Product added to cart successfully!");
            return "redirect:/cart";
        }
    }


    @GetMapping("/remove/{productId}")
    public String removeCartItem(@PathVariable Long productId, Principal principal, HttpSession session) {
        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (principal != null) {
            User currentUser = getUserFromPrincipal(principal);
            shoppingCartService.removeCartItemByUserAndProduct(currentUser, product);
        } else {
            String sessionId = session.getId();
            shoppingCartService.removeCartItemBySessionIdAndProduct(sessionId, product);
        }
        return "redirect:/cart";
    }

    private User getUserFromPrincipal(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("User is not authenticated");
        }

        String email = principal.getName(); // Assuming email is the principal name
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

}

