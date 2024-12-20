package com.goonok.electronicstore.repository;

import com.goonok.electronicstore.model.Product;
import com.goonok.electronicstore.model.ShoppingCart;
import com.goonok.electronicstore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
    List<ShoppingCart> findByUser_UserId(Long userId);
    List<ShoppingCart> findByUser_UserIdAndProduct_ProductId(Long userId, Long productId);
    void deleteByUser_UserId(Long userId);
    void deleteByUser_UserIdAndProduct_ProductId(Long userId, Long productId);


    int countByUser(User user);

    public void deleteByProduct_ProductId(Long productId);

    // For registered users
    List<ShoppingCart> findByUser(User user);

    Optional<ShoppingCart> findByUserAndProduct(User user, Product product);

    // For guest users
    List<ShoppingCart> findBySessionId(String sessionId);

    Optional<ShoppingCart> findBySessionIdAndProduct(String sessionId, Product product);

    void deleteBySessionId(String sessionId);
}

