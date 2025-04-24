package com.goonok.electronicstore.service;

import com.goonok.electronicstore.dto.CartDto;
import com.goonok.electronicstore.dto.CartItemDto;
import com.goonok.electronicstore.exception.ResourceNotFoundException;
import com.goonok.electronicstore.model.Product;
import com.goonok.electronicstore.model.ShoppingCartItem;
import com.goonok.electronicstore.model.User;
import com.goonok.electronicstore.repository.ProductRepository;
import com.goonok.electronicstore.repository.ShoppingCartItemRepository;
import com.goonok.electronicstore.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime; // Added for timestamp logic
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CartService {

    @Autowired
    private ShoppingCartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ModelMapper modelMapper;

    /**
     * Adds an item to the user's shopping cart or updates the quantity if it already exists.
     *
     * @param userEmail The email of the logged-in user.
     * @param productId The ID of the product to add.
     * @param quantity  The quantity to add.
     * @return The updated CartDto.
     * @throws ResourceNotFoundException if user or product not found.
     * @throws IllegalArgumentException if requested quantity exceeds stock or is invalid.
     */
    @Transactional
    public CartDto addItemToCart(String userEmail, Long productId, int quantity) {
        log.info("Attempting to add product ID {} quantity {} for user {}", productId, quantity, userEmail);

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "ID", productId));

        // Check stock
        if (product.getStockQuantity() == null || product.getStockQuantity() < quantity) {
            throw new IllegalArgumentException("Insufficient stock for product: " + product.getName() + ". Available: " + product.getStockQuantity());
        }
        if (!product.isInStock()) {
            throw new IllegalArgumentException("Product '" + product.getName() + "' is currently out of stock.");
        }


        // Check if the product is already in the user's cart
        Optional<ShoppingCartItem> existingCartItemOpt = cartItemRepository.findByUserAndProduct(user, product);

        ShoppingCartItem cartItem;
        if (existingCartItemOpt.isPresent()) {
            // Update quantity of existing item
            cartItem = existingCartItemOpt.get();
            int newQuantity = cartItem.getQuantity() + quantity;
            // Re-check stock for the total quantity
            if (product.getStockQuantity() < newQuantity) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName() + ". Requested total: " + newQuantity + ", Available: " + product.getStockQuantity());
            }
            cartItem.setQuantity(newQuantity);
            // cartItem.setUpdatedAt(LocalDateTime.now()); // Update timestamp if entity has it
            log.info("Updated quantity for existing cart item ID {}", cartItem.getCartItemId());
        } else {
            // Create a new cart item
            cartItem = new ShoppingCartItem();
            cartItem.setUser(user);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setPricePerItem(product.getPrice()); // Store price at time of adding
            // cartItem.setCreatedAt(LocalDateTime.now()); // Set timestamp if entity has it
            // cartItem.setUpdatedAt(LocalDateTime.now());
            log.info("Creating new cart item for user ID {} and product ID {}", user.getUserId(), productId);
        }

        cartItemRepository.save(cartItem);

        // Return the updated cart view
        return getCartForUser(userEmail);
    }

    /**
     * Retrieves the user's current shopping cart.
     *
     * @param userEmail The email of the logged-in user.
     * @return The CartDto representing the user's cart.
     * @throws ResourceNotFoundException if the user is not found.
     */
    @Transactional(readOnly = true)
    public CartDto getCartForUser(String userEmail) {
        log.debug("Fetching cart for user: {}", userEmail);
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        List<ShoppingCartItem> items = cartItemRepository.findByUser(user);
        CartDto cartDto = new CartDto();
        cartDto.setUserId(user.getUserId());

        // Map items to DTOs first
        List<CartItemDto> itemDtos = items.stream().map(item -> {
            CartItemDto itemDto = new CartItemDto();
            itemDto.setCartItemId(item.getCartItemId());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setPricePerItem(item.getPricePerItem());
            itemDto.setProductStockQuantity(item.getProduct().getStockQuantity());

            // Calculate item total price
            BigDecimal itemTotal = item.getPricePerItem().multiply(BigDecimal.valueOf(item.getQuantity()));
            itemDto.setItemTotalPrice(itemTotal);

            // Add product details needed for display
            if (item.getProduct() != null) {
                itemDto.setProductId(item.getProduct().getProductId());
                itemDto.setProductName(item.getProduct().getName());
                itemDto.setProductImagePath(item.getProduct().getImagePath());
            } else {
                itemDto.setProductName("Product Unavailable");
            }

            return itemDto;
        }).collect(Collectors.toList());

        // Calculate total price AFTER collecting the DTOs
        BigDecimal totalCartPrice = itemDtos.stream()
                .map(CartItemDto::getItemTotalPrice) // Get the pre-calculated total for each item
                .reduce(BigDecimal.ZERO, BigDecimal::add); // Sum them up

        cartDto.setItems(itemDtos);
        cartDto.setCartTotalPrice(totalCartPrice);

        log.debug("Cart fetched for user {}: {} items, Total Price: {}", userEmail, itemDtos.size(), totalCartPrice);
        return cartDto;
    }

    /**
     * Updates the quantity of a specific item in the user's cart.
     *
     * @param userEmail  The email of the logged-in user.
     * @param cartItemId The ID of the cart item to update.
     * @param newQuantity The new quantity (must be > 0).
     * @return The updated CartDto.
     * @throws ResourceNotFoundException if user or cart item not found/doesn't belong to user.
     * @throws IllegalArgumentException if quantity is invalid or exceeds stock.
     */
    @Transactional
    public CartDto updateCartItemQuantity(String userEmail, Long cartItemId, int newQuantity) {
        log.info("Attempting to update quantity for cart item ID {} to {} for user {}", cartItemId, newQuantity, userEmail);

        if (newQuantity <= 0) {
            log.warn("Quantity is zero or less for cart item ID {}, removing item.", cartItemId);
            return removeCartItem(userEmail, cartItemId);
        }

        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));
        ShoppingCartItem cartItem = cartItemRepository.findByCartItemIdAndUser(cartItemId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart Item", "ID", cartItemId + " for user " + userEmail));

        Product product = cartItem.getProduct();
        if (product == null) {
            log.error("Product associated with cart item ID {} not found.", cartItemId);
            cartItemRepository.delete(cartItem);
            throw new ResourceNotFoundException("Product", "associated with cart item", cartItemId);
        }

        // Check stock
        if (product.getStockQuantity() == null || product.getStockQuantity() < newQuantity) {
            throw new IllegalArgumentException("Insufficient stock for product: " + product.getName() + ". Requested: " + newQuantity + ", Available: " + product.getStockQuantity());
        }

        cartItem.setQuantity(newQuantity);
        cartItemRepository.save(cartItem);
        log.info("Quantity updated for cart item ID {}", cartItemId);

        return getCartForUser(userEmail);
    }

    /**
     * Removes a specific item from the user's cart.
     *
     * @param userEmail  The email of the logged-in user.
     * @param cartItemId The ID of the cart item to remove.
     * @return The updated CartDto.
     * @throws ResourceNotFoundException if user or cart item not found/doesn't belong to user.
     */
    @Transactional
    public CartDto removeCartItem(String userEmail, Long cartItemId) {
        log.warn("Attempting to remove cart item ID {} for user {}", cartItemId, userEmail);
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));
        ShoppingCartItem cartItem = cartItemRepository.findByCartItemIdAndUser(cartItemId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart Item", "ID", cartItemId + " for user " + userEmail));

        cartItemRepository.delete(cartItem);
        log.warn("Removed cart item ID {}", cartItemId);

        return getCartForUser(userEmail);
    }

    /**
     * Clears all items from the user's shopping cart.
     *
     * @param userEmail The email of the logged-in user.
     * @throws ResourceNotFoundException if the user is not found.
     */
    @Transactional
    public void clearCart(String userEmail) {
        log.warn("Attempting to clear cart for user {}", userEmail);
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        long deletedCount = cartItemRepository.deleteByUser(user);
        log.warn("Cleared {} items from cart for user {}", deletedCount, userEmail);
    }
}
