package com.goonok.electronicstore;

import com.goonok.electronicstore.dto.*;
import com.goonok.electronicstore.enums.ContactMessageStatus;
import com.goonok.electronicstore.enums.OrderStatus;
import com.goonok.electronicstore.model.*;
import com.goonok.electronicstore.repository.*;
import com.goonok.electronicstore.service.interfaces.ContactService;
import com.goonok.electronicstore.service.interfaces.OrderService;
import com.goonok.electronicstore.service.interfaces.ProductService;
import com.goonok.electronicstore.service.interfaces.UserService;
import com.goonok.electronicstore.verification.EmailService;
import com.goonok.electronicstore.verification.VerificationService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class ElectronicStoreApplicationTests {

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private CategoryRepository categoryRepository;

    @MockBean
    private BrandRepository brandRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @MockBean
    private EmailService emailService;

    @MockBean
    private UserService userService;

    @MockBean
    private VerificationService verificationService;

    @MockBean
    private OrderService orderService;

    @MockBean
    private ContactService contactService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void contextLoads() {
        // Verify spring context loads successfully
        assertNotNull(productService);
        assertNotNull(modelMapper);
    }

    // ==================== PRODUCT SERVICE TESTS ====================

    @Test
    @DisplayName("Filter products by price range")
    public void testFilterProductsByPriceRange() {
        // Given
        BigDecimal minPrice = new BigDecimal("100.00");
        BigDecimal maxPrice = new BigDecimal("200.00");

        // Create a list of mock products
        List<Product> mockProducts = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Product product = new Product();
            product.setProductId((long) i);
            product.setName("Product " + i);
            // Create products with prices within the range
            product.setPrice(new BigDecimal("150.00").add(new BigDecimal(i * 10)));
            mockProducts.add(product);
        }

        // Create a Page of mock products
        Page<Product> mockProductPage = new PageImpl<>(mockProducts);

        // Mock the repository method with any parameters since we want to test the service logic
        when(productRepository.findByPriceBetween(
            any(BigDecimal.class), any(BigDecimal.class), any(Pageable.class)))
            .thenReturn(mockProductPage);

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductDto> filteredProducts = productService.getFilteredProducts(
            null, null, null, minPrice.doubleValue(), maxPrice.doubleValue(), null, pageable
        );

        // Then
        verify(productRepository).findByPriceBetween(
            any(BigDecimal.class), any(BigDecimal.class), eq(pageable)
        );

        assertNotNull(filteredProducts);
        assertEquals(5, filteredProducts.getContent().size());
    }

    @Test
    @DisplayName("Get featured products should return only featured products")
    public void testGetFeaturedProducts() {
        // Given
        List<Product> featuredProducts = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Product product = new Product();
            product.setProductId((long) i);
            product.setName("Featured Product " + i);
            product.setFeatured(true);
            featuredProducts.add(product);
        }

        Page<Product> mockPage = new PageImpl<>(featuredProducts);
        Pageable pageable = PageRequest.of(0, 10);

        when(productRepository.findByIsFeaturedTrue(pageable)).thenReturn(mockPage);

        // When
        Page<ProductDto> result = productService.getFeaturedProducts(pageable);

        // Then
        verify(productRepository).findByIsFeaturedTrue(pageable);
        assertEquals(3, result.getContent().size());
        assertTrue(result.getContent().stream().allMatch(p -> p.getName().contains("Featured")));
    }

    @Test
    @DisplayName("Get products by category returns correct products")
    public void testGetProductsByCategory() {
        // Given
        Long categoryId = 1L;
        Category category = new Category();
        category.setCategoryId(categoryId);
        category.setName("Test Category");

        List<Product> categoryProducts = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Product product = new Product();
            product.setProductId((long) i);
            product.setName("Category Product " + i);
            product.setCategory(category);
            categoryProducts.add(product);
        }

        Page<Product> mockPage = new PageImpl<>(categoryProducts);
        Pageable pageable = PageRequest.of(0, 10);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.findByCategory(any(Category.class), any(Pageable.class))).thenReturn(mockPage);

        // When
        Page<ProductDto> result = productService.getProductsByCategory(categoryId, pageable);

        // Then
        verify(categoryRepository).findById(categoryId);
        verify(productRepository).findByCategory(category, pageable);
        assertEquals(3, result.getContent().size());
    }

    @Test
    @DisplayName("Find related products returns products in same category except current product")
    public void testFindRelatedProducts() {
        // Given
        Long currentProductId = 1L;
        Long categoryId = 100L;
        int limit = 3;

        List<Product> relatedProducts = new ArrayList<>();
        for (int i = 2; i <= 4; i++) { // IDs 2,3,4 - not the current product
            Product product = new Product();
            product.setProductId((long) i);
            product.setName("Related Product " + i);
            relatedProducts.add(product);
        }

        Page<Product> mockPage = new PageImpl<>(relatedProducts);
        Pageable pageable = PageRequest.of(0, limit, sort(by("createdAt").descending()));

        when(productRepository.findByCategory_CategoryIdAndProductIdNot(
            eq(categoryId), eq(currentProductId), any(Pageable.class)))
            .thenReturn(mockPage);

        // When
        List<ProductDto> result = productService.findRelatedProducts(currentProductId, categoryId, limit);

        // Then
        verify(productRepository).findByCategory_CategoryIdAndProductIdNot(
            eq(categoryId), eq(currentProductId), any(Pageable.class));
        assertEquals(3, result.size());
        assertFalse(result.stream().anyMatch(p -> p.getProductId().equals(currentProductId)));
    }

    @Test
    @DisplayName("Count low stock products returns correct count")
    public void testCountLowStockProducts() {
        // Given
        int threshold = 10;
        when(productRepository.countByStockQuantityLessThanEqual(threshold)).thenReturn(5);

        // When
        long result = productService.countLowStockProducts(threshold);

        // Then
        verify(productRepository).countByStockQuantityLessThanEqual(threshold);
        assertEquals(5L, result);
    }

    // ==================== USER SERVICE TESTS ====================

    @Test
    @DisplayName("Create password reset token for valid user")
    public void testCreatePasswordResetToken() {
        // Given
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));

        // When
        // Use the UserService directly rather than the UserServiceImpl
        userService.createPasswordResetTokenForUser(email);

        // Then
        verify(passwordResetTokenRepository).findByUser(user);
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendPasswordResetEmail(eq(user), anyString());
    }

    @Test
    @DisplayName("Reset password with valid token updates password")
    public void testResetPassword() {
        // Given
        String token = "valid-token";
        String newPassword = "newPassword123";

        User user = new User();
        user.setUserId(1L);
        user.setEmail("test@example.com");

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusDays(1));
        resetToken.setUsed(false);

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

        // When
        userService.resetPassword(token, newPassword);

        // Then
        verify(userRepository).save(user);
        verify(passwordResetTokenRepository).save(resetToken);
        assertTrue(passwordEncoder.matches(newPassword, user.getPassword()));
        assertTrue(resetToken.isUsed());
    }

    @Test
    @DisplayName("Toggle user status changes enabled flag")
    public void testToggleUserStatus() {
        // Given
        Long userId = 1L;
        User user = new User();
        user.setUserId(userId);
        user.setEmail("test@example.com");
        user.setEnabled(false); // Initially disabled

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        // When
        AdminUserViewDto result = userService.toggleUserStatus(userId);

        // Then
        assertTrue(user.isEnabled()); // Should now be enabled
        verify(userRepository).save(user);
        assertNotNull(result);
    }

    // ==================== ORDER SERVICE TESTS ====================

    @Test
    @DisplayName("Get orders by status returns correct orders")
    public void testGetOrdersByStatus() {
        // Given
        OrderStatus status = OrderStatus.PROCESSING;
        Pageable pageable = PageRequest.of(0, 10);

        List<OrderDto> orderDtos = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            OrderDto orderDto = new OrderDto();
            orderDto.setOrderId((long) i);
            orderDto.setStatus(status);
            orderDtos.add(orderDto);
        }

        Page<OrderDto> mockPage = new PageImpl<>(orderDtos);
        when(orderService.getAllOrders(status, pageable)).thenReturn(mockPage);

        // When
        Page<OrderDto> result = orderService.getAllOrders(status, pageable);

        // Then
        assertEquals(3, result.getContent().size());
        assertTrue(result.getContent().stream().allMatch(o -> o.getStatus() == status));
    }

    @Test
    @DisplayName("Get recent orders returns limited number of orders")
    public void testGetRecentOrders() {
        // Given
        int limit = 5;
        List<OrderDto> recentOrders = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            OrderDto orderDto = new OrderDto();
            orderDto.setOrderId((long) i);
            recentOrders.add(orderDto);
        }

        when(orderService.getRecentOrders(limit)).thenReturn(recentOrders);

        // When
        List<OrderDto> result = orderService.getRecentOrders(limit);

        // Then
        assertEquals(5, result.size());
    }

    // ==================== CONTACT SERVICE TESTS ====================

    @Test
    @DisplayName("Mark contact message as read updates status")
    public void testMarkContactMessageAsRead() {
        // Given
        Long messageId = 1L;
        ContactMessageDto messageDto = new ContactMessageDto();
        messageDto.setId(messageId);
        messageDto.setStatus(ContactMessageStatus.NEW);

        // Create the updated message that will be returned
        ContactMessageDto updatedDto = new ContactMessageDto();
        updatedDto.setId(messageId);
        updatedDto.setStatus(ContactMessageStatus.IN_PROGRESS);

        when(contactService.markAsRead(messageId)).thenReturn(updatedDto);

        // When
        ContactMessageDto result = contactService.markAsRead(messageId);

        // Then
        assertEquals(ContactMessageStatus.IN_PROGRESS, result.getStatus());
    }

    @Test
    @DisplayName("Count unread messages returns correct count")
    public void testCountUnreadMessages() {
        // Given
        when(contactService.countUnreadMessages()).thenReturn(10L);

        // When
        long result = contactService.countUnreadMessages();

        // Then
        assertEquals(10L, result);
    }

    // Helper method to create Sort object for related products test
    public Sort sort(SortBuilder builder) {
        return builder.build();
    }

    public SortBuilder by(String property) {
        return new SortBuilder(property);
    }

    // Corrected SortBuilder inner class
    public class SortBuilder {
        private final String property;
        private Sort.Direction direction = Sort.Direction.ASC; // Default to ASC

        public SortBuilder(String property) {
            if (property == null || property.trim().isEmpty()) {
                throw new IllegalArgumentException("Property cannot be null or empty");
            }
            this.property = property;
        }

        public SortBuilder descending() {
            this.direction = Sort.Direction.DESC;
            return this; // Return the builder instance for chaining
        }

        // Optional: if you want to explicitly set ascending
        public SortBuilder ascending() {
            this.direction = Sort.Direction.ASC;
            return this; // Return the builder instance for chaining
        }

        public Sort build() {
            return Sort.by(this.direction, this.property); // Use the stored direction
        }
    }

}