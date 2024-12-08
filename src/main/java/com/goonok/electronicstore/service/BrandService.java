package com.goonok.electronicstore.service;


import com.goonok.electronicstore.model.Brand;
import com.goonok.electronicstore.model.Brand;
import com.goonok.electronicstore.repository.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BrandService {

    @Autowired
    private BrandRepository brandRepository;

    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    public Optional<Brand> getBrandById(Long id) {
        return brandRepository.findById(id);
    }

    public Brand addBrand(Brand Brand) {
        return brandRepository.save(Brand);
    }

    public void deleteBrand(Long id) {
        brandRepository.deleteById(id);
    }

    public Brand updateBrand(Long id, Brand Brand) {
        Optional<Brand> existingBrand = brandRepository.findById(id);
        if (existingBrand.isPresent()) {
            Brand updatedBrand = existingBrand.get();
            updatedBrand.setName(Brand.getName());
            updatedBrand.setDescription(Brand.getDescription());
            return brandRepository.save(updatedBrand);
        }
        throw new RuntimeException("Brand not found with ID: " + id);
    }
}