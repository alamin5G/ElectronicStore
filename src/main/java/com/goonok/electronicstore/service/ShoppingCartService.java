package com.goonok.electronicstore.service;

import com.goonok.electronicstore.model.Product;
import com.goonok.electronicstore.model.ShoppingCart;
import com.goonok.electronicstore.model.User;
import com.goonok.electronicstore.repository.ShoppingCartRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ShoppingCartService {

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    public List<ShoppingCart> getCartItemsByUser(User user) {
        return shoppingCartRepository.findByUser(user);
    }



    public void addOrUpdateCartItem(User user, Product product, int quantity) {
        // Check if the specific product already exists in the user's cart
        Optional<ShoppingCart> cartItemOptional = shoppingCartRepository.findByUser_UserIdAndProduct_ProductId(user.getUserId(), product.getProductId());
        log.info("cartItemOptional this should be empty: " + cartItemOptional.isEmpty());
        if (cartItemOptional.isPresent()) {
            // Update the quantity if the item exists
            ShoppingCart existingItem = cartItemOptional.get();
            int newQuantity = existingItem.getQuantity() + quantity;

            // Ensure the new quantity does not exceed available stock
            if (product.getStockQuantity() < newQuantity) {
                throw new RuntimeException("Not enough stock available!");
            }

            existingItem.setQuantity(newQuantity);
            shoppingCartRepository.save(existingItem);
        } else {

                ShoppingCart newCartItem = new ShoppingCart();
                newCartItem.setUser(user);
                newCartItem.setProduct(product);

                if (product.getStockQuantity() < quantity) {
                    throw new RuntimeException("Not enough stock available!");
                }

                newCartItem.setQuantity(quantity);
                shoppingCartRepository.save(newCartItem);



        }
    }


    public void clearCartForUser(User user) {
        shoppingCartRepository.deleteByUser_UserId(user.getUserId());
    }

    public int getCartItemCountByUser(User user) {
        return shoppingCartRepository.countByUser(user);
    }

    public void removeCartItemById(Long cartId) {
        shoppingCartRepository.deleteById(cartId);
    }
}
