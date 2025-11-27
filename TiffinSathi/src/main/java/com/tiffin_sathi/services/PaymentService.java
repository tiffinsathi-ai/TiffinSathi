package com.tiffin_sathi.services;

import com.tiffin_sathi.model.Payment;
import com.tiffin_sathi.model.Subscription;
import com.tiffin_sathi.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

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
}