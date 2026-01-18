package com.tiffin_sathi.repository;

import com.tiffin_sathi.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    // Find all payments for a subscription (returns list instead of single payment)
    List<Payment> findBySubscriptionSubscriptionId(String subscriptionId);

    // Find payment by gateway transaction ID (unique)
    Optional<Payment> findByGatewayTransactionId(String gatewayTransactionId);

    // Find payment by transaction ID (unique)
    Optional<Payment> findByTransactionId(String transactionId);

    // Find latest payment for a subscription
    @Query("SELECT p FROM Payment p WHERE p.subscription.subscriptionId = :subscriptionId ORDER BY p.createdAt DESC")
    List<Payment> findLatestPaymentsBySubscriptionId(@Param("subscriptionId") String subscriptionId);

    // Find latest payment with specific status
    @Query("SELECT p FROM Payment p WHERE p.subscription.subscriptionId = :subscriptionId AND p.paymentStatus = :status ORDER BY p.createdAt DESC")
    Optional<Payment> findLatestPaymentBySubscriptionIdAndStatus(
            @Param("subscriptionId") String subscriptionId,
            @Param("status") Payment.PaymentStatus status);

    // Find edit payments for a subscription
    @Query("SELECT p FROM Payment p WHERE p.subscription.subscriptionId = :subscriptionId AND p.paymentType = 'EDIT'")
    List<Payment> findEditPaymentsBySubscriptionId(@Param("subscriptionId") String subscriptionId);

    // Find edit payment by subscription and status - UPDATED: Return List instead of Optional
    @Query("SELECT p FROM Payment p WHERE p.subscription.subscriptionId = :subscriptionId AND p.paymentType = 'EDIT' AND p.paymentStatus = :status ORDER BY p.createdAt DESC")
    List<Payment> findEditPaymentBySubscriptionAndStatus(
            @Param("subscriptionId") String subscriptionId,
            @Param("status") Payment.PaymentStatus status);

    // Find payments by status
    List<Payment> findByPaymentStatus(Payment.PaymentStatus status);

    // Find payments by type and status
    @Query("SELECT p FROM Payment p WHERE p.paymentType = :paymentType AND p.paymentStatus = :status")
    List<Payment> findByPaymentTypeAndStatus(
            @Param("paymentType") String paymentType,
            @Param("status") Payment.PaymentStatus status);

    // Find payments within date range
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<Payment> findPaymentsBetweenDates(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

    // Find completed payments for a subscription
    @Query("SELECT p FROM Payment p WHERE p.subscription.subscriptionId = :subscriptionId AND p.paymentStatus = 'COMPLETED'")
    List<Payment> findCompletedPaymentsBySubscriptionId(@Param("subscriptionId") String subscriptionId);

    // Find payment by subscription and payment type
    @Query("SELECT p FROM Payment p WHERE p.subscription.subscriptionId = :subscriptionId AND p.paymentType = :paymentType ORDER BY p.createdAt DESC")
    List<Payment> findBySubscriptionAndPaymentType(
            @Param("subscriptionId") String subscriptionId,
            @Param("paymentType") String paymentType);
    
}