package com.tiffin_sathi.services;

import com.tiffin_sathi.dtos.AdminPaymentDTO;
import com.tiffin_sathi.model.*;
import com.tiffin_sathi.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Transactional
    public Payment createPayment(Subscription subscription, String paymentMethod, Double amount) {
        Payment payment = new Payment();
        payment.setPaymentId(generatePaymentId());
        payment.setSubscription(subscription);
        payment.setPaymentMethod(Payment.PaymentMethod.valueOf(paymentMethod.toUpperCase()));
        payment.setAmount(amount);

        // For demo purposes, mark all payments as completed
        // In real scenario, you'd integrate with payment gateway
        payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);
        payment.setPaidAt(LocalDateTime.now());
        payment.setTransactionId("TXN" + UUID.randomUUID().toString().substring(0, 10).toUpperCase());

        Payment savedPayment = paymentRepository.save(payment);
        subscription.setPayment(savedPayment);

        System.out.println("Payment created: " + savedPayment.getPaymentId() +
                " for subscription: " + subscription.getSubscriptionId() +
                " Transaction ID: " + savedPayment.getTransactionId());
        return savedPayment;
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
            return basicDto;
        }
    }
}