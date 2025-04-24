package com.goonok.electronicstore.repository;

import com.goonok.electronicstore.model.Product;
import com.goonok.electronicstore.model.ShoppingCartItem;
import com.goonok.electronicstore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShoppingCartItemRepository extends JpaRepository<ShoppingCartItem, Long> {

    Optional<ShoppingCartItem> findByUser_UserId(Long userId);

    void deleteByUser_UserId(Long userId);
    void deleteByUser_UserIdAndProduct_ProductId(Long userId, Long productId);

    Optional<ShoppingCartItem> findByUser_UserIdAndProduct_ProductId(Long userId, Long productId);

    int countByUser(User user);

    public void deleteByProduct_ProductId(Long productId);

    Optional<ShoppingCartItem> findByUserAndProduct_ProductId(User user, Long productId);

    /**
     * Finds all cart items belonging to a specific user.
     * @param user The user entity.
     * @return A list of ShoppingCartItem entities.
     */
    List<ShoppingCartItem> findByUser(User user);

    /**
     * Finds a specific cart item by its ID, ensuring it belongs to the specified user.
     * @param cartItemId The ID of the cart item.
     * @param user The user entity.
     * @return An Optional containing the ShoppingCartItem if found and owned by the user.
     */
    Optional<ShoppingCartItem> findByCartItemIdAndUser(Long cartItemId, User user);

    /**
     * Finds a cart item for a specific user and a specific product.
     * Useful for checking if a product is already in the cart before adding.
     * @param user The user entity.
     * @param product The product entity.
     * @return An Optional containing the ShoppingCartItem if the user already has this product in their cart.
     */
    Optional<ShoppingCartItem> findByUserAndProduct(User user, Product product);

    /**
     * Deletes all cart items belonging to a specific user.
     * Useful for clearing the cart after checkout or manually.
     * @param user The user entity.
     * @return The number of cart items deleted.
     */
    long deleteByUser(User user); // Spring Data JPA provides deleteBy... methods
}

