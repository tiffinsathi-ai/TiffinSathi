package com.tiffin_sathi.dtos;

import java.time.LocalDateTime;

public class AdminPaymentDTO {
    private String paymentId;
    private String paymentMethod;
    private String paymentStatus;
    private Double amount;
    private String transactionId;
    private String gatewayTransactionId;
    private LocalDateTime paidAt;
    private String paymentType; // Added field for payment type (REGULAR, EDIT, REFUND, etc.)

    // Subscription info
    private String subscriptionId;
    private String subscriptionStatus;

    // Package info
    private String packageId;
    private String packageName;
    private Double packagePrice;
    private Integer durationDays;

    // User info
    private String userId;
    private String userName;
    private String userEmail;
    private String userPhone;

    // Vendor info
    private String vendorId;
    private String vendorName;
    private String vendorEmail;

    // Constructors
    public AdminPaymentDTO() {}

    public AdminPaymentDTO(String paymentId, String paymentMethod, String paymentStatus,
                           Double amount, String transactionId, String gatewayTransactionId,
                           LocalDateTime paidAt, String paymentType,
                           String subscriptionId, String subscriptionStatus,
                           String packageId, String packageName, Double packagePrice, Integer durationDays,
                           String userId, String userName, String userEmail, String userPhone,
                           String vendorId, String vendorName, String vendorEmail) {
        this.paymentId = paymentId;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.amount = amount;
        this.transactionId = transactionId;
        this.gatewayTransactionId = gatewayTransactionId;
        this.paidAt = paidAt;
        this.paymentType = paymentType;
        this.subscriptionId = subscriptionId;
        this.subscriptionStatus = subscriptionStatus;
        this.packageId = packageId;
        this.packageName = packageName;
        this.packagePrice = packagePrice;
        this.durationDays = durationDays;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPhone = userPhone;
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.vendorEmail = vendorEmail;
    }

    // Getters and Setters
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getGatewayTransactionId() { return gatewayTransactionId; }
    public void setGatewayTransactionId(String gatewayTransactionId) { this.gatewayTransactionId = gatewayTransactionId; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public String getPaymentType() { return paymentType; } // Getter
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; } // Setter - This was missing

    public String getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(String subscriptionId) { this.subscriptionId = subscriptionId; }

    public String getSubscriptionStatus() { return subscriptionStatus; }
    public void setSubscriptionStatus(String subscriptionStatus) { this.subscriptionStatus = subscriptionStatus; }

    public String getPackageId() { return packageId; }
    public void setPackageId(String packageId) { this.packageId = packageId; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public Double getPackagePrice() { return packagePrice; }
    public void setPackagePrice(Double packagePrice) { this.packagePrice = packagePrice; }

    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }

    public String getVendorId() { return vendorId; }
    public void setVendorId(String vendorId) { this.vendorId = vendorId; }

    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }

    public String getVendorEmail() { return vendorEmail; }
    public void setVendorEmail(String vendorEmail) { this.vendorEmail = vendorEmail; }

    // Optional: toString method for debugging
    @Override
    public String toString() {
        return "AdminPaymentDTO{" +
                "paymentId='" + paymentId + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", paymentStatus='" + paymentStatus + '\'' +
                ", amount=" + amount +
                ", transactionId='" + transactionId + '\'' +
                ", gatewayTransactionId='" + gatewayTransactionId + '\'' +
                ", paidAt=" + paidAt +
                ", paymentType='" + paymentType + '\'' +
                ", subscriptionId='" + subscriptionId + '\'' +
                ", subscriptionStatus='" + subscriptionStatus + '\'' +
                ", packageId='" + packageId + '\'' +
                ", packageName='" + packageName + '\'' +
                ", packagePrice=" + packagePrice +
                ", durationDays=" + durationDays +
                ", userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", userPhone='" + userPhone + '\'' +
                ", vendorId='" + vendorId + '\'' +
                ", vendorName='" + vendorName + '\'' +
                ", vendorEmail='" + vendorEmail + '\'' +
                '}';
    }
}