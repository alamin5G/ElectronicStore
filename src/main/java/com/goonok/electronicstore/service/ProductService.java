package com.goonok.electronicstore.service;

import com.goonok.electronicstore.model.Product;
import com.goonok.electronicstore.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product productDetails) {
        return productRepository.findById(id).map(product -> {
            product.setName(productDetails.getName());
            product.setDescription(productDetails.getDescription());
            product.setPrice(productDetails.getPrice());
            product.setStockQuantity(productDetails.getStockQuantity());
            product.setMinimumStockLevel(productDetails.getMinimumStockLevel());
            product.setCategory(productDetails.getCategory());
            product.setBrand(productDetails.getBrand());
            product.setImageUrl(productDetails.getImageUrl());
            return productRepository.save(product);
        }).orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    // New method to filter products based on category, brand, and price range
    public List<Product> getFilteredProducts(Long categoryId, Long brandId, String priceRange) {
        List<Product> products = productRepository.findAll();

        if (categoryId != null) {
            products = products.stream()
                    .filter(product -> product.getCategory().getCategoryId().equals(categoryId))
                    .collect(Collectors.toList());
        }

        if (brandId != null) {
            products = products.stream()
                    .filter(product -> product.getBrand().getBrandId().equals(brandId))
                    .collect(Collectors.toList());
        }

        if (priceRange != null) {
            products = products.stream().filter(product -> {
                switch (priceRange) {
                    case "low": return product.getPrice().compareTo(BigDecimal.valueOf(50)) < 0;
                    case "mid": return product.getPrice().compareTo(BigDecimal.valueOf(50)) >= 0 &&
                            product.getPrice().compareTo(BigDecimal.valueOf(200)) <= 0;
                    case "high": return product.getPrice().compareTo(BigDecimal.valueOf(200)) > 0;
                    default: return true;
                }
            }).collect(Collectors.toList());
        }

        return products;
    }

}
