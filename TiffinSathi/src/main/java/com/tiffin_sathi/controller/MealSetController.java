package com.tiffin_sathi.controller;

import com.tiffin_sathi.config.VendorContext;
import com.tiffin_sathi.dtos.*;
import com.tiffin_sathi.model.MealSet;
import com.tiffin_sathi.services.MealSetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meal-sets")
@CrossOrigin(origins = "*")
public class MealSetController {

    @Autowired
    private MealSetService mealSetService;

    @Autowired
    private VendorContext vendorContext;

    // Vendor endpoints
    @PostMapping
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> createMealSet(@Valid @RequestBody CreateMealSetDTO createMealSetDTO) {
        try {
            Long vendorId = vendorContext.getCurrentVendorId();
            if (vendorId == null) {
                return ResponseEntity.badRequest().body("Vendor not found");
            }
            MealSetDTO mealSet = mealSetService.createMealSet(vendorId, createMealSetDTO);
            return ResponseEntity.ok(mealSet);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/vendor/my-sets")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> getMyMealSets() {
        try {
            Long vendorId = vendorContext.getCurrentVendorId();
            if (vendorId == null) {
                return ResponseEntity.badRequest().body("Vendor not found");
            }
            List<MealSetDTO> mealSets = mealSetService.getMealSetsByVendor(vendorId);
            return ResponseEntity.ok(mealSets);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/vendor/available")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> getMyAvailableMealSets() {
        try {
            Long vendorId = vendorContext.getCurrentVendorId();
            if (vendorId == null) {
                return ResponseEntity.badRequest().body("Vendor not found");
            }
            List<MealSetDTO> mealSets = mealSetService.getAvailableMealSetsByVendor(vendorId);
            return ResponseEntity.ok(mealSets);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/vendor/{setId}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> getMyMealSetById(@PathVariable String setId) {
        try {
            Long vendorId = vendorContext.getCurrentVendorId();
            if (vendorId == null) {
                return ResponseEntity.badRequest().body("Vendor not found");
            }
            MealSetDTO mealSet = mealSetService.getMealSetByIdAndVendor(setId, vendorId);
            return ResponseEntity.ok(mealSet);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/vendor/{setId}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> updateMealSet(@PathVariable String setId, @Valid @RequestBody UpdateMealSetDTO updateMealSetDTO) {
        try {
            Long vendorId = vendorContext.getCurrentVendorId();
            if (vendorId == null) {
                return ResponseEntity.badRequest().body("Vendor not found");
            }
            MealSetDTO mealSet = mealSetService.updateMealSet(setId, vendorId, updateMealSetDTO);
            return ResponseEntity.ok(mealSet);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/vendor/{setId}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> deleteMealSet(@PathVariable String setId) {
        try {
            Long vendorId = vendorContext.getCurrentVendorId();
            if (vendorId == null) {
                return ResponseEntity.badRequest().body("Vendor not found");
            }
            mealSetService.deleteMealSet(setId, vendorId);
            return ResponseEntity.ok("Meal set deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/vendor/{setId}/toggle-availability")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> toggleMealSetAvailability(@PathVariable String setId) {
        try {
            Long vendorId = vendorContext.getCurrentVendorId();
            if (vendorId == null) {
                return ResponseEntity.badRequest().body("Vendor not found");
            }
            mealSetService.toggleMealSetAvailability(setId, vendorId);
            return ResponseEntity.ok("Meal set availability toggled successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Public endpoints
    @GetMapping
    public ResponseEntity<?> getAllAvailableMealSets() {
        try {
            List<MealSetDTO> mealSets = mealSetService.getAllAvailableMealSets();
            return ResponseEntity.ok(mealSets);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{setId}")
    public ResponseEntity<?> getMealSetById(@PathVariable String setId) {
        try {
            MealSetDTO mealSet = mealSetService.getMealSetById(setId);
            return ResponseEntity.ok(mealSet);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<?> getMealSetsByType(@PathVariable String type) {
        try {
            MealSet.MealSetType mealSetType = MealSet.MealSetType.valueOf(type.toUpperCase());
            List<MealSetDTO> mealSets = mealSetService.getMealSetsByType(mealSetType);
            return ResponseEntity.ok(mealSets);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}