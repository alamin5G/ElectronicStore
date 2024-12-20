package com.goonok.electronicstore.service;

import com.goonok.electronicstore.model.Product;
import com.goonok.electronicstore.model.ShoppingCart;
import com.goonok.electronicstore.model.User;
import com.goonok.electronicstore.repository.ShoppingCartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShoppingCartService {

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    public List<ShoppingCart> getCartItemsByUser(User user) {
        return shoppingCartRepository.findByUser(user);
    }

    public List<ShoppingCart> getCartItemsBySessionId(String sessionId) {
        return shoppingCartRepository.findBySessionId(sessionId);
    }

    public void addOrUpdateCartItem(User user, Product product, int quantity) {
        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Not enough stock available!");
        }

        Optional<ShoppingCart> cartItem = shoppingCartRepository.findByUserAndProduct(user, product);

        if (cartItem.isPresent()) {
            ShoppingCart existingItem = cartItem.get();
            int newQuantity = existingItem.getQuantity() + quantity;

            if (product.getStockQuantity() < newQuantity) {
                throw new RuntimeException("Not enough stock available!");
            }

            existingItem.setQuantity(newQuantity);
            shoppingCartRepository.save(existingItem);
        } else {
            ShoppingCart newCartItem = new ShoppingCart();
            newCartItem.setUser(user);
            newCartItem.setProduct(product);
            newCartItem.setQuantity(quantity);
            shoppingCartRepository.save(newCartItem);
        }
    }


    public void removeCartItemByUserAndProduct(User user, Product product) {
        shoppingCartRepository.findByUserAndProduct(user, product)
                .ifPresent(shoppingCartRepository::delete);
    }

    public void removeCartItemBySessionIdAndProduct(String sessionId, Product product) {
        shoppingCartRepository.findBySessionIdAndProduct(sessionId, product)
                .ifPresent(shoppingCartRepository::delete);
    }

    public void removeCartItemsBySessionId(String sessionId) {
        shoppingCartRepository.deleteBySessionId(sessionId);
    }

    public void removeCartItemsByUser(User user) {
        shoppingCartRepository.deleteByUser_UserId(user.getUserId());
    }

    public void clearCartForUser(User user) {
        shoppingCartRepository.deleteByUser_UserId(user.getUserId());
    }

    public int getCartItemCountByUser(User user) {
        return shoppingCartRepository.countByUser(user);
    }
}
