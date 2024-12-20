package com.goonok.electronicstore.repository;

import com.goonok.electronicstore.model.Product;
import com.goonok.electronicstore.model.ShoppingCart;
import com.goonok.electronicstore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {

    Optional<ShoppingCart> findByUser_UserId(Long userId);

    void deleteByUser_UserId(Long userId);
    void deleteByUser_UserIdAndProduct_ProductId(Long userId, Long productId);

    Optional<ShoppingCart> findByUser_UserIdAndProduct_ProductId(Long userId, Long productId);

    int countByUser(User user);

    public void deleteByProduct_ProductId(Long productId);

    // For registered users
    List<ShoppingCart> findByUser(User user);

    Optional<ShoppingCart> findByUserAndProduct(User user, Product product);


    Optional<ShoppingCart> findByUserAndProduct_ProductId(User user, Long productId);
}

