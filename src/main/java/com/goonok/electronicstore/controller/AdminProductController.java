package com.goonok.electronicstore.controller;

import com.goonok.electronicstore.dto.ProductDto;
import com.goonok.electronicstore.exception.ResourceNotFoundException; // Ensure this is defined
import com.goonok.electronicstore.service.WarrantyService;
import com.goonok.electronicstore.service.BrandService;
import com.goonok.electronicstore.service.CategoryService;
import com.goonok.electronicstore.service.interfaces.ProductService;
// Import WarrantyService if needed for dropdowns
import jakarta.validation.Valid; // For validating DTO
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; // For validation results
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // For flash messages


@Slf4j
@Controller
@RequestMapping("/admin/products") // Base path for admin product actions
public class AdminProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;
     @Autowired
     private WarrantyService warrantyService; // Inject if needed for warranty dropdown

    /**
     * Displays the list of all products for the admin with pagination.
     */
    @GetMapping
    public String listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, // Adjust default size for admin view
            @RequestParam(defaultValue = "productId,asc") String sort,
            Model model) {

        log.info("Admin request received for product list - Page: {}, Size: {}, Sort: {}", page, size, sort);

        String[] sortParams = sort.split(",");
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")) ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortParams[0]
        ));

        Page<ProductDto> productPage = productService.getAllProducts(pageable);
        model.addAttribute("productPage", productPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("sort", sort);

        return "product/admin/product-list"; // e.g., templates/product/admin/product-list.html
    }

    /**
     * Shows the form for adding a new product.
     */
    @GetMapping("/add")
    public String showAddProductForm(Model model) {
        log.info("Admin request received for add product form");
        model.addAttribute("productDto", new ProductDto()); // Use DTO as the form backing object
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("brands", brandService.getAllBrands());
        model.addAttribute("warranties", warrantyService.getAllWarranties()); // Add if needed
        model.addAttribute("pageTitle", "Add New Product"); // For dynamic title in form view
        return "product/admin/product-form"; // e.g., templates/product/admin/product-form.html
    }

    /**
     * Processes the submission of the add product form.
     */
    @PostMapping("/add")
    public String processAddProduct(
            @Valid @ModelAttribute("productDto") ProductDto productDto, // Validate the DTO
            BindingResult bindingResult, // Capture validation results
            @RequestParam("imageFile") MultipartFile imageFile, // Use a distinct name for the file input
            RedirectAttributes redirectAttributes, // For success/error messages
            Model model) { // To repopulate dropdowns on error

        log.info("Admin attempting to add product: {}", productDto.getName());

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors found while adding product: {}", bindingResult.getAllErrors());
            // Repopulate dropdowns needed for the form view
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("brands", brandService.getAllBrands());
            model.addAttribute("warranties", warrantyService.getAllWarranties());
            model.addAttribute("pageTitle", "Add New Product");
            return "product/admin/product-form"; // Return to form view with errors
        }

        try {
            // Pass DTO and image file to the service
            productService.createProduct(productDto, imageFile);
            redirectAttributes.addFlashAttribute("successMessage", "Product added successfully!");
            log.info("Product added successfully: {}", productDto.getName());
            return "redirect:/admin/products"; // Redirect to list view on success
        } catch (Exception e) {
            // Catch potential exceptions (e.g., file storage, database issues)
            log.error("Error adding product: {}", productDto.getName(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding product: " + e.getMessage());
            // Repopulate dropdowns and return to form
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("brands", brandService.getAllBrands());
            model.addAttribute("warranties", warrantyService.getAllWarranties());
            model.addAttribute("pageTitle", "Add New Product");
            // Add the DTO back to the model to retain user input (optional but good UX)
            model.addAttribute("productDto", productDto);
            return "product/admin/product-form";
        }
    }

    /**
     * Shows the form for editing an existing product.
     */
    @GetMapping("/edit/{id}")
    public String showEditProductForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        log.info("Admin request received for edit product form: ID={}", id);
        try {
            ProductDto productDto = productService.getProductDtoById(id);
            model.addAttribute("productDto", productDto);
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("brands", brandService.getAllBrands());
            model.addAttribute("warranties", warrantyService.getAllWarranties());
            model.addAttribute("pageTitle", "Edit Product"); // Dynamic title
            return "product/admin/product-form";
        } catch (ResourceNotFoundException e) {
            log.error("Product not found for editing: ID={}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Product not found with ID: " + id);
            return "redirect:/admin/products";
        }
    }

    /**
     * Processes the submission of the edit product form.
     */
    @PostMapping("/edit/{id}")
    public String processEditProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute("productDto") ProductDto productDto, // Validate DTO
            BindingResult bindingResult,
            @RequestParam("imageFile") MultipartFile imageFile, // Image file input
            RedirectAttributes redirectAttributes,
            Model model) {

        log.info("Admin attempting to update product: ID={}", id);
        productDto.setProductId(id); // Ensure the ID is set in the DTO for the service

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors found while updating product {}: {}", id, bindingResult.getAllErrors());
            // Repopulate dropdowns
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("brands", brandService.getAllBrands());
            model.addAttribute("warranties", warrantyService.getAllWarranties());
            model.addAttribute("pageTitle", "Edit Product");
            // Keep the DTO with errors to show in the form
            model.addAttribute("productDto", productDto);
            return "product/admin/product-form";
        }

        try {
            productService.updateProduct(id, productDto, imageFile);
            redirectAttributes.addFlashAttribute("successMessage", "Product updated successfully!");
            log.info("Product updated successfully: ID={}", id);
            return "redirect:/admin/products";
        } catch (ResourceNotFoundException e) {
            log.error("Product not found during update: ID={}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Product not found with ID: " + id);
            return "redirect:/admin/products";
        } catch (Exception e) {
            log.error("Error updating product: ID={}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating product: " + e.getMessage());
            // Repopulate dropdowns and return to form
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("brands", brandService.getAllBrands());
            model.addAttribute("warranties", warrantyService.getAllWarranties());
            model.addAttribute("pageTitle", "Edit Product");
            model.addAttribute("productDto", productDto); // Keep DTO data
            return "product/admin/product-form";
        }
    }

    /**
     * Handles the deletion of a product.
     * Changed to POST mapping for safety. Requires a form submit in the view.
     */
    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.warn("Admin attempting to delete product: ID={}", id);
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("successMessage", "Product deleted successfully!");
            log.warn("Product deleted successfully: ID={}", id);
        } catch (ResourceNotFoundException e) {
            log.error("Product not found for deletion: ID={}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Product not found with ID: " + id);
        } catch (Exception e) {
            // Catch other potential errors (e.g., database constraints if product is in an order)
            log.error("Error deleting product: ID={}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting product. It might be associated with existing orders.");
        }
        return "redirect:/admin/products";
    }



    /**
     * Toggles the featured status of a product.
     * Changed to POST mapping for safety. Requires a form submit in the view.
     */

    @PostMapping("/toggle-featured/{id}")
    public String toggleFeatured(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.toggleFeaturedStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Featured status updated successfully for product ID: " + id);
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Product not found with ID: " + id);
        } catch (Exception e) {
            log.error("Error toggling featured status for product ID {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating featured status.");
        }
        // Redirect back to the list, preserving pagination/sort might require passing params
        // For simplicity, redirecting to the base list URL
        return "redirect:/admin/products";
    }

    @PostMapping("/toggle-new/{id}")
    public String toggleNewArrival(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.toggleNewArrivalStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "New arrival status updated successfully for product ID: " + id);
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Product not found with ID: " + id);
        } catch (Exception e) {
            log.error("Error toggling new arrival status for product ID {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating new arrival status.");
        }
        // Redirect back to the list
        return "redirect:/admin/products";
    }
}
