package com.goonok.electronicstore.controller;

import com.goonok.electronicstore.dto.WarrantyDto;
import com.goonok.electronicstore.exception.ResourceNotFoundException;
import com.goonok.electronicstore.service.WarrantyService;
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
@RequestMapping("/admin/warranties") // Base path for admin warranty actions
public class AdminWarrantyController {

    @Autowired
    private WarrantyService warrantyService;

    /**
     * Displays the list of all warranties with pagination.
     */
    @GetMapping
    public String listWarranties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "type,asc") String sort,
            Model model) {

        log.info("Admin request received for warranty list - Page: {}, Size: {}, Sort: {}", page, size, sort);

        String[] sortParams = sort.split(",");
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")) ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortParams[0]
        ));

        Page<WarrantyDto> warrantyPage = warrantyService.getAllWarranties(pageable);
        model.addAttribute("warrantyPage", warrantyPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("sort", sort);

        return "warranty/admin/warranty-list"; // e.g., templates/warranty/admin/warranty-list.html
    }

    /**
     * Shows the form for adding a new warranty.
     */
    @GetMapping("/add")
    public String showAddWarrantyForm(Model model) {
        log.info("Admin request received for add warranty form");
        model.addAttribute("warrantyDto", new WarrantyDto());
        model.addAttribute("pageTitle", "Add New Warranty");
        return "warranty/admin/warranty-form"; // e.g., templates/warranty/admin/warranty-form.html
    }

    /**
     * Processes the submission of the add warranty form.
     */
    @PostMapping("/add")
    public String processAddWarranty(
            @Valid @ModelAttribute("warrantyDto") WarrantyDto warrantyDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        log.info("Admin attempting to add warranty: {}", warrantyDto.getType());

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors found while adding warranty: {}", bindingResult.getAllErrors());
            model.addAttribute("pageTitle", "Add New Warranty");
            return "warranty/admin/warranty-form"; // Return to form with errors
        }

        try {
            warrantyService.createWarranty(warrantyDto);
            redirectAttributes.addFlashAttribute("successMessage", "Warranty '" + warrantyDto.getType() + "' added successfully!");
            log.info("Warranty added successfully: {}", warrantyDto.getType());
            return "redirect:/admin/warranties"; // Redirect to list view
        } catch (IllegalArgumentException e) {
            log.error("Error adding warranty: {}", warrantyDto.getType(), e);
            bindingResult.rejectValue("type", "error.warrantyDto", e.getMessage()); // Show duplicate error on form
            model.addAttribute("pageTitle", "Add New Warranty");
            return "warranty/admin/warranty-form";
        } catch (Exception e) {
            log.error("Error adding warranty: {}", warrantyDto.getType(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding warranty: " + e.getMessage());
            model.addAttribute("pageTitle", "Add New Warranty");
            model.addAttribute("warrantyDto", warrantyDto); // Keep DTO data
            return "warranty/admin/warranty-form";
        }
    }

    /**
     * Shows the form for editing an existing warranty.
     */
    @GetMapping("/edit/{id}")
    public String showEditWarrantyForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        log.info("Admin request received for edit warranty form: ID={}", id);
        try {
            WarrantyDto warrantyDto = warrantyService.getWarrantyDtoById(id);
            model.addAttribute("warrantyDto", warrantyDto);
            model.addAttribute("pageTitle", "Edit Warranty");
            return "warranty/admin/warranty-form";
        } catch (ResourceNotFoundException e) {
            log.error("Warranty not found for editing: ID={}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Warranty not found with ID: " + id);
            return "redirect:/admin/warranties";
        }
    }

    /**
     * Processes the submission of the edit warranty form.
     */
    @PostMapping("/edit/{id}")
    public String processEditWarranty(
            @PathVariable Long id,
            @Valid @ModelAttribute("warrantyDto") WarrantyDto warrantyDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        log.info("Admin attempting to update warranty: ID={}", id);
        warrantyDto.setWarrantyId(id); // Ensure ID is set

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors found while updating warranty {}: {}", id, bindingResult.getAllErrors());
            model.addAttribute("pageTitle", "Edit Warranty");
            model.addAttribute("warrantyDto", warrantyDto); // Keep DTO data
            return "warranty/admin/warranty-form";
        }

        try {
            warrantyService.updateWarranty(id, warrantyDto);
            redirectAttributes.addFlashAttribute("successMessage", "Warranty updated successfully!");
            log.info("Warranty updated successfully: ID={}", id);
            return "redirect:/admin/warranties";
        } catch (ResourceNotFoundException e) {
            log.error("Warranty not found during update: ID={}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Warranty not found with ID: " + id);
            return "redirect:/admin/warranties";
        } catch (IllegalArgumentException e) {
            log.error("Error updating warranty: {}", warrantyDto.getType(), e);
            bindingResult.rejectValue("type", "error.warrantyDto", e.getMessage()); // Show duplicate error
            model.addAttribute("pageTitle", "Edit Warranty");
            model.addAttribute("warrantyDto", warrantyDto); // Keep DTO data
            return "warranty/admin/warranty-form";
        } catch (Exception e) {
            log.error("Error updating warranty: ID={}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating warranty: " + e.getMessage());
            model.addAttribute("pageTitle", "Edit Warranty");
            model.addAttribute("warrantyDto", warrantyDto); // Keep DTO data
            return "warranty/admin/warranty-form";
        }
    }

    /**
     * Handles the deletion of a warranty.
     */
    @PostMapping("/delete/{id}")
    public String deleteWarranty(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.warn("Admin attempting to delete warranty: ID={}", id);
        try {
            warrantyService.deleteWarranty(id);
            redirectAttributes.addFlashAttribute("successMessage", "Warranty deleted successfully!");
            log.warn("Warranty deleted successfully: ID={}", id);
        } catch (ResourceNotFoundException e) {
            log.error("Warranty not found for deletion: ID={}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Warranty not found with ID: " + id);
        } catch (IllegalStateException e) { // Catch constraint violation
            log.error("Error deleting warranty: ID={}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage()); // Show specific error
        } catch (Exception e) {
            log.error("Error deleting warranty: ID={}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred while deleting the warranty.");
        }
        return "redirect:/admin/warranties";
    }
}
