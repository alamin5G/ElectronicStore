package com.goonok.electronicstore.controller;



import com.goonok.electronicstore.model.Brand;
import com.goonok.electronicstore.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/admin/brand")
public class BrandController {

    @Autowired
    private BrandService brandService;

    @GetMapping
    public String listBrands(Model model) {
        model.addAttribute("brands", brandService.getAllBrands());
        return "brand/list"; // Path to brand list.html
    }

    @GetMapping("/add")
    public String showAddBrandForm(Model model) {
        model.addAttribute("brand", new Brand());
        return "brand/add"; // Path to add.html
    }

    @PostMapping("/add")
    public String addBrand(@ModelAttribute Brand brand) {
        brandService.addBrand(brand);
        return "redirect:/brands";
    }

    @GetMapping("/edit/{id}")
    public String showEditBrandForm(@PathVariable Long id, Model model) {
        Brand brand = brandService.getBrandById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found"));
        model.addAttribute("brand", brand);
        return "brand/edit"; // Path to edit.html
    }

    @PostMapping("/edit/{id}")
    public String updateBrand(@PathVariable Long id, @ModelAttribute Brand brand) {
        brandService.updateBrand(id, brand);
        return "redirect:/brands";
    }

    @GetMapping("/delete/{id}")
    public String deleteBrand(@PathVariable Long id) {
        brandService.deleteBrand(id);
        return "redirect:/brands";
    }

}

