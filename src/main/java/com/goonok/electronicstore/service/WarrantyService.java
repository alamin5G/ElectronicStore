package com.goonok.electronicstore.service;

import com.goonok.electronicstore.dto.WarrantyDto; // Import DTO
import com.goonok.electronicstore.exception.ResourceNotFoundException; // Ensure this is defined
import com.goonok.electronicstore.model.Warranty;
import com.goonok.electronicstore.repository.WarrantyRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page; // Import Page
import org.springframework.data.domain.Pageable; // Import Pageable
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // Keep for List mapping if needed

@Slf4j
@Service
public class WarrantyService {

    @Autowired
    private WarrantyRepository warrantyRepository;

    @Autowired
    private ModelMapper modelMapper;

    // --- Create ---
    @Transactional
    public WarrantyDto createWarranty(WarrantyDto warrantyDto) {
        log.info("Attempting to create warranty: Type={}, Duration={} months", warrantyDto.getType(), warrantyDto.getDurationMonths());
        warrantyRepository.findByTypeIgnoreCase(warrantyDto.getType()).ifPresent(w -> {
            throw new IllegalArgumentException("Warranty with type '" + warrantyDto.getType() + "' already exists.");
        });
        Warranty warranty = modelMapper.map(warrantyDto, Warranty.class);
        Warranty savedWarranty = warrantyRepository.save(warranty);
        log.info("Warranty created successfully with ID: {}", savedWarranty.getWarrantyId());
        return modelMapper.map(savedWarranty, WarrantyDto.class);
    }

    // --- Update ---
    @Transactional
    public WarrantyDto updateWarranty(Long warrantyId, WarrantyDto warrantyDto) {
        log.info("Attempting to update warranty with ID: {}", warrantyId);
        Warranty existingWarranty = warrantyRepository.findById(warrantyId)
                .orElseThrow(() -> new ResourceNotFoundException("Warranty", "ID", warrantyId));

        if (!existingWarranty.getType().equalsIgnoreCase(warrantyDto.getType())) {
            warrantyRepository.findByTypeIgnoreCase(warrantyDto.getType()).ifPresent(w -> {
                throw new IllegalArgumentException("Another warranty with type '" + warrantyDto.getType() + "' already exists.");
            });
        }

        existingWarranty.setType(warrantyDto.getType());
        existingWarranty.setDurationMonths(warrantyDto.getDurationMonths());
        existingWarranty.setDescription(warrantyDto.getDescription());
        existingWarranty.setTerms(warrantyDto.getTerms());

        Warranty updatedWarranty = warrantyRepository.save(existingWarranty);
        log.info("Warranty updated successfully: ID={}", updatedWarranty.getWarrantyId());
        return modelMapper.map(updatedWarranty, WarrantyDto.class);
    }

    // --- Delete ---
    @Transactional
    public void deleteWarranty(Long warrantyId) {
        log.warn("Attempting to delete warranty with ID: {}", warrantyId);
        Warranty warranty = warrantyRepository.findById(warrantyId)
                .orElseThrow(() -> new ResourceNotFoundException("Warranty", "ID", warrantyId));

        if (warranty.getProducts() != null && !warranty.getProducts().isEmpty()) {
            log.error("Attempted to delete warranty ID {} which has associated products.", warrantyId);
            throw new IllegalStateException("Cannot delete warranty with ID " + warrantyId + " as it is associated with products. Reassign products first.");
        }

        warrantyRepository.delete(warranty);
        log.warn("Warranty deleted successfully: ID={}", warrantyId);
    }

    // --- Read Single ---
    @Transactional(readOnly = true)
    public WarrantyDto getWarrantyDtoById(Long warrantyId) {
        log.info("Fetching warranty DTO with ID: {}", warrantyId);
        Warranty warranty = warrantyRepository.findById(warrantyId)
                .orElseThrow(() -> new ResourceNotFoundException("Warranty", "ID", warrantyId));
        return modelMapper.map(warranty, WarrantyDto.class);
    }

    @Transactional(readOnly = true)
    public Optional<Warranty> getWarrantyById(Long warrantyId) {
        log.info("Fetching warranty entity with ID: {}", warrantyId);
        return warrantyRepository.findById(warrantyId);
    }

    // --- Read All (Non-Paginated) - Useful for dropdowns ---
    /**
     * Retrieves all warranties, sorted by type.
     * Useful for populating dropdowns.
     *
     * @return List of all Warranty entities.
     */
    @Transactional(readOnly = true)
    public List<Warranty> getAllWarranties() {
        log.info("Fetching all warranties (non-paginated)");
        return warrantyRepository.findAll(Sort.by("type"));
        // If DTOs are needed for dropdowns too:
        // return warrantyRepository.findAll(Sort.by("type")).stream()
        //          .map(w -> modelMapper.map(w, WarrantyDto.class))
        //          .collect(Collectors.toList());
    }

    // --- Read All (Paginated) - Used by the Admin Controller List View ---
    /**
     * Retrieves all warranties with pagination.
     *
     * @param pageable Pagination information.
     * @return Page of WarrantyDto objects.
     */
    @Transactional(readOnly = true)
    public Page<WarrantyDto> getAllWarranties(Pageable pageable) {
        log.info("Fetching warranties page: {}", pageable);
        // Fetch Page<Warranty> from repository
        Page<Warranty> warrantyPage = warrantyRepository.findAll(pageable);
        // Map Page<Warranty> to Page<WarrantyDto> using ModelMapper
        return warrantyPage.map(warranty -> modelMapper.map(warranty, WarrantyDto.class));
    }
}
