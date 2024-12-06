package com.goonok.electronicstore.controller;


import com.goonok.electronicstore.dto.EditUserByAdminDto;
import com.goonok.electronicstore.model.User;
import com.goonok.electronicstore.repository.UserRepository;
import com.goonok.electronicstore.services.EditUserByAdminDtoService;
import com.goonok.electronicstore.services.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;



    @Autowired
    private EditUserByAdminDtoService editUserByAdminDtoService;
/*
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private TagService tagService;*/

    // Admin dashboard
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        // Retrieve the list of all users
        model.addAttribute("users", userService.getAllUsers());

        // Retrieve the count of enabled and disabled users
        long enabledUsersCount = userRepository.countByEnabled(true);
        long disabledUsersCount = userRepository.countByEnabled(false);

        // Adding user count statistics to the model
        model.addAttribute("enabledUsersCount", enabledUsersCount);
        model.addAttribute("disabledUsersCount", disabledUsersCount);

        // Separate lists for enabled and disabled users
        List<User> enabledUsers = userService.getEnabledUsers();
        List<User> disabledUsers = userService.getDisabledUsers();

        model.addAttribute("enabledUsers", enabledUsers);
        model.addAttribute("disabledUsers", disabledUsers);

        model.addAttribute("admins", userService.getAdmins());
        model.addAttribute("users", userService.getUsers());



        // count the categories and tags
       /* model.addAttribute("categoriesCount", categoryService.countCategories());
        model.addAttribute("tagsCount", tagService.countTags());*/

        // Check if there's any error message passed from other actions
        model.addAttribute("error", model.containsAttribute("error") ? model.getAttribute("error") : null);

        return "admin/dashboard";
    }


    //get all admins or view all admins
    @GetMapping("/admins")
    public String manageAdmins(Model model) {
        List<User> admins = userService.findAllAdmins();
        model.addAttribute("admins", admins);
        return "admin/admins";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("user", new User());
        return "admin/ad-re-fm";
    }

    // Create a new admin user
    @PostMapping("/create")
    public String createAdmin(@Valid @ModelAttribute("user") User user, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        boolean isAdminExist = userRepository.findByUsername(user.getUsername()).isPresent();
        boolean isEmailExist = userRepository.findByEmail(user.getEmail()).isPresent();
        boolean isPhoneExist = userRepository.findByPhone(user.getPhone()).isPresent();


        if (isAdminExist) {
            bindingResult.rejectValue("username","error.username", "This username is already in use");
        }

        if (isEmailExist) {
            bindingResult.rejectValue("email","error.email", "This email is already in use");
        }

        if (isPhoneExist) {

            bindingResult.rejectValue("phone","error.phone", "This phone is already in use");
        }

        if (bindingResult.hasErrors() || isEmailExist || isPhoneExist || isAdminExist) {
            log.info( "Binding Result Error: " + bindingResult.getAllErrors().toString());
            return "admin/ad-re-fm";
        }

        try {
            userService.createAdmin(user);
            redirectAttributes.addFlashAttribute("successMessage",   user.getUsername() + ", is registered successfully.");
            return "redirect:/admin/admins";
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred during registration. Please try again.");
            return "admin/ad-re-fm";
        }
    }


    //find enable, disable and non verified user + admin list + user list
    @GetMapping("/users")
    public String manageUsers(@RequestParam(required = false) String status, Model model) {

        // Generate a 6-digit random number as a string
        Random random = new Random();
        String randomPassword = String.format("%06d", random.nextInt(1000000));

        // Add randomPassword to the model
        model.addAttribute("randomPassword", randomPassword);

        List<User> users = userService.findAllByStatus(status);
        model.addAttribute("users", users);
        return "admin/users";
    }



    //get user info by admin excluding password and fullName
    @GetMapping("/users/edit/{id}")
    public String editUser(@PathVariable int id, Model model) {
        model.addAttribute("editUserByAdminDto", editUserByAdminDtoService.showEditUserInformationByAdminDto(id));
        return "admin/edit-user";
    }


    // Handle user information Update by the admin
    @PostMapping("/users/update-info")
    public String updateProfile(@Valid @ModelAttribute("editUserByAdminDto") EditUserByAdminDto editUserByAdminDto, BindingResult result, Model model,
                                RedirectAttributes redirectAttributes) {

        // Skip password and other validation if not changing them

        Optional<User> isUserExist = userService.getUserById(editUserByAdminDto.getId());
        if (isUserExist.isPresent()) {
            var user = isUserExist.get();

            // Set the correct user ID
            editUserByAdminDto.setId(user.getId());

            // Check if the email already exists
            if (userRepository.findByEmail(editUserByAdminDto.getEmail()).isPresent() && !editUserByAdminDto.getEmail().equals(user.getEmail())) {
                result.rejectValue("email", "error.user", "This email is already in use");
            }

            // Check if the phone number already exists
            if (userRepository.findByPhone(editUserByAdminDto.getPhone()).isPresent() && !editUserByAdminDto.getPhone().equals(user.getPhone())) {
                result.rejectValue("phone", "error.user", "This phone number is already in use");
            }

            if (result.hasErrors()) {
                log.info("Binding Result Error: " + result.getAllErrors().toString());
                return "admin/edit-user";
            }


            // Save the updated user data
            userService.updateUserInformationByAdmin(editUserByAdminDto);
            redirectAttributes.addFlashAttribute("successMessage",  user.getUsername() + ", updated successfully");
            return "redirect:/admin/users";
        } else {
            model.addAttribute("error", "User not found");
            return "admin/error";
        }
    }

    //without validation
    /*@PostMapping("/users/update")
    public String updateUser(@ModelAttribute("editUserByAdminDto") EditUserByAdminDto editUserByAdminDto) {
        //userService.updateUserInformationByAdmin(editUserByAdminDto);
        return "redirect:/admin/users";
    }*/



   /* @GetMapping("/services")
    public String manageServices(Model model) {
        List<Service> services = serviceService.findAll();
        model.addAttribute("services", services);
        return "admin/services";
    }*/


    /*@GetMapping("/logs")
    public String viewLogs(Model model) {
        List<ActivityLog> logs = logService.findAll();
        model.addAttribute("logs", logs);
        return "admin/logs";
    }*/

    /*@GetMapping("/notifications")
    public String manageNotifications(Model model) {
        List<Notification> notifications = notificationService.findAll();
        model.addAttribute("notifications", notifications);
        return "admin/notifications";
    }*/


    // List enabled users
    @GetMapping("/users/enabled")
    public String listEnabledUsers(Model model) {
        model.addAttribute("users", userService.getEnabledUsers());
        return "admin/users";
    }

    // Enable user by ID
    @PostMapping("/users/enable")
    public String enableUser(@RequestParam("userId") Integer userId, Model model) {
        Optional<User> userOptional = userService.getUserById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setEnabled(true);
            userRepository.save(user);
            return "redirect:/admin/users";
        } else {
            model.addAttribute("error", "User not found.");
            return "admin/users"; // Returning to the users page with error
        }
    }

    // List disabled users
    @GetMapping("/users/disabled")
    public String listDisabledUsers(Model model) {
        model.addAttribute("users", userService.getDisabledUsers());
        return "admin/users";
    }

    // Disable user by ID
    @PostMapping("/users/disable")
    public String disableUserById(@RequestParam("userId") Integer userId) {
        userService.updateUserStatus(userId, false);
        return "redirect:/admin/users";
    }

    // Disable admin by ID
    @PostMapping("/admins/disable")
    public String disableAdminById(@RequestParam("userId") Integer userId) {
        userService.updateUserStatus(userId, false);
        return "redirect:/admin/admins";
    }


    // Approve user by ID
    @PostMapping("/users/approve")
    public String approveUser(@RequestParam("userId") Integer userId) {
        userService.updateUserStatus(userId, true);
        return "redirect:/admin/users";
    }

    // Approve user by ID
    @PostMapping("/admins/approve")
    public String approveAdmin(@RequestParam("userId") Integer userId) {
        userService.updateUserStatus(userId, true);
        return "redirect:/admin/admins";
    }

    // Approve user by ID (via direct link)
    @GetMapping("/approve/{userId}")
    public String approveUserById(@PathVariable Integer userId, Model model) {
        try {
            userService.approveUser(userId);
            return "redirect:/admin/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to approve user: " + e.getMessage());
            return "admin/dashboard"; // Returning to the dashboard with error
        }
    }

    // Approve user by ID (via form submission)
    @PostMapping("/users/approves")
    public String approveUserViaForm(@RequestParam("userId") Integer userId, Model model) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setEnabled(true);
            userRepository.save(user);
            return "redirect:/admin/users";
        } else {
            model.addAttribute("error", "User not found.");
            return "admin/users"; // Returning to the users page with error
        }
    }



    // Edit user password
    @PostMapping("/users/edit-password")
    public String editUserPassword(@RequestParam("userId") Integer userId, @RequestParam("password") String password, RedirectAttributes redirectAttributes) {

        //here i can send the new generated password to the users email or phone
        //emailService logic goes here
        userService.updateUserPassword(userId, password);
        redirectAttributes.addFlashAttribute("successMessage", userService.getUserById(userId).get().getUsername() +", Password updated successfully");

        return "redirect:/admin/users";
    }


}
