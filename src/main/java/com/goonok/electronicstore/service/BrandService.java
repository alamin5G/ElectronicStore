package com.goonok.electronicstore.service;

import com.goonok.electronicstore.dto.BrandDto;
import com.goonok.electronicstore.exception.ResourceNotFoundException; // Ensure this is defined
import com.goonok.electronicstore.model.Brand;
import com.goonok.electronicstore.repository.BrandRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
@Service
public class BrandService {

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ModelMapper modelMapper;

    // Inject the path where brand logos will be stored
    @Value("${brand.logo.path}") // Define this property in application.properties/yml
    private String logoStoragePath;

    /**
     * Creates a new brand, potentially uploading a logo.
     *
     * @param brandDto  The DTO containing brand details.
     * @param logoFile  The uploaded logo file (can be null).
     * @return The created BrandDto.
     */
    @Transactional
    public BrandDto createBrand(BrandDto brandDto, MultipartFile logoFile) {
        log.info("Attempting to create brand: {}", brandDto.getName());

        // Optional: Check if brand name already exists
        brandRepository.findByNameIgnoreCase(brandDto.getName()).ifPresent(b -> {
            throw new IllegalArgumentException("Brand with name '" + brandDto.getName() + "' already exists.");
        });

        Brand brand = modelMapper.map(brandDto, Brand.class);

        // Handle logo upload
        if (logoFile != null && !logoFile.isEmpty()) {
            String logoFileName = saveLogo(logoFile);
            brand.setLogoUrl(logoFileName); // Store the filename/path
        } else {
            // Optionally set a default logo or leave null
            brand.setLogoUrl(null); // Or "default_brand_logo.png"
        }

        // Timestamps are handled by annotations
        Brand savedBrand = brandRepository.save(brand);
        log.info("Brand created successfully with ID: {}", savedBrand.getBrandId());
        return modelMapper.map(savedBrand, BrandDto.class);
    }

