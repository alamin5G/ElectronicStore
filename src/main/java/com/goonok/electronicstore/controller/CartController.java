package com.goonok.electronicstore.controller;

import com.goonok.electronicstore.dto.CartDto;
import com.goonok.electronicstore.exception.ResourceNotFoundException;
import com.goonok.electronicstore.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView; // For redirecting back

import jakarta.servlet.http.HttpServletRequest; // For getting referer URL

@Slf4j
@Controller
@RequestMapping("/cart") // Base path for cart actions
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * Adds an item to the shopping cart.
     * Requires authentication.
     */
    @PostMapping("/add/{productId}")
    public String addToCart(@PathVariable Long productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes,
                            HttpServletRequest request) { // Get request to find referer

        if (authentication == null || !authentication.isAuthenticated()) {
            // Should be handled by Spring Security, but good practice to check
            log.warn("Attempt to add to cart by unauthenticated user.");
            return "redirect:/login";
        }
        String username = authentication.getName();
        log.info("User {} attempting to add product ID {} quantity {} to cart", username, productId, quantity);

        try {
            cartService.addItemToCart(username, productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Item added to cart successfully!");
        } catch (IllegalArgumentException | ResourceNotFoundException e) {
            log.error("Error adding item to cart for user {}: {}", username, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding item: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error adding item to cart for user {}: {}", username, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred while adding the item.");
        }

        // Redirect back to the previous page (product details or listing) or to the cart page
        // Getting the referer URL is common but can be unreliable or null
        // String referer = request.getHeader("Referer");
        // Redirecting to cart page is simpler and more consistent
        return "redirect:/cart";
        // Or: return new RedirectView(referer != null ? referer : "/products");
    }

    /**
     * Displays the user's shopping cart.
     * Requires authentication.
     */
    @GetMapping
    public String viewCart(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String username = authentication.getName();
        log.info("User {} viewing cart", username);

        try {
            CartDto cart = cartService.getCartForUser(username);
            model.addAttribute("cart", cart);
            model.addAttribute("pageTitle", "Shopping Cart");
        } catch (ResourceNotFoundException e) {
            log.error("User not found when viewing cart: {}", username, e);
            // Handle appropriately - maybe redirect to login or show error message
            model.addAttribute("errorMessage", "Could not load cart: User not found.");
        } catch (Exception e) {
            log.error("Error retrieving cart for user {}: {}", username, e.getMessage(), e);
            model.addAttribute("errorMessage", "Could not load cart due to an error.");
        }

        return "cart/cart-view"; // e.g., templates/cart/cart-view.html
    }

    /**
     * Updates the quantity of an item in the cart.
     * Requires authentication.
     */
    @PostMapping("/update/{cartItemId}")
    public String updateCartItemQuantity(@PathVariable Long cartItemId,
                                         @RequestParam int quantity,
                                         Authentication authentication,
                                         RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String username = authentication.getName();
        log.info("User {} attempting to update cart item ID {} to quantity {}", username, cartItemId, quantity);

        try {
            cartService.updateCartItemQuantity(username, cartItemId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Cart updated successfully!");
        } catch (IllegalArgumentException | ResourceNotFoundException e) {
            log.error("Error updating cart item {} for user {}: {}", cartItemId, username, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating item: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error updating cart item {} for user {}: {}", cartItemId, username, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred.");
        }
        return "redirect:/cart";
    }

    /**
     * Removes an item from the cart.
     * Requires authentication.
     */
    @PostMapping("/remove/{cartItemId}")
    public String removeCartItem(@PathVariable Long cartItemId,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String username = authentication.getName();
        log.warn("User {} attempting to remove cart item ID {}", username, cartItemId);

        try {
            cartService.removeCartItem(username, cartItemId);
            redirectAttributes.addFlashAttribute("successMessage", "Item removed from cart.");
        } catch (ResourceNotFoundException e) {
            log.error("Error removing cart item {} for user {}: {}", cartItemId, username, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error removing item: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error removing cart item {} for user {}: {}", cartItemId, username, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred.");
        }
        return "redirect:/cart";
    }

    /**
     * Clears all items from the cart.
     * Requires authentication.
     */
    @PostMapping("/clear")
    public String clearCart(Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String username = authentication.getName();
        log.warn("User {} attempting to clear cart", username);

        try {
            cartService.clearCart(username);
            redirectAttributes.addFlashAttribute("successMessage", "Shopping cart cleared.");
        } catch (ResourceNotFoundException e) {
            log.error("User not found when clearing cart: {}", username, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Could not clear cart: User not found.");
        } catch (Exception e) {
            log.error("Error clearing cart for user {}: {}", username, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred while clearing the cart.");
        }
        return "redirect:/cart";
    }

}
