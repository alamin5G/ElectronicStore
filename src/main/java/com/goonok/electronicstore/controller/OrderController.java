package com.goonok.electronicstore.controller;

import com.goonok.electronicstore.dto.AddressDto;
import com.goonok.electronicstore.dto.OrderDto;
import com.goonok.electronicstore.dto.CheckoutDto;
import com.goonok.electronicstore.enums.OrderStatus;
import com.goonok.electronicstore.exception.AccessDeniedException;
import com.goonok.electronicstore.dto.PaymentDetailsSubmissionDto;
import com.goonok.electronicstore.dto.PaymentSubmissionResponse;
import com.goonok.electronicstore.exception.AccessDeniedException;
import com.goonok.electronicstore.exception.ResourceNotFoundException;
import com.goonok.electronicstore.service.interfaces.OrderService;
import com.goonok.electronicstore.service.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page; // Import Page
import org.springframework.data.domain.PageRequest; // Import PageRequest
import org.springframework.data.domain.Pageable; // Import Pageable
import org.springframework.data.domain.Sort; // Import Sort
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserService userService; // Add UserService to fetch addresses

    @GetMapping("/confirmation/{orderId}")
    public String showOrderConfirmation(@PathVariable Long orderId, Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String username = authentication.getName();
        log.info("Showing order confirmation for order ID {} for user {}", orderId, username);

        try {
            // Get order details
            OrderDto orderDetails = orderService.getOrderByIdForUser(username, orderId);
            
            // Fetch user's addresses
            List<AddressDto> userAddresses = userService.getAddressesForUser(username);
            
            // Find default billing address
            AddressDto billingAddress = userAddresses.stream()
                .filter(AddressDto::isDefaultBilling)
                .findFirst()
                .orElse(null);

            // If no default billing address is found, try to find default shipping address
            if (billingAddress == null) {
                billingAddress = userAddresses.stream()
                    .filter(AddressDto::isDefaultShipping)
                    .findFirst()
                    .orElse(null);
            }

            // Add both order and billing details to model
            model.addAttribute("order", orderDetails);
            model.addAttribute("billingAddress", billingAddress);
            model.addAttribute("pageTitle", "Order Confirmation - " + orderDetails.getOrderNumber());

        } catch (ResourceNotFoundException e) {
            log.error("Order ID {} not found or does not belong to user {} for confirmation", orderId, username, e);
            model.addAttribute("errorMessage", "Order details could not be found.");
            return "error/404";
        } catch (Exception e) {
            log.error("Error retrieving order ID {} for confirmation for user {}", orderId, username, e);
            model.addAttribute("errorMessage", "An error occurred while retrieving your order details.");
            return "error/generic-error";
        }
        
        return "order/order-confirmation";
    }

    /**
     * Displays the user's order history with pagination.
     * Requires authentication.
     */
    @GetMapping("/history") // Changed from "/orders" to "/history" under /order mapping
    public String showOrderHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, // Number of orders per page
            Model model, Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String username = authentication.getName();
        log.info("Fetching order history for user {} - Page: {}, Size: {}", username, page, size);

        // Always sort by order date descending
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());

        try {
            Page<OrderDto> orderPage = orderService.getOrdersForUser(username, pageable);
            model.addAttribute("orderPage", orderPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("pageTitle", "My Order History");

        } catch (ResourceNotFoundException e) {
            log.error("User not found when fetching order history: {}", username, e);
            model.addAttribute("errorMessage", "Could not load order history: User not found.");
            // Or redirect to profile?
        } catch (Exception e) {
            log.error("Error retrieving order history for user {}: {}", username, e.getMessage(), e);
            model.addAttribute("errorMessage", "An error occurred while retrieving your order history.");
        }

        return "order/order-history"; // e.g., templates/order/order-history.html
    }

    /**
     * Displays the details of a specific past order for the logged-in user.
     * Requires authentication.
     */
    @GetMapping("/{orderId}") // View details of a specific order
    public String showOrderDetails(@PathVariable Long orderId, Model model, Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String username = authentication.getName();
        log.info("Showing order details for order ID {} for user {}", orderId, username);

        try {
            // Service method already checks if order belongs to user
            OrderDto orderDetails = orderService.getOrderByIdForUser(username, orderId);
            model.addAttribute("order", orderDetails);
            model.addAttribute("pageTitle", "Order Details - " + orderDetails.getOrderNumber());

        } catch (ResourceNotFoundException e) {
            log.error("Order ID {} not found or does not belong to user {} for viewing details", orderId, username, e);
            model.addAttribute("errorMessage", "Order details could not be found or you do not have permission to view it.");
            return "error/404"; // Or redirect:/order/history?error=notFound
        } catch (Exception e) {
            log.error("Error retrieving order ID {} details for user {}", orderId, username, e);
            model.addAttribute("errorMessage", "An error occurred while retrieving your order details.");
            return "error/generic-error"; // Or redirect:/order/history?error=retrievalFailed
        }

        return "order/order-details"; // e.g., templates/order/order-details.html
    }

    @PostMapping("/submit-payment/{orderId}")
    public String submitPaymentDetails(
            @PathVariable Long orderId,
            @Valid @ModelAttribute PaymentDetailsSubmissionDto paymentDetails,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String username = authentication.getName();
        log.info("User {} submitting payment details for order {}", username, orderId);

        try {
            // Verify order belongs to current user and submit payment
            PaymentSubmissionResponse response = orderService.submitPaymentDetails(orderId, paymentDetails);

            if (response.isSuccess()) {
                redirectAttributes.addFlashAttribute("successMessage", response.getMessage());
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", response.getMessage());
            }

        } catch (AccessDeniedException e) {
            log.error("Unauthorized payment submission attempt for order {} by user {}", orderId, username);
            redirectAttributes.addFlashAttribute("errorMessage", "Not authorized to submit payment for this order");
        } catch (Exception e) {
            log.error("Error processing payment submission for order {} by user {}: {}", orderId, username, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while processing your payment submission");
        }

        return "redirect:/order/" + orderId;
    }

}