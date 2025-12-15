package com.tiffin_sathi.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiffin_sathi.config.PaymentGatewayConfig;
import com.tiffin_sathi.dtos.AdminPaymentDTO;
import com.tiffin_sathi.dtos.PaymentInitiationResponse;
import com.tiffin_sathi.model.*;
import com.tiffin_sathi.repository.PaymentRepository;
import com.tiffin_sathi.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public Payment createPayment(Subscription subscription, String paymentMethod, Double amount) {
        Payment payment = new Payment();

        // Generate payment ID (PAY202412...)
        String paymentId = generatePaymentId();
        payment.setPaymentId(paymentId);

        // Generate transaction ID (TXN...)
        String transactionId = "TXN" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        payment.setTransactionId(transactionId);

        payment.setSubscription(subscription);
        payment.setPaymentMethod(Payment.PaymentMethod.valueOf(paymentMethod.toUpperCase()));
        payment.setAmount(amount);

        // For logging purposes
        System.out.println("Created Payment - Payment ID: " + paymentId + ", Transaction ID: " + transactionId);

        // Set status based on payment method
        if (paymentMethod.equalsIgnoreCase("CASH_ON_DELIVERY")) {
            payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);
            payment.setPaidAt(LocalDateTime.now());
        } else {
            payment.setPaymentStatus(Payment.PaymentStatus.PENDING);
        }

        Payment savedPayment = paymentRepository.save(payment);
        subscription.setPayment(savedPayment);
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

        // Create payment record
        Payment payment = createPayment(subscription, paymentMethod, amount);

        // Generate payment response based on gateway
        if (paymentMethod.equalsIgnoreCase("ESEWA")) {
            return initiateEsewaPayment(payment);
        } else if (paymentMethod.equalsIgnoreCase("KHALTI")) {
            return initiateKhaltiPayment(payment);
        } else {
            throw new RuntimeException("Unsupported payment method: " + paymentMethod);
        }
    }

    private PaymentInitiationResponse initiateEsewaPayment(Payment payment) {
        try {
            String amountStr = String.format("%.2f", payment.getAmount());

            // For eSewa, we need to send transaction_uuid
            // We can use paymentId as transaction_uuid since it's unique and eSewa expects it
            String transactionUuid = payment.getPaymentId(); // Using paymentId as transaction_uuid

            // Generate signature
            String dataToSign = "total_amount=" + amountStr +
                    ",transaction_uuid=" + transactionUuid +
                    ",product_code=" + paymentGatewayConfig.getEsewaMerchantCode();
            String signature = generateHmacSha256(dataToSign, paymentGatewayConfig.getEsewaSecretKey());

            // Prepare payment data
            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("amount", amountStr);
            paymentData.put("tax_amount", "0");
            paymentData.put("total_amount", amountStr);
            paymentData.put("transaction_uuid", transactionUuid);  // Using paymentId
            paymentData.put("product_code", paymentGatewayConfig.getEsewaMerchantCode());
            paymentData.put("product_service_charge", "0");
            paymentData.put("product_delivery_charge", "0");
            paymentData.put("success_url", "http://localhost:8080/api/payments/callback/esewa");
            paymentData.put("failure_url", "http://localhost:8080/api/payments/callback/esewa/failure");
            paymentData.put("signed_field_names", "total_amount,transaction_uuid,product_code");
            paymentData.put("signature", signature);

            // Store the transaction_uuid as gatewayTransactionId for reference
            payment.setGatewayTransactionId(transactionUuid);
            paymentRepository.save(payment);

            // Create response
            PaymentInitiationResponse response = new PaymentInitiationResponse();
            response.setPaymentId(payment.getPaymentId());
            response.setPaymentMethod("ESEWA");
            response.setPaymentUrl(paymentGatewayConfig.getEsewaBaseUrl());
            response.setPaymentData(paymentData);
            response.setMessage("eSewa payment initiated successfully");

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Failed to initiate eSewa payment: " + e.getMessage());
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
            body.put("purchase_order_id", payment.getTransactionId()); // Use our transaction ID
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
            ResponseEntity<Map> response = restTemplate.postForEntity(khaltiInitiateUrl, entity, Map.class);

            Map<String, Object> responseBody = response.getBody();

            if (response.getStatusCode() == HttpStatus.OK && responseBody != null) {
                // Update payment with Khalti pidx
                String pidx = (String) responseBody.get("pidx");
                payment.setGatewayTransactionId(pidx); // Store pidx as gateway transaction ID
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
                throw new RuntimeException("Failed to initiate Khalti payment");
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to initiate Khalti payment: " + e.getMessage());
        }
    }

    @Transactional
    public void handleEsewaCallback(String encodedData) {
        try {
            // Decode base64 data
            byte[] decodedBytes = Base64.getDecoder().decode(encodedData);
            String jsonData = new String(decodedBytes);

            System.out.println("Received eSewa callback data: " + jsonData);

            // Parse JSON properly using Jackson
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> dataMap = objectMapper.readValue(jsonData, new TypeReference<Map<String, String>>() {});

            String transactionUuid = dataMap.get("transaction_uuid");
            String status = dataMap.get("status");
            String transactionCode = dataMap.get("transaction_code");

            System.out.println("Extracted values - transaction_uuid: " + transactionUuid +
                    ", status: " + status + ", transaction_code: " + transactionCode);

            if (transactionUuid == null) {
                throw new RuntimeException("No transaction_uuid found in eSewa response");
            }

            // Find payment by paymentId (which we used as transaction_uuid)
            Payment payment = paymentRepository.findById(transactionUuid)
                    .orElseThrow(() -> new RuntimeException("Payment not found for ID: " + transactionUuid));

            if ("COMPLETE".equalsIgnoreCase(status)) {
                // Payment successful
                payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);
                payment.setPaidAt(LocalDateTime.now());
                payment.setGatewayTransactionId(transactionCode); // Store eSewa transaction code
                payment.setPaymentGatewayResponse(jsonData);

                paymentRepository.save(payment);

                // Update subscription
                Subscription subscription = payment.getSubscription();
                subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
                subscriptionRepository.save(subscription);

                System.out.println("eSewa payment completed for: " + payment.getPaymentId());
            } else {
                // Payment failed
                payment.setPaymentStatus(Payment.PaymentStatus.FAILED);
                payment.setPaymentGatewayResponse(jsonData);
                paymentRepository.save(payment);

                System.out.println("eSewa payment failed for: " + payment.getPaymentId());
                throw new RuntimeException("Payment failed with status: " + status);
            }

        } catch (Exception e) {
            System.err.println("Error processing eSewa callback: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Payment verification failed: " + e.getMessage());
        }
    }

    @Transactional
    public void handleKhaltiCallback(String pidx) {
        try {
            System.out.println("Processing Khalti callback for pidx: " + pidx);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Key " + paymentGatewayConfig.getKhaltiSecretKey());
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(headers);

            String verifyUrl = paymentGatewayConfig.getKhaltiBaseUrl() + "/epayment/lookup/?pidx=" + pidx;
            ResponseEntity<Map> response = restTemplate.postForEntity(verifyUrl, entity, Map.class);

            System.out.println("Khalti verification response: " + response.getBody());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String status = (String) responseBody.get("status");

                if ("Completed".equals(status)) {
                    // Find payment by pidx
                    Payment payment = paymentRepository.findByGatewayTransactionId(pidx)
                            .orElseThrow(() -> new RuntimeException("Payment not found for pidx: " + pidx));

                    payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);
                    payment.setPaidAt(LocalDateTime.now());
                    payment.setPaymentGatewayResponse(responseBody.toString());

                    paymentRepository.save(payment);

                    // Update subscription
                    Subscription subscription = payment.getSubscription();
                    subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
                    subscriptionRepository.save(subscription);

                    System.out.println("Khalti payment completed for payment ID: " + payment.getPaymentId());
                } else {
                    System.err.println("Khalti payment not completed. Status: " + status);
                    throw new RuntimeException("Payment not completed. Status: " + status);
                }
            } else {
                System.err.println("Failed to verify Khalti payment");
                throw new RuntimeException("Failed to verify Khalti payment");
            }

        } catch (Exception e) {
            System.err.println("Error processing Khalti callback: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Payment verification failed: " + e.getMessage());
        }
    }

    // Add a method to get payment details with subscription
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

            result.put("subscription", subscriptionInfo);
        }

        return result;
    }

    // Update the existing getPaymentById method
    @Transactional(readOnly = true)
    public Payment getPaymentById(String paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
    }

    // Method to get payment by pidx
    @Transactional(readOnly = true)
    public Payment getPaymentByPidx(String pidx) {
        return paymentRepository.findByGatewayTransactionId(pidx)
                .orElseThrow(() -> new RuntimeException("Payment not found for pidx: " + pidx));
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

        // Update subscription status if payment completed
        if (status == Payment.PaymentStatus.COMPLETED) {
            payment.getSubscription().setStatus(Subscription.SubscriptionStatus.ACTIVE);
            subscriptionRepository.save(payment.getSubscription());
        }
    }

    public void handleCancellationRefund(String subscriptionId) {
        // Implement refund logic based on payment method
        // This would integrate with payment gateways for actual refunds
        System.out.println("Processing refund for cancelled subscription: " + subscriptionId);
    }

    private String generatePaymentId() {
        return "PAY" + LocalDateTime.now().getYear() +
                String.format("%02d", LocalDateTime.now().getMonthValue()) +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
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
            return basicDto;
        }
    }

    private String generateHmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
            return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC signature", e);
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
            System.err.println("Error extracting key '" + key + "' from JSON: " + e.getMessage());
        }
        return null;
    }
}