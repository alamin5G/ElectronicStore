package com.goonok.electronicstore.repository;


import com.goonok.electronicstore.model.Brand;
import com.goonok.electronicstore.model.Category;
import com.goonok.electronicstore.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {



    // --- Methods used by ProductService (Basic Reads) ---

    /**
     * Finds featured products with pagination.
     * @param pageable Pagination information.
     * @return A page of featured products.
     */
    Page<Product> findByIsFeaturedTrue(Pageable pageable);

    /**
     * Finds new arrival products with pagination.
     * @param pageable Pagination information.
     * @return A page of new arrival products.
     */
    Page<Product> findByIsNewArrivalTrue(Pageable pageable);

    /**
     * Finds products belonging to a specific category with pagination.
     * @param category The category entity.
     * @param pageable Pagination information.
     * @return A page of products in the given category.
     */
    Page<Product> findByCategory(Category category, Pageable pageable);

    /**
     * Finds products belonging to a specific brand with pagination.
     * @param brand The brand entity.
     * @param pageable Pagination information.
     * @return A page of products for the given brand.
     */
    Page<Product> findByBrand(Brand brand, Pageable pageable);

    /**
     * Searches for products by name or description (case-insensitive) with pagination.
     * @param nameQuery The query string to search in the name.
     * @param descriptionQuery The query string to search in the description.
     * @param pageable Pagination information.
     * @return A page of products matching the query.
     */
    Page<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String nameQuery, String descriptionQuery, Pageable pageable);


    // --- Methods potentially used by ProductService (Filtering) ---
    // NOTE: These methods support the complex filtering logic. Using JpaSpecificationExecutor
    // is generally preferred over defining every single combination like this.

    // Find by Category ID (used in filter logic if only category is provided)
    Page<Product> findByCategory_CategoryId(Long categoryId, Pageable pageable);

    // Find by Brand ID (used in filter logic if only brand is provided)
    Page<Product> findByBrand_BrandId(Long brandId, Pageable pageable);

    // Find by Price between (used in filter logic if only price range is provided)
    Page<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    // Find by Price greater than or equal (used for 'price+' ranges)
    Page<Product> findByPriceGreaterThanEqual(BigDecimal minPrice, Pageable pageable);

    // --- Combinations for Filtering (Examples - Add more as needed or use Specifications) ---

    // Category AND Brand
    Page<Product> findByCategory_CategoryIdAndBrand_BrandId(Long categoryId, Long brandId, Pageable pageable);

    // Category AND Price Between
    Page<Product> findByCategory_CategoryIdAndPriceBetween(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    // Brand AND Price Between
    Page<Product> findByBrand_BrandIdAndPriceBetween(Long brandId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    // Category AND Price Greater Than Equal
    Page<Product> findByCategory_CategoryIdAndPriceGreaterThanEqual(Long categoryId, BigDecimal minPrice, Pageable pageable);

    // Brand AND Price Greater Than Equal
    Page<Product> findByBrand_BrandIdAndPriceGreaterThanEqual(Long brandId, BigDecimal minPrice, Pageable pageable);

    // Four Filters
    /**
     * Finds products matching category, brand, price range, and new arrival status, with pagination.
     * @param categoryId ID of the category.
     * @param brandId ID of the brand.
     * @param minPrice Minimum price.
     * @param maxPrice Maximum price.
     * @param pageable Pagination information.
     * @return A page of matching products.
     */
    Page<Product> findByCategory_CategoryIdAndBrand_BrandIdAndPriceBetweenAndIsNewArrivalTrue(Long categoryId, Long brandId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable); // <-- DEFINED THIS METHOD


    // Category AND Brand AND Price Between
    Page<Product> findByCategory_CategoryIdAndBrand_BrandIdAndPriceBetween(Long categoryId, Long brandId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);



    // Category AND Brand AND Price Greater Than Equal
    Page<Product> findByCategory_CategoryIdAndBrand_BrandIdAndPriceGreaterThanEqual(Long categoryId, Long brandId, BigDecimal minPrice, Pageable pageable);


    // --- Non-paginated versions (if needed, but generally prefer pagination) ---

    List<Product> findByIsFeaturedTrue();
    List<Product> findByIsNewArrivalTrue();
    List<Product> findByCategory_CategoryId(Long categoryId);
    List<Product> findByBrand_BrandId(Long brandId);
    List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String nameQuery, String descriptionQuery);
    List<Product> findByCategory_CategoryIdAndBrand_BrandId(Long categoryId, Long brandId);
    List<Product> findByCategory_CategoryIdAndPriceBetween(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice);
    List<Product> findByBrand_BrandIdAndPriceBetween(Long brandId, BigDecimal minPrice, BigDecimal maxPrice);
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    List<Product> findByCategory_CategoryIdAndBrand_BrandIdAndPriceBetween(Long categoryId, Long brandId, BigDecimal minPrice, BigDecimal maxPrice);
    List<Product> findByCategory_CategoryIdAndPriceGreaterThan(Long categoryId, BigDecimal minPrice);
    List<Product> findByBrand_BrandIdAndPriceGreaterThan(Long brandId, BigDecimal minPrice);
    List<Product> findByPriceGreaterThan(BigDecimal minPrice);
    List<Product> findByCategory_CategoryIdAndBrand_BrandIdAndPriceGreaterThan(Long categoryId, Long brandId, BigDecimal minPrice);




    // Two Filters
    Page<Product> findByCategory_CategoryIdAndIsNewArrivalTrue(Long categoryId, Pageable pageable); // Added for newArrival filter
    Page<Product> findByBrand_BrandIdAndIsNewArrivalTrue(Long brandId, Pageable pageable); // Added for newArrival filter
    Page<Product> findByPriceBetweenAndIsNewArrivalTrue(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable); // Added for newArrival filter
    Page<Product> findByPriceGreaterThanEqualAndIsNewArrivalTrue(BigDecimal minPrice, Pageable pageable); // Added for newArrival filter


    // Three Filters
    Page<Product> findByCategory_CategoryIdAndPriceBetweenAndIsNewArrivalTrue(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable); // Added for newArrival filter
    Page<Product> findByBrand_BrandIdAndPriceBetweenAndIsNewArrivalTrue(Long brandId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable); // Added for newArrival filter
    Page<Product> findByCategory_CategoryIdAndPriceGreaterThanEqualAndIsNewArrivalTrue(Long categoryId, BigDecimal minPrice, Pageable pageable); // Added for newArrival filter
    Page<Product> findByBrand_BrandIdAndPriceGreaterThanEqualAndIsNewArrivalTrue(Long brandId, BigDecimal minPrice, Pageable pageable); // Added for newArrival filter
    Page<Product> findByCategory_CategoryIdAndBrand_BrandIdAndIsNewArrivalTrue(Long categoryId, Long brandId, Pageable pageable); // Added for newArrival filter



    /**
     * Finds products matching category, brand, minimum price, and new arrival status, with pagination.
     * @param categoryId ID of the category.
     * @param brandId ID of the brand.
     * @param minPrice Minimum price.
     * @param pageable Pagination information.
     * @return A page of matching products.
     */
    Page<Product> findByCategory_CategoryIdAndBrand_BrandIdAndPriceGreaterThanEqualAndIsNewArrivalTrue(Long categoryId, Long brandId, BigDecimal minPrice, Pageable pageable); // <-- DEFINED THIS METHOD


    // *** ADDED for Related Products ***
    /**
     * Finds products by category ID, excluding a specific product ID, with pagination.
     * @param categoryId The category ID to search within.
     * @param productIdToExclude The product ID to exclude from the results.
     * @param pageable Pagination information.
     * @return A page of related products.
     */
    Page<Product> findByCategory_CategoryIdAndProductIdNot(Long categoryId, Long productIdToExclude, Pageable pageable);
    // ********************************


    /**
     * Finds products by brand ID, excluding a specific product ID, with pagination.
     * @param brandId The brand ID to search within.
     * @param productIdToExclude The product ID to exclude from the results.
     * @param pageable Pagination information.
     * @return A page of related products from the same brand.
     */
    Page<Product> findByBrand_BrandIdAndProductIdNot(Long brandId, Long productIdToExclude, Pageable pageable); // <-- ADDED METHOD


    int countByStockQuantityLessThanEqual(int threshold);
    Page<Product> findByStockQuantityLessThanEqual(int threshold, Pageable pageable);


}
