package com.goonok.electronicstore.controller;

import com.goonok.electronicstore.model.Product;
import com.goonok.electronicstore.model.ShoppingCart;
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

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

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
    public String showCart(Model model, Principal principal) {
        List<ShoppingCart> cartItems = List.of(); // Default to an empty list

        if (principal != null) {
            User currentUser = getUserFromPrincipal(principal);
            cartItems = shoppingCartService.getCartItemsByUser(currentUser);
        }

        // Calculate the total price
        BigDecimal totalPrice = cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice); // Pass total price to the view
        return "cart/list";
    }


    @PostMapping("/add/{productId}")
    public String addToCart(@PathVariable Long productId, @RequestParam Integer quantity,
                            Principal principal, RedirectAttributes redirectAttributes) {
        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Validate stock quantity
        if (product.getStockQuantity() < quantity) {
            redirectAttributes.addFlashAttribute("error", "Not enough stock available!");
            return "redirect:/products"; // Redirect to products page or handle error appropriately
        }

        if (principal != null) {
            // Process cart for logged-in users
            User currentUser = getUserFromPrincipal(principal);
            shoppingCartService.addOrUpdateCartItem(currentUser, product, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Product added to cart successfully!");
            return "redirect:/cart";
        }

        return "redirect:/login";
    }



    @GetMapping("/remove/{cartId}")
    public String removeCartItem(@PathVariable Long cartId, RedirectAttributes redirectAttributes) {
        try {
            shoppingCartService.removeCartItemById(cartId);
            redirectAttributes.addFlashAttribute("successMessage", "Item removed from cart successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to remove item from cart.");
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

