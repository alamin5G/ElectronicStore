package com.goonok.electronicstore.global; // Ensure this package exists and is scanned by Spring

import com.goonok.electronicstore.dto.CartDto; // Needed for CartService return type
import com.goonok.electronicstore.model.Brand;
import com.goonok.electronicstore.model.Category;
import com.goonok.electronicstore.service.BrandService;
import com.goonok.electronicstore.service.CartService; // Need to inject CartService
import com.goonok.electronicstore.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication; // Needed to check login status
import org.springframework.security.core.context.SecurityContextHolder; // Needed to get current user
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;
import java.util.List;

/**
 * Controller Advice to add common model attributes needed across multiple views,
 * especially for layout components like the navbar.
 */
@Slf4j
@ControllerAdvice // Marks this class to apply logic across controllers
public class GlobalControllerAdvice {

    // Inject the services needed to fetch the common data
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CartService cartService; // Inject the CartService

    /**
     * Populates the 'menuCategories' model attribute before each request.
     * This attribute is used by the navbar's category dropdown.
     *
     * @return A List of Category objects, or an empty list if an error occurs.
     */
    @ModelAttribute("menuCategories") // The return value will be added to the Model with this key
    public List<Category> populateMenuCategories() {
        try {
            // Fetch all categories (sorted by name as defined in the service)
            return categoryService.getAllCategories();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * Populates the 'menuBrands' model attribute before each request.
     * This attribute is used by the navbar's brand dropdown.
     *
     * @return A List of Brand objects, or an empty list if an error occurs.
     */
    @ModelAttribute("menuBrands") // The return value will be added to the Model with this key
    public List<Brand> populateMenuBrands() {
        try {
            return brandService.getAllBrands();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * Populates the 'cartItemCount' model attribute before each request.
     * This attribute is used by the navbar's cart icon badge.
     * It calculates the count based on the currently logged-in user's cart.
     *
     * @return The number of distinct items in the user's cart, or 0 if not logged in or error.
     */
    @ModelAttribute("cartItemCount") // The return value will be added to the Model with this key
    public int populateCartItemCount() {
        // 1. Get the current user's authentication information
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int count = 0;

        // 2. Check if the user is actually logged in (not anonymous)
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            // 3. Get the username (which is the email in our case)
            String username = authentication.getName();
            try {
                // 4. Call the CartService to get the user's cart details
                CartDto cart = cartService.getCartForUser(username);
                // 5. If the cart and its items are not null, get the number of items
                if (cart != null && cart.getItems() != null) {
                    count = cart.getItems().size(); // Count distinct product lines
                }
            } catch (Exception e) {
                return count;
            }
        }
        // 8. Return the calculated count (this value is added to the model as "cartItemCount")
        return count;
    }
}
