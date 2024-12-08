package com.goonok.electronicstore.controller;


import com.goonok.electronicstore.repository.UserRepository;
import com.goonok.electronicstore.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;


    // Admin dashboard
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {


        return "admin/dashboard";
    }

}
