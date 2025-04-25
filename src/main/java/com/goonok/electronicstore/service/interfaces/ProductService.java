package com.goonok.electronicstore.service.interfaces;

import com.goonok.electronicstore.dto.ProductDto;
import com.goonok.electronicstore.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ProductService {

    // --- Existing Methods ---
    ProductDto createProduct(ProductDto productDto, MultipartFile imageFile);
    ProductDto updateProduct(Long productId, ProductDto productDto, MultipartFile imageFile);
    ProductDto getProductDtoById(Long productId);
    Product getProductEntityById(Long productId); // Keep if needed internally
    Page<ProductDto> getAllProducts(Pageable pageable);
    void deleteProduct(Long productId);
    Page<ProductDto> getFeaturedProducts(Pageable pageable);
    Page<ProductDto> getNewProducts(Pageable pageable);
    Page<ProductDto> getProductsByCategory(Long categoryId, Pageable pageable);
    Page<ProductDto> getProductsByBrand(Long brandId, Pageable pageable);
    Page<ProductDto> searchProducts(String query, Pageable pageable);
    Page<ProductDto> getFilteredProducts(Long categoryId, Long brandId, String priceRange, Double minPrice, Double maxPrice, Boolean newArrival, Pageable pageable);

    @Transactional(readOnly = true)
    Page<ProductDto> getFilteredProducts(Long categoryId, Long brandId, String priceRange, Double minPrice, Double maxPrice, Pageable pageable);

    void toggleFeaturedStatus(Long productId);
    void toggleNewArrivalStatus(Long productId);
    List<ProductDto> findRelatedProducts(Long currentProductId, Long categoryId, int limit);
    List<ProductDto> findMoreByBrand(Long currentProductId, Long brandId, int limit);

    // --- Dashboard Methods ---
    /**
     * Gets the total count of products.
     * @return Total product count.
     */
    @Transactional(readOnly = true)
    long countTotalProducts(); // <-- ADDED METHOD

    /**
     * Gets the count of products with stock quantity at or below a threshold.
     * @param threshold The minimum stock level threshold.
     * @return Count of low stock products.
     */
    @Transactional(readOnly = true)
    long countLowStockProducts(int threshold); // <-- ADDED METHOD

    List<ProductDto> getLowStockProductsList(int limit);

}
