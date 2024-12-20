package com.goonok.electronicstore.service;

import com.goonok.electronicstore.model.Product;
import com.goonok.electronicstore.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // New method to get all products
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // New method to get featured products
    public List<Product> getFeaturedProducts() {
        return productRepository.findByIsFeaturedTrue(); // Ensure this method exists in your repository
    }

    // New method to get new products
    public List<Product> getNewProducts() {
        return productRepository.findByIsNewArrivalTrue(); // Ensure this method exists in your repository
    }

    // New method to get products by category
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategory_CategoryId(categoryId);
    }

    // New method to get products by brand
    public List<Product> getProductsByBrand(Long brandId) {
        return productRepository.findByBrand_BrandId(brandId);
    }

    // New method to search for products
    public List<Product> searchProducts(String query) {
        return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);
    }


    // New method to get a product by ID
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    // New method to add a product
    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    // New method to update a product
    public Product updateProduct(Long id, Product productDetails) {
        return productRepository.findById(id).map(product -> {
            product.setName(productDetails.getName());
            product.setDescription(productDetails.getDescription());
            product.setPrice(productDetails.getPrice());
            product.setStockQuantity(productDetails.getStockQuantity());
            product.setMinimumStockLevel(productDetails.getMinimumStockLevel());
            product.setCategory(productDetails.getCategory());
            product.setBrand(productDetails.getBrand());
            product.setImagePath(productDetails.getImagePath());
            product.setFeatured(productDetails.isFeatured());
            product.setNewArrival(productDetails.isNewArrival());
            return productRepository.save(product);
        }).orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));
    }


    // New method to delete a product
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }


    // New method to get filtered products based on category, brand, and price range
    public List<Product> getFilteredProducts(Long categoryId, Long brandId, String priceRange, Double minPrice, Double maxPrice) {
        List<Product> products;

        // Handle custom price range first (takes priority over predefined ranges)
        if (minPrice != null && maxPrice != null) {
            BigDecimal min = BigDecimal.valueOf(minPrice);
            BigDecimal max = BigDecimal.valueOf(maxPrice);

            if (categoryId != null && brandId != null) {
                products = productRepository.findByCategory_CategoryIdAndBrand_BrandIdAndPriceBetween(categoryId, brandId, min, max);
            } else if (categoryId != null) {
                products = productRepository.findByCategory_CategoryIdAndPriceBetween(categoryId, min, max);
            } else if (brandId != null) {
                products = productRepository.findByBrand_BrandIdAndPriceBetween(brandId, min, max);
            } else {
                products = productRepository.findByPriceBetween(min, max);
            }
        }
        // Handle predefined price range if custom range is not provided
        else if (priceRange != null) {
            if (priceRange.endsWith("+")) {
                double price = Double.parseDouble(priceRange.substring(0, priceRange.length() - 1));
                BigDecimal min = BigDecimal.valueOf(price);

                if (categoryId != null && brandId != null) {
                    products = productRepository.findByCategory_CategoryIdAndBrand_BrandIdAndPriceGreaterThan(categoryId, brandId, min);
                } else if (categoryId != null) {
                    products = productRepository.findByCategory_CategoryIdAndPriceGreaterThan(categoryId, min);
                } else if (brandId != null) {
                    products = productRepository.findByBrand_BrandIdAndPriceGreaterThan(brandId, min);
                } else {
                    products = productRepository.findByPriceGreaterThan(min);
                }
            } else {
                String[] range = priceRange.split("-");
                double min = Double.parseDouble(range[0]);
                double max = Double.parseDouble(range[1]);
                BigDecimal mPrice = BigDecimal.valueOf(min);
                BigDecimal mxPrice = BigDecimal.valueOf(max);

                if (categoryId != null && brandId != null) {
                    products = productRepository.findByCategory_CategoryIdAndBrand_BrandIdAndPriceBetween(categoryId, brandId, mPrice, mxPrice);
                } else if (categoryId != null) {
                    products = productRepository.findByCategory_CategoryIdAndPriceBetween(categoryId, mPrice, mxPrice);
                } else if (brandId != null) {
                    products = productRepository.findByBrand_BrandIdAndPriceBetween(brandId, mPrice, mxPrice);
                } else {
                    products = productRepository.findByPriceBetween(mPrice, mxPrice);
                }
            }
        }
        // If neither custom nor predefined price ranges are provided, use category and brand filters
        else {
            if (categoryId != null && brandId != null) {
                products = productRepository.findByCategory_CategoryIdAndBrand_BrandId(categoryId, brandId);
            } else if (categoryId != null) {
                products = productRepository.findByCategory_CategoryId(categoryId);
            } else if (brandId != null) {
                products = productRepository.findByBrand_BrandId(brandId);
            } else {
                products = productRepository.findAll(); // No filters applied
            }
        }

        log.info("Products quantity after filtering: " + (products != null ? products.size() : 0));
        return products;
    }



}
