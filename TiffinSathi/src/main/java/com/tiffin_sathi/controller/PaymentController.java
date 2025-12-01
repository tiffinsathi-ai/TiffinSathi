package com.tiffin_sathi.controller;

import com.tiffin_sathi.dtos.AdminPaymentDTO;
import com.tiffin_sathi.model.Payment;
import com.tiffin_sathi.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/{paymentId}/status")
    public ResponseEntity<Void> updatePaymentStatus(
            @PathVariable String paymentId,
            @RequestBody Map<String, String> request) {

        String status = request.get("status");
        String transactionId = request.get("transactionId");

        try {
            paymentService.updatePaymentStatus(paymentId,
                    Payment.PaymentStatus.valueOf(status.toUpperCase()), transactionId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // NEW: Get all payments with subscription, package, and user info for admin
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminPaymentDTO>> getAllPaymentsWithDetails() {
        try {
            List<AdminPaymentDTO> payments = paymentService.getAllPaymentsWithDetails();
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            System.err.println("Error fetching payment details: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}