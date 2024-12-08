package com.goonok.electronicstore.controller;

import com.goonok.electronicstore.model.User;
import com.goonok.electronicstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/user/profile")
    public String userProfile(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("user", user);  // Add the logged-in user to the model
        return "user/profile";  // Return the user profile view
    }
}
