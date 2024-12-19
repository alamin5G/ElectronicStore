package com.goonok.electronicstore.controller;

import com.goonok.electronicstore.model.Brand;
import com.goonok.electronicstore.model.Category;
import com.goonok.electronicstore.model.Product;
import com.goonok.electronicstore.service.BrandService;
import com.goonok.electronicstore.service.CategoryService;
import com.goonok.electronicstore.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class ProductDetailsController {

    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;

    @GetMapping("/products/{id}")
    public String showProductDetails(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        model.addAttribute("product", product);
        return "product/user/details"; // Points to the details.html template
    }

    @GetMapping("/products/brand/{brandId}")
    public String showProductsByBrand(@PathVariable Long brandId, Model model) {
        List<Product> products = productService.getProductsByBrand(brandId);
        model.addAttribute("products", products);
        model.addAttribute("filterType", "Brand");
        model.addAttribute("filterValue", brandService.getBrandById(brandId).map(Brand::getName).orElse("Unknown Brand"));
        return "product/user/list"; // Points to a generic product list template
    }

    @GetMapping("/products/category/{categoryId}")
    public String showProductsByCategory(@PathVariable Long categoryId, Model model) {
        List<Product> products = productService.getProductsByCategory(categoryId);
        model.addAttribute("products", products);
        model.addAttribute("filterType", "Category");
        model.addAttribute("filterValue", categoryService.getCategoryById(categoryId).map(Category::getName).orElse("Unknown Category"));
        return "product/user/list"; // Points to a generic product list template
    }


}
