package com.goonok.electronicstore.global;

import com.goonok.electronicstore.model.Brand;
import com.goonok.electronicstore.model.Category;
import com.goonok.electronicstore.model.ShoppingCart;
import com.goonok.electronicstore.model.User;
import com.goonok.electronicstore.service.BrandService;
import com.goonok.electronicstore.service.CategoryService;
import com.goonok.electronicstore.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;
import java.util.List;

@ControllerAdvice
public class GlobalModelAttributes {

    @Autowired
    private BrandService brandService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ShoppingCartService shoppingCartService;

    @ModelAttribute("menuBrands")
    public List<Brand> getBrands() {
        return brandService.getAllBrands(); // Fetch and return all brands
    }

    @ModelAttribute("menuCategories")
    public List<Category> getCategories() {
        return categoryService.getAllCategories(); // Fetch and return all categories
    }

  /*  @ModelAttribute("cartItemCount")
    public int getCartItemCount(Principal principal) {
        if (principal == null) {
            return 0; // No items for unauthenticated users
        }

        User currentUser = getUserFromPrincipal(principal);
        return shoppingCartService.getCartItemCountByUser(currentUser);
    }*/




}

