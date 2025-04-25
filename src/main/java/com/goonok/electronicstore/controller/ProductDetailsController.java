package com.goonok.electronicstore.controller;

import com.goonok.electronicstore.dto.ProductDto;
import com.goonok.electronicstore.exception.ResourceNotFoundException; // Ensure this is defined
import com.goonok.electronicstore.model.Brand;
import com.goonok.electronicstore.model.Category;
import com.goonok.electronicstore.service.BrandService; // Assume this service exists
import com.goonok.electronicstore.service.CategoryService; // Assume this service exists
import com.goonok.electronicstore.service.interfaces.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/products") // Base mapping for product-related URLs
public class ProductDetailsController {

    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService; // Assuming this service exists
    @Autowired
    private BrandService brandService;       // Assuming this service exists

    /**
     * Handles displaying the main product listing page with filtering and pagination.
     *
     * @param categoryId Optional category ID filter.
     * @param brandId    Optional brand ID filter.
     * @param priceRange Optional predefined price range string (e.g., "1000-5000", "10000+").
     * @param minPrice   Optional minimum price filter (takes precedence over priceRange).
     * @param maxPrice   Optional maximum price filter (takes precedence over priceRange).
     * @param page       Page number (default is 0).
     * @param size       Number of items per page (default is 12).
     * @param sort       Sorting criteria (default is 'name,asc').
     * @param model      The Spring MVC model.
     * @return The view name for the product listing page.
     */
    @GetMapping
    public String listProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String priceRange, // Keep for potential UI elements
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Boolean newArrival, // <-- ADDED RequestParam
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "name,asc") String sort, // Example: name,asc or price,desc
            Model model) {

        log.info("Request received for product listing with filters - Category: {}, Brand: {}, PriceRange: {}, MinPrice: {}, MaxPrice: {}, Page: {}, Size: {}, Sort: {}",
                categoryId, brandId, priceRange, minPrice, maxPrice, page, size, sort);

        // --- Create Pageable (Keep as is) ---
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDirection = (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")) ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        // -----------------------


        // --- Fetch Filtered Products ---
        // Pass minPrice/maxPrice directly, let service handle priceRange parsing if needed
        Page<ProductDto> productPage = productService.getFilteredProducts(
                categoryId, brandId, priceRange, minPrice, maxPrice, newArrival, pageable
        );
        // ---------------------------

        // --- Add data to Model ---
        model.addAttribute("productPage", productPage); // Contains products and pagination info
        model.addAttribute("categories", categoryService.getAllCategories()); // For filter dropdowns
        model.addAttribute("brands", brandService.getAllBrands());           // For filter dropdowns

        // Pass filter parameters back to the view to maintain state
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedBrandId", brandId);
        model.addAttribute("selectedPriceRange", priceRange); // Keep if UI uses it
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("newArrival", newArrival);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("sort", sort);
        // -----------------------

        // Return the view name (adjust path as needed)
        return "product/product-listing"; // e.g., templates/product/product-listing.html
    }

    /**
     * Displays the details page for a single product.
     *
     * @param id    The ID of the product to display.
     * @param model The Spring MVC model.
     * @return The view name for the product details page.
     */
    @GetMapping("/{id}")
    public String showProductDetails(@PathVariable Long id, Model model) {
        log.info("Request received for product details: ID={}", id);
        try {
            ProductDto productDto = productService.getProductDtoById(id);
            model.addAttribute("product", productDto);
            model.addAttribute("pageTitle", productDto.getName() + " - Details");

            // --- Fetch additional product lists ---
            int relatedLimit = 4; // Number of products to show in each section
            Pageable relatedPageable = PageRequest.of(0, relatedLimit, Sort.by("createdAt").descending());

            List<ProductDto> moreFromBrand = Collections.emptyList();
            if (productDto.getBrandId() != null) {
                // Assuming a method like findMoreByBrand exists in ProductService
                // You might need to create this method similar to findRelatedProducts
                //moreFromBrand = productService.findMoreByBrand(id, productDto.getBrandId(), relatedLimit);
            }

            Page<ProductDto> newProductsPage = productService.getNewProducts(relatedPageable);
            Page<ProductDto> featuredProductsPage = productService.getFeaturedProducts(relatedPageable); // Using featured as popular

            model.addAttribute("moreFromBrand", moreFromBrand); // Add the list
            model.addAttribute("newProducts", newProductsPage.getContent());
            model.addAttribute("featuredProducts", featuredProductsPage.getContent()); // Add the list
            // --- End fetching additional lists ---

            // TODO: Fetch and add reviews if needed

            return "product/product-details";

        } catch (ResourceNotFoundException e) {
            log.error("Product not found for ID: {}", id, e);
            // Redirect to an error page or product listing with an error message
            model.addAttribute("error", "Product not found.");
            return "error/404"; // Or redirect:/products?error=notfound
        }
    }

    /**
     * Displays a list of products filtered by brand.
     * (Note: This might be redundant if the main /products endpoint handles filtering well)
     *
     * @param brandId The ID of the brand to filter by.
     * @param page    Page number.
     * @param size    Page size.
     * @param sort    Sorting criteria.
     * @param model   The Spring MVC model.
     * @return The view name for the product listing page.
     */
    @GetMapping("/brand/{brandId}")
    public String showProductsByBrand(
            @PathVariable Long brandId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "name,asc") String sort,
            Model model) {

        log.info("Request received for products by brand: ID={}, Page: {}, Size: {}, Sort: {}", brandId, page, size, sort);

        String[] sortParams = sort.split(",");
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")) ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortParams[0]
        ));

        Page<ProductDto> productPage = productService.getProductsByBrand(brandId, pageable);
        Optional<Brand> brandOpt = brandService.getBrandById(brandId); // Assuming BrandService exists

        model.addAttribute("productPage", productPage);
        model.addAttribute("filterType", "Brand");
        model.addAttribute("filterValue", brandOpt.map(Brand::getName).orElse("Unknown Brand"));
        // Add categories/brands for potential further filtering on the page
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("brands", brandService.getAllBrands());
        model.addAttribute("selectedBrandId", brandId); // Keep track of the current filter
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("sort", sort);


        return "product/product-listing"; // Reuse the main listing view
    }

    /**
     * Displays a list of products filtered by category.
     * (Note: This might be redundant if the main /products endpoint handles filtering well)
     *
     * @param categoryId The ID of the category to filter by.
     * @param page       Page number.
     * @param size       Page size.
     * @param sort       Sorting criteria.
     * @param model      The Spring MVC model.
     * @return The view name for the product listing page.
     */
    @GetMapping("/category/{categoryId}")
    public String showProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "name,asc") String sort,
            Model model) {

        log.info("Request received for products by category: ID={}, Page: {}, Size: {}, Sort: {}", categoryId, page, size, sort);

        String[] sortParams = sort.split(",");
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")) ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortParams[0]
        ));

        Page<ProductDto> productPage = productService.getProductsByCategory(categoryId, pageable);
        Optional<Category> categoryOpt = categoryService.getCategoryById(categoryId); // Assuming CategoryService exists

        model.addAttribute("productPage", productPage);
        model.addAttribute("filterType", "Category");
        model.addAttribute("filterValue", categoryOpt.map(Category::getName).orElse("Unknown Category"));
        // Add categories/brands for potential further filtering on the page
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("brands", brandService.getAllBrands());
        model.addAttribute("selectedCategoryId", categoryId); // Keep track of the current filter
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("sort", sort);

        return "product/product-listing"; // Reuse the main listing view
    }

    // Add search endpoint if needed
    @GetMapping("/search")
    public String searchProducts(
            @RequestParam("query") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "name,asc") String sort,
            Model model) {

        log.info("Request received for product search: Query='{}', Page: {}, Size: {}, Sort: {}", query, page, size, sort);

        String[] sortParams = sort.split(",");
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")) ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortParams[0]
        ));

        Page<ProductDto> productPage = productService.searchProducts(query, pageable);

        model.addAttribute("productPage", productPage);
        model.addAttribute("searchQuery", query); // Pass query back to display it
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("brands", brandService.getAllBrands());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("sort", sort);

        return "product/product-listing"; // Reuse the main listing view
    }
}
