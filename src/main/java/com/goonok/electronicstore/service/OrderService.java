package com.goonok.electronicstore.service;

import com.goonok.electronicstore.enums.OrderStatus;
import com.goonok.electronicstore.model.*;
import com.goonok.electronicstore.repository.OrderRepository;
import com.goonok.electronicstore.repository.OrderItemRepository;
import com.goonok.electronicstore.repository.ProductRepository;
import com.goonok.electronicstore.service.ShoppingCartService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public void placeOrder(User user, List<ShoppingCart> cartItems, String shippingAddress) {
        BigDecimal totalPrice = cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create a new order
        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(shippingAddress);
        order.setTotalPrice(totalPrice);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus("UNPAID");
        order.setPaymentType("Cash on Delivery");
        order.setOrderDate(LocalDateTime.now());

        List<OrderItem> orderItems = new ArrayList<>();
        for (ShoppingCart cartItem : cartItems) {
            Product product = cartItem.getProduct();

            // Validate stock quantity
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Not enough stock available for product: " + product.getName());
            }

            // Create an order item
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtTime(product.getPrice());
            orderItem.setOrder(order);

            orderItems.add(orderItem);

            // Deduct stock quantity
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }

        // Save the order and its items
        order.setOrderItems(orderItems);
        orderRepository.save(order);
    }

    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUser_UserId(user.getUserId());
    }
}
