package com.goonok.electronicstore.service.impl;
import com.goonok.electronicstore.dto.ProductDto;
import com.goonok.electronicstore.exception.ResourceNotFoundException; // Make sure this is defined
import com.goonok.electronicstore.model.Brand;
import com.goonok.electronicstore.model.Category;
import com.goonok.electronicstore.model.Product;
import com.goonok.electronicstore.model.Warranty;
import com.goonok.electronicstore.repository.BrandRepository;
import com.goonok.electronicstore.repository.CategoryRepository;
import com.goonok.electronicstore.repository.ProductRepository;
import com.goonok.electronicstore.repository.WarrantyRepository;
import com.goonok.electronicstore.service.interfaces.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // Needed for image path property
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile; // Import for image handling

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
@Service
public class ProductServiceImpl implements ProductService {


    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private WarrantyRepository warrantyRepository;
    @Autowired
    private ModelMapper modelMapper;

    // Inject the path where product images will be stored from application properties
    @Value("${product.image.path}")
    private String imageStoragePath;



    // --- Admin CRUD Operations using DTOs ---
    @Override
    @Transactional
    public ProductDto createProduct(ProductDto productDto, MultipartFile imageFile) {
        log.info("Attempting to create product: {}", productDto.getName());

        Category category = categoryRepository.findById(productDto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "ID", productDto.getCategoryId()));
        Brand brand = brandRepository.findById(productDto.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "ID", productDto.getBrandId()));
        Warranty warranty = null;
        if (productDto.getWarrantyId() != null) {
            warranty = warrantyRepository.findById(productDto.getWarrantyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Warranty", "ID", productDto.getWarrantyId()));
        }

        Product product = modelMapper.map(productDto, Product.class);
        product.setCategory(category);
        product.setBrand(brand);
        product.setWarranty(warranty);

        // --- Image Handling ---
        String uniqueImageName = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            log.info("Image file received: {}", imageFile.getOriginalFilename());
            uniqueImageName = saveImage(imageFile); // Save the image and get its name/path
            product.setImagePath(uniqueImageName); // Store the unique name/path
        } else {
            // Optionally set a default image path if no image is uploaded
            product.setImagePath("default_product_image.jpg");
            log.warn("No image file provided for product: {}", productDto.getName());
        }
        // --------------------

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getProductId());
        return modelMapper.map(savedProduct, ProductDto.class);
    }

    @Override
    @Transactional
    public ProductDto updateProduct(Long productId, ProductDto productDto, MultipartFile imageFile) {
        log.info("Attempting to update product with ID: {}", productId);

        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "ID", productId));

        Category category = categoryRepository.findById(productDto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "ID", productDto.getCategoryId()));
        Brand brand = brandRepository.findById(productDto.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "ID", productDto.getBrandId()));
        Warranty warranty = null;
        if (productDto.getWarrantyId() != null) {
            warranty = warrantyRepository.findById(productDto.getWarrantyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Warranty", "ID", productDto.getWarrantyId()));
        }

        // Update fields from DTO
        existingProduct.setName(productDto.getName());
        existingProduct.setDescription(productDto.getDescription());
        existingProduct.setPrice(productDto.getPrice());
        existingProduct.setStockQuantity(productDto.getStockQuantity());
        existingProduct.setMinimumStockLevel(productDto.getMinimumStockLevel());
        existingProduct.setSpecifications(productDto.getSpecifications());
        existingProduct.setFeatured(productDto.isFeatured());
        existingProduct.setNewArrival(productDto.isNewArrival());
        existingProduct.setCategory(category);
        existingProduct.setBrand(brand);
        existingProduct.setWarranty(warranty);

        // --- Image Handling ---
        if (imageFile != null && !imageFile.isEmpty()) {
            log.info("New image file received for update: {}", imageFile.getOriginalFilename());
            // Delete the old image file if it exists
            if (existingProduct.getImagePath() != null && !existingProduct.getImagePath().isEmpty()) {
                deleteImage(existingProduct.getImagePath());
            }
            // Save the new image file
            String uniqueImageName = saveImage(imageFile);
            existingProduct.setImagePath(uniqueImageName); // Update with new image name/path
        } else {
            // If no new image file, retain the existing image path from DTO (if provided) or keep the old one
            if (productDto.getImagePath() != null) {
                // This case might be used if only the path string is updated without a file upload
                // (e.g., referencing an external image URL), but usually you'd handle file uploads.
                // Ensure you don't delete the old file if the path is just being updated without a new file.
                existingProduct.setImagePath(productDto.getImagePath());
            }
            // If imageFile is null AND productDto.getImagePath() is null/empty, the existing image path remains unchanged.
        }
        // --------------------

        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Product updated successfully with ID: {}", updatedProduct.getProductId());
        return modelMapper.map(updatedProduct, ProductDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProductDtoById(Long productId) {
        log.info("Fetching product DTO with ID: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "ID", productId));
        return modelMapper.map(product, ProductDto.class);
    }


    @Override
    @Transactional(readOnly = true)
    public Product getProductEntityById(Long productId) {
        log.info("Fetching product entity with ID: {}", productId);
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "ID", productId));
    }


    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> getAllProducts(Pageable pageable) {
        log.info("Fetching all products page: {}", pageable);
        Page<Product> productPage = productRepository.findAll(pageable);
        return productPage.map(product -> modelMapper.map(product, ProductDto.class)); // Simpler mapping for Page
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId) {
        log.warn("Attempting to delete product with ID: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "ID", productId));

        // --- Image Handling ---
        if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
            if (!product.getImagePath().equals("default_product_image.jpg")){
                deleteImage(product.getImagePath()); // Delete associated image file
            }
        }
        // --------------------

        productRepository.delete(product);
        log.warn("Product deleted successfully with ID: {}", productId);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> getFeaturedProducts(Pageable pageable) {
        log.info("Fetching featured products page: {}", pageable);
        // Ensure findByIsFeaturedTrue(Pageable) exists in ProductRepository
        Page<Product> products = productRepository.findByIsFeaturedTrue(pageable);
        return products.map(product -> modelMapper.map(product, ProductDto.class));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> getNewProducts(Pageable pageable) {
        log.info("Fetching new arrival products page: {}", pageable);
        // Ensure findByIsNewArrivalTrue(Pageable) exists in ProductRepository
        Page<Product> products = productRepository.findByIsNewArrivalTrue(pageable);
        return products.map(product -> modelMapper.map(product, ProductDto.class));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> getProductsByCategory(Long categoryId, Pageable pageable) {
        log.info("Fetching products for category ID: {}, page: {}", categoryId, pageable);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "ID", categoryId));
        // Ensure findByCategory(Category, Pageable) exists in ProductRepository
        Page<Product> productPage = productRepository.findByCategory(category, pageable);
        return productPage.map(product -> modelMapper.map(product, ProductDto.class));
    }


    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> getProductsByBrand(Long brandId, Pageable pageable) {
        log.info("Fetching products for brand ID: {}, page: {}", brandId, pageable);
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "ID", brandId));
        // Ensure findByBrand(Brand, Pageable) exists in ProductRepository
        Page<Product> productPage = productRepository.findByBrand(brand, pageable);
        return productPage.map(product -> modelMapper.map(product, ProductDto.class));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> searchProducts(String query, Pageable pageable) {
        log.info("Searching products with query: '{}', page: {}", query, pageable);
        // Ensure findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String, String, Pageable) exists
        Page<Product> productPage = productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query, pageable);
        return productPage.map(product -> modelMapper.map(product, ProductDto.class));
    }

    // Filtered Products - Keeping your logic structure but returning Page<ProductDto>
    // IMPORTANT: Requires corresponding methods in ProductRepository accepting Pageable!

    @Transactional(readOnly = true)
    @Override
    public Page<ProductDto> getFilteredProducts(Long categoryId, Long brandId, String priceRange, Double minPrice, Double maxPrice, Pageable pageable) {
        log.info("Filtering products - Category: {}, Brand: {}, PriceRange: {}, MinPrice: {}, MaxPrice: {}, Page: {}",
                categoryId, brandId, priceRange, minPrice, maxPrice, pageable);

        Page<Product> productPage;
        BigDecimal min = (minPrice != null) ? BigDecimal.valueOf(minPrice) : null;
        BigDecimal max = (maxPrice != null) ? BigDecimal.valueOf(maxPrice) : null;

        // --- Price Range Parsing ---
        if (min == null && max == null && priceRange != null && !priceRange.trim().isEmpty()) {
            try {
                if (priceRange.endsWith("+")) {
                    min = BigDecimal.valueOf(Double.parseDouble(priceRange.substring(0, priceRange.length() - 1)));
                } else if (priceRange.contains("-")) {
                    String[] parts = priceRange.split("-");
                    if (parts.length == 2) {
                        min = BigDecimal.valueOf(Double.parseDouble(parts[0]));
                        max = BigDecimal.valueOf(Double.parseDouble(parts[1]));
                    }
                }
            } catch (NumberFormatException e) {
                log.error("Invalid price range format: {}", priceRange, e);
                // Decide how to handle invalid range - ignore it, throw exception?
                // Ignoring for now, will proceed with other filters.
                min = null;
                max = null;
            }
        }
        // --- End Price Range Parsing ---

        // --- Querying (Requires matching methods in ProductRepository accepting Pageable) ---
        // !!! IMPORTANT: You MUST define these specific methods in your ProductRepository interface !!!
        // Using JPA Specifications or Querydsl is STRONGLY recommended for this complexity.

        if (categoryId != null && brandId != null && min != null && max != null) {
            productPage = productRepository.findByCategory_CategoryIdAndBrand_BrandIdAndPriceBetween(categoryId, brandId, min, max, pageable);
        } else if (categoryId != null && brandId != null && min != null) { // Handling PriceGreaterThan combo
            productPage = productRepository.findByCategory_CategoryIdAndBrand_BrandIdAndPriceGreaterThanEqual(categoryId, brandId, min, pageable); // Assuming '>=' for '+' range
        } else if (categoryId != null && min != null && max != null) {
            productPage = productRepository.findByCategory_CategoryIdAndPriceBetween(categoryId, min, max, pageable);
        } else if (brandId != null && min != null && max != null) {
            productPage = productRepository.findByBrand_BrandIdAndPriceBetween(brandId, min, max, pageable);
        } else if (categoryId != null && min != null) { // Handling PriceGreaterThan combo
            productPage = productRepository.findByCategory_CategoryIdAndPriceGreaterThanEqual(categoryId, min, pageable); // Assuming '>=' for '+' range
        } else if (brandId != null && min != null) { // Handling PriceGreaterThan combo
            productPage = productRepository.findByBrand_BrandIdAndPriceGreaterThanEqual(brandId, min, pageable); // Assuming '>=' for '+' range
        } else if (min != null && max != null) {
            productPage = productRepository.findByPriceBetween(min, max, pageable);
        } else if (min != null) { // Handling PriceGreaterThan combo
            productPage = productRepository.findByPriceGreaterThanEqual(min, pageable); // Assuming '>=' for '+' range
        } else if (categoryId != null && brandId != null) {
            productPage = productRepository.findByCategory_CategoryIdAndBrand_BrandId(categoryId, brandId, pageable);
        } else if (categoryId != null) {
            productPage = productRepository.findByCategory_CategoryId(categoryId, pageable);
        } else if (brandId != null) {
            productPage = productRepository.findByBrand_BrandId(brandId, pageable);
        } else {
            productPage = productRepository.findAll(pageable); // No specific filters
        }
        // --- End Querying ---

        log.info("Found {} products after filtering", productPage.getTotalElements());
        return productPage.map(product -> modelMapper.map(product, ProductDto.class));
    }

    @Transactional
    public void toggleFeaturedStatus(Long productId) {
        log.info("Toggling featured status for product ID: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "ID", productId));
        product.setFeatured(!product.isFeatured()); // Flip the boolean value
        productRepository.save(product);
        log.info("Featured status for product ID {} updated to: {}", productId, product.isFeatured());
    }

    @Transactional
    public void toggleNewArrivalStatus(Long productId) {
        log.info("Toggling new arrival status for product ID: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "ID", productId));
        product.setNewArrival(!product.isNewArrival()); // Flip the boolean value
        productRepository.save(product);
        log.info("New arrival status for product ID {} updated to: {}", productId, product.isNewArrival());
    }



    /**
     * Finds more products from the same brand, excluding the current product.
     * @param currentProductId The ID of the product being viewed.
     * @param brandId The ID of the brand to find products in.
     * @param limit The maximum number of products to return.
     * @return A List of related ProductDto objects from the same brand.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> findMoreByBrand(Long currentProductId, Long brandId, int limit) {
        log.info("Finding more products for product ID {} in brand ID {}", currentProductId, brandId);
        if (brandId == null) {
            return Collections.emptyList(); // Cannot find by brand if brandId is null
        }
        // Create page request to limit results
        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());

        // Find products in the same brand, excluding the current one
        // Requires findByBrand_BrandIdAndProductIdNot method in repository
        Page<Product> moreProductsPage = productRepository.findByBrand_BrandIdAndProductIdNot(brandId, currentProductId, pageable);

        return moreProductsPage.getContent().stream()
                .map(this::mapToDto) // Use helper
                .collect(Collectors.toList());
    }

    // --- UPDATED getFilteredProducts ---
    @Transactional(readOnly = true)
    public Page<ProductDto> getFilteredProducts(
            Long categoryId, Long brandId, String priceRange,
            Double minPrice, Double maxPrice, Boolean newArrival, Pageable pageable) {

        log.info("Filtering products - Category: {}, Brand: {}, PriceRange: {}, MinPrice: {}, MaxPrice: {}, NewArrival: {}, Page: {}",
                categoryId, brandId, priceRange, minPrice, maxPrice, newArrival, pageable);

        Page<Product> productPage;
        BigDecimal min = (minPrice != null) ? BigDecimal.valueOf(minPrice) : null;
        BigDecimal max = (maxPrice != null) ? BigDecimal.valueOf(maxPrice) : null;

        // --- Price Range Parsing (Only if min/max not provided directly) ---
        if (min == null && max == null && priceRange != null && !priceRange.trim().isEmpty()) {
            try {
                if (priceRange.endsWith("+")) {
                    min = BigDecimal.valueOf(Double.parseDouble(priceRange.substring(0, priceRange.length() - 1)));
                    max = null; // Explicitly null for greater than
                } else if (priceRange.contains("-")) {
                    String[] parts = priceRange.split("-");
                    if (parts.length == 2) {
                        min = BigDecimal.valueOf(Double.parseDouble(parts[0]));
                        max = BigDecimal.valueOf(Double.parseDouble(parts[1]));
                    }
                }
            } catch (NumberFormatException e) {
                log.error("Invalid price range format: {}", priceRange, e);
                min = null; max = null; // Ignore invalid range
            }
        }
        // --- End Price Range Parsing ---

        // --- Determine active filters ---
        boolean hasCategory = (categoryId != null && categoryId > 0);
        boolean hasBrand = (brandId != null && brandId > 0);
        boolean hasMinPrice = (min != null);
        boolean hasMaxPrice = (max != null);
        boolean filterByNew = (newArrival != null && newArrival);
        // --- End Determine active filters ---


        // --- Querying Logic (Refined Order) ---
        // !!! IMPORTANT: Requires corresponding repository methods accepting Pageable !!!
        // !!! Using JPA Specifications is strongly recommended for maintainability !!!

        log.debug("Filtering with: hasCategory={}, hasBrand={}, hasMinPrice={}, hasMaxPrice={}, filterByNew={}",
                hasCategory, hasBrand, hasMinPrice, hasMaxPrice, filterByNew);

        if (hasCategory && hasBrand && hasMinPrice && hasMaxPrice) {
            productPage = filterByNew ?
                    productRepository.findByCategory_CategoryIdAndBrand_BrandIdAndPriceBetweenAndIsNewArrivalTrue(categoryId, brandId, min, max, pageable) :
                    productRepository.findByCategory_CategoryIdAndBrand_BrandIdAndPriceBetween(categoryId, brandId, min, max, pageable);
        } else if (hasCategory && hasBrand && hasMinPrice) { // Min price only (>=)
            productPage = filterByNew ?
                    productRepository.findByCategory_CategoryIdAndBrand_BrandIdAndPriceGreaterThanEqualAndIsNewArrivalTrue(categoryId, brandId, min, pageable) :
                    productRepository.findByCategory_CategoryIdAndBrand_BrandIdAndPriceGreaterThanEqual(categoryId, brandId, min, pageable);
        } else if (hasCategory && hasMinPrice && hasMaxPrice) {
            productPage = filterByNew ?
                    productRepository.findByCategory_CategoryIdAndPriceBetweenAndIsNewArrivalTrue(categoryId, min, max, pageable) :
                    productRepository.findByCategory_CategoryIdAndPriceBetween(categoryId, min, max, pageable);
        } else if (hasBrand && hasMinPrice && hasMaxPrice) {
            productPage = filterByNew ?
                    productRepository.findByBrand_BrandIdAndPriceBetweenAndIsNewArrivalTrue(brandId, min, max, pageable) :
                    productRepository.findByBrand_BrandIdAndPriceBetween(brandId, min, max, pageable);
        } else if (hasCategory && hasBrand) {
            productPage = filterByNew ?
                    productRepository.findByCategory_CategoryIdAndBrand_BrandIdAndIsNewArrivalTrue(categoryId, brandId, pageable) :
                    productRepository.findByCategory_CategoryIdAndBrand_BrandId(categoryId, brandId, pageable);
        } else if (hasCategory && hasMinPrice) { // Min price only (>=)
            productPage = filterByNew ?
                    productRepository.findByCategory_CategoryIdAndPriceGreaterThanEqualAndIsNewArrivalTrue(categoryId, min, pageable) :
                    productRepository.findByCategory_CategoryIdAndPriceGreaterThanEqual(categoryId, min, pageable);
        } else if (hasBrand && hasMinPrice) { // Min price only (>=)
            productPage = filterByNew ?
                    productRepository.findByBrand_BrandIdAndPriceGreaterThanEqualAndIsNewArrivalTrue(brandId, min, pageable) :
                    productRepository.findByBrand_BrandIdAndPriceGreaterThanEqual(brandId, min, pageable);
        } else if (hasMinPrice && hasMaxPrice) { // *** Price Range Only ***
            log.debug("Executing findByPriceBetween branch"); // Add specific log
            productPage = filterByNew ?
                    productRepository.findByPriceBetweenAndIsNewArrivalTrue(min, max, pageable) :
                    productRepository.findByPriceBetween(min, max, pageable); // <-- Should hit this branch
        } else if (hasMinPrice) { // Min price only (>=)
            productPage = filterByNew ?
                    productRepository.findByPriceGreaterThanEqualAndIsNewArrivalTrue(min, pageable) :
                    productRepository.findByPriceGreaterThanEqual(min, pageable);
        } else if (hasCategory) {
            productPage = filterByNew ?
                    productRepository.findByCategory_CategoryIdAndIsNewArrivalTrue(categoryId, pageable) :
                    productRepository.findByCategory_CategoryId(categoryId, pageable);
        } else if (hasBrand) {
            productPage = filterByNew ?
                    productRepository.findByBrand_BrandIdAndIsNewArrivalTrue(brandId, pageable) :
                    productRepository.findByBrand_BrandId(brandId, pageable);
        } else if (filterByNew) {
            productPage = productRepository.findByIsNewArrivalTrue(pageable);
        } else {
            log.debug("Executing findAll branch");
            productPage = productRepository.findAll(pageable); // No specific filters
        }
        // --- End Querying ---

        log.info("Found {} products after filtering", productPage.getTotalElements());
        return productPage.map(this::mapToDto); // Use helper
    }


    /**
     * Finds related products, e.g., by category, excluding the current product.
     * @param currentProductId The ID of the product being viewed.
     * @param categoryId The ID of the category to find related products in.
     * @param limit The maximum number of related products to return.
     * @return A List of related ProductDto objects.
     */
    @Transactional(readOnly = true)
    public List<ProductDto> findRelatedProducts(Long currentProductId, Long categoryId, int limit) {
        log.info("Finding related products for product ID {} in category ID {}", currentProductId, categoryId);
        if (categoryId == null) {
            return Collections.emptyList(); // Cannot find related by category if categoryId is null
        }
        // Create page request to limit results and maybe sort (e.g., by creation date)
        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());

        // Find products in the same category, excluding the current one
        Page<Product> relatedProductsPage = productRepository.findByCategory_CategoryIdAndProductIdNot(categoryId, currentProductId, pageable);

        return relatedProductsPage.getContent().stream()
                .map(this::mapToDto) // Use helper
                .collect(Collectors.toList());
    }



    @Override
    @Transactional(readOnly = true)
    public long countTotalProducts() {
        return productRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countLowStockProducts(int threshold) {
        return productRepository.countByStockQuantityLessThanEqual(threshold);
    }

    // Add this if you need to get the actual low stock products
    @Transactional(readOnly = true)
    public List<ProductDto> getLowStockProductsList(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findByStockQuantityLessThanEqual(limit, pageable)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // --- Helper Methods ---

    // Helper method for mapping Entity to DTO
    private ProductDto mapToDto(Product product) {
        ProductDto dto = modelMapper.map(product, ProductDto.class);
        // Manually map related entity names and details
        if (product.getCategory() != null) {
            dto.setCategoryName(product.getCategory().getName());
            // categoryId is mapped automatically by ModelMapper if names match
        }
        if (product.getBrand() != null) {
            dto.setBrandName(product.getBrand().getName());
            // brandId is mapped automatically
        }
        if (product.getWarranty() != null) {
            dto.setWarrantyId(product.getWarranty().getWarrantyId()); // Ensure ID is set
            dto.setWarrantyType(product.getWarranty().getType());
            dto.setWarrantyDurationMonths(product.getWarranty().getDurationMonths());
            dto.setWarrantyTerms(product.getWarranty().getTerms());
        }
        // Derived/read-only fields
        dto.setInStock(product.isInStock()); // Ensure Product entity has isInStock() getter or Lombok generates it
        // Timestamps are mapped automatically if names match
        return dto;
    }

    // Helper method for mapping DTO to Entity (basic fields only)
    private Product mapToEntity(ProductDto productDto) {
        // Note: This doesn't set Category/Brand/Warranty - those are fetched by ID
        Product product = modelMapper.map(productDto, Product.class);
        // Ensure ID is not carried over when mapping for creation
        if (productDto.getProductId() == null) {
            product.setProductId(null);
        }
        return product;
    }



    // --- Helper Methods for Image Handling (Example using local filesystem) ---

    private String saveImage(MultipartFile imageFile) {
        String originalFilename = imageFile.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        // Generate a unique filename to prevent collisions
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        Path filePath = Paths.get(imageStoragePath, uniqueFileName);

        try {
            // Create directory if it doesn't exist
            Files.createDirectories(Paths.get(imageStoragePath));
            // Save the file
            Files.copy(imageFile.getInputStream(), filePath);
            log.info("Image saved successfully: {}", uniqueFileName);
            return uniqueFileName; // Return the unique name to store in DB
        } catch (IOException e) {
            log.error("Failed to save image file: {}", originalFilename, e);
            // Handle appropriately - maybe throw a custom FileStorageException
            throw new RuntimeException("Failed to save image file", e);
        }
    }

    private void deleteImage(String imageFileName) {
        Path filePath = Paths.get(imageStoragePath, imageFileName);
        try {
            Files.deleteIfExists(filePath);
            log.info("Image deleted successfully: {}", imageFileName);
        } catch (IOException e) {
            log.error("Failed to delete image file: {}", imageFileName, e);
            // Handle error - log or potentially throw exception if deletion failure is critical
        }
    }



}
