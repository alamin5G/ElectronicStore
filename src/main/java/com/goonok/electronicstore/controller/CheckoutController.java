package com.goonok.electronicstore.controller;


import com.goonok.electronicstore.model.ShoppingCart;
import com.goonok.electronicstore.model.User;
import com.goonok.electronicstore.service.OrderService;
import com.goonok.electronicstore.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
public class CheckoutController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/checkout")
    public String redirectToCart() {
        return "redirect:/cart"; // Redirect to cart page
    }


    @PostMapping("/checkout")
    public String processCheckout(
            @RequestParam String shippingAddress,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (principal == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to proceed with checkout.");
            return "redirect:/login";
        }

        User currentUser = getUserFromPrincipal(principal);

        // Get cart items
        List<ShoppingCart> cartItems = shoppingCartService.getCartItemsByUser(currentUser);

        if (cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Your cart is empty.");
            return "redirect:/cart";
        }

        try {
            // Place order with Cash on Delivery
            orderService.placeOrder(currentUser, cartItems, shippingAddress);

            // Clear cart after order placement
            shoppingCartService.clearCartForUser(currentUser);

            redirectAttributes.addFlashAttribute("successMessage", "Order placed successfully with Cash on Delivery!");
            return "redirect:/orders";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An error occurred during checkout. Please try again.");
            return "redirect:/cart";
        }
    }

    private User getUserFromPrincipal(Principal principal) {
        // Fetch the user using the principal
        // Replace with your user retrieval logic
        return new User(); // Placeholder
    }
}

