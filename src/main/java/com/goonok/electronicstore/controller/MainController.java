package com.goonok.electronicstore.controller;

import com.goonok.electronicstore.model.Product;
import com.goonok.electronicstore.service.BrandService;
import com.goonok.electronicstore.service.CategoryService;
import com.goonok.electronicstore.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


@Controller
public class MainController {

    @Autowired
    private ProductService productService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/")
    public String home(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String);

        model.addAttribute("isAuthenticated", isAuthenticated);
        model.addAttribute("username", isAuthenticated ? authentication.getName() : "");
        model.addAttribute("pageTitle", "Home");

        List<Product> products = productService.getFeaturedProducts(); // Fetch only featured products
        // Fetch featured products (e.g., first 8 or by criteria)
        List<Product> newProducts = productService.getNewProducts(); // Adjust filtering as needed
        model.addAttribute("products", products);
        model.addAttribute("newProducts", newProducts);
        model.addAttribute("brands", brandService.getAllBrands());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "index"; // Ensure this matches your index template location
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("pageTitle", "About Us");
        return "visitor/about-us"; // Ensure this matches your about template location
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("pageTitle", "Contact Us");
        return "visitor/contact"; // Ensure this matches your contact template location
    }

    @GetMapping("/category")
    public String category(Model model) {
        model.addAttribute("pageTitle", "Categories");
        return "category"; // Ensure this matches your category template location
    }

    @GetMapping("/product")
    public String products(Model model) {
        model.addAttribute("pageTitle", "Products");
        return "products"; // Ensure this matches your products template location
    }

    @GetMapping("/brand")
    public String brands(Model model) {
        model.addAttribute("pageTitle", "Brands");
        return "brands"; // Ensure this matches your brands template location
    }
}

