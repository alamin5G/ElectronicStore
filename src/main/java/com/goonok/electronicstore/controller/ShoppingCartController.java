package com.goonok.electronicstore.controller;

import com.goonok.electronicstore.model.Product;
import com.goonok.electronicstore.model.ShoppingCart;
import com.goonok.electronicstore.model.User;
import com.goonok.electronicstore.service.ProductService;
import com.goonok.electronicstore.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/cart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private ProductService productService;

    @GetMapping
    public String showCart(Model model, Principal principal) {
        User currentUser = getUserFromPrincipal(principal);
        model.addAttribute("cartItems", shoppingCartService.getCartItemsByUser(currentUser));
        return "cart/list";
    }

    @PostMapping("/add/{productId}")
    public String addToCart(@PathVariable Long productId, @RequestParam Integer quantity, Principal principal) {
        User currentUser = getUserFromPrincipal(principal);
        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ShoppingCart cartItem = new ShoppingCart();
        cartItem.setUser(currentUser);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);

        shoppingCartService.addOrUpdateCartItem(cartItem);
        return "redirect:/cart";
    }

    @GetMapping("/remove/{cartId}")
    public String removeCartItem(@PathVariable Long cartId) {
        shoppingCartService.removeCartItem(cartId);
        return "redirect:/cart";
    }

    private User getUserFromPrincipal(Principal principal) {
        // Simulated user retrieval based on Principal
        // Replace this with your actual user retrieval logic (e.g., from a UserService)
        return new User(); // Mock user
    }
}
