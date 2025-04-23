package com.goonok.electronicstore.controller;

import com.goonok.electronicstore.dto.BrandDto;
import com.goonok.electronicstore.exception.ResourceNotFoundException;
import com.goonok.electronicstore.service.BrandService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/admin/brands") // Base path for admin brand actions
public class AdminBrandController {

    @Autowired
    private BrandService brandService;

    /**
     * Displays the list of all brands with pagination.
     */
    @GetMapping
    public String listBrands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name,asc") String sort,
            Model model) {

        log.info("Admin request received for brand list - Page: {}, Size: {}, Sort: {}", page, size, sort);

        String[] sortParams = sort.split(",");
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")) ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortParams[0]
        ));

        Page<BrandDto> brandPage = brandService.getAllBrands(pageable);
        model.addAttribute("brandPage", brandPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("sort", sort);

        return "brand/admin/brand-list"; // e.g., templates/brand/admin/brand-list.html
    }

    /**
     * Shows the form for adding a new brand.
     */
    @GetMapping("/add")
    public String showAddBrandForm(Model model) {
        log.info("Admin request received for add brand form");
        model.addAttribute("brandDto", new BrandDto());
        model.addAttribute("pageTitle", "Add New Brand");
        return "brand/admin/brand-form"; // e.g., templates/brand/admin/brand-form.html
    }

    /**
     * Processes the submission of the add brand form.
     */
    @PostMapping("/add")
    public String processAddBrand(
            @Valid @ModelAttribute("brandDto") BrandDto brandDto,
            BindingResult bindingResult,
            @RequestParam("logoFile") MultipartFile logoFile, // Input name for logo file
            RedirectAttributes redirectAttributes,
            Model model) {

        log.info("Admin attempting to add brand: {}", brandDto.getName());

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors found while adding brand: {}", bindingResult.getAllErrors());
            model.addAttribute("pageTitle", "Add New Brand");
            return "brand/admin/brand-form"; // Return to form with errors
        }

        try {
            brandService.createBrand(brandDto, logoFile);
            redirectAttributes.addFlashAttribute("successMessage", "Brand '" + brandDto.getName() + "' added successfully!");
            log.info("Brand added successfully: {}", brandDto.getName());
            return "redirect:/admin/brands"; // Redirect to list view
        } catch (IllegalArgumentException e) { // Catch specific exceptions like duplicate name
            log.error("Error adding brand: {}", brandDto.getName(), e);
            // Add error to binding result to display on form
            bindingResult.rejectValue("name", "error.brandDto", e.getMessage());
            model.addAttribute("pageTitle", "Add New Brand");
            return "brand/admin/brand-form";
        } catch (Exception e) {
            log.error("Error adding brand: {}", brandDto.getName(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding brand: " + e.getMessage());
            model.addAttribute("pageTitle", "Add New Brand");
            // Keep DTO data on general error
            model.addAttribute("brandDto", brandDto);
            return "brand/admin/brand-form";
        }
    }

    /**
     * Shows the form for editing an existing brand.
     */
    @GetMapping("/edit/{id}")
    public String showEditBrandForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        log.info("Admin request received for edit brand form: ID={}", id);
        try {
            BrandDto brandDto = brandService.getBrandDtoById(id);
            model.addAttribute("brandDto", brandDto);
            model.addAttribute("pageTitle", "Edit Brand");
            return "brand/admin/brand-form";
        } catch (ResourceNotFoundException e) {
            log.error("Brand not found for editing: ID={}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Brand not found with ID: " + id);
            return "redirect:/admin/brands";
        }
    }

    /**
     * Processes the submission of the edit brand form.
     */
    @PostMapping("/edit/{id}")
    public String processEditBrand(
            @PathVariable Long id,
            @Valid @ModelAttribute("brandDto") BrandDto brandDto,
            BindingResult bindingResult,
            @RequestParam("logoFile") MultipartFile logoFile,
            RedirectAttributes redirectAttributes,
            Model model) {

        log.info("Admin attempting to update brand: ID={}", id);
        brandDto.setBrandId(id); // Ensure ID is set

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors found while updating brand {}: {}", id, bindingResult.getAllErrors());
            model.addAttribute("pageTitle", "Edit Brand");
            // Keep the DTO with errors
            model.addAttribute("brandDto", brandDto);
            return "brand/admin/brand-form";
        }

        try {
            brandService.updateBrand(id, brandDto, logoFile);
            redirectAttributes.addFlashAttribute("successMessage", "Brand updated successfully!");
            log.info("Brand updated successfully: ID={}", id);
            return "redirect:/admin/brands";
        } catch (ResourceNotFoundException e) {
            log.error("Brand not found during update: ID={}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Brand not found with ID: " + id);
            return "redirect:/admin/brands";
        } catch (IllegalArgumentException e) { // Catch specific exceptions like duplicate name
            log.error("Error updating brand: {}", brandDto.getName(), e);
            bindingResult.rejectValue("name", "error.brandDto", e.getMessage());
            model.addAttribute("pageTitle", "Edit Brand");
            model.addAttribute("brandDto", brandDto); // Keep DTO data
            return "brand/admin/brand-form";
        } catch (Exception e) {
            log.error("Error updating brand: ID={}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating brand: " + e.getMessage());
            model.addAttribute("pageTitle", "Edit Brand");
            model.addAttribute("brandDto", brandDto); // Keep DTO data
            return "brand/admin/brand-form";
        }
    }

    /**
     * Handles the deletion of a brand.
     */
    @PostMapping("/delete/{id}")
    public String deleteBrand(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.warn("Admin attempting to delete brand: ID={}", id);
        try {
            brandService.deleteBrand(id);
            redirectAttributes.addFlashAttribute("successMessage", "Brand deleted successfully!");
            log.warn("Brand deleted successfully: ID={}", id);
        } catch (ResourceNotFoundException e) {
            log.error("Brand not found for deletion: ID={}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Brand not found with ID: " + id);
        } catch (Exception e) {
            // Catch other potential errors (e.g., constraints if brand is used by products)
            log.error("Error deleting brand: ID={}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting brand. It might be associated with existing products.");
        }
        return "redirect:/admin/brands";
    }
}
