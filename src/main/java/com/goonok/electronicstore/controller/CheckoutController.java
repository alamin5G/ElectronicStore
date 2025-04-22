package com.goonok.electronicstore.controller;


import com.goonok.electronicstore.model.ShoppingCartItem;
import com.goonok.electronicstore.model.User;
import com.goonok.electronicstore.service.OrderService;
import com.goonok.electronicstore.service.ShoppingCartService;
import com.goonok.electronicstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
public class CheckoutController {

    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private UserService userService;

    @GetMapping("/checkout")
    public String showCheckoutPage(Principal principal, Model model, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to proceed with checkout.");
            return "redirect:/login";
        }

        User currentUser = getUserFromPrincipal(principal);
        List<ShoppingCartItem> cartItems = shoppingCartService.getCartItemsByUser(currentUser);

        if (cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Your cart is empty.");
            return "redirect:/cart";
        }

        // Calculate total price
        double totalPrice = cartItems.stream()
                .mapToDouble(item -> item.getProduct().getPrice().doubleValue() * item.getQuantity())
                .sum();

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);

        return "checkout"; // Points to checkout
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
        List<ShoppingCartItem> cartItems = shoppingCartService.getCartItemsByUser(currentUser);

        if (cartItems == null || cartItems.isEmpty()) {
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
            // Log the exception
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "An error occurred during checkout: " + e.getMessage());
            return "redirect:/cart";
        }
    }


    private User getUserFromPrincipal(Principal principal) {
        // Replace with actual user retrieval logic
        Optional<User> user = userService.getUserByEmail(principal.getName());
        return user.orElse(null);
    }
}
