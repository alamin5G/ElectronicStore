package com.goonok.electronicstore.service;

import com.goonok.electronicstore.dto.CategoryDto;
import com.goonok.electronicstore.exception.ResourceNotFoundException; // Ensure this is defined
import com.goonok.electronicstore.model.Category;
import com.goonok.electronicstore.repository.CategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    /**
     * Creates a new category.
     *
     * @param categoryDto DTO containing category details.
     * @return The created CategoryDto.
     * @throws IllegalArgumentException if a category with the same name already exists.
     */
    @Transactional
    public CategoryDto createCategory(CategoryDto categoryDto) {
        log.info("Attempting to create category: {}", categoryDto.getName());

        // Check if category name already exists (case-insensitive)
        categoryRepository.findByNameIgnoreCase(categoryDto.getName()).ifPresent(c -> {
            throw new IllegalArgumentException("Category with name '" + categoryDto.getName() + "' already exists.");
        });

        Category category = modelMapper.map(categoryDto, Category.class);
        // Timestamps are handled by annotations
        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with ID: {}", savedCategory.getCategoryId());
        return modelMapper.map(savedCategory, CategoryDto.class);
    }

    /**
     * Updates an existing category.
     *
     * @param categoryId The ID of the category to update.
     * @param categoryDto DTO containing updated details.
     * @return The updated CategoryDto.
     * @throws ResourceNotFoundException if the category is not found.
     * @throws IllegalArgumentException if the updated name conflicts with another existing category.
     */
    @Transactional
    public CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto) {
        log.info("Attempting to update category with ID: {}", categoryId);
        Category existingCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "ID", categoryId));

        // Check if new name conflicts with another existing category
        if (!existingCategory.getName().equalsIgnoreCase(categoryDto.getName())) {
            categoryRepository.findByNameIgnoreCase(categoryDto.getName()).ifPresent(c -> {
                throw new IllegalArgumentException("Another category with name '" + categoryDto.getName() + "' already exists.");
            });
        }

        // Update fields
        existingCategory.setName(categoryDto.getName());
        existingCategory.setDescription(categoryDto.getDescription());
        // updatedAt timestamp handled by annotation

        Category updatedCategory = categoryRepository.save(existingCategory);
        log.info("Category updated successfully: ID={}", updatedCategory.getCategoryId());
        return modelMapper.map(updatedCategory, CategoryDto.class);
    }

    /**
     * Deletes a category by its ID.
     *
     * @param categoryId The ID of the category to delete.
     * @throws ResourceNotFoundException if the category is not found.
     * @throws IllegalStateException if the category is associated with products.
     */
    @Transactional
    public void deleteCategory(Long categoryId) {
        log.warn("Attempting to delete category with ID: {}", categoryId);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "ID", categoryId));

        // IMPORTANT: Check if category is associated with any products before deleting
        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            log.error("Attempted to delete category ID {} which has associated products.", categoryId);
            throw new IllegalStateException("Cannot delete category with ID " + categoryId + " as it is associated with products. Reassign products first.");
        }

        categoryRepository.delete(category);
        log.warn("Category deleted successfully: ID={}", categoryId);
    }

    /**
     * Retrieves a category by its ID as a DTO.
     *
     * @param categoryId The ID of the category.
     * @return CategoryDto if found.
     * @throws ResourceNotFoundException if the category is not found.
     */
    @Transactional(readOnly = true)
    public CategoryDto getCategoryDtoById(Long categoryId) {
        log.info("Fetching category DTO with ID: {}", categoryId);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "ID", categoryId));
        return modelMapper.map(category, CategoryDto.class);
    }

    /**
     * Retrieves a category entity by its ID.
     *
     * @param categoryId The ID of the category.
     * @return Optional<Category>.
     */
    @Transactional(readOnly = true)
    public Optional<Category> getCategoryById(Long categoryId) {
        log.info("Fetching category entity with ID: {}", categoryId);
        return categoryRepository.findById(categoryId);
    }


    /**
     * Retrieves all categories, sorted by name.
     * Useful for dropdowns.
     *
     * @return List of all Category entities.
     */
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        log.info("Fetching all categories");
        return categoryRepository.findAll(Sort.by("name"));
    }

    /**
     * Retrieves all categories with pagination.
     *
     * @param pageable Pagination information.
     * @return Page of CategoryDto objects.
     */
    @Transactional(readOnly = true)
    public Page<CategoryDto> getAllCategories(Pageable pageable) {
        log.info("Fetching categories page: {}", pageable);
        Page<Category> categoryPage = categoryRepository.findAll(pageable);
        return categoryPage.map(category -> modelMapper.map(category, CategoryDto.class));
    }
}
