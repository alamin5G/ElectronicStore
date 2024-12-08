package com.goonok.electronicstore.service;

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
        return shoppingCartRepository.findByUser_UserId(user.getUserId());
    }

    public ShoppingCart addOrUpdateCartItem(ShoppingCart cartItem) {
        Optional<ShoppingCart> existingCartItem = shoppingCartRepository.findById(cartItem.getCartId());
        if (existingCartItem.isPresent()) {
            ShoppingCart existing = existingCartItem.get();
            existing.setQuantity(existing.getQuantity() + cartItem.getQuantity());
            return shoppingCartRepository.save(existing);
        }
        return shoppingCartRepository.save(cartItem);
    }

    public void removeCartItem(Long cartId) {
        shoppingCartRepository.deleteById(cartId);
    }

    public void clearCartForUser(User user) {
        List<ShoppingCart> cartItems = shoppingCartRepository.findByUser_UserId(user.getUserId());
        shoppingCartRepository.deleteAll(cartItems);
    }
}
