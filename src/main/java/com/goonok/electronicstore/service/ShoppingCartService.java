package com.goonok.electronicstore.service;

import com.goonok.electronicstore.model.Product;
import com.goonok.electronicstore.model.ShoppingCartItem;
import com.goonok.electronicstore.model.User;
import com.goonok.electronicstore.repository.ShoppingCartItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ShoppingCartService {

    @Autowired
    private ShoppingCartItemRepository shoppingCartItemRepository;

    public List<ShoppingCartItem> getCartItemsByUser(User user) {
        return shoppingCartItemRepository.findByUser(user);
    }



    public void addOrUpdateCartItem(User user, Product product, int quantity) {
        // Check if the specific product already exists in the user's cart
        Optional<ShoppingCartItem> cartItemOptional = shoppingCartItemRepository.findByUser_UserIdAndProduct_ProductId(user.getUserId(), product.getProductId());
        log.info("cartItemOptional this should be empty: " + cartItemOptional.isEmpty());
        if (cartItemOptional.isPresent()) {
            // Update the quantity if the item exists
            ShoppingCartItem existingItem = cartItemOptional.get();
            int newQuantity = existingItem.getQuantity() + quantity;

            // Ensure the new quantity does not exceed available stock
            if (product.getStockQuantity() < newQuantity) {
                throw new RuntimeException("Not enough stock available!");
            }

            existingItem.setQuantity(newQuantity);
            shoppingCartItemRepository.save(existingItem);
        } else {

                ShoppingCartItem newCartItem = new ShoppingCartItem();
                newCartItem.setUser(user);
                newCartItem.setProduct(product);

                if (product.getStockQuantity() < quantity) {
                    throw new RuntimeException("Not enough stock available!");
                }

                newCartItem.setQuantity(quantity);
                shoppingCartItemRepository.save(newCartItem);



        }
    }


    public void clearCartForUser(User user) {
        shoppingCartItemRepository.deleteByUser_UserId(user.getUserId());
    }

    public int getCartItemCountByUser(User user) {
        return shoppingCartItemRepository.countByUser(user);
    }

    public void removeCartItemById(Long cartId) {
        shoppingCartItemRepository.deleteById(cartId);
    }
}
