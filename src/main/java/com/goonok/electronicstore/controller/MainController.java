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
import org.springframework.web.bind.annotation.RequestParam;

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


    @GetMapping("/search")
    public String searchProducts(@RequestParam("q") String query, Model model) {
        List<Product> products = productService.searchProducts(query);
        model.addAttribute("products", products);
        model.addAttribute("query", query);
        return "product/user/search-results"; // Points to the search results template
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


}

