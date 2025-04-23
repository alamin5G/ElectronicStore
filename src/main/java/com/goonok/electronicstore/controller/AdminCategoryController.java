package com.goonok.electronicstore.controller;

import com.goonok.electronicstore.dto.CategoryDto;
import com.goonok.electronicstore.exception.ResourceNotFoundException;
import com.goonok.electronicstore.service.CategoryService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/admin/categories") // Base path for admin category actions
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * Displays the list of all categories with pagination.
     */
    @GetMapping
    public String listCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name,asc") String sort,
            Model model) {

        log.info("Admin request received for category list - Page: {}, Size: {}, Sort: {}", page, size, sort);

        String[] sortParams = sort.split(",");
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")) ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortParams[0]
        ));

        Page<CategoryDto> categoryPage = categoryService.getAllCategories(pageable);
        model.addAttribute("categoryPage", categoryPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("sort", sort);

        return "category/admin/category-list"; // e.g., templates/category/admin/category-list.html
    }

    /**
     * Shows the form for adding a new category.
     */
    @GetMapping("/add")
    public String showAddCategoryForm(Model model) {
        log.info("Admin request received for add category form");
        model.addAttribute("categoryDto", new CategoryDto());
        model.addAttribute("pageTitle", "Add New Category");
        return "category/admin/category-form"; // e.g., templates/category/admin/category-form.html
    }

    /**
     * Processes the submission of the add category form.
     */
    @PostMapping("/add")
    public String processAddCategory(
            @Valid @ModelAttribute("categoryDto") CategoryDto categoryDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) { // Keep Model for returning to form on error

        log.info("Admin attempting to add category: {}", categoryDto.getName());

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors found while adding category: {}", bindingResult.getAllErrors());
            model.addAttribute("pageTitle", "Add New Category");
            return "category/admin/category-form"; // Return to form with errors
        }

        try {
            categoryService.createCategory(categoryDto);
            redirectAttributes.addFlashAttribute("successMessage", "Category '" + categoryDto.getName() + "' added successfully!");
            log.info("Category added successfully: {}", categoryDto.getName());
            return "redirect:/admin/categories"; // Redirect to list view
        } catch (IllegalArgumentException e) { // Catch specific exceptions like duplicate name
            log.error("Error adding category: {}", categoryDto.getName(), e);
            // Add error to binding result to display on form
            bindingResult.rejectValue("name", "error.categoryDto", e.getMessage());
            model.addAttribute("pageTitle", "Add New Category");
            return "category/admin/category-form";
        } catch (Exception e) {
            log.error("Error adding category: {}", categoryDto.getName(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding category: " + e.getMessage());
            model.addAttribute("pageTitle", "Add New Category");
            // Keep DTO data on general error
            model.addAttribute("categoryDto", categoryDto);
            return "category/admin/category-form";
        }
    }

    /**
     * Shows the form for editing an existing category.
     */
    @GetMapping("/edit/{id}")
    public String showEditCategoryForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        log.info("Admin request received for edit category form: ID={}", id);
        try {
            CategoryDto categoryDto = categoryService.getCategoryDtoById(id);
            model.addAttribute("categoryDto", categoryDto);
            model.addAttribute("pageTitle", "Edit Category");
            return "category/admin/category-form";
        } catch (ResourceNotFoundException e) {
            log.error("Category not found for editing: ID={}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Category not found with ID: " + id);
            return "redirect:/admin/categories";
        }
    }

    /**
     * Processes the submission of the edit category form.
     */
    @PostMapping("/edit/{id}")
    public String processEditCategory(
            @PathVariable Long id,
            @Valid @ModelAttribute("categoryDto") CategoryDto categoryDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        log.info("Admin attempting to update category: ID={}", id);
        categoryDto.setCategoryId(id); // Ensure ID is set

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors found while updating category {}: {}", id, bindingResult.getAllErrors());
            model.addAttribute("pageTitle", "Edit Category");
            // Keep the DTO with errors
            model.addAttribute("categoryDto", categoryDto);
            return "category/admin/category-form";
        }

        try {
            categoryService.updateCategory(id, categoryDto);
            redirectAttributes.addFlashAttribute("successMessage", "Category updated successfully!");
            log.info("Category updated successfully: ID={}", id);
            return "redirect:/admin/categories";
        } catch (ResourceNotFoundException e) {
            log.error("Category not found during update: ID={}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Category not found with ID: " + id);
            return "redirect:/admin/categories";
        } catch (IllegalArgumentException e) { // Catch specific exceptions like duplicate name
            log.error("Error updating category: {}", categoryDto.getName(), e);
            bindingResult.rejectValue("name", "error.categoryDto", e.getMessage());
            model.addAttribute("pageTitle", "Edit Category");
            model.addAttribute("categoryDto", categoryDto); // Keep DTO data
            return "category/admin/category-form";
        } catch (Exception e) {
            log.error("Error updating category: ID={}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating category: " + e.getMessage());
            model.addAttribute("pageTitle", "Edit Category");
            model.addAttribute("categoryDto", categoryDto); // Keep DTO data
            return "category/admin/category-form";
        }
    }

    /**
     * Handles the deletion of a category.
     */
    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.warn("Admin attempting to delete category: ID={}", id);
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully!");
            log.warn("Category deleted successfully: ID={}", id);
        } catch (ResourceNotFoundException e) {
            log.error("Category not found for deletion: ID={}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Category not found with ID: " + id);
        } catch (IllegalStateException e) { // Catch constraint violation
            log.error("Error deleting category: ID={}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage()); // Show specific error
        } catch (Exception e) {
            log.error("Error deleting category: ID={}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred while deleting the category.");
        }
        return "redirect:/admin/categories";
    }
}
