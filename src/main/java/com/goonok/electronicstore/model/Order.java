package com.goonok.electronicstore.model;

import com.goonok.electronicstore.enums.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty; // Added
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode; // Added
import lombok.ToString;         // Added
import org.hibernate.annotations.CreationTimestamp; // Added
import org.hibernate.annotations.UpdateTimestamp;   // Added

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList; // Added
import java.util.List;

@Data
@Entity
@Table(name = "`order`") // Keep escaped name
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Column(nullable = false, unique = true, length=50) // Added
    private String orderNumber; // Added user-friendly number

    @Column(updatable = false)
    @CreationTimestamp // Use annotation
    private LocalDateTime orderDate; // Renamed from createdAt for clarity

    @UpdateTimestamp // Use annotation
    private LocalDateTime updatedAt;

    @NotNull(message = "Order Status cannot be null") // Added
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50) // Added constraints
    private OrderStatus status; // Using Enum

    @NotNull(message = "Total Amount cannot be null") // Added
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount; // Renamed from totalPrice

    @NotNull(message = "Shipping Cost cannot be null") // Added
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingCost; // Added

    @NotNull(message = "Tax Amount cannot be null") // Added
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount; // Added

    // --- Shipping Details ---
    @NotEmpty(message = "Shipping street cannot be empty") // Added
    @Column(nullable = false)
    private String shippingStreet; // Split from shippingAddress

    @NotEmpty(message = "Shipping city cannot be empty") // Added
    @Column(nullable = false)
    private String shippingCity; // Split from shippingAddress

    @NotEmpty(message = "Shipping postal code cannot be empty") // Added
    @Column(nullable = false)
    private String shippingPostalCode; // Split from shippingAddress

    @NotEmpty(message = "Shipping state cannot be empty") // Added
    @Column(nullable = false)
    private String shippingState; // Split from shippingAddress

    @NotEmpty(message = "Shipping country cannot be empty") // Added
    @Column(nullable = false)
    private String shippingCountry; // Split from shippingAddress

    //shipping name
    @NotEmpty(message = "Shipping name cannot be empty") // Added
    @Column(nullable = false)
    private String shippingName; // Added

    //shipping phone
    @NotEmpty(message = "Shipping phone cannot be empty") // Added
    @Column(nullable = false)
    private String shippingPhone; // Added

    @NotEmpty(message = "Shipping method cannot be empty") // Added
    @Column(length = 50) // Added
    private String shippingMethod; // Added

    // --- Payment Details ---
    @Column(nullable = false, length = 50) // Keep default from user code for now
    private String paymentMethod = "Cash on Delivery"; // Changed from paymentType

    @Column(length = 50) // Added
    private String paymentStatus; // e.g., PENDING, COMPLETED, FAILED

    @Column(unique = true) // Transaction IDs should ideally be unique if stored
    private String transactionId; // Added

    // --- Tracking ---
    @Column(length = 100) // Added length
    private String trackingNumber;

    // --- Relationships ---
    @ManyToOne(fetch = FetchType.LAZY) // Set fetch type
    @JoinColumn(name = "user_id", nullable = false) // Added nullable = false
    @ToString.Exclude         // Added
    @EqualsAndHashCode.Exclude // Added
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY) // Added details
    @ToString.Exclude         // Added
    @EqualsAndHashCode.Exclude // Added
    private List<OrderItem> orderItems = new ArrayList<>(); // Initialize

    // Removed Admin, Payment, Discount relationships for simplification based on typical patterns

    @ManyToOne(fetch = FetchType.LAZY) // Owning side of the relationship
    @JoinColumn(name = "admin_id") // Specifies the foreign key column name
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Admin admin; // This is the field 'mappedBy="admin"' refers to



    // Relationship to Payment (Owning side of the OneToOne)
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    // optional=true means an Order can exist before a Payment record is created
    // cascade=ALL means if order is deleted, payment record might be too (review if this is desired)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Payment payment; // This field name matches `mappedBy="order"` in Payment

    // Relationship to Discount (Owning side of the ManyToOne)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_id") // Foreign key column in the 'order' table
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Discount discount; // This field name matches `mappedBy="discount"` in Discount



    // Method to associate Payment (important for OneToOne mappedBy)
    public void setPayment(Payment payment) {
        if (payment == null) {
            if (this.payment != null) {
                this.payment.setOrder(null);
            }
        } else {
            payment.setOrder(this);
        }
        this.payment = payment;
    }


    // Helper methods
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }

    public void removeOrderItem(OrderItem item) {
        orderItems.remove(item);
        item.setOrder(null);
    }

}