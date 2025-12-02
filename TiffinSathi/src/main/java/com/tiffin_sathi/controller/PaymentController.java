package com.tiffin_sathi.controller;

import com.tiffin_sathi.dtos.AdminPaymentDTO;
import com.tiffin_sathi.dtos.PaymentInitiationRequest;
import com.tiffin_sathi.dtos.PaymentInitiationResponse;
import com.tiffin_sathi.dtos.PaymentResponseDTO;
import com.tiffin_sathi.model.Payment;
import com.tiffin_sathi.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    // Get payment details with subscription info (for success page)
    @GetMapping("/{paymentId}")
    public ResponseEntity<Map<String, Object>> getPayment(@PathVariable String paymentId) {
        try {
            Map<String, Object> paymentDetails = paymentService.getPaymentWithSubscription(paymentId);
            return ResponseEntity.ok(paymentDetails);
        } catch (Exception e) {
            System.err.println("Error fetching payment: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // Get payment status
    @GetMapping("/{paymentId}/status")
    public ResponseEntity<Map<String, Object>> getPaymentStatus(@PathVariable String paymentId) {
        try {
            Map<String, Object> paymentDetails = paymentService.getPaymentWithSubscription(paymentId);
            return ResponseEntity.ok(paymentDetails);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Initiate online payment
    @PostMapping("/initiate")
    public ResponseEntity<PaymentInitiationResponse> initiatePayment(@RequestBody PaymentInitiationRequest request) {
        try {
            PaymentInitiationResponse response = paymentService.initiateOnlinePayment(
                    request.getSubscriptionId(),
                    request.getPaymentMethod(),
                    request.getAmount()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error initiating payment: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // eSewa callback (GET - eSewa uses GET with query parameters)
    @GetMapping("/callback/esewa")
    public void esewaCallback(@RequestParam("data") String data,
                              HttpServletResponse response) throws IOException {
        try {
            paymentService.handleEsewaCallback(data);

            // Extract paymentId from the data
            byte[] decodedBytes = java.util.Base64.getDecoder().decode(data);
            String jsonData = new String(decodedBytes);
            String transactionUuid = extractValueFromJson(jsonData, "transaction_uuid");

            // Find payment by transactionId
            Payment payment = paymentService.getPaymentById(transactionUuid);
            if (payment != null) {
                response.sendRedirect("http://localhost:3000/payment/success?paymentId=" + payment.getPaymentId());
            } else {
                response.sendRedirect("http://localhost:3000/payment/success");
            }
        } catch (Exception e) {
            System.err.println("eSewa callback error: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect("http://localhost:3000/payment/failure?error=payment_failed&message=" + e.getMessage());
        }
    }

    // eSewa callback (POST - for form data submission)
    @PostMapping("/callback/esewa")
    public void esewaCallbackPost(@RequestParam("data") String data,
                                  HttpServletResponse response) throws IOException {
        esewaCallback(data, response);
    }

    // eSewa failure callback
    @GetMapping("/callback/esewa/failure")
    public void esewaFailureCallback(@RequestParam(value = "data", required = false) String data,
                                     HttpServletResponse response) throws IOException {
        try {
            if (data != null) {
                System.err.println("eSewa payment failed: " + data);
            }
            response.sendRedirect("http://localhost:3000/payment/failure?error=payment_cancelled");
        } catch (Exception e) {
            response.sendRedirect("http://localhost:3000/payment/failure");
        }
    }

    // Khalti callback
    @GetMapping("/callback/khalti")
    public void khaltiCallback(@RequestParam("pidx") String pidx,
                               HttpServletResponse response) throws IOException {
        try {
            paymentService.handleKhaltiCallback(pidx);

            // Get payment ID from pidx
            Payment payment = paymentService.getPaymentByPidx(pidx);
            response.sendRedirect("http://localhost:3000/payment/success?paymentId=" + payment.getPaymentId());
        } catch (Exception e) {
            System.err.println("Khalti callback error: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect("http://localhost:3000/payment/failure?error=payment_verification_failed&message=" + e.getMessage());
        }
    }

    // Update payment status (for admin/manual updates)
    @PostMapping("/{paymentId}/status")
    @PreAuthorize("hasRole('ADMIN')")
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

    // Get all payments with subscription, package, and user info for admin
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

    private String extractValueFromJson(String json, String key) {
        try {
            // Simple JSON extraction
            String[] pairs = json.replace("{", "").replace("}", "").replace("\"", "").split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                if (keyValue.length == 2 && keyValue[0].trim().equals(key)) {
                    return keyValue[1].trim();
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting key from JSON: " + e.getMessage());
        }
        return null;
    }
}