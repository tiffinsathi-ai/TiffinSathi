    package com.tiffin_sathi.model;

    import jakarta.persistence.*;
    import org.hibernate.annotations.CreationTimestamp;
    import java.time.LocalDateTime;

    @Entity
    @Table(name = "payments")
    public class Payment {

        @Id
        @Column(name = "payment_id", length = 50)
        private String paymentId;

        @ManyToOne(fetch = FetchType.LAZY)  // Changed from @OneToOne to @ManyToOne
        @JoinColumn(name = "subscription_id", nullable = false)  // Removed unique = true
        private Subscription subscription;

        @Enumerated(EnumType.STRING)
        @Column(name = "payment_method", nullable = false, length = 20)
        private PaymentMethod paymentMethod;

        @Enumerated(EnumType.STRING)
        @Column(name = "payment_status", nullable = false, length = 20)
        private PaymentStatus paymentStatus;

        @Column(name = "amount", nullable = false)
        private Double amount;

        @Column(name = "transaction_id", length = 100)
        private String transactionId;

        @Column(name = "gateway_transaction_id", length = 100)
        private String gatewayTransactionId; // For Khalti's pidx or eSewa's transaction_uuid

        @Column(name = "payment_gateway_response", columnDefinition = "TEXT")
        private String paymentGatewayResponse;

        @CreationTimestamp
        @Column(name = "created_at")
        private LocalDateTime createdAt;

        @Column(name = "paid_at")
        private LocalDateTime paidAt;

        @Column(name = "payment_type", length = 20)  // Added to distinguish payment types
        private String paymentType = "REGULAR"; // REGULAR, EDIT, REFUND, etc.

        public enum PaymentMethod {
            ESEWA,
            KHALTI,
            CARD,
            CASH_ON_DELIVERY
        }

        public enum PaymentStatus {
            PENDING,
            COMPLETED,
            FAILED,
            REFUNDED
        }

        // Constructors
        public Payment() {}

        public Payment(String paymentId, Subscription subscription, PaymentMethod paymentMethod,
                       PaymentStatus paymentStatus, Double amount) {
            this.paymentId = paymentId;
            this.subscription = subscription;
            this.paymentMethod = paymentMethod;
            this.paymentStatus = paymentStatus;
            this.amount = amount;
        }

        // Getters and Setters
        public String getPaymentId() { return paymentId; }
        public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

        public Subscription getSubscription() { return subscription; }
        public void setSubscription(Subscription subscription) { this.subscription = subscription; }

        public PaymentMethod getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

        public PaymentStatus getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }

        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

        public String getGatewayTransactionId() { return gatewayTransactionId; }
        public void setGatewayTransactionId(String gatewayTransactionId) { this.gatewayTransactionId = gatewayTransactionId; }

        public String getPaymentGatewayResponse() { return paymentGatewayResponse; }
        public void setPaymentGatewayResponse(String paymentGatewayResponse) { this.paymentGatewayResponse = paymentGatewayResponse; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getPaidAt() { return paidAt; }
        public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

        public String getPaymentType() { return paymentType; }
        public void setPaymentType(String paymentType) { this.paymentType = paymentType; }
    }