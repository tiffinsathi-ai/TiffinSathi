package com.tiffin_sathi.controller;

import com.tiffin_sathi.dtos.*;
import com.tiffin_sathi.model.Payment;
import com.tiffin_sathi.services.PaymentService;
import com.tiffin_sathi.services.SubscriptionEditService;
import com.tiffin_sathi.services.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private SubscriptionEditService subscriptionEditService;

    @Autowired
    private SubscriptionService subscriptionService;

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
            System.out.println("Received eSewa callback with data: " + data.substring(0, Math.min(100, data.length())) + "...");

            // Use the service method to parse eSewa callback data
            Map<String, String> dataMap = paymentService.parseEsewaCallbackData(data);
            String transactionUuid = dataMap.get("transaction_uuid");

            if (transactionUuid == null) {
                System.err.println("Could not extract transaction_uuid from eSewa response");
                response.sendRedirect("http://localhost:3000/payment/failure?error=no_transaction_id");
                return;
            }

            System.out.println("Looking up payment with transaction_uuid: " + transactionUuid);

            // Handle the payment callback
            Payment updatedPayment = paymentService.handleEsewaCallback(data);

            System.out.println("Found payment: " + updatedPayment.getPaymentId() +
                    " of type: " + updatedPayment.getPaymentType() +
                    " with status: " + updatedPayment.getPaymentStatus());

            // Check payment status
            if (updatedPayment.getPaymentStatus() == Payment.PaymentStatus.COMPLETED) {
                // Check if it's an edit payment
                if ("EDIT".equals(updatedPayment.getPaymentType())) {
                    // Complete the edit after payment
                    System.out.println("Completing edit after payment for: " + updatedPayment.getPaymentId());
                    subscriptionEditService.completeEditAfterPayment(updatedPayment.getPaymentId());
                    response.sendRedirect("http://localhost:3000/subscription/edit/success?paymentId=" + updatedPayment.getPaymentId());
                } else {
                    // Regular payment success
                    subscriptionService.completeSubscriptionAfterPayment(updatedPayment.getPaymentId());
                    response.sendRedirect("http://localhost:3000/payment/success?paymentId=" + updatedPayment.getPaymentId());
                }
            } else {
                // Payment failed
                response.sendRedirect("http://localhost:3000/payment/failure?paymentId=" + updatedPayment.getPaymentId() +
                        "&error=payment_failed&status=" + updatedPayment.getPaymentStatus());
            }

        } catch (Exception e) {
            System.err.println("eSewa callback error: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect("http://localhost:3000/payment/failure?error=callback_error&message=" +
                    e.getMessage().replace(" ", "_"));
        }
    }

    // eSewa edit callback (specific for edit payments)
    @GetMapping("/callback/esewa/edit")
    public void esewaEditCallback(@RequestParam("data") String data,
                                  HttpServletResponse response) throws IOException {
        try {
            System.out.println("Received eSewa edit callback");

            // Use the service method to parse eSewa callback data
            Map<String, String> dataMap = paymentService.parseEsewaCallbackData(data);
            String transactionUuid = dataMap.get("transaction_uuid");

            if (transactionUuid == null) {
                throw new RuntimeException("No transaction_uuid found in eSewa response");
            }

            // Handle payment callback
            Payment updatedPayment = paymentService.handleEsewaCallback(data);

            // Complete the edit after payment
            subscriptionEditService.completeEditAfterPayment(updatedPayment.getPaymentId());

            // Redirect to edit success page
            response.sendRedirect("http://localhost:3000/subscription/edit/success?paymentId=" + updatedPayment.getPaymentId());

        } catch (Exception e) {
            System.err.println("eSewa edit callback error: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect("http://localhost:3000/subscription/edit/failure?error=" +
                    e.getMessage().replace(" ", "_") + "&type=edit");
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
                System.err.println("eSewa payment failed with data: " + data);
            }
            response.sendRedirect("http://localhost:3000/payment/failure?error=payment_cancelled");
        } catch (Exception e) {
            response.sendRedirect("http://localhost:3000/payment/failure");
        }
    }

    // eSewa edit failure callback
    @GetMapping("/callback/esewa/edit/failure")
    public void esewaEditFailureCallback(@RequestParam(value = "data", required = false) String data,
                                         HttpServletResponse response) throws IOException {
        try {
            if (data != null) {
                System.err.println("eSewa edit payment failed: " + data);
            }
            response.sendRedirect("http://localhost:3000/subscription/edit/failure?error=payment_cancelled&type=edit");
        } catch (Exception e) {
            response.sendRedirect("http://localhost:3000/subscription/edit/failure");
        }
    }

    // Khalti callback
    @GetMapping("/callback/khalti")
    public void khaltiCallback(@RequestParam("pidx") String pidx,
                               HttpServletResponse response) throws IOException {
        try {
            System.out.println("Received Khalti callback with pidx: " + pidx);

            Payment updatedPayment = paymentService.handleKhaltiCallback(pidx);

            System.out.println("Found payment: " + updatedPayment.getPaymentId() +
                    " of type: " + updatedPayment.getPaymentType() +
                    " with status: " + updatedPayment.getPaymentStatus());

            // Check payment status
            if (updatedPayment.getPaymentStatus() == Payment.PaymentStatus.COMPLETED) {
                // Check if it's an edit payment
                if ("EDIT".equals(updatedPayment.getPaymentType())) {
                    // Complete the edit after payment
                    subscriptionEditService.completeEditAfterPayment(updatedPayment.getPaymentId());
                    response.sendRedirect("http://localhost:3000/subscription/edit/success?paymentId=" + updatedPayment.getPaymentId());
                } else {
                    response.sendRedirect("http://localhost:3000/payment/success?paymentId=" + updatedPayment.getPaymentId());
                }
            } else {
                response.sendRedirect("http://localhost:3000/payment/failure?paymentId=" + updatedPayment.getPaymentId() +
                        "&error=payment_failed&status=" + updatedPayment.getPaymentStatus());
            }
        } catch (Exception e) {
            System.err.println("Khalti callback error: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect("http://localhost:3000/payment/failure?error=payment_verification_failed&message=" +
                    e.getMessage().replace(" ", "_"));
        }
    }

    // Khalti edit callback
    @GetMapping("/callback/khalti/edit")
    public void khaltiEditCallback(@RequestParam("pidx") String pidx,
                                   HttpServletResponse response) throws IOException {
        try {
            System.out.println("Received Khalti edit callback with pidx: " + pidx);

            Payment updatedPayment = paymentService.handleKhaltiCallback(pidx);

            if ("EDIT".equals(updatedPayment.getPaymentType())) {
                // Complete the edit after payment
                subscriptionEditService.completeEditAfterPayment(updatedPayment.getPaymentId());
                response.sendRedirect("http://localhost:3000/subscription/edit/success?paymentId=" + updatedPayment.getPaymentId());
            } else {
                response.sendRedirect("http://localhost:3000/payment/success");
            }
        } catch (Exception e) {
            System.err.println("Khalti edit callback error: " + e.getMessage());
            response.sendRedirect("http://localhost:3000/subscription/edit/failure?error=" +
                    e.getMessage().replace(" ", "_") + "&type=edit");
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

    // Complete edit after payment (for direct API calls)
    @PostMapping("/edit/complete/{paymentId}")
    public ResponseEntity<EditSubscriptionResponseDTO> completeEditAfterPayment(@PathVariable String paymentId) {
        try {
            EditSubscriptionResponseDTO response = subscriptionEditService.completeEditAfterPayment(paymentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error completing edit after payment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Get subscription payment summary
    @GetMapping("/subscription/{subscriptionId}/summary")
    public ResponseEntity<Map<String, Object>> getSubscriptionPaymentSummary(@PathVariable String subscriptionId) {
        try {
            Map<String, Object> summary = paymentService.getSubscriptionPaymentSummary(subscriptionId);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            System.err.println("Error fetching payment summary: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Get payments by subscription
    @GetMapping("/subscription/{subscriptionId}")
    public ResponseEntity<List<Payment>> getPaymentsBySubscription(@PathVariable String subscriptionId) {
        try {
            List<Payment> payments = paymentService.getPaymentsBySubscriptionId(subscriptionId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            System.err.println("Error fetching subscription payments: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Process refund
    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> processRefund(@PathVariable String paymentId,
                                                             @RequestBody Map<String, String> request) {
        try {
            String reason = request.get("reason");
            if (reason == null || reason.trim().isEmpty()) {
                reason = "Refund requested by admin";
            }

            Payment refundedPayment = paymentService.processRefund(paymentId, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Refund processed successfully");
            response.put("paymentId", refundedPayment.getPaymentId());
            response.put("paymentStatus", refundedPayment.getPaymentStatus().name());
            response.put("amount", refundedPayment.getAmount());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error processing refund: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Helper method to extract value from JSON string
    private String extractValueFromJson(String json, String key) {
        try {
            // Look for key in JSON
            String searchKey = "\"" + key + "\"";
            int keyIndex = json.indexOf(searchKey);
            if (keyIndex == -1) return null;

            // Find the value after the key
            int valueStart = json.indexOf(":", keyIndex) + 1;
            int valueEnd;

            // Check if value is string or number
            char firstChar = json.charAt(valueStart);
            if (firstChar == '"') {
                // String value
                valueStart++; // Skip opening quote
                valueEnd = json.indexOf("\"", valueStart);
            } else {
                // Number or boolean value
                valueEnd = json.indexOf(",", valueStart);
                if (valueEnd == -1) valueEnd = json.indexOf("}", valueStart);
            }

            if (valueEnd == -1) return null;
            return json.substring(valueStart, valueEnd).trim().replace("\"", "");
        } catch (Exception e) {
            System.err.println("Error extracting key from JSON: " + e.getMessage());
            return null;
        }
    }
}