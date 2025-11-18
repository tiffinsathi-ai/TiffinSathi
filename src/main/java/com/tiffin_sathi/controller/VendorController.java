package com.tiffin_sathi.controller;

import com.tiffin_sathi.dtos.ChangePasswordDTO;
import com.tiffin_sathi.dtos.UpdateVendorDTO;
import com.tiffin_sathi.dtos.VendorStatusUpdateDTO;
import com.tiffin_sathi.model.Vendor;
import com.tiffin_sathi.model.VendorStatus;
import com.tiffin_sathi.services.VendorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/vendors")
@CrossOrigin(origins = "*")
public class VendorController {

    @Autowired
    private VendorService vendorService;

    // Get current vendor profile
    @GetMapping("/profile")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> getCurrentVendorProfile(Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<Vendor> vendor = vendorService.getVendorByEmail(email);
            return vendor.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching vendor profile: " + e.getMessage());
        }
    }

    // Update current vendor profile
    @PutMapping("/profile")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> updateCurrentVendor(
            Authentication authentication,
            @Valid @RequestBody UpdateVendorDTO updateVendorDTO) {
        try {
            String email = authentication.getName();
            Optional<Vendor> vendor = vendorService.getVendorByEmail(email);

            if (vendor.isPresent()) {
                Vendor updatedVendor = vendorService.updateVendor(vendor.get().getVendorId(), updateVendorDTO);
                return ResponseEntity.ok(updatedVendor);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Change password for current vendor
    @PutMapping("/change-password")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> changeCurrentVendorPassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        try {
            String email = authentication.getName();
            Optional<Vendor> vendor = vendorService.getVendorByEmail(email);

            if (vendor.isPresent()) {
                String message = vendorService.changeVendorPassword(
                        vendor.get().getVendorId(),
                        changePasswordDTO.getCurrentPassword(),
                        changePasswordDTO.getNewPassword(),
                        changePasswordDTO.getConfirmPassword()
                );
                return ResponseEntity.ok(message);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Admin only - Get all vendors
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Vendor>> getAllVendors() {
        List<Vendor> vendors = vendorService.getAllVendors();
        return ResponseEntity.ok(vendors);
    }

    // Get vendors by status
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Vendor>> getVendorsByStatus(@PathVariable VendorStatus status) {
        List<Vendor> vendors = vendorService.getVendorsByStatus(status);
        return ResponseEntity.ok(vendors);
    }

    // Get vendor by ID
    @GetMapping("/{vendorId}")
    public ResponseEntity<?> getVendorById(@PathVariable Long vendorId) {
        Optional<Vendor> vendor = vendorService.getVendorById(vendorId);
        return vendor.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get vendor by email
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getVendorByEmail(@PathVariable String email) {
        Optional<Vendor> vendor = vendorService.getVendorByEmail(email);
        return vendor.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update vendor profile by ID
    @PutMapping("/{vendorId}")
    public ResponseEntity<?> updateVendor(@PathVariable Long vendorId, @Valid @RequestBody UpdateVendorDTO updateVendorDTO) {
        try {
            Vendor updatedVendor = vendorService.updateVendor(vendorId, updateVendorDTO);
            return ResponseEntity.ok(updatedVendor);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Admin only - Update vendor status (approve/reject)
    @PutMapping("/{vendorId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateVendorStatus(@PathVariable Long vendorId, @Valid @RequestBody VendorStatusUpdateDTO statusUpdateDTO) {
        try {
            Vendor updatedVendor = vendorService.updateVendorStatus(vendorId, statusUpdateDTO.getStatus(), statusUpdateDTO.getReason());
            return ResponseEntity.ok(updatedVendor);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Change password for vendor by ID
    @PutMapping("/{vendorId}/change-password")
    public ResponseEntity<?> changeVendorPassword(@PathVariable Long vendorId, @Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        try {
            String message = vendorService.changeVendorPassword(vendorId,
                    changePasswordDTO.getCurrentPassword(),
                    changePasswordDTO.getNewPassword(),
                    changePasswordDTO.getConfirmPassword());
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Admin only - Delete vendor
    @DeleteMapping("/{vendorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteVendor(@PathVariable Long vendorId) {
        try {
            vendorService.deleteVendor(vendorId);
            return ResponseEntity.ok("Vendor deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}