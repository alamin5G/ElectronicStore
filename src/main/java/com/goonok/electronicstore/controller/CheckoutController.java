package com.goonok.electronicstore.controller;

import com.goonok.electronicstore.dto.AddressDto;
import com.goonok.electronicstore.dto.CartDto;
import com.goonok.electronicstore.dto.CheckoutDto;
import com.goonok.electronicstore.dto.OrderDto; // Import OrderDto
import com.goonok.electronicstore.exception.ResourceNotFoundException; // Import
import com.goonok.electronicstore.service.CartService;
import com.goonok.electronicstore.service.interfaces.OrderService; // Import OrderService
import com.goonok.electronicstore.service.interfaces.UserService;
import jakarta.servlet.http.HttpSession; // Import HttpSession
import jakarta.validation.constraints.NotEmpty; // Import for validation
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated; // Import for validating request params
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus; // Import for clearing session attribute
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal; // Import BigDecimal
import java.util.Arrays; // Import for list of payment methods
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/checkout")
@SessionAttributes("checkoutDto") // Manage checkoutDto in session
@Validated // Needed for validating RequestParams like paymentMethod
public class CheckoutController {

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService; // Inject OrderService

    // Session attribute name constant
    private static final String CHECKOUT_DTO_ATTR = "checkoutDto";

    // Define available payment methods
    private static final List<String> AVAILABLE_PAYMENT_METHODS = Arrays.asList("COD", "BKASH", "NAGAD");

    @ModelAttribute(CHECKOUT_DTO_ATTR)
    public CheckoutDto initializeCheckoutDto() {
        log.debug("Initializing/Retrieving CheckoutDto in session.");
        return new CheckoutDto();
    }

    // === Step 1: Shipping Address ===
    @GetMapping("/shipping")
    public String showShippingAddressSelection(Model model, Authentication authentication,
                                               @ModelAttribute(CHECKOUT_DTO_ATTR) CheckoutDto checkoutDto,
                                               RedirectAttributes redirectAttributes) {
        // ... (code remains the same) ...
        if (authentication == null || !authentication.isAuthenticated()) { return "redirect:/login?redirect=/checkout/shipping"; }
        String username = authentication.getName();
        log.info("User {} starting checkout - Step 1: Shipping Address", username);
        try {
            CartDto cart = cartService.getCartForUser(username);
            if (cart.getItems() == null || cart.getItems().isEmpty()) {
                log.warn("User {} attempted checkout with an empty cart.", username);
                redirectAttributes.addFlashAttribute("errorMessage", "Your cart is empty. Please add items before checking out.");
                return "redirect:/cart";
            }
            // --- Store cart snapshot in session DTO ---
            checkoutDto.setCheckoutCart(cart); // Store the cart state
            // ------------------------------------------
            model.addAttribute("cartTotal", cart.getCartTotalPrice());
        } catch (Exception e) {
            log.error("Error fetching cart for user {} during checkout", username, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Could not retrieve cart details.");
            return "redirect:/cart";
        }
        try {
            List<AddressDto> addresses = userService.getAddressesForUser(username);
            model.addAttribute("addresses", addresses);
            model.addAttribute("selectedAddressId", checkoutDto.getSelectedShippingAddress() != null ? checkoutDto.getSelectedShippingAddress().getAddressId() : null);
        } catch (Exception e) {
            log.error("Error fetching addresses for user {} during checkout", username, e);
            model.addAttribute("errorMessage", "Could not load addresses.");
            model.addAttribute("addresses", List.of());
        }
        model.addAttribute("pageTitle", "Checkout - Shipping Address");
        return "checkout/shipping-address";
    }

    @PostMapping("/shipping/select")
    public String processShippingAddressSelection(@RequestParam("selectedAddressId") Long selectedAddressId,
                                                  @ModelAttribute(CHECKOUT_DTO_ATTR) CheckoutDto checkoutDto,
                                                  Authentication authentication,
                                                  RedirectAttributes redirectAttributes,
                                                  HttpSession session) {
        // ... (code remains the same) ...
        if (authentication == null || !authentication.isAuthenticated()) { return "redirect:/login"; }
        String username = authentication.getName();
        log.info("User {} selected shipping address ID: {}", username, selectedAddressId);
        try {
            AddressDto selectedAddress = userService.getAddressByIdAndUser(selectedAddressId, username);
            checkoutDto.setSelectedShippingAddress(selectedAddress);
            // --- Calculate and store shipping cost (example) ---
            checkoutDto.setSelectedShippingMethod("Standard Delivery"); // Example method
            checkoutDto.setShippingCost(calculateShippingCost(selectedAddress)); // Store calculated cost
            // -------------------------------------------------
            session.setAttribute(CHECKOUT_DTO_ATTR, checkoutDto);
            log.info("Shipping address and cost set for checkout for user {}", username);
            return "redirect:/checkout/payment";
        } catch (ResourceNotFoundException e) {
            log.error("Selected address ID {} not found or does not belong to user {}", selectedAddressId, username, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid address selected. Please choose from your saved addresses.");
            return "redirect:/checkout/shipping";
        } catch (Exception e) {
            log.error("Error processing selected shipping address for user {}: {}", username, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred. Please try again.");
            return "redirect:/checkout/shipping";
        }
    }

    // === Step 2: Payment Method ===
    @GetMapping("/payment")
    public String showPaymentMethodSelection(Model model, Authentication authentication,
                                             @ModelAttribute(CHECKOUT_DTO_ATTR) CheckoutDto checkoutDto,
                                             RedirectAttributes redirectAttributes) {
        // ... (code remains the same) ...
        if (authentication == null || !authentication.isAuthenticated()) { return "redirect:/login?redirect=/checkout/payment"; }
        String username = authentication.getName();
        log.info("User {} checkout - Step 2: Payment Method", username);
        if (checkoutDto.getSelectedShippingAddress() == null) {
            log.warn("User {} accessed payment step without selecting shipping address.", username);
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a shipping address first.");
            return "redirect:/checkout/shipping";
        }
        model.addAttribute("paymentMethods", AVAILABLE_PAYMENT_METHODS);
        model.addAttribute("pageTitle", "Checkout - Payment Method");
        return "checkout/payment-method";
    }

    @PostMapping("/payment/select")
    public String processPaymentMethodSelection(
            @RequestParam @NotEmpty(message = "Please select a payment method.") String selectedPaymentMethod,
            @ModelAttribute(CHECKOUT_DTO_ATTR) CheckoutDto checkoutDto,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        // ... (code remains the same) ...
        if (authentication == null || !authentication.isAuthenticated()) { return "redirect:/login"; }
        String username = authentication.getName();
        log.info("User {} selected payment method: {}", username, selectedPaymentMethod);
        if (!AVAILABLE_PAYMENT_METHODS.contains(selectedPaymentMethod)) {
            log.warn("User {} selected an invalid payment method: {}", username, selectedPaymentMethod);
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid payment method selected.");
            return "redirect:/checkout/payment";
        }
        if (checkoutDto.getSelectedShippingAddress() == null) {
            log.warn("User {} attempted to set payment method without shipping address.", username);
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a shipping address first.");
            return "redirect:/checkout/shipping";
        }
        checkoutDto.setSelectedPaymentMethod(selectedPaymentMethod);
        session.setAttribute(CHECKOUT_DTO_ATTR, checkoutDto);
        log.info("Payment method set to {} for checkout for user {}", selectedPaymentMethod, username);
        return "redirect:/checkout/review";
    }

    // === Step 3: Order Review ===

    /**
     * Step 3: Display Order Review Page.
     * Requires authentication and completed previous steps.
     */
    @GetMapping("/review")
    public String showOrderReview(Model model, Authentication authentication,
                                  @ModelAttribute(CHECKOUT_DTO_ATTR) CheckoutDto checkoutDto,
                                  RedirectAttributes redirectAttributes) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login?redirect=/checkout/review";
        }
        String username = authentication.getName();
        log.info("User {} checkout - Step 3: Order Review", username);

        // Ensure previous steps are completed
        if (checkoutDto.getSelectedShippingAddress() == null) {
            log.warn("User {} accessed review step without selecting shipping address.", username);
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a shipping address first.");
            return "redirect:/checkout/shipping";
        }
        if (checkoutDto.getSelectedPaymentMethod() == null) {
            log.warn("User {} accessed review step without selecting payment method.", username);
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a payment method first.");
            return "redirect:/checkout/payment";
        }
        // Ensure cart details are present (either from snapshot or fetch again)
        if (checkoutDto.getCheckoutCart() == null) {
            log.warn("Cart details missing in checkout session for user {}. Refetching.", username);
            try {
                CartDto cart = cartService.getCartForUser(username);
                if (cart.getItems() == null || cart.getItems().isEmpty()) {
                    return "redirect:/cart"; // Redirect if cart became empty
                }
                checkoutDto.setCheckoutCart(cart); // Store fresh cart details
            } catch (Exception e) {
                log.error("Error fetching cart for review step for user {}", username, e);
                redirectAttributes.addFlashAttribute("errorMessage", "Could not retrieve cart details.");
                return "redirect:/cart";
            }
        }

        // Calculate final totals (Subtotal + Shipping + Tax)
        BigDecimal subtotal = checkoutDto.getCheckoutCart().getCartTotalPrice();
        BigDecimal shipping = checkoutDto.getShippingCost() != null ? checkoutDto.getShippingCost() : BigDecimal.ZERO;
        BigDecimal tax = calculateTax(subtotal); // Example tax calculation
        BigDecimal finalTotal = subtotal.add(shipping).add(tax);

        model.addAttribute("checkout", checkoutDto); // Pass the DTO with selected address/payment
        model.addAttribute("cart", checkoutDto.getCheckoutCart()); // Pass the cart details
        model.addAttribute("shippingCostDisplay", shipping);
        model.addAttribute("taxAmountDisplay", tax);
        model.addAttribute("finalTotalAmount", finalTotal);
        model.addAttribute("pageTitle", "Checkout - Review Order");

        return "checkout/order-review"; // e.g., templates/checkout/order-review.html
    }

    // === Step 4: Place Order ===

    /**
     * Step 4 (POST): Place the order.
     * Requires authentication and completed checkout DTO.
     */
    @PostMapping("/place-order")
    public String placeOrder(Authentication authentication,
                             @ModelAttribute(CHECKOUT_DTO_ATTR) CheckoutDto checkoutDto,
                             RedirectAttributes redirectAttributes,
                             SessionStatus sessionStatus) { // Inject SessionStatus to clear session attribute

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String username = authentication.getName();
        log.info("User {} attempting to place order.", username);

        // Final validation before placing order
        if (checkoutDto.getSelectedShippingAddress() == null || checkoutDto.getSelectedPaymentMethod() == null || checkoutDto.getCheckoutCart() == null || checkoutDto.getCheckoutCart().getItems().isEmpty()) {
            log.error("Checkout state incomplete for user {} during place order attempt.", username);
            redirectAttributes.addFlashAttribute("errorMessage", "Checkout process incomplete. Please start again.");
            return "redirect:/checkout/shipping"; // Go back to first step
        }

        try {
            // Call the OrderService to create the order
            OrderDto placedOrder = orderService.placeOrder(username, checkoutDto);

            // Clear the checkout DTO from the session upon successful order placement
            sessionStatus.setComplete();
            log.info("Checkout session cleared for user {}", username);

            // Redirect to order confirmation page with order ID/number
            redirectAttributes.addFlashAttribute("successMessage", "Order placed successfully! Your order number is " + placedOrder.getOrderNumber());
            return "redirect:/order/confirmation/" + placedOrder.getOrderId(); // TODO: Create OrderController and confirmation page

        } catch (IllegalStateException | IllegalArgumentException | ResourceNotFoundException e) {
            // Handle specific errors like stock issues, invalid data
            log.error("Error placing order for user {}: {}", username, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Could not place order: " + e.getMessage());
            // Redirect back to review page or cart? Review might be better.
            return "redirect:/checkout/review";
        } catch (Exception e) {
            log.error("Unexpected error placing order for user {}: {}", username, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred while placing your order. Please try again or contact support.");
            return "redirect:/checkout/review";
        }
    }


    // === Cancel Checkout ===
    @GetMapping("/cancel")
    public String cancelCheckout(SessionStatus status, Authentication authentication) {
        String username = (authentication != null) ? authentication.getName() : "Unknown user";
        log.warn("User {} cancelled checkout process.", username);
        status.setComplete(); // Clears the session attribute managed by @SessionAttributes
        return "redirect:/cart";
    }

    // --- Helper Methods ---
    // Replace with actual logic
    private BigDecimal calculateShippingCost(AddressDto shippingAddress) {
        log.warn("SHIPPING COST CALCULATION NOT IMPLEMENTED - Returning default 50");
        return new BigDecimal("50.00"); // Placeholder
    }
    private BigDecimal calculateTax(BigDecimal subtotal) {
        log.warn("TAX CALCULATION NOT IMPLEMENTED - Returning default 0");
        return BigDecimal.ZERO; // Placeholder
    }
}
