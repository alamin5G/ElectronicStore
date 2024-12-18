package com.goonok.electronicstore.controller;

import com.goonok.electronicstore.model.ShoppingCart;
import com.goonok.electronicstore.model.User;
import com.goonok.electronicstore.service.OrderService;
import com.goonok.electronicstore.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @GetMapping
    public String viewOrders(Model model, Principal principal) {
        User currentUser = getUserFromPrincipal(principal);
        model.addAttribute("orders", orderService.getOrdersByUser(currentUser));
        return "order/list";
    }

    private User getUserFromPrincipal(Principal principal) {
        // Simulated user retrieval based on Principal
        // Replace this with your actual user retrieval logic (e.g., from a UserService)
        return new User(); // Mock user
    }

    @GetMapping("/checkout")
    public String showCheckoutPage(Model model, Principal principal) {
        User currentUser = getUserFromPrincipal(principal);
        List<ShoppingCart> cartItems = shoppingCartService.getCartItemsByUser(currentUser);

        BigDecimal totalPrice = cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);
        return "order/checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(@RequestParam String shippingAddress, Principal principal) {
        User currentUser = getUserFromPrincipal(principal);
        orderService.placeOrder(currentUser, shippingAddress);
        return "redirect:/orders";
    }

}
