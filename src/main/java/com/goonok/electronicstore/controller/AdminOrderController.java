package com.goonok.electronicstore.controller;

import com.goonok.electronicstore.dto.AddressDto;
import com.goonok.electronicstore.dto.OrderDto;
import com.goonok.electronicstore.enums.OrderStatus;
import com.goonok.electronicstore.enums.PaymentStatus;
import com.goonok.electronicstore.exception.ResourceNotFoundException;
import com.goonok.electronicstore.service.interfaces.OrderService;
import com.goonok.electronicstore.service.interfaces.UserService;
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

import java.util.Arrays;

@Slf4j
@Controller
@RequestMapping("/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private UserService userService;

    /**
     * Displays the list of all orders with pagination and optional status filter.
     */
    @GetMapping
    public String listAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "orderDate,desc") String sort,
            @RequestParam(required = false) OrderStatus status, // Optional status filter
            Model model) {

        log.info("Admin request received for orders list - Page: {}, Size: {}, Sort: {}, Status: {}", page, size, sort, status);

        String[] sortParams = sort.split(",");
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc")) ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortParams[0]
        ));

        try {
            Page<OrderDto> orderPage;
            // Call appropriate service method based on whether status filter is present
            if (status != null) {
                orderPage = orderService.getAllOrders(status, pageable);
            } else {
                // Use the method that fetches all orders (ensure correct sorting)
                // Assuming getAllOrders(pageable) uses the default sort from pageable
                // Or call a specific sorted fetch like findAllByOrderByOrderDateDesc if implemented
                orderPage = orderService.getAllOrders(pageable); // Or specific sorted fetch
            }

            model.addAttribute("orderPage", orderPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("sort", sort);
            model.addAttribute("selectedStatus", status); // Pass selected status back to view
            model.addAttribute("orderStatuses", Arrays.asList(OrderStatus.values())); // For filter dropdown
            model.addAttribute("pageTitle", "Manage Orders" + (status != null ? " (" + status + ")" : ""));

        } catch (Exception e) {
            log.error("Error fetching orders for admin view", e);
            model.addAttribute("errorMessage", "Could not load orders.");
            model.addAttribute("orderPage", Page.empty(pageable));
            model.addAttribute("orderStatuses", Arrays.asList(OrderStatus.values())); // Still needed for dropdown
        }

        return "admin/order-list";
    }

    /**
     * Displays the details of a specific order for the admin.
     */
    @GetMapping("/{orderId}")
    public String showAdminOrderDetails(@PathVariable Long orderId, Model model, RedirectAttributes redirectAttributes) {
    try {
        OrderDto orderDetails = orderService.getOrderById(orderId);
        // Add billing address if available
        AddressDto billingAddress = userService.getDefaultBillingAddress(orderDetails.getUserEmail());
        
        model.addAttribute("order", orderDetails);
        model.addAttribute("billingAddress", billingAddress);
        model.addAttribute("pageTitle", "Order Details - " + orderDetails.getOrderNumber());
        model.addAttribute("orderStatuses", Arrays.asList(OrderStatus.values()));
        model.addAttribute("paymentStatuses", Arrays.asList(PaymentStatus.values())); // Add this line
    } catch (ResourceNotFoundException e) {
        return "redirect:/admin/orders";
    }
    return "admin/order-details";
}

    /**
     * Handles updating the status of an order.
     */
    @PostMapping("/update-status/{orderId}")
    public String updateOrderStatus(@PathVariable Long orderId,
                                    @RequestParam("newStatus") OrderStatus newStatus,
                                    RedirectAttributes redirectAttributes) {
        // ... (code remains the same) ...
        log.info("Admin attempting to update order ID {} status to {}", orderId, newStatus);
        try {
            orderService.updateOrderStatus(orderId, newStatus);
            redirectAttributes.addFlashAttribute("successMessage", "Order #" + orderId + " status updated to " + newStatus + ".");
        } catch (ResourceNotFoundException e) { /* ... error handling ... */ return "redirect:/admin/orders"; }
        catch (Exception e) { /* ... error handling ... */ return "redirect:/admin/orders/" + orderId; }
        return "redirect:/admin/orders/" + orderId;
    }


    /**
     * Handles cancelling an order. (Basic implementation)
     */
    @PostMapping("/cancel/{orderId}")
    public String cancelOrder(@PathVariable Long orderId, RedirectAttributes redirectAttributes) {
        log.warn("Admin attempting to cancel order ID {}", orderId);
        try {
            // Call the specific cancel method in the service
            orderService.cancelOrder(orderId); // Assumes cancelOrder sets status to CANCELLED
            redirectAttributes.addFlashAttribute("successMessage", "Order #" + orderId + " has been cancelled.");
        } catch (ResourceNotFoundException e) {
            log.error("Order ID {} not found for cancellation", orderId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Order not found with ID: " + orderId);
            return "redirect:/admin/orders";
        } catch (IllegalArgumentException e) { // Catch if already delivered/cancelled
            log.error("Cannot cancel order ID {}: {}", orderId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot cancel order: " + e.getMessage());
            return "redirect:/admin/orders/" + orderId; // Go back to details
        } catch (Exception e) {
            log.error("Error cancelling order ID {}: {}", orderId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while cancelling the order.");
            return "redirect:/admin/orders/" + orderId;
        }
        // Redirect back to the details page after successful cancellation
        return "redirect:/admin/orders/" + orderId;
    }


    /**
     * Handles updating the payment details (status, transaction ID) of an order.
     */
    @PostMapping("/update-payment/{orderId}")
    public String updatePaymentDetails(@PathVariable Long orderId,
                                       @RequestParam("paymentStatus") String paymentStatus, // Receive as String
                                       @RequestParam(required = false) String transactionId, // Optional
                                       RedirectAttributes redirectAttributes) {
        log.info("Admin attempting to update payment details for order ID {}. Status: {}, TxnID: {}", orderId, paymentStatus, transactionId);
        try {
            // Service method takes String for paymentStatus
            orderService.updatePaymentDetails(orderId, paymentStatus, transactionId);
            redirectAttributes.addFlashAttribute("successMessage", "Payment details for Order #" + orderId + " updated successfully.");
        } catch (ResourceNotFoundException e) {
            log.error("Order ID {} not found for payment update", orderId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Order not found with ID: " + orderId);
            return "redirect:/admin/orders"; // Redirect to list if order doesn't exist
        } catch (IllegalArgumentException e) { // Catch validation errors from service
            log.error("Invalid payment details for order ID {}: {}", orderId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot update payment details: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error updating payment details for order ID {}: {}", orderId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while updating payment details.");
        }
        // Redirect back to the details page
        return "redirect:/admin/orders/" + orderId;
    }

}