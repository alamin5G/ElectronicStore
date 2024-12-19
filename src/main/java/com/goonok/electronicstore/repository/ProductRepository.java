package com.goonok.electronicstore.repository;


import com.goonok.electronicstore.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory_CategoryId(Long categoryId);
    List<Product> findByBrand_BrandId(Long brandId);

    List<Product> findByIsFeaturedTrue();
    List<Product> findByIsNewArrivalTrue();

    List<Product> findCategoryByCategory_CategoryId(Long categoryCategoryId);
    List<Product> findBrandByBrand_BrandId(Long brandBrandId);

    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);
    List<Product> findByPriceLessThanEqual(Double price);
    List<Product> findByPriceGreaterThanEqual(Double price);
    List<Product> findByPriceLessThanEqualAndPriceGreaterThanEqual(Double maxPrice, Double minPrice);
    List<Product> findByCategory_CategoryIdAndBrand_BrandId(Long categoryId, Long brandId);
    List<Product> findByCategory_CategoryIdAndPriceBetween(Long categoryId, Double minPrice, Double maxPrice);
    List<Product> findByBrand_BrandIdAndPriceBetween(Long brandId, Double minPrice, Double maxPrice);
    List<Product> findByCategory_CategoryIdAndBrand_BrandIdAndPriceBetween(Long categoryId, Long brandId, Double minPrice, Double maxPrice);
    List<Product> findByCategory_CategoryIdAndPriceLessThanEqual(Long categoryId, Double price);
    List<Product> findByCategory_CategoryIdAndPriceGreaterThanEqual(Long categoryId, Double price);
    List<Product> findByBrand_BrandIdAndPriceLessThanEqual(Long brandId, Double price);
    List<Product> findByBrand_BrandIdAndPriceGreaterThanEqual(Long brandId, Double price);
    List<Product> findByCategory_CategoryIdAndBrand_BrandIdAndPriceLessThanEqual(Long categoryId, Long brandId, Double price);
    List<Product> findByCategory_CategoryIdAndBrand_BrandIdAndPriceGreaterThanEqual(Long categoryId, Long brandId, Double price);
}
