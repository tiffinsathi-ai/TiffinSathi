package com.tiffin_sathi.controller;

import com.tiffin_sathi.dtos.ChangePasswordDTO;
import com.tiffin_sathi.dtos.DeliveryPartnerDTO;
import com.tiffin_sathi.dtos.UpdateDeliveryPartnerDTO;
import com.tiffin_sathi.services.DeliveryPartnerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/delivery")
@CrossOrigin(origins = "*")
public class DeliveryPartnerSelfController {

    @Autowired
    private DeliveryPartnerService deliveryPartnerService;

    // Get current delivery partner's email from security context
    private String getCurrentDeliveryPartnerEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName(); // This returns the email (username)
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<?> getMyProfile() {
        try {
            String email = getCurrentDeliveryPartnerEmail();
            // Get delivery partner by email
            DeliveryPartnerDTO deliveryPartner = deliveryPartnerService.getDeliveryPartnerByEmail(email);
            return ResponseEntity.ok(deliveryPartner);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/change-password")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        try {
            String email = getCurrentDeliveryPartnerEmail();
            String message = deliveryPartnerService.changePassword(email, changePasswordDTO);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<?> updateMyProfile(@Valid @RequestBody UpdateDeliveryPartnerDTO dto) {
        try {
            String email = getCurrentDeliveryPartnerEmail();
            DeliveryPartnerDTO updated = deliveryPartnerService.updateDeliveryPartnerSelf(email, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/availability")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<?> toggleAvailability() {
        try {
            String email = getCurrentDeliveryPartnerEmail();
            String message = deliveryPartnerService.toggleAvailability(email);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}