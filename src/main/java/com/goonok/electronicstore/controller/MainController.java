package com.goonok.electronicstore.controller;

import com.goonok.electronicstore.dto.ProductDto; // Import DTO
import com.goonok.electronicstore.model.Brand;
import com.goonok.electronicstore.model.Category;
import com.goonok.electronicstore.service.BrandService;
import com.goonok.electronicstore.service.CategoryService;
import com.goonok.electronicstore.service.interfaces.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@Controller
public class MainController {

    @Autowired
    private ProductService productService;
    @Autowired
    private BrandService brandService; // Assuming this service exists
    @Autowired
    private CategoryService categoryService; // Assuming this service exists

    /**
     * Handles the homepage request.
     * Displays featured and new products (limited quantity).
     */
    @GetMapping("/")
    public String home(Model model) {
        log.info("Request received for homepage");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String);

        model.addAttribute("isAuthenticated", isAuthenticated);
        model.addAttribute("username", isAuthenticated ? authentication.getName() : "");
        model.addAttribute("pageTitle", "Home - Electronic Store");

        // Fetch a small number of featured and new products for the homepage display
        // Using Pageable to limit results, fetching only the first page
        int homepageProductLimit = 8; // Show up to 8 featured/new products
        Pageable featuredPageable = PageRequest.of(0, homepageProductLimit, Sort.by("createdAt").descending()); // Example sort
        Pageable newPageable = PageRequest.of(0, homepageProductLimit, Sort.by("createdAt").descending()); // Example sort

        try {
            Page<ProductDto> featuredProductPage = productService.getFeaturedProducts(featuredPageable);
            Page<ProductDto> newProductPage = productService.getNewProducts(newPageable);

            model.addAttribute("featuredProducts", featuredProductPage.getContent()); // Pass the list of DTOs
            model.addAttribute("newProducts", newProductPage.getContent()); // Pass the list of DTOs
        } catch (Exception e) {
            log.error("Error fetching products for homepage", e);
            // Add empty lists or handle error display in the template
            model.addAttribute("featuredProducts", List.of());
            model.addAttribute("newProducts", List.of());
            model.addAttribute("homepageError", "Could not load products at this time.");
        }

        // Fetch categories and brands for navigation/display
        try {
            model.addAttribute("brands", brandService.getAllBrands());
            model.addAttribute("categories", categoryService.getAllCategories());
        } catch (Exception e) {
            log.error("Error fetching brands or categories for homepage", e);
            model.addAttribute("brands", List.of());
            model.addAttribute("categories", List.of());
        }

        return "index"; // templates/index.html
    }

    /**
     * Handles product search requests.
     * Displays search results with pagination.
     */
    @GetMapping("/search")
    public String searchProducts(
            @RequestParam("q") String query, // Changed parameter name to 'q' for common practice
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size, // Use same default size as product listing
            @RequestParam(defaultValue = "name,asc") String sort,
            Model model) {

        log.info("Request received for product search: Query='{}', Page: {}, Size: {}, Sort: {}", query, page, size, sort);

        String[] sortParams = sort.split(",");
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")) ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortParams[0]
        ));

        Page<ProductDto> productPage = productService.searchProducts(query, pageable);

        model.addAttribute("productPage", productPage); // Pass the whole page object
        model.addAttribute("searchQuery", query); // Pass query back to display it
        model.addAttribute("pageTitle", "Search Results for '" + query + "'");

        // Add categories/brands if needed for filtering options on search results page
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("brands", brandService.getAllBrands());

        // Add pagination info for the view
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("sort", sort);


        // Reuse the main product listing view to display search results
        return "product/product-listing"; // e.g., templates/product/product-listing.html
    }

    /**
     * Displays the About Us page.
     */
    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("pageTitle", "About Us");
        return "visitor/about-us"; // Ensure this template exists
    }

    /**
     * Displays the Contact Us page.
     */
    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("pageTitle", "Contact Us");
        return "visitor/contact"; // Ensure this template exists
    }


}
