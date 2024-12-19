package com.goonok.electronicstore.controller;

import com.goonok.electronicstore.model.Product;
import com.goonok.electronicstore.service.BrandService;
import com.goonok.electronicstore.service.CategoryService;
import com.goonok.electronicstore.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/admin/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @GetMapping
    public String getAllProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "product/admin/list";
    }

    @GetMapping("/add")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("brands", brandService.getAllBrands());
        return "product/admin/add";
    }

    @PostMapping("/add")
    public String addProduct(@ModelAttribute Product product, @RequestParam Long category, @RequestParam Long brand, @RequestParam("image") MultipartFile imageFile) throws IOException {
        if (!imageFile.isEmpty()) {
            // Save the file to a specific directory
            String filename = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
            Path filePath = Paths.get("uploads/images/", filename);
            log.info("File path: " + filePath);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, imageFile.getBytes());
            // Save the filename or path in the product
            product.setImagePath(filename);
        }

        product.setCategory(categoryService.getCategoryById(category).orElseThrow(() -> new RuntimeException("Category not found")));
        product.setBrand(brandService.getBrandById(brand).orElseThrow(() -> new RuntimeException("Brand not found")));
        productService.addProduct(product);
        return "redirect:/admin/products";
    }


    @GetMapping("/edit/{id}")
    public String showEditProductForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("brands", brandService.getAllBrands());
        return "product/admin/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable Long id, @ModelAttribute Product product, @RequestParam Long category, @RequestParam Long brand,
                                @RequestParam("image") MultipartFile imageFile) throws IOException {

        if (!imageFile.isEmpty()) {
            // Save the file to a specific directory
            String filename = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
            Path filePath = Paths.get("uploads/images", filename);

            Files.createDirectories(filePath.getParent());
            Files.write(filePath, imageFile.getBytes());

            // Update the product's image path
            product.setImagePath(filename);
        }
        product.setCategory(categoryService.getCategoryById(category).orElseThrow(() -> new RuntimeException("Category not found")));
        product.setBrand(brandService.getBrandById(brand).orElseThrow(() -> new RuntimeException("Brand not found")));
        productService.updateProduct(id, product);
        return "redirect:/admin/products";
    }


    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/admin/products";
    }


}