    /**
     * Updates an existing brand, potentially replacing the logo.
     *
     * @param brandId   The ID of the brand to update.
     * @param brandDto  The DTO containing updated details.
     * @param logoFile  The new logo file (can be null).
     * @return The updated BrandDto.
     */
    @Transactional
    public BrandDto updateBrand(Long brandId, BrandDto brandDto, MultipartFile logoFile) {
        log.info("Attempting to update brand with ID: {}", brandId);
        Brand existingBrand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "ID", brandId));

        // Optional: Check if new name conflicts with another existing brand
        if (!existingBrand.getName().equalsIgnoreCase(brandDto.getName())) {
            brandRepository.findByNameIgnoreCase(brandDto.getName()).ifPresent(b -> {
                throw new IllegalArgumentException("Another brand with name '" + brandDto.getName() + "' already exists.");
            });
        }

        // Update fields from DTO
        existingBrand.setName(brandDto.getName());
        existingBrand.setDescription(brandDto.getDescription());

        // Handle logo update
        if (logoFile != null && !logoFile.isEmpty()) {
            // Delete old logo if it exists
            if (existingBrand.getLogoUrl() != null && !existingBrand.getLogoUrl().isEmpty()) {
                deleteLogo(existingBrand.getLogoUrl());
            }
            // Save new logo
            String logoFileName = saveLogo(logoFile);
            existingBrand.setLogoUrl(logoFileName);
        } else {
            // If no new file, keep the existing logo path unless explicitly cleared in DTO
            // For simplicity, we assume null/empty logoUrl in DTO means keep existing if no file uploaded
            if (brandDto.getLogoUrl() == null || brandDto.getLogoUrl().trim().isEmpty()) {
                // Keep existing logoUrl if DTO doesn't provide one and no file is uploaded
                // No action needed here as we are updating existingBrand
            } else {
                // If DTO provides a path/URL without a file upload (e.g., external URL)
                // Check if it's different from current one before potentially deleting old file
                if(existingBrand.getLogoUrl() != null && !existingBrand.getLogoUrl().equals(brandDto.getLogoUrl())) {
                    // Delete old file only if path changes and no new file uploaded
                    deleteLogo(existingBrand.getLogoUrl());
                }
                existingBrand.setLogoUrl(brandDto.getLogoUrl());
            }
        }

        // Timestamps updated by annotation
        Brand updatedBrand = brandRepository.save(existingBrand);
        log.info("Brand updated successfully: ID={}", updatedBrand.getBrandId());
        return modelMapper.map(updatedBrand, BrandDto.class);
    }

    /**
     * Deletes a brand by its ID, also deleting its associated logo file.
     *
     * @param brandId The ID of the brand to delete.
     */
    @Transactional
    public void deleteBrand(Long brandId) {
        log.warn("Attempting to delete brand with ID: {}", brandId);
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "ID", brandId));

        // Check if brand is associated with any products before deleting?
        // if (!brand.getProducts().isEmpty()) {
        //     throw new IllegalStateException("Cannot delete brand with ID " + brandId + " as it is associated with products.");
        // }

        // Delete logo file if it exists
        if (brand.getLogoUrl() != null && !brand.getLogoUrl().isEmpty()) {
            deleteLogo(brand.getLogoUrl());
        }

        brandRepository.delete(brand);
        log.warn("Brand deleted successfully: ID={}", brandId);
    }

    /**
     * Retrieves a brand by its ID.
     *
     * @param brandId The ID of the brand.
     * @return BrandDto if found.
     */
    @Transactional(readOnly = true)
    public BrandDto getBrandDtoById(Long brandId) {
        log.info("Fetching brand DTO with ID: {}", brandId);
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "ID", brandId));
        return modelMapper.map(brand, BrandDto.class);
    }

    /**
     * Retrieves a brand entity by its ID.
     *
     * @param brandId The ID of the brand.
     * @return Optional<Brand>.
     */
    @Transactional(readOnly = true)
    public Optional<Brand> getBrandById(Long brandId) {
        log.info("Fetching brand entity with ID: {}", brandId);
        return brandRepository.findById(brandId);
    }


    /**
     * Retrieves all brands, suitable for dropdowns or basic lists.
     *
     * @return List of all Brand entities.
     */
    @Transactional(readOnly = true)
    public List<Brand> getAllBrands() {
        log.info("Fetching all brands");
        return brandRepository.findAll(Sort.by("name")); // Sort alphabetically
    }

    /**
     * Retrieves all brands with pagination.
     *
     * @param pageable Pagination information.
     * @return Page of BrandDto objects.
     */
    @Transactional(readOnly = true)
    public Page<BrandDto> getAllBrands(Pageable pageable) {
        log.info("Fetching brands page: {}", pageable);
        Page<Brand> brandPage = brandRepository.findAll(pageable);
        return brandPage.map(brand -> modelMapper.map(brand, BrandDto.class));
    }


    // --- Helper Methods for Logo File Handling ---

    private String saveLogo(MultipartFile logoFile) {
        String originalFilename = logoFile.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFileName = "brand-" + UUID.randomUUID().toString() + fileExtension;
        Path filePath = Paths.get(logoStoragePath, uniqueFileName);

        try {
            Files.createDirectories(Paths.get(logoStoragePath));
            Files.copy(logoFile.getInputStream(), filePath);
            log.info("Brand logo saved successfully: {}", uniqueFileName);
            return uniqueFileName; // Return the unique name
        } catch (IOException e) {
            log.error("Failed to save brand logo file: {}", originalFilename, e);
            throw new RuntimeException("Failed to save brand logo file", e); // Or custom exception
        }
    }

    private void deleteLogo(String logoFileName) {
        if (logoFileName == null || logoFileName.trim().isEmpty()) {
            return; // Nothing to delete
        }
        // Avoid deleting default paths/URLs if they are not actual filenames
        if (logoFileName.equals("default_brand_logo.png") || logoFileName.startsWith("http")) {
            log.info("Skipping deletion for default or external logo URL: {}", logoFileName);
            return;
        }

        Path filePath = Paths.get(logoStoragePath, logoFileName);
        try {
            Files.deleteIfExists(filePath);
            log.info("Brand logo deleted successfully: {}", logoFileName);
        } catch (IOException e) {
            log.error("Failed to delete brand logo file: {}", logoFileName, e);
            // Log error, but don't necessarily stop the overall process
        }
    }
}
