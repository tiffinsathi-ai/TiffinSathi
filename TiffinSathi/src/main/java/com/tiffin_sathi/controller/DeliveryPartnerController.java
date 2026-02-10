package com.tiffin_sathi.controller;

import com.tiffin_sathi.config.VendorContext;
import com.tiffin_sathi.dtos.*;
import com.tiffin_sathi.services.DeliveryPartnerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/delivery-partners")
@CrossOrigin(origins = "*")
public class DeliveryPartnerController {

    @Autowired
    private DeliveryPartnerService deliveryPartnerService;

    @Autowired
    private VendorContext vendorContext;

    @PostMapping
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> createDeliveryPartner(@Valid @RequestBody CreateDeliveryPartnerDTO createDeliveryPartnerDTO) {
        try {
            Long vendorId = vendorContext.getCurrentVendorId();
            if (vendorId == null) {
                return ResponseEntity.badRequest().body("Vendor not found");
            }
            Map<String, Object> response = deliveryPartnerService.createDeliveryPartner(vendorId, createDeliveryPartnerDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Add password reset endpoint for vendors
    @PostMapping("/vendor/{partnerId}/reset-password")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> resetDeliveryPartnerPassword(@PathVariable Long partnerId) {
        try {
            Long vendorId = vendorContext.getCurrentVendorId();
            if (vendorId == null) {
                return ResponseEntity.badRequest().body("Vendor not found");
            }
            Map<String, Object> response = deliveryPartnerService.resetDeliveryPartnerPassword(partnerId, vendorId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/vendor/my-partners")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> getMyDeliveryPartners() {
        try {
            Long vendorId = vendorContext.getCurrentVendorId();
            if (vendorId == null) {
                return ResponseEntity.badRequest().body("Vendor not found");
            }
            List<DeliveryPartnerDTO> partners = deliveryPartnerService.getDeliveryPartnersByVendor(vendorId);
            return ResponseEntity.ok(partners);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/vendor/stats")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> getDeliveryStats() {
        try {
            Long vendorId = vendorContext.getCurrentVendorId();
            if (vendorId == null) {
                return ResponseEntity.badRequest().body("Vendor not found");
            }

            List<DeliveryPartnerDTO> partners = deliveryPartnerService.getDeliveryPartnersByVendor(vendorId);

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalPartners", partners.size());
            stats.put("activeCount", partners.stream().filter(p ->
                    p.getIsActive() && "AVAILABLE".equals(p.getAvailabilityStatus())).count());
            stats.put("busyCount", partners.stream().filter(p ->
                    p.getIsActive() && "BUSY".equals(p.getAvailabilityStatus())).count());
            stats.put("inactiveCount", partners.stream().filter(p -> !p.getIsActive()).count());

            // Calculate growth rate (last month)
            long oneMonthAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
            long newPartners = partners.stream()
                    .filter(p -> p.getCreatedAt() != null &&
                            p.getCreatedAt().toEpochSecond(java.time.ZoneOffset.UTC) * 1000 > oneMonthAgo)
                    .count();
            stats.put("growthRate", partners.size() > 0 ?
                    Math.round((newPartners * 100.0) / partners.size()) : 0);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/vendor/active")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> getMyActiveDeliveryPartners() {
        try {
            Long vendorId = vendorContext.getCurrentVendorId();
            if (vendorId == null) {
                return ResponseEntity.badRequest().body("Vendor not found");
            }
            List<DeliveryPartnerDTO> partners = deliveryPartnerService.getActiveDeliveryPartnersByVendor(vendorId);
            return ResponseEntity.ok(partners);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/vendor/count")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> getMyActivePartnersCount() {
        try {
            Long vendorId = vendorContext.getCurrentVendorId();
            if (vendorId == null) {
                return ResponseEntity.badRequest().body("Vendor not found");
            }
            long count = deliveryPartnerService.getActivePartnersCount(vendorId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/vendor/{partnerId}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> getMyDeliveryPartnerById(@PathVariable Long partnerId) {
        try {
            Long vendorId = vendorContext.getCurrentVendorId();
            if (vendorId == null) {
                return ResponseEntity.badRequest().body("Vendor not found");
            }
            DeliveryPartnerDTO partner = deliveryPartnerService.getDeliveryPartnerByIdAndVendor(partnerId, vendorId);
            return ResponseEntity.ok(partner);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/vendor/search")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> searchDeliveryPartners(@RequestParam String name) {
        try {
            Long vendorId = vendorContext.getCurrentVendorId();
            if (vendorId == null) {
                return ResponseEntity.badRequest().body("Vendor not found");
            }
            List<DeliveryPartnerDTO> partners = deliveryPartnerService.searchDeliveryPartnersByName(vendorId, name);
            return ResponseEntity.ok(partners);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/vendor/{partnerId}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> updateDeliveryPartner(@PathVariable Long partnerId, @Valid @RequestBody UpdateDeliveryPartnerDTO updateDeliveryPartnerDTO) {
        try {
            Long vendorId = vendorContext.getCurrentVendorId();
            if (vendorId == null) {
                return ResponseEntity.badRequest().body("Vendor not found");
            }
            DeliveryPartnerDTO partner = deliveryPartnerService.updateDeliveryPartner(partnerId, vendorId, updateDeliveryPartnerDTO);
            return ResponseEntity.ok(partner);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/vendor/{partnerId}/profile-picture")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> updateProfilePicture(@PathVariable Long partnerId, @RequestBody UpdateProfilePictureDTO updateProfilePictureDTO) {
        try {
            Long vendorId = vendorContext.getCurrentVendorId();
            if (vendorId == null) {
                return ResponseEntity.badRequest().body("Vendor not found");
            }
            DeliveryPartnerDTO partner = deliveryPartnerService.updateProfilePicture(
                    partnerId,
                    vendorId,
                    updateProfilePictureDTO.getProfilePicture(),
                    updateProfilePictureDTO.getProfilePictureUrl()
            );
            return ResponseEntity.ok(partner);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/vendor/{partnerId}/profile-picture")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> removeProfilePicture(@PathVariable Long partnerId) {
        try {
            Long vendorId = vendorContext.getCurrentVendorId();
            if (vendorId == null) {
                return ResponseEntity.badRequest().body("Vendor not found");
            }
            deliveryPartnerService.removeProfilePicture(partnerId, vendorId);
            return ResponseEntity.ok("Profile picture removed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/vendor/{partnerId}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> deleteDeliveryPartner(@PathVariable Long partnerId) {
        try {
            Long vendorId = vendorContext.getCurrentVendorId();
            if (vendorId == null) {
                return ResponseEntity.badRequest().body("Vendor not found");
            }
            deliveryPartnerService.deleteDeliveryPartner(partnerId, vendorId);
            return ResponseEntity.ok("Delivery partner deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/vendor/{partnerId}/toggle-availability")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> toggleDeliveryPartnerAvailability(@PathVariable Long partnerId) {
        try {
            Long vendorId = vendorContext.getCurrentVendorId();
            if (vendorId == null) {
                return ResponseEntity.badRequest().body("Vendor not found");
            }
            DeliveryPartnerDTO partner = deliveryPartnerService.toggleDeliveryPartnerAvailability(partnerId, vendorId);
            return ResponseEntity.ok(partner);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}