package com.tiffin_sathi.controller;

import com.tiffin_sathi.dtos.ChangePasswordDTO;
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

    @GetMapping("/profile")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<?> getMyProfile() {
        try {
            String email = getCurrentDeliveryPartnerEmail();
            // You can add a method to get delivery partner profile by email
            // For now, returning a simple response
            return ResponseEntity.ok("Profile endpoint for: " + email);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}