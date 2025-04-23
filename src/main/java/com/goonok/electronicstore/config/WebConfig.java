package com.goonok.electronicstore.config; // Or your config package

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Inject the same path used in BrandService
    @Value("${brand.logo.path}")
    private String brandLogoPath;

    // Inject the path used in ProductService (add this if not already done)
    @Value("${product.image.path}")
    private String productImagePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // --- Brand Logo Mapping ---
        // Map URL path "/brand-logos/**" to the physical directory specified by brand.logo.path
        String brandPathPattern = "/brand-logos/**";
        String brandResourceLocation = "file:" + brandLogoPath; // Use "file:" prefix for external directories

        registry.addResourceHandler(brandPathPattern)
                .addResourceLocations(brandResourceLocation);
        System.out.println("Configured resource handler: " + brandPathPattern + " -> " + brandResourceLocation); // Debug log

        // --- Product Image Mapping ---
        // Map URL path "/product-images/**" to the physical directory specified by product.image.path
        String productPathPattern = "/product-images/**";
        String productResourceLocation = "file:" + productImagePath;

        registry.addResourceHandler(productPathPattern)
                .addResourceLocations(productResourceLocation);
        System.out.println("Configured resource handler: " + productPathPattern + " -> " + productResourceLocation); // Debug log

    }
}