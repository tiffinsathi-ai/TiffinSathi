package com.tiffin_sathi.controller;

import com.tiffin_sathi.config.VendorContext;
import com.tiffin_sathi.dtos.*;
import com.tiffin_sathi.services.MealPackageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meal-packages")
@CrossOrigin(origins = "*")
public class MealPackageController {

    @Autowired
    private MealPackageService mealPackageService;

    @Autowired
    private VendorContext vendorContext;

    // Vendor endpoints
    @PostMapping
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> createMealPackage(@Valid @RequestBody CreateMealPackageDTO createMealPackageDTO) {
        try {
            Long vendorId = vendorContext.getCurrentVendorId();
            if (vendorId == null) {
                return ResponseEntity.badRequest().body("Vendor not found");
            }
            MealPackageDTO mealPackage = mealPackageService.createMealPackage(vendorId, createMealPackageDTO);
            return ResponseEntity.ok(mealPackage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/vendor/my-packages")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> getMyMealPackages() {
        try {
            Long vendorId = vendorContext.getCurrentVendorId();
            if (vendorId == null) {
                return ResponseEntity.badRequest().body("Vendor not found");
            }
            List<MealPackageDTO> mealPackages = mealPackageService.getMealPackagesByVendor(vendorId);
            return ResponseEntity.ok(mealPackages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/vendor/available")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> getMyAvailableMealPackages() {
        try {
            Long vendorId = vendorContext.getCurrentVendorId();
            if (vendorId == null) {
                return ResponseEntity.badRequest().body("Vendor not found");
            }
            List<MealPackageDTO> mealPackages = mealPackageService.getAvailableMealPackagesByVendor(vendorId);
            return ResponseEntity.ok(mealPackages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/vendor/{packageId}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> getMyMealPackageById(@PathVariable String packageId) {
        try {
            Long vendorId = vendorContext.getCurrentVendorId();
            if (vendorId == null) {
                return ResponseEntity.badRequest().body("Vendor not found");
            }
            MealPackageDTO mealPackage = mealPackageService.getMealPackageByIdAndVendor(packageId, vendorId);
            return ResponseEntity.ok(mealPackage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/vendor/{packageId}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> updateMealPackage(@PathVariable String packageId, @Valid @RequestBody UpdateMealPackageDTO updateMealPackageDTO) {
        try {
            Long vendorId = vendorContext.getCurrentVendorId();
            if (vendorId == null) {
                return ResponseEntity.badRequest().body("Vendor not found");
            }
            MealPackageDTO mealPackage = mealPackageService.updateMealPackage(packageId, vendorId, updateMealPackageDTO);
            return ResponseEntity.ok(mealPackage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/vendor/{packageId}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> deleteMealPackage(@PathVariable String packageId) {
        try {
            Long vendorId = vendorContext.getCurrentVendorId();
            if (vendorId == null) {
                return ResponseEntity.badRequest().body("Vendor not found");
            }
            mealPackageService.deleteMealPackage(packageId, vendorId);
            return ResponseEntity.ok("Meal package deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/vendor/{packageId}/toggle-availability")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> toggleMealPackageAvailability(@PathVariable String packageId) {
        try {
            Long vendorId = vendorContext.getCurrentVendorId();
            if (vendorId == null) {
                return ResponseEntity.badRequest().body("Vendor not found");
            }
            mealPackageService.toggleMealPackageAvailability(packageId, vendorId);
            return ResponseEntity.ok("Meal package availability toggled successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Public endpoints
    @GetMapping
    public ResponseEntity<?> getAllAvailableMealPackages() {
        try {
            List<MealPackageDTO> mealPackages = mealPackageService.getAllAvailableMealPackages();
            return ResponseEntity.ok(mealPackages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{packageId}")
    public ResponseEntity<?> getMealPackageById(@PathVariable String packageId) {
        try {
            MealPackageDTO mealPackage = mealPackageService.getMealPackageById(packageId);
            return ResponseEntity.ok(mealPackage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/type/{packageType}")
    public ResponseEntity<?> getMealPackagesByType(@PathVariable String packageType) {
        try {
            List<MealPackageDTO> mealPackages = mealPackageService.getMealPackagesByType(packageType);
            return ResponseEntity.ok(mealPackages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/duration/{duration}")
    public ResponseEntity<?> getMealPackagesByDuration(@PathVariable Integer duration) {
        try {
            List<MealPackageDTO> mealPackages = mealPackageService.getMealPackagesByDuration(duration);
            return ResponseEntity.ok(mealPackages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/price-range")
    public ResponseEntity<?> getMealPackagesByPriceRange(@RequestParam Double minPrice, @RequestParam Double maxPrice) {
        try {
            List<MealPackageDTO> mealPackages = mealPackageService.getMealPackagesByPriceRange(minPrice, maxPrice);
            return ResponseEntity.ok(mealPackages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}