package com.tiffin_sathi.dtos;

import java.time.LocalDateTime;

public class EditSubscriptionResponseDTO {
    private String subscriptionId;
    private String status;
    private String editStatus;
    private Double additionalPayment;
    private Double refundAmount;

    // NEW fields for UI breakdown
    private Double oldCost;
    private Double newCost;
    private String message;
    private String editHistoryId;
    private String vendorPhone;
    private String vendorName;
    private LocalDateTime editedAt;

    // Add missing field
    private String paymentId;

    // Getters and Setters
    public String getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(String subscriptionId) { this.subscriptionId = subscriptionId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getEditStatus() { return editStatus; }
    public void setEditStatus(String editStatus) { this.editStatus = editStatus; }

    public Double getAdditionalPayment() { return additionalPayment; }
    public void setAdditionalPayment(Double additionalPayment) { this.additionalPayment = additionalPayment; }

    public Double getRefundAmount() { return refundAmount; }
    public void setRefundAmount(Double refundAmount) { this.refundAmount = refundAmount; }

    public Double getOldCost() { return oldCost; }
    public void setOldCost(Double oldCost) { this.oldCost = oldCost; }

    public Double getNewCost() { return newCost; }
    public void setNewCost(Double newCost) { this.newCost = newCost; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getEditHistoryId() { return editHistoryId; }
    public void setEditHistoryId(String editHistoryId) { this.editHistoryId = editHistoryId; }

    public String getVendorPhone() { return vendorPhone; }
    public void setVendorPhone(String vendorPhone) { this.vendorPhone = vendorPhone; }

    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }

    public LocalDateTime getEditedAt() { return editedAt; }
    public void setEditedAt(LocalDateTime editedAt) { this.editedAt = editedAt; }

    // Add missing getter and setter
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
}