package com.goonok.electronicstore.service;

import com.goonok.electronicstore.enums.OrderStatus;
import com.goonok.electronicstore.model.*;
import com.goonok.electronicstore.repository.OrderRepository;
import com.goonok.electronicstore.repository.OrderItemRepository;
import com.goonok.electronicstore.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ShoppingCartService shoppingCartService;

    public Order placeOrder(User user, String shippingAddress) {
        // Get cart items for the user
        List<ShoppingCart> cartItems = shoppingCartService.getCartItemsByUser(user);

        // Calculate total price
        BigDecimal totalPrice = cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create the order
        Order order = new Order();
        order.setUser(user);
        order.setTotalPrice(totalPrice);
        order.setShippingAddress(shippingAddress);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);

        // Add order items
        for (ShoppingCart cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtTime(cartItem.getProduct().getPrice());
            orderItemRepository.save(orderItem);
        }

        // Clear the user's cart
        shoppingCartService.clearCartForUser(user);

        return savedOrder;
    }

    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUser_UserId(user.getUserId());
    }
}
