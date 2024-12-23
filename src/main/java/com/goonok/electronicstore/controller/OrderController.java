package com.goonok.electronicstore.controller;

import com.goonok.electronicstore.model.ShoppingCart;
import com.goonok.electronicstore.model.User;
import com.goonok.electronicstore.service.OrderService;
import com.goonok.electronicstore.service.ShoppingCartService;
import com.goonok.electronicstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private UserService userService;
    @Autowired
    private ShoppingCartService shoppingCartService;

    @GetMapping
    public String viewOrders(Model model, Principal principal) {
        User currentUser = getUserFromPrincipal(principal);
        model.addAttribute("orders", orderService.getOrdersByUser(currentUser));
        return "order/list";
    }

    private User getUserFromPrincipal(Principal principal) {
        if (principal == null) {
            return null;
        }else {
            return userService.getUserByEmail(principal.getName()).orElse(null);
        }
    }



}
