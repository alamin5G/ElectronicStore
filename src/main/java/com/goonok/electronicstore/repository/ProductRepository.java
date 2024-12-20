package com.goonok.electronicstore.repository;


import com.goonok.electronicstore.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory_CategoryId(Long categoryId);
    List<Product> findByBrand_BrandId(Long brandId);

    List<Product> findByIsFeaturedTrue();
    List<Product> findByIsNewArrivalTrue();

    // Filters for missing category or brand

    List<Product> findByBrand_BrandIdAndPriceBetween(Long brandId, BigDecimal minPrice, BigDecimal maxPrice);

    // General price range queries
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    List<Product> findByPriceGreaterThan(BigDecimal minPrice);

    List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);

    List<Product> findByCategory_CategoryIdAndBrand_BrandId(Long categoryId, Long brandId);

    List<Product> findByCategory_CategoryIdAndBrand_BrandIdAndPriceBetween(Long categoryCategoryId, Long brandBrandId, BigDecimal priceAfter, BigDecimal priceBefore);

    List<Product> findByCategory_CategoryIdAndBrand_BrandIdAndPriceGreaterThan(Long categoryCategoryId, Long brandBrandId, BigDecimal priceIsGreaterThan);

    List<Product> findCategoryByCategory_CategoryId(Long categoryCategoryId);
    List<Product> findBrandByBrand_BrandId(Long brandBrandId);
    List<Product> findByBrand_BrandIdAndPriceGreaterThan(Long brandBrandId, BigDecimal priceIsGreaterThan);

    List<Product> findByCategory_CategoryIdAndPriceBetween(Long categoryCategoryId, BigDecimal priceAfter, BigDecimal priceBefore);

    List<Product> findByCategory_CategoryIdAndBrand_BrandIdAndPriceLessThan(Long categoryId, Long brandId, BigDecimal maxPrice);
    List<Product> findByCategory_CategoryIdAndPriceGreaterThan(Long categoryId, BigDecimal minPrice);
    List<Product> findByCategory_CategoryIdAndPriceLessThan(Long categoryId, BigDecimal maxPrice);

}
