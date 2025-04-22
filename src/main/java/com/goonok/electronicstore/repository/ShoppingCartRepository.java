package com.goonok.electronicstore.repository;

import com.goonok.electronicstore.model.Product;
import com.goonok.electronicstore.model.ShoppingCartItem;
import com.goonok.electronicstore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCartItem, Long> {

    Optional<ShoppingCartItem> findByUser_UserId(Long userId);

    void deleteByUser_UserId(Long userId);
    void deleteByUser_UserIdAndProduct_ProductId(Long userId, Long productId);

    Optional<ShoppingCartItem> findByUser_UserIdAndProduct_ProductId(Long userId, Long productId);

    int countByUser(User user);

    public void deleteByProduct_ProductId(Long productId);

    // For registered users
    List<ShoppingCartItem> findByUser(User user);

    Optional<ShoppingCartItem> findByUserAndProduct(User user, Product product);


    Optional<ShoppingCartItem> findByUserAndProduct_ProductId(User user, Long productId);
}

