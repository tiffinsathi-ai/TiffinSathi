package com.tiffin_sathi.controller;

import com.tiffin_sathi.model.Payment;
import com.tiffin_sathi.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}