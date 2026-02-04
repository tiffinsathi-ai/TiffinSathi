package com.tiffin_sathi.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiffin_sathi.config.PaymentGatewayConfig;
import com.tiffin_sathi.dtos.AdminPaymentDTO;
import com.tiffin_sathi.dtos.EditSubscriptionResponseDTO;
import com.tiffin_sathi.dtos.PaymentInitiationResponse;
import com.tiffin_sathi.dtos.SubscriptionDayDTO;
import com.tiffin_sathi.model.*;
import com.tiffin_sathi.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private PaymentGatewayConfig paymentGatewayConfig;

    @Autowired
    private SubscriptionEditHistoryRepository editHistoryRepository;

    @Autowired
    private MealSetRepository mealSetRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final RestTemplate restTemplate = new RestTemplate();
    private final Set<String> generatedPaymentIds = new HashSet<>();

    // NEW METHOD: Parse eSewa callback data
    @Transactional
    public Map<String, String> parseEsewaCallbackData(String encodedData) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encodedData);
            String jsonData = new String(decodedBytes);
            return objectMapper.readValue(jsonData, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse eSewa callback data: " + e.getMessage(), e);
        }
    }

    // Transaction ID lookup
    @Transactional(readOnly = true)
    public Optional<Payment> findByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId);
    }

    // Gateway transaction ID lookup
    @Transactional(readOnly = true)
    public Payment getPaymentByGatewayTransactionId(String gatewayTransactionId) {
        return paymentRepository.findByGatewayTransactionId(gatewayTransactionId)
                .orElseThrow(() -> new RuntimeException("Payment not found for gatewayTransactionId: " + gatewayTransactionId));
    }

    // Pidx lookup (Khalti)
    @Transactional(readOnly = true)
    public Payment getPaymentByPidx(String pidx) {
        return paymentRepository.findByGatewayTransactionId(pidx)
                .orElseThrow(() -> new RuntimeException("Payment not found for pidx: " + pidx));
    }

    @Transactional
    public Payment createPayment(Subscription subscription, String paymentMethod, Double amount) {
        Payment payment = new Payment();

        // Generate payment ID (PAY202412...)
        String paymentId = generateUniquePaymentId("PAY");
        payment.setPaymentId(paymentId);

        // Generate transaction ID (TXN...)
        String transactionId = "TXN" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        payment.setTransactionId(transactionId);

        payment.setSubscription(subscription);
        payment.setPaymentMethod(Payment.PaymentMethod.valueOf(paymentMethod.toUpperCase()));
        payment.setAmount(amount);
        payment.setPaymentType("REGULAR");

        // Set status based on payment method
        if (paymentMethod.equalsIgnoreCase("CASH_ON_DELIVERY")) {
            payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);
            payment.setPaidAt(LocalDateTime.now());
        } else {
            payment.setPaymentStatus(Payment.PaymentStatus.PENDING);
        }

        Payment savedPayment = paymentRepository.save(payment);

        // Update subscription with the new payment
        subscription.getPayments().add(savedPayment);
        subscriptionRepository.save(subscription);

        System.out.println("Payment created successfully. Payment ID: " + savedPayment.getPaymentId() +
                " for subscription: " + subscription.getSubscriptionId() +
                " Transaction ID: " + savedPayment.getTransactionId());
        return savedPayment;
    }

    @Transactional
    public PaymentInitiationResponse initiateOnlinePayment(String subscriptionId, String paymentMethod, Double amount) {
        // Get subscription
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found: " + subscriptionId));

        // Check if subscription already has active payment
        Optional<Payment> existingPendingPayment = paymentRepository.findLatestPaymentBySubscriptionIdAndStatus(
                subscriptionId, Payment.PaymentStatus.PENDING);

        Payment payment;
        if (existingPendingPayment.isPresent() &&
                existingPendingPayment.get().getPaymentType().equals("REGULAR")) {
            // Update existing pending payment
            payment = existingPendingPayment.get();
            payment.setPaymentMethod(Payment.PaymentMethod.valueOf(paymentMethod.toUpperCase()));
            payment.setAmount(amount);
            payment = paymentRepository.save(payment);
            System.out.println("Updated existing pending payment: " + payment.getPaymentId());
        } else {
            // Create new payment record
            payment = createPayment(subscription, paymentMethod, amount);
        }

        // Generate payment response based on gateway
        if (paymentMethod.equalsIgnoreCase("ESEWA")) {
            return initiateEsewaPayment(payment);
        } else if (paymentMethod.equalsIgnoreCase("KHALTI")) {
            return initiateKhaltiPayment(payment);
        } else {
            throw new RuntimeException("Unsupported payment method: " + paymentMethod);
        }
    }

    @Transactional
    public PaymentInitiationResponse initiateOnlinePaymentForEdit(String subscriptionId, String paymentMethod,
                                                                  Double amount, String editPaymentId) {
        // Get subscription
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found: " + subscriptionId));

        // Find the edit payment
        Payment payment = paymentRepository.findById(editPaymentId)
                .orElseThrow(() -> new RuntimeException("Edit payment not found: " + editPaymentId));

        // Verify this is an edit payment
        if (!"EDIT".equals(payment.getPaymentType())) {
            throw new RuntimeException("Payment is not an edit payment: " + editPaymentId);
        }

        // Update payment amount if different
        if (!payment.getAmount().equals(amount)) {
            payment.setAmount(amount);
            paymentRepository.save(payment);
        }

        // Generate payment response based on gateway
        if (paymentMethod.equalsIgnoreCase("ESEWA")) {
            return initiateEsewaPaymentForEdit(payment);
        } else if (paymentMethod.equalsIgnoreCase("KHALTI")) {
            return initiateKhaltiPaymentForEdit(payment);
        } else {
            throw new RuntimeException("Unsupported payment method: " + paymentMethod);
        }
    }

    private PaymentInitiationResponse initiateEsewaPayment(Payment payment) {
        try {
            String amountStr = String.format("%.2f", payment.getAmount());
            String taxAmount = "0";
            String totalAmount = amountStr;

            // IMPORTANT: eSewa requires transaction_uuid to be unique and in specific format
            // Using a timestamp-based UUID for better compatibility
            String transactionUuid = "TS" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            // Get merchant code from config
            String productCode = paymentGatewayConfig.getEsewaMerchantCode();

            // Prepare data for signature - MUST match exactly what eSewa expects
            // The order and format is critical!
            String dataToSign = "total_amount=" + totalAmount +
                    ",transaction_uuid=" + transactionUuid +
                    ",product_code=" + productCode;

            System.out.println("eSewa data to sign: " + dataToSign);

            // Generate HMAC SHA256 signature
            String signature = generateHmacSha256(dataToSign, paymentGatewayConfig.getEsewaSecretKey());

            System.out.println("Generated eSewa signature: " + signature);

            // Prepare payment data EXACTLY as eSewa expects
            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("amount", totalAmount);
            paymentData.put("tax_amount", taxAmount);
            paymentData.put("total_amount", totalAmount);
            paymentData.put("transaction_uuid", transactionUuid);
            paymentData.put("product_code", productCode);
            paymentData.put("product_service_charge", "0");
            paymentData.put("product_delivery_charge", "0");

            // Use HTTPS for production, HTTP for local testing
            String baseUrl = "http://localhost:8080"; // Change to your actual base URL
            paymentData.put("success_url", baseUrl + "/api/payments/callback/esewa");
            paymentData.put("failure_url", baseUrl + "/api/payments/callback/esewa/failure");

            // CRITICAL: These fields must be exactly as eSewa expects
            paymentData.put("signed_field_names", "total_amount,transaction_uuid,product_code");
            paymentData.put("signature", signature);

            // Add optional fields for better compatibility
            paymentData.put("transaction_code", transactionUuid);
            paymentData.put("customer_name", payment.getSubscription().getUser().getUserName());
            paymentData.put("customer_email", payment.getSubscription().getUser().getEmail());
            paymentData.put("customer_phone", payment.getSubscription().getUser().getPhoneNumber());

            // Store the transaction_uuid as gatewayTransactionId for reference
            payment.setGatewayTransactionId(transactionUuid);
            paymentRepository.save(payment);

            System.out.println("Payment data for eSewa: " + paymentData);

            // Create response
            PaymentInitiationResponse response = new PaymentInitiationResponse();
            response.setPaymentId(payment.getPaymentId());
            response.setPaymentMethod("ESEWA");
            response.setPaymentUrl(paymentGatewayConfig.getEsewaBaseUrl()); // eSewa payment page URL
            response.setPaymentData(paymentData);
            response.setMessage("eSewa payment initiated successfully. Please proceed with payment on eSewa.");

            return response;

        } catch (Exception e) {
            System.err.println("Failed to initiate eSewa payment: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initiate eSewa payment. Please check your eSewa configuration: " + e.getMessage());
        }
    }

    private PaymentInitiationResponse initiateEsewaPaymentForEdit(Payment payment) {
        try {
            String amountStr = String.format("%.2f", payment.getAmount());
            String taxAmount = "0";
            String totalAmount = amountStr;

            // Unique transaction ID for edit
            String transactionUuid = "EDIT_" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            String productCode = paymentGatewayConfig.getEsewaMerchantCode();

            String dataToSign = "total_amount=" + totalAmount +
                    ",transaction_uuid=" + transactionUuid +
                    ",product_code=" + productCode;

            String signature = generateHmacSha256(dataToSign, paymentGatewayConfig.getEsewaSecretKey());

            // Prepare payment data
            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("amount", totalAmount);
            paymentData.put("tax_amount", taxAmount);
            paymentData.put("total_amount", totalAmount);
            paymentData.put("transaction_uuid", transactionUuid);
            paymentData.put("product_code", productCode);
            paymentData.put("product_service_charge", "0");
            paymentData.put("product_delivery_charge", "0");

            String baseUrl = "http://localhost:8080";
            paymentData.put("success_url", baseUrl + "/api/payments/callback/esewa/edit");
            paymentData.put("failure_url", baseUrl + "/api/payments/callback/esewa/edit/failure");

            paymentData.put("signed_field_names", "total_amount,transaction_uuid,product_code");
            paymentData.put("signature", signature);

            // Store transaction_uuid
            payment.setGatewayTransactionId(transactionUuid);
            paymentRepository.save(payment);

            PaymentInitiationResponse response = new PaymentInitiationResponse();
            response.setPaymentId(payment.getPaymentId());
            response.setPaymentMethod("ESEWA");
            response.setPaymentUrl(paymentGatewayConfig.getEsewaBaseUrl());
            response.setPaymentData(paymentData);
            response.setMessage("eSewa edit payment initiated successfully");

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Failed to initiate eSewa edit payment: " + e.getMessage());
        }
    }

    private PaymentInitiationResponse initiateKhaltiPayment(Payment payment) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Key " + paymentGatewayConfig.getKhaltiSecretKey());
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("return_url", "http://localhost:8080/api/payments/callback/khalti");
            body.put("website_url", paymentGatewayConfig.getFrontendBaseUrl());
            body.put("amount", payment.getAmount() * 100); // Convert to paisa
            body.put("purchase_order_id", payment.getTransactionId());
            body.put("purchase_order_name", "Tiffin Subscription - " + payment.getSubscription().getSubscriptionId());

            // Add customer info if available
            if (payment.getSubscription().getUser() != null) {
                Map<String, String> customerInfo = new HashMap<>();
                customerInfo.put("name", payment.getSubscription().getUser().getUserName());
                customerInfo.put("email", payment.getSubscription().getUser().getEmail());
                customerInfo.put("phone", payment.getSubscription().getUser().getPhoneNumber());
                body.put("customer_info", customerInfo);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            String khaltiInitiateUrl = paymentGatewayConfig.getKhaltiBaseUrl() + "/epayment/initiate/";
            System.out.println("Calling Khalti URL: " + khaltiInitiateUrl);
            System.out.println("Khalti request body: " + body);

            ResponseEntity<Map> response = restTemplate.postForEntity(khaltiInitiateUrl, entity, Map.class);

            System.out.println("Khalti response status: " + response.getStatusCode());
            System.out.println("Khalti response body: " + response.getBody());

            Map<String, Object> responseBody = response.getBody();

            if (response.getStatusCode() == HttpStatus.OK && responseBody != null) {
                // Update payment with Khalti pidx
                String pidx = (String) responseBody.get("pidx");
                payment.setGatewayTransactionId(pidx);
                paymentRepository.save(payment);

                // Create response
                PaymentInitiationResponse paymentResponse = new PaymentInitiationResponse();
                paymentResponse.setPaymentId(payment.getPaymentId());
                paymentResponse.setPaymentMethod("KHALTI");
                paymentResponse.setPaymentUrl((String) responseBody.get("payment_url"));
                paymentResponse.setPaymentData(Map.of("pidx", pidx));
                paymentResponse.setMessage("Khalti payment initiated successfully");

                return paymentResponse;
            } else {
                System.err.println("Failed to initiate Khalti payment. Status: " + response.getStatusCode());
                throw new RuntimeException("Failed to initiate Khalti payment. Please try again.");
            }

        } catch (Exception e) {
            System.err.println("Failed to initiate Khalti payment: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initiate Khalti payment: " + e.getMessage());
        }
    }

    private PaymentInitiationResponse initiateKhaltiPaymentForEdit(Payment payment) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Key " + paymentGatewayConfig.getKhaltiSecretKey());
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("return_url", "http://localhost:8080/api/payments/callback/khalti/edit");
            body.put("website_url", paymentGatewayConfig.getFrontendBaseUrl());
            body.put("amount", payment.getAmount() * 100);
            body.put("purchase_order_id", payment.getTransactionId());
            body.put("purchase_order_name", "Tiffin Subscription Edit - " + payment.getSubscription().getSubscriptionId());

            // Add customer info if available
            if (payment.getSubscription().getUser() != null) {
                Map<String, String> customerInfo = new HashMap<>();
                customerInfo.put("name", payment.getSubscription().getUser().getUserName());
                customerInfo.put("email", payment.getSubscription().getUser().getEmail());
                customerInfo.put("phone", payment.getSubscription().getUser().getPhoneNumber());
                body.put("customer_info", customerInfo);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            String khaltiInitiateUrl = paymentGatewayConfig.getKhaltiBaseUrl() + "/epayment/initiate/";
            ResponseEntity<Map> response = restTemplate.postForEntity(khaltiInitiateUrl, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (response.getStatusCode() == HttpStatus.OK && responseBody != null) {
                String pidx = (String) responseBody.get("pidx");
                payment.setGatewayTransactionId(pidx);
                paymentRepository.save(payment);

                PaymentInitiationResponse paymentResponse = new PaymentInitiationResponse();
                paymentResponse.setPaymentId(payment.getPaymentId());
                paymentResponse.setPaymentMethod("KHALTI");
                paymentResponse.setPaymentUrl((String) responseBody.get("payment_url"));
                paymentResponse.setPaymentData(Map.of("pidx", pidx));
                paymentResponse.setMessage("Khalti edit payment initiated successfully");

                return paymentResponse;
            } else {
                throw new RuntimeException("Failed to initiate Khalti edit payment");
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to initiate Khalti edit payment: " + e.getMessage());
        }
    }

    @Transactional
    public Payment createEditPayment(Subscription subscription, String paymentMethod, Double amount, String description) {
        // Check for existing pending edit payment
        List<Payment> existingEditPayments = paymentRepository.findEditPaymentBySubscriptionAndStatus(
                subscription.getSubscriptionId(), Payment.PaymentStatus.PENDING);

        Payment payment;
        if (!existingEditPayments.isEmpty()) {
            // Update existing edit payment
            payment = existingEditPayments.get(0);
            payment.setPaymentMethod(Payment.PaymentMethod.valueOf(paymentMethod.toUpperCase()));
            payment.setAmount(amount);
            payment.setPaymentStatus(Payment.PaymentStatus.PENDING);
            System.out.println("Updated existing edit payment: " + payment.getPaymentId());
        } else {
            // Create new edit payment with unique ID
            payment = new Payment();

            // Generate unique edit payment ID
            String paymentId = generateUniqueEditPaymentId();
            payment.setPaymentId(paymentId);

            // Generate transaction ID
            String transactionId = "EDIT_TXN" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
            payment.setTransactionId(transactionId);

            payment.setSubscription(subscription);
            payment.setPaymentMethod(Payment.PaymentMethod.valueOf(paymentMethod.toUpperCase()));
            payment.setAmount(amount);
            payment.setPaymentStatus(Payment.PaymentStatus.PENDING);
            payment.setPaymentType("EDIT");

            System.out.println("Created new edit payment: " + payment.getPaymentId());
        }

        // Store description in payment gateway response field
        if (description != null) {
            payment.setPaymentGatewayResponse("{\"description\": \"" + description + "\"}");
        }

        Payment savedPayment = paymentRepository.save(payment);

        // Add to subscription payments list
        subscription.getPayments().add(savedPayment);
        subscriptionRepository.save(subscription);

        System.out.println("Edit payment created/updated: " + savedPayment.getPaymentId() +
                " for subscription: " + subscription.getSubscriptionId());
        return savedPayment;
    }

    @Transactional
    public Payment handleEsewaCallback(String encodedData) {
        try {
            // Decode base64 data
            byte[] decodedBytes = Base64.getDecoder().decode(encodedData);
            String jsonData = new String(decodedBytes);

            System.out.println("Received eSewa callback data: " + jsonData);

            // Parse JSON properly using Jackson
            Map<String, String> dataMap = objectMapper.readValue(jsonData, new TypeReference<Map<String, String>>() {});

            String transactionUuid = dataMap.get("transaction_uuid");
            String status = dataMap.get("status");
            String transactionCode = dataMap.get("transaction_code");

            System.out.println("Extracted values - transaction_uuid: " + transactionUuid +
                    ", status: " + status + ", transaction_code: " + transactionCode);

            if (transactionUuid == null) {
                throw new RuntimeException("No transaction_uuid found in eSewa response");
            }

            // Try to find payment by gatewayTransactionId first
            Payment payment = paymentRepository.findByGatewayTransactionId(transactionUuid).orElse(null);

            if (payment == null && transactionCode != null) {
                // Try by transaction_code
                payment = paymentRepository.findByTransactionId(transactionCode).orElse(null);
            }

            if (payment == null) {
                // Last resort: try by transaction_uuid as transactionId
                payment = paymentRepository.findByTransactionId(transactionUuid).orElse(null);
            }

            if (payment == null) {
                System.err.println("Payment not found for any identifier. transaction_uuid: " + transactionUuid +
                        ", transaction_code: " + transactionCode);
                throw new RuntimeException("Payment not found. Please contact support.");
            }

            System.out.println("Found payment: " + payment.getPaymentId() +
                    " with current status: " + payment.getPaymentStatus() +
                    " and type: " + payment.getPaymentType());

            if ("COMPLETE".equalsIgnoreCase(status)) {
                // Payment successful
                payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);
                payment.setPaidAt(LocalDateTime.now());

                // Store eSewa transaction code if available
                if (transactionCode != null && !transactionCode.trim().isEmpty()) {
                    payment.setTransactionId(transactionCode);
                }

                // Ensure gateway transaction ID is stored
                if (payment.getGatewayTransactionId() == null || payment.getGatewayTransactionId().isEmpty()) {
                    payment.setGatewayTransactionId(transactionUuid);
                }

                payment.setPaymentGatewayResponse(jsonData);

                // Save payment
                Payment updatedPayment = paymentRepository.save(payment);
                System.out.println("Payment saved with COMPLETED status for: " + updatedPayment.getPaymentId());

                // Check payment type
                if ("EDIT".equals(updatedPayment.getPaymentType())) {
                    System.out.println("Edit payment completed for: " + updatedPayment.getPaymentId());
                    return updatedPayment;
                }

                // Activate subscription for regular payments
                Subscription subscription = updatedPayment.getSubscription();
                if (subscription != null && subscription.getStatus() != Subscription.SubscriptionStatus.ACTIVE) {
                    subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
                    subscriptionRepository.save(subscription);
                    System.out.println("Subscription activated for: " + subscription.getSubscriptionId());
                }

                System.out.println("Regular payment completed: " + updatedPayment.getPaymentId());
                return updatedPayment;
            } else {
                // Payment failed
                payment.setPaymentStatus(Payment.PaymentStatus.FAILED);
                payment.setPaymentGatewayResponse(jsonData);
                Payment failedPayment = paymentRepository.save(payment);

                System.out.println("eSewa payment failed for: " + failedPayment.getPaymentId());
                throw new RuntimeException("Payment failed with status: " + status);
            }

        } catch (Exception e) {
            System.err.println("Error processing eSewa callback: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Payment verification failed: " + e.getMessage());
        }
    }

    @Transactional
    public Payment handleKhaltiCallback(String pidx) {
        try {
            System.out.println("Processing Khalti callback for pidx: " + pidx);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Key " + paymentGatewayConfig.getKhaltiSecretKey());
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(headers);

            String verifyUrl = paymentGatewayConfig.getKhaltiBaseUrl() + "/epayment/lookup/?pidx=" + pidx;
            System.out.println("Verifying Khalti payment at: " + verifyUrl);

            ResponseEntity<Map> response = restTemplate.postForEntity(verifyUrl, entity, Map.class);

            System.out.println("Khalti verification response status: " + response.getStatusCode());
            System.out.println("Khalti verification response body: " + response.getBody());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String status = (String) responseBody.get("status");

                if ("Completed".equals(status)) {
                    // Find payment by pidx
                    Payment payment = paymentRepository.findByGatewayTransactionId(pidx).orElse(null);
                    if (payment == null) {
                        // Try to find by purchase_order_id
                        Object purchaseOrderObj = responseBody.get("purchase_order_id");
                        if (purchaseOrderObj != null) {
                            String purchaseOrderId = purchaseOrderObj.toString();
                            payment = paymentRepository.findByTransactionId(purchaseOrderId).orElse(null);
                        }
                    }

                    if (payment == null) {
                        throw new RuntimeException("Payment not found for pidx: " + pidx);
                    }

                    payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);
                    payment.setPaidAt(LocalDateTime.now());

                    // Store Khalti transaction ID if available
                    Object transactionIdObj = responseBody.get("transaction_id");
                    if (transactionIdObj != null) {
                        payment.setTransactionId(transactionIdObj.toString());
                    }

                    // Ensure gatewayTransactionId is stored
                    if (payment.getGatewayTransactionId() == null || payment.getGatewayTransactionId().isEmpty()) {
                        payment.setGatewayTransactionId(pidx);
                    }

                    payment.setPaymentGatewayResponse(responseBody.toString());

                    // Save payment
                    Payment updatedPayment = paymentRepository.save(payment);
                    System.out.println("Payment saved with COMPLETED status for: " + updatedPayment.getPaymentId());

                    // Check payment type
                    if ("EDIT".equals(updatedPayment.getPaymentType())) {
                        System.out.println("Edit payment completed for payment ID: " + updatedPayment.getPaymentId());
                        return updatedPayment;
                    }

                    // Activate subscription for regular payments
                    Subscription subscription = updatedPayment.getSubscription();
                    if (subscription != null && subscription.getStatus() != Subscription.SubscriptionStatus.ACTIVE) {
                        subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
                        subscriptionRepository.save(subscription);
                        System.out.println("Subscription activated for: " + subscription.getSubscriptionId());
                    }

                    System.out.println("Regular payment completed: " + updatedPayment.getPaymentId());
                    return updatedPayment;
                } else {
                    // Update payment status to failed
                    Payment payment = paymentRepository.findByGatewayTransactionId(pidx).orElse(null);
                    if (payment != null) {
                        payment.setPaymentStatus(Payment.PaymentStatus.FAILED);
                        payment.setPaymentGatewayResponse(responseBody.toString());
                        Payment failedPayment = paymentRepository.save(payment);
                        System.out.println("Payment marked as FAILED for pidx: " + pidx);
                        throw new RuntimeException("Payment not completed. Status: " + status);
                    } else {
                        throw new RuntimeException("Payment not found for pidx: " + pidx);
                    }
                }
            } else {
                System.err.println("Failed to verify Khalti payment. Status: " + response.getStatusCode());
                throw new RuntimeException("Failed to verify Khalti payment");
            }

        } catch (Exception e) {
            System.err.println("Error processing Khalti callback: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Payment verification failed: " + e.getMessage());
        }
    }

    @Transactional
    public void handleEditPaymentCompletion(String paymentId) {
        try {
            System.out.println("Processing edit payment completion for: " + paymentId);

            // Find payment
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

            // Verify payment is completed
            if (payment.getPaymentStatus() != Payment.PaymentStatus.COMPLETED) {
                throw new RuntimeException("Payment is not completed yet: " + paymentId);
            }

            // Find edit history by payment ID
            List<SubscriptionEditHistory> editHistories = editHistoryRepository.findByPaymentId(paymentId);

            if (editHistories.isEmpty()) {
                System.err.println("No edit history found for payment: " + paymentId);
                return;
            }

            SubscriptionEditHistory editHistory = editHistories.get(0);
            Subscription subscription = editHistory.getSubscription();

            // Update edit history status
            editHistory.setStatus("COMPLETED");
            editHistory.setCompletedAt(LocalDateTime.now());
            editHistoryRepository.save(editHistory);

            System.out.println("Updated edit history status to COMPLETED for: " + editHistory.getEditHistoryId());

            // Apply the edit to subscription
            applySubscriptionEditFromHistory(editHistory, subscription);

        } catch (Exception e) {
            System.err.println("Error handling edit payment completion: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to process edit payment completion: " + e.getMessage());
        }
    }

    void applySubscriptionEditFromHistory(SubscriptionEditHistory editHistory, Subscription subscription) {
        try {
            System.out.println("Applying edit to subscription: " + subscription.getSubscriptionId());

            // Parse new schedule from JSON
            List<SubscriptionDayDTO> newSchedule = parseScheduleFromJson(editHistory.getNewSchedule());

            if (newSchedule == null || newSchedule.isEmpty()) {
                throw new RuntimeException("No valid schedule found in edit history");
            }

            // Clear existing subscription days
            subscription.getSubscriptionDays().clear();

            // Create new subscription days
            createNewSubscriptionDays(subscription, newSchedule);

            // Update subscription total amount
            updateSubscriptionTotalAmount(subscription, editHistory);

            // Save updated subscription
            subscriptionRepository.save(subscription);

            // Regenerate orders
            regenerateOrdersForEdit(subscription, editHistory);

            System.out.println("Successfully applied edit to subscription: " + subscription.getSubscriptionId());

        } catch (Exception e) {
            System.err.println("Error applying edit from history: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to apply subscription edit: " + e.getMessage());
        }
    }

    private List<SubscriptionDayDTO> parseScheduleFromJson(String json) {
        try {
            if (json == null || json.isEmpty()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(json, new TypeReference<List<SubscriptionDayDTO>>() {});
        } catch (Exception e) {
            System.err.println("Error parsing schedule JSON: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void createNewSubscriptionDays(Subscription subscription, List<SubscriptionDayDTO> newSchedule) {
        if (newSchedule == null || newSchedule.isEmpty()) {
            throw new RuntimeException("New schedule is required");
        }

        for (SubscriptionDayDTO dayDTO : newSchedule) {
            SubscriptionDay subscriptionDay = new SubscriptionDay();
            subscriptionDay.setSubscription(subscription);
            subscriptionDay.setDayOfWeek(dayDTO.getDayOfWeek());
            subscriptionDay.setIsEnabled(dayDTO.getEnabled());

            if (dayDTO.getEnabled() && dayDTO.getMeals() != null && !dayDTO.getMeals().isEmpty()) {
                List<SubscriptionDayMeal> dayMeals = new ArrayList<>();
                for (var mealDTO : dayDTO.getMeals()) {
                    MealSet mealSet = mealSetRepository.findById(mealDTO.getSetId())
                            .orElseThrow(() -> new RuntimeException("Meal set not found: " + mealDTO.getSetId()));

                    SubscriptionDayMeal dayMeal = new SubscriptionDayMeal();
                    dayMeal.setSubscriptionDay(subscriptionDay);
                    dayMeal.setMealSet(mealSet);
                    dayMeal.setQuantity(mealDTO.getQuantity());
                    dayMeal.setUnitPrice(subscription.getMealPackage().getPricePerSet());

                    dayMeals.add(dayMeal);
                }
                subscriptionDay.setSubscriptionDayMeals(dayMeals);
            }
            subscription.getSubscriptionDays().add(subscriptionDay);
        }
    }

    // FIXED: Changed parameter type from lowercase 'subscription' to uppercase 'Subscription'
    private void updateSubscriptionTotalAmount(Subscription subscription, SubscriptionEditHistory editHistory) {
        try {
            if (editHistory.getAdditionalAmount() != null && editHistory.getAdditionalAmount() > 0) {
                // Update total amount with additional payment
                subscription.setTotalAmount(subscription.getTotalAmount() + editHistory.getAdditionalAmount());
                System.out.println("Updated subscription amount by: " + editHistory.getAdditionalAmount() +
                        " New total: " + subscription.getTotalAmount());
            }
        } catch (Exception e) {
            System.err.println("Error updating subscription amount: " + e.getMessage());
        }
    }

    private void regenerateOrdersForEdit(Subscription subscription, SubscriptionEditHistory editHistory) {
        try {
            LocalDate today = LocalDate.now();
            LocalDate startDate = today.isAfter(subscription.getStartDate()) ? today : subscription.getStartDate();

            // Delete future orders
            deleteFutureOrdersForSubscription(subscription.getSubscriptionId());

            // Regenerate orders from today/start date
            generateOrdersForEditPeriod(subscription, startDate);

        } catch (Exception e) {
            System.err.println("Error regenerating orders: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteFutureOrdersForSubscription(String subscriptionId) {
        try {
            LocalDate today = LocalDate.now();

            // Find future orders using the repository method
            List<Order> futureOrders = orderRepository.findBySubscriptionSubscriptionId(subscriptionId).stream()
                    .filter(order -> !order.getDeliveryDate().isBefore(today))
                    .filter(order -> order.getStatus() == Order.OrderStatus.PENDING ||
                            order.getStatus() == Order.OrderStatus.CONFIRMED)
                    .collect(Collectors.toList());

            if (!futureOrders.isEmpty()) {
                orderRepository.deleteAll(futureOrders);
                System.out.println("Deleted " + futureOrders.size() + " future orders for subscription: " + subscriptionId);
            }
        } catch (Exception e) {
            System.err.println("Error deleting future orders: " + e.getMessage());
        }
    }

    private void generateOrdersForEditPeriod(Subscription subscription, LocalDate startDate) {
        try {
            LocalDate endDate = subscription.getEndDate();
            LocalDate currentDate = startDate;
            List<Order> newOrders = new ArrayList<>();

            while (!currentDate.isAfter(endDate)) {
                DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
                String dayName = dayOfWeek.name();

                // Check if this day is enabled in subscription
                boolean isDayEnabled = subscription.getSubscriptionDays().stream()
                        .anyMatch(day -> day.getDayOfWeek().equals(dayName) && day.getIsEnabled());

                if (isDayEnabled) {
                    Order order = createOrderForEdit(subscription, currentDate);
                    if (order != null) {
                        newOrders.add(order);
                    }
                }

                currentDate = currentDate.plusDays(1);
            }

            if (!newOrders.isEmpty()) {
                orderRepository.saveAll(newOrders);
                System.out.println("Generated " + newOrders.size() + " new orders for edited subscription: " +
                        subscription.getSubscriptionId());
            }

        } catch (Exception e) {
            System.err.println("Error generating orders for edit period: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Order createOrderForEdit(Subscription subscription, LocalDate deliveryDate) {
        try {
            Order order = new Order();
            order.setSubscription(subscription);
            order.setDeliveryDate(deliveryDate);
            order.setStatus(Order.OrderStatus.PENDING);
            order.setPreferredDeliveryTime(subscription.getPreferredDeliveryTime());
            order.setDeliveryAddress(subscription.getDeliveryAddress());
            order.setSpecialInstructions(subscription.getSpecialInstructions());

            // Generate order meals based on subscription days
            String dayName = deliveryDate.getDayOfWeek().name();
            Optional<SubscriptionDay> subscriptionDayOpt = subscription.getSubscriptionDays().stream()
                    .filter(day -> day.getDayOfWeek().equals(dayName) && day.getIsEnabled())
                    .findFirst();

            if (subscriptionDayOpt.isPresent()) {
                SubscriptionDay subscriptionDay = subscriptionDayOpt.get();

                if (subscriptionDay.getSubscriptionDayMeals() != null &&
                        !subscriptionDay.getSubscriptionDayMeals().isEmpty()) {

                    List<OrderMeal> orderMeals = new ArrayList<>();
                    for (SubscriptionDayMeal subscriptionDayMeal : subscriptionDay.getSubscriptionDayMeals()) {
                        OrderMeal orderMeal = new OrderMeal();
                        orderMeal.setOrder(order);
                        orderMeal.setMealSet(subscriptionDayMeal.getMealSet());
                        orderMeal.setQuantity(subscriptionDayMeal.getQuantity());
                        orderMeals.add(orderMeal);
                    }
                    order.setOrderMeals(orderMeals);
                    return order;
                }
            }

            return null;

        } catch (Exception e) {
            System.err.println("Error creating order for edit: " + e.getMessage());
            return null;
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getPaymentWithSubscription(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        Map<String, Object> result = new HashMap<>();
        result.put("paymentId", payment.getPaymentId());
        result.put("paymentMethod", payment.getPaymentMethod().name());
        result.put("paymentStatus", payment.getPaymentStatus().name());
        result.put("amount", payment.getAmount());
        result.put("transactionId", payment.getTransactionId());
        result.put("gatewayTransactionId", payment.getGatewayTransactionId());
        result.put("paidAt", payment.getPaidAt());
        result.put("paymentType", payment.getPaymentType());
        result.put("paymentGatewayResponse", payment.getPaymentGatewayResponse());

        // Add subscription details
        if (payment.getSubscription() != null) {
            Subscription subscription = payment.getSubscription();
            Map<String, Object> subscriptionInfo = new HashMap<>();
            subscriptionInfo.put("subscriptionId", subscription.getSubscriptionId());
            subscriptionInfo.put("status", subscription.getStatus().name());
            subscriptionInfo.put("startDate", subscription.getStartDate());
            subscriptionInfo.put("endDate", subscription.getEndDate());
            subscriptionInfo.put("deliveryAddress", subscription.getDeliveryAddress());
            subscriptionInfo.put("preferredDeliveryTime", subscription.getPreferredDeliveryTime());
            subscriptionInfo.put("totalAmount", subscription.getTotalAmount());

            // Add package info
            if (subscription.getMealPackage() != null) {
                Map<String, Object> packageInfo = new HashMap<>();
                packageInfo.put("packageId", subscription.getMealPackage().getPackageId());
                packageInfo.put("packageName", subscription.getMealPackage().getName());
                subscriptionInfo.put("package", packageInfo);
            }

            // Add user info
            if (subscription.getUser() != null) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("userId", subscription.getUser().getId());
                userInfo.put("userName", subscription.getUser().getUserName());
                userInfo.put("email", subscription.getUser().getEmail());
                subscriptionInfo.put("user", userInfo);
            }

            result.put("subscription", subscriptionInfo);
        }

        return result;
    }

    @Transactional(readOnly = true)
    public Payment getPaymentById(String paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
    }

    @Transactional
    public void updatePaymentStatus(String paymentId, Payment.PaymentStatus status, String transactionId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        payment.setPaymentStatus(status);
        if (transactionId != null) {
            payment.setTransactionId(transactionId);
        }
        if (status == Payment.PaymentStatus.COMPLETED) {
            payment.setPaidAt(LocalDateTime.now());
        }

        paymentRepository.save(payment);

        // Update subscription status if payment completed and it's a regular payment
        if (status == Payment.PaymentStatus.COMPLETED && "REGULAR".equals(payment.getPaymentType())) {
            if (payment.getSubscription() != null && payment.getSubscription().getStatus() != Subscription.SubscriptionStatus.ACTIVE) {
                payment.getSubscription().setStatus(Subscription.SubscriptionStatus.ACTIVE);
                subscriptionRepository.save(payment.getSubscription());
            }
        }
    }

    @Transactional
    public Payment processRefund(String paymentId, String reason) {
        try {
            System.out.println("Processing refund for payment ID: " + paymentId);

            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

            if (payment.getPaymentStatus() != Payment.PaymentStatus.COMPLETED) {
                throw new RuntimeException("Cannot refund a payment that is not completed. Current status: " + payment.getPaymentStatus());
            }

            payment.setPaymentStatus(Payment.PaymentStatus.REFUNDED);

            // Update payment gateway response with refund info
            String currentResponse = payment.getPaymentGatewayResponse() != null ? payment.getPaymentGatewayResponse() : "";
            String refundInfo = String.format("\n=== REFUND DETAILS ===\n" +
                            "Refund Reason: %s\n" +
                            "Refunded At: %s\n" +
                            "Refund Processed By: System",
                    reason, LocalDateTime.now());

            payment.setPaymentGatewayResponse(currentResponse + refundInfo);

            Payment refundedPayment = paymentRepository.save(payment);
            System.out.println("Payment refunded successfully. Payment ID: " + refundedPayment.getPaymentId());

            return refundedPayment;
        } catch (Exception e) {
            System.err.println("Error processing refund: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to process refund: " + e.getMessage());
        }
    }

    public void handleCancellationRefund(String subscriptionId) {
        // Implement refund logic based on payment method
        System.out.println("Processing refund for cancelled subscription: " + subscriptionId);
    }

    private String generateUniquePaymentId(String prefix) {
        String paymentId;
        int attempt = 0;

        do {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            paymentId = prefix + timestamp + random;
            attempt++;

            if (attempt > 10) {
                throw new RuntimeException("Failed to generate unique payment ID after 10 attempts");
            }

        } while (paymentRepository.existsById(paymentId) || generatedPaymentIds.contains(paymentId));

        generatedPaymentIds.add(paymentId);
        return paymentId;
    }

    private String generateUniqueEditPaymentId() {
        String paymentId;
        int attempt = 0;

        do {
            // Generate edit payment ID with timestamp and random UUID
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
            String random = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            paymentId = "EDIT_PAY_" + timestamp + "_" + random;
            attempt++;

            // Safety check to prevent infinite loop
            if (attempt > 10) {
                throw new RuntimeException("Failed to generate unique payment ID after 10 attempts");
            }

        } while (paymentRepository.existsById(paymentId) || generatedPaymentIds.contains(paymentId));

        // Add to local cache to prevent duplicates in same session
        generatedPaymentIds.add(paymentId);

        return paymentId;
    }

    public List<AdminPaymentDTO> getAllPaymentsWithDetails() {
        try {
            List<Payment> payments = paymentRepository.findAll();

            return payments.stream()
                    .map(this::convertToAdminPaymentDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error fetching all payment details: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch payment details: " + e.getMessage());
        }
    }

    private AdminPaymentDTO convertToAdminPaymentDTO(Payment payment) {
        try {
            AdminPaymentDTO dto = new AdminPaymentDTO();

            // Payment details
            dto.setPaymentId(payment.getPaymentId());
            dto.setPaymentMethod(payment.getPaymentMethod().name());
            dto.setPaymentStatus(payment.getPaymentStatus().name());
            dto.setAmount(payment.getAmount());
            dto.setTransactionId(payment.getTransactionId());
            dto.setGatewayTransactionId(payment.getGatewayTransactionId());
            dto.setPaidAt(payment.getPaidAt());
            dto.setPaymentType(payment.getPaymentType());

            // Subscription details
            if (payment.getSubscription() != null) {
                Subscription subscription = payment.getSubscription();
                dto.setSubscriptionId(subscription.getSubscriptionId());
                dto.setSubscriptionStatus(subscription.getStatus().name());

                // Package details
                if (subscription.getMealPackage() != null) {
                    MealPackage mealPackage = subscription.getMealPackage();
                    dto.setPackageId(mealPackage.getPackageId());
                    dto.setPackageName(mealPackage.getName());
                    dto.setPackagePrice(mealPackage.getPricePerSet());
                    dto.setDurationDays(mealPackage.getDurationDays());

                    // Vendor details
                    if (mealPackage.getVendor() != null) {
                        Vendor vendor = mealPackage.getVendor();
                        dto.setVendorId(String.valueOf(vendor.getVendorId()));
                        dto.setVendorName(vendor.getBusinessName());
                        dto.setVendorEmail(vendor.getBusinessEmail());
                    }
                }

                // User details
                if (subscription.getUser() != null) {
                    User user = subscription.getUser();
                    dto.setUserId(String.valueOf(user.getId()));
                    dto.setUserName(user.getUserName());
                    dto.setUserEmail(user.getEmail());
                    dto.setUserPhone(user.getPhoneNumber());
                }
            }

            return dto;

        } catch (Exception e) {
            System.err.println("Error converting payment to AdminPaymentDTO: " + e.getMessage());
            e.printStackTrace();
            // Return basic payment info if conversion fails
            AdminPaymentDTO basicDto = new AdminPaymentDTO();
            basicDto.setPaymentId(payment.getPaymentId());
            basicDto.setPaymentMethod(payment.getPaymentMethod().name());
            basicDto.setPaymentStatus(payment.getPaymentStatus().name());
            basicDto.setAmount(payment.getAmount());
            basicDto.setTransactionId(payment.getTransactionId());
            basicDto.setPaymentType(payment.getPaymentType());
            return basicDto;
        }
    }

    private String generateHmacSha256(String data, String secret) {
        try {
            // IMPORTANT: eSewa expects the data string to be signed exactly as provided
            // Do not modify the data string in any way
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
            return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes()));
        } catch (Exception e) {
            System.err.println("Failed to generate HMAC signature. Data: " + data);
            e.printStackTrace();
            throw new RuntimeException("Failed to generate HMAC signature", e);
        }
    }

    // New method to get subscription payments
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsBySubscriptionId(String subscriptionId) {
        return paymentRepository.findBySubscriptionSubscriptionId(subscriptionId);
    }

    // New method to get subscription payment summary
    @Transactional(readOnly = true)
    public Map<String, Object> getSubscriptionPaymentSummary(String subscriptionId) {
        List<Payment> payments = paymentRepository.findBySubscriptionSubscriptionId(subscriptionId);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalPayments", payments.size());
        summary.put("totalAmount", payments.stream().mapToDouble(Payment::getAmount).sum());
        summary.put("completedPayments", payments.stream()
                .filter(p -> p.getPaymentStatus() == Payment.PaymentStatus.COMPLETED)
                .count());
        summary.put("pendingPayments", payments.stream()
                .filter(p -> p.getPaymentStatus() == Payment.PaymentStatus.PENDING)
                .count());
        summary.put("refundedPayments", payments.stream()
                .filter(p -> p.getPaymentStatus() == Payment.PaymentStatus.REFUNDED)
                .count());
        summary.put("regularPayments", payments.stream()
                .filter(p -> "REGULAR".equals(p.getPaymentType()))
                .count());
        summary.put("editPayments", payments.stream()
                .filter(p -> "EDIT".equals(p.getPaymentType()))
                .count());

        // Get latest payment
        payments.stream()
                .max(Comparator.comparing(Payment::getCreatedAt))
                .ifPresent(latest -> {
                    summary.put("latestPaymentId", latest.getPaymentId());
                    summary.put("latestPaymentStatus", latest.getPaymentStatus().name());
                    summary.put("latestPaymentAmount", latest.getAmount());
                });

        return summary;
    }

    // New method to handle refund processing
    @Transactional
    public EditSubscriptionResponseDTO processRefundForEdit(String editHistoryId, String userEmail) {
        try {
            // Find edit history
            SubscriptionEditHistory editHistory = editHistoryRepository.findById(editHistoryId)
                    .orElseThrow(() -> new RuntimeException("Edit history not found: " + editHistoryId));

            Subscription subscription = editHistory.getSubscription();

            // Verify user authorization
            if (!subscription.getUser().getEmail().equals(userEmail)) {
                throw new RuntimeException("You are not authorized to process this refund");
            }

            // Check if refund is available
            if (editHistory.getRefundAmount() == null || editHistory.getRefundAmount() <= 0) {
                throw new RuntimeException("No refund available for this edit");
            }

            // Update edit history status
            editHistory.setStatus("REFUND_PROCESSING");
            editHistory.setCompletedAt(LocalDateTime.now());
            editHistoryRepository.save(editHistory);

            // Send refund processing notification
            sendRefundProcessingNotification(subscription, editHistory.getRefundAmount(), editHistory.getEditReason());

            // Create response
            EditSubscriptionResponseDTO response = new EditSubscriptionResponseDTO();
            response.setSubscriptionId(subscription.getSubscriptionId());
            response.setStatus(subscription.getStatus().name());
            response.setRefundAmount(editHistory.getRefundAmount());
            response.setEditStatus("REFUND_PROCESSING");
            response.setMessage("Refund of Rs. " + String.format("%.2f", editHistory.getRefundAmount()) +
                    " is being processed. It will be credited to your original payment method within 5-7 business days.");
            response.setEditedAt(LocalDateTime.now());
            response.setEditHistoryId(editHistoryId);
            response.setVendorPhone(subscription.getMealPackage().getVendor().getPhone());
            response.setVendorName(subscription.getMealPackage().getVendor().getBusinessName());

            return response;

        } catch (Exception e) {
            System.err.println("Error processing refund for edit: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to process refund: " + e.getMessage());
        }
    }

    private void sendRefundProcessingNotification(Subscription subscription, Double refundAmount, String reason) {
        try {
            // Email to user
            String userSubject = "Refund Processing - TiffinSathi";
            String userMessage = String.format(
                    "Dear %s,\n\n" +
                            "Your refund is being processed!\n\n" +
                            "Subscription ID: %s\n" +
                            "Refund Amount: Rs. %.2f\n" +
                            "Reason: %s\n" +
                            "Expected Processing Time: 5-7 business days\n" +
                            "Refund Method: Original payment method\n\n" +
                            "If you have any questions, please contact:\n" +
                            "Vendor: %s\n" +
                            "Phone: %s\n" +
                            "Email: %s\n\n" +
                            "Thank you for choosing TiffinSathi!",
                    subscription.getUser().getUserName(),
                    subscription.getSubscriptionId(),
                    refundAmount,
                    reason,
                    subscription.getMealPackage().getVendor().getBusinessName(),
                    subscription.getMealPackage().getVendor().getPhone(),
                    subscription.getMealPackage().getVendor().getBusinessEmail()
            );

            // Email to vendor
            String vendorSubject = "Refund Processed - Subscription: " + subscription.getSubscriptionId();
            String vendorMessage = String.format(
                    "A refund has been processed for subscription edit.\n\n" +
                            "Subscription ID: %s\n" +
                            "Customer: %s\n" +
                            "Customer Email: %s\n" +
                            "Refund Amount: Rs. %.2f\n" +
                            "Reason: %s\n\n" +
                            "Please note that the refund will be processed through the original payment gateway.",
                    subscription.getSubscriptionId(),
                    subscription.getUser().getUserName(),
                    subscription.getUser().getEmail(),
                    refundAmount,
                    reason
            );

            // In a real implementation, you would send emails here
            System.out.println("Refund notification prepared for user: " + subscription.getUser().getEmail());
            System.out.println("Refund notification prepared for vendor: " + subscription.getMealPackage().getVendor().getBusinessEmail());

        } catch (Exception e) {
            System.err.println("Failed to send refund notification: " + e.getMessage());
        }
    }

    // Helper method to get payment by transaction ID (for backward compatibility)
    public Payment getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Payment not found for transactionId: " + transactionId));
    }
}