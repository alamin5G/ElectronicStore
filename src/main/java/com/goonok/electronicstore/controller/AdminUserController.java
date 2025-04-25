package com.goonok.electronicstore.controller;

import com.goonok.electronicstore.dto.AdminUserViewDto;
import com.goonok.electronicstore.exception.ResourceNotFoundException;
import com.goonok.electronicstore.model.Role;
import com.goonok.electronicstore.repository.RoleRepository;
import com.goonok.electronicstore.service.interfaces.UserService; // Updated import
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping
    public String listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "userId,asc") String sort,
            Model model) {

        String[] sortParams = sort.split(",");
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")) ?
                        Sort.Direction.DESC : Sort.Direction.ASC,
                sortParams[0]
        ));

        try {
            Page<AdminUserViewDto> userPage = userService.getAllUsers(pageable);
            model.addAttribute("userPage", userPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("sort", sort);
            model.addAttribute("pageTitle", "Manage Users");
        } catch (Exception e) {
            log.error("Error fetching users for admin view", e);
            model.addAttribute("errorMessage", "Could not load users.");
            model.addAttribute("userPage", Page.empty(pageable));
        }
        return "admin/user-list";
    }

    @GetMapping("/view/{id}")
    public String viewUserDetails(@PathVariable("id") Long userId, Model model, 
                                RedirectAttributes redirectAttributes) {
        log.info("Admin request received for user details: ID={}", userId);
        try {
            AdminUserViewDto userDto = userService.getUserByIdForAdmin(userId);
            AdminUserViewDto userAnalytics = userService.getUserAnalytics(userId); // Added analytics
            List<Role> allRoles = roleRepository.findAll(Sort.by("roleName"));

            model.addAttribute("userDetail", userDto);
            model.addAttribute("userAnalytics", userAnalytics); // Added analytics to model
            model.addAttribute("allRoles", allRoles);
            model.addAttribute("pageTitle", "User Details - " + userDto.getName());

        } catch (ResourceNotFoundException e) {
            log.error("User ID {} not found for admin view", userId, e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "User not found with ID: " + userId);
            return "redirect:/admin/users";
        } catch (Exception e) {
            log.error("Error fetching user details for admin view, ID {}: {}", 
                userId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Could not load user details.");
            return "redirect:/admin/users";
        }
        return "admin/user-details";
    }

    @PostMapping("/toggle-status/{id}")
    public String toggleUserStatus(@PathVariable("id") Long userId, 
                                 RedirectAttributes redirectAttributes) {
        log.info("Admin request to toggle status for user ID: {}", userId);
        try {
            AdminUserViewDto updatedUser = userService.toggleUserStatus(userId);
            String status = updatedUser.isEnabled() ? "enabled" : "disabled";
            redirectAttributes.addFlashAttribute("successMessage", 
                "User account has been " + status + ".");
        } catch (ResourceNotFoundException e) {
            log.error("User ID {} not found for status toggle", userId, e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "User not found with ID: " + userId);
            return "redirect:/admin/users";
        } catch (Exception e) {
            log.error("Error toggling status for user ID {}: {}", 
                userId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Could not toggle user status.");
        }
        return "redirect:/admin/users/view/" + userId;
    }

    @PostMapping("/update-roles/{id}")
    public String updateUserRoles(@PathVariable("id") Long userId,
                                @RequestParam(value = "roleIds", required = false) 
                                Set<Long> roleIds,
                                RedirectAttributes redirectAttributes) {
        log.info("Admin request to update roles for user ID: {} with Role IDs: {}", 
            userId, roleIds);
        try {
            AdminUserViewDto updatedUser = userService.updateUserRoles(userId, 
                roleIds == null ? Collections.emptySet() : roleIds);
            redirectAttributes.addFlashAttribute("successMessage", 
                "User roles updated successfully.");
        } catch (ResourceNotFoundException e) {
            log.error("User or Role not found during role update for user ID {}: {}", 
                userId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "User or one of the roles not found.");
        } catch (Exception e) {
            log.error("Error updating roles for user ID {}: {}", 
                userId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Could not update user roles.");
        }
        return "redirect:/admin/users/view/" + userId;
    }
}