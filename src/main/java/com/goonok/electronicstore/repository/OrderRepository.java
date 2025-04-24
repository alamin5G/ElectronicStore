package com.goonok.electronicstore.repository;

import com.goonok.electronicstore.model.Order;
import com.goonok.electronicstore.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser_UserId(Long userId);

    /**
     * Finds all orders placed by a specific user, ordered by date descending.
     * @param user The user entity.
     * @param pageable Pagination information.
     * @return A page of the user's orders.
     */
    Page<Order> findByUserOrderByOrderDateDesc(User user, Pageable pageable);

    /**
     * Finds all orders placed by a specific user (non-paginated version if needed).
     * @param user The user entity.
     * @return A list of the user's orders.
     */
    List<Order> findByUserOrderByOrderDateDesc(User user); // Non-paginated version

    // Add other query methods if needed (e.g., find by status, find by date range)

}
