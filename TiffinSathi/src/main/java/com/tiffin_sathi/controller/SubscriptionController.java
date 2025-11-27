package com.tiffin_sathi.controller;

import com.tiffin_sathi.dtos.SubscriptionRequestDTO;
import com.tiffin_sathi.dtos.SubscriptionResponseDTO;
import com.tiffin_sathi.model.Subscription;
import com.tiffin_sathi.services.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/subscriptions")
@CrossOrigin(origins = "*")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SubscriptionResponseDTO> createSubscription(
            @RequestBody SubscriptionRequestDTO request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            SubscriptionResponseDTO subscription = subscriptionService.createSubscription(request, email);
            return ResponseEntity.status(HttpStatus.CREATED).body(subscription);
        } catch (Exception e) {
            System.err.println("Error creating subscription: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<SubscriptionResponseDTO>> getUserSubscriptions(Authentication authentication) {
        try {
            String email = authentication.getName();
            System.out.println("Fetching subscriptions for user: " + email);
            List<SubscriptionResponseDTO> subscriptions = subscriptionService.getUserSubscriptions(email);
            System.out.println("Returning " + subscriptions.size() + " subscriptions for user: " + email);
            return new ResponseEntity<>(subscriptions, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error in getUserSubscriptions controller: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/vendor/active")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<List<SubscriptionResponseDTO>> getActiveVendorSubscriptions(Authentication authentication) {
        try {
            String vendorEmail = authentication.getName();
            List<SubscriptionResponseDTO> subscriptions = subscriptionService.getActiveSubscriptionsByVendorEmail(vendorEmail);
            return new ResponseEntity<>(subscriptions, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // NEW: Get all vendor subscriptions (including inactive ones)
    @GetMapping("/vendor/all")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<List<SubscriptionResponseDTO>> getAllVendorSubscriptions(Authentication authentication) {
        try {
            String vendorEmail = authentication.getName();
            List<SubscriptionResponseDTO> subscriptions = subscriptionService.getSubscriptionsByVendorEmail(vendorEmail);
            return new ResponseEntity<>(subscriptions, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // NEW: Get vendor subscriptions by status
    @GetMapping("/vendor/status/{status}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<List<SubscriptionResponseDTO>> getVendorSubscriptionsByStatus(
            @PathVariable Subscription.SubscriptionStatus status,
            Authentication authentication) {
        try {
            String vendorEmail = authentication.getName();
            List<SubscriptionResponseDTO> subscriptions = subscriptionService.getSubscriptionsByVendorEmailAndStatus(vendorEmail, status);
            return new ResponseEntity<>(subscriptions, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{subscriptionId}")
    @PreAuthorize("hasAnyRole('USER', 'VENDOR', 'ADMIN')")
    public ResponseEntity<SubscriptionResponseDTO> getSubscriptionById(@PathVariable String subscriptionId) {
        try {
            Optional<SubscriptionResponseDTO> subscription = subscriptionService.getSubscriptionById(subscriptionId);
            return subscription.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{subscriptionId}/status")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public ResponseEntity<SubscriptionResponseDTO> updateSubscriptionStatus(
            @PathVariable String subscriptionId,
            @RequestParam Subscription.SubscriptionStatus status) {
        try {
            SubscriptionResponseDTO updated = subscriptionService.updateSubscriptionStatus(subscriptionId, status);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{subscriptionId}/pause")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SubscriptionResponseDTO> pauseSubscription(
            @PathVariable String subscriptionId,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            SubscriptionResponseDTO updated = subscriptionService.pauseSubscription(subscriptionId, userEmail);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{subscriptionId}/resume")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SubscriptionResponseDTO> resumeSubscription(
            @PathVariable String subscriptionId,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            SubscriptionResponseDTO updated = subscriptionService.resumeSubscription(subscriptionId, userEmail);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{subscriptionId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> cancelSubscription(@PathVariable String subscriptionId) {
        try {
            subscriptionService.cancelSubscription(subscriptionId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}