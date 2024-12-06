package com.goonok.electronicstore.controller;


import com.goonok.electronicstore.model.Role;
import com.goonok.electronicstore.services.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    // Display the list of roles
    @GetMapping
    public String manageRoles(Model model) {
        List<Role> roles = roleService.getAllRoles();
        model.addAttribute("roles", roles);
        return "admin/roles";
    }

    // Show form to create a new role
    @GetMapping("/create")
    public String showCreateRoleForm(Model model) {
        model.addAttribute("role", new Role());
        return "admin/role/create_role";
    }

    // Handle the form submission for creating a new role
    @PostMapping("/create")
    public String createRole(@ModelAttribute("role") Role role, RedirectAttributes redirectAttributes) {
        roleService.saveRole(role);
        redirectAttributes.addFlashAttribute("successMessage", "Role created successfully!");
        return "redirect:/admin/roles";
    }

    // Show form to edit an existing role
    @GetMapping("/edit/{id}")
    public String showEditRoleForm(@PathVariable("id") int id, Model model) {
        Role role = roleService.getRoleById(id);
        model.addAttribute("role", role);
        return "admin/role/edit_role";
    }

    // Handle the form submission for editing an existing role
    @PostMapping("/edit/{id}")
    public String updateRole(@PathVariable("id") int id, @ModelAttribute("role") Role role, RedirectAttributes redirectAttributes) {
        roleService.updateRole(id, role);
        redirectAttributes.addFlashAttribute("successMessage", "Role updated successfully!");
        return "redirect:/admin/roles";
    }

    // Handle deleting a role
    @GetMapping("/delete/{id}")
    public String deleteRole(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        roleService.deleteRole(id);
        redirectAttributes.addFlashAttribute("successMessage", "Role deleted successfully!");
        return "redirect:/admin/roles";
    }
}

