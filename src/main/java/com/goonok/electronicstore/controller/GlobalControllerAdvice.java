package com.goonok.electronicstore.controller; // Adjust package as needed

import com.goonok.electronicstore.model.Brand;
import com.goonok.electronicstore.model.Category;
import com.goonok.electronicstore.service.BrandService;
import com.goonok.electronicstore.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;
import java.util.List;

@Slf4j
@ControllerAdvice // This annotation makes methods available across controllers
public class GlobalControllerAdvice {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    // Inject CartService if you have one to calculate item count
    // @Autowired
    // private CartService cartService;

    /**
     * Populates 'menuCategories' model attribute for all requests.
     * Used by the navbar category dropdown.
     * @return List of categories or empty list on error.
     */
    // Inside GlobalControllerAdvice.java

    @ModelAttribute("menuCategories")
    public List<Category> populateMenuCategories() {
        try {
            return categoryService.getAllCategories();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @ModelAttribute("menuBrands")
    public List<Brand> populateMenuBrands() {
        try {
            return brandService.getAllBrands();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @ModelAttribute("cartItemCount")
    public int populateCartItemCount() {
        // --- Replace with your actual cart count logic ---
        int count = 0; // Default
        // ... your logic to get count ...
        return count;
    }
}
