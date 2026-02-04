package com.tiffin_sathi.dtos;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class EditSubscriptionRequestDTO {

    @NotNull(message = "Subscription ID is required")
    private String subscriptionId;

    private List<SubscriptionDayDTO> newSchedule;
    private Double additionalPayment = 0.0;
    private Double refundAmount = 0.0;
    private String editReason;
    private String paymentMethod;  // Added missing field

    // Getters and Setters
    public String getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(String subscriptionId) { this.subscriptionId = subscriptionId; }

    public List<SubscriptionDayDTO> getNewSchedule() { return newSchedule; }
    public void setNewSchedule(List<SubscriptionDayDTO> newSchedule) { this.newSchedule = newSchedule; }

    public Double getAdditionalPayment() { return additionalPayment; }
    public void setAdditionalPayment(Double additionalPayment) { this.additionalPayment = additionalPayment; }

    public Double getRefundAmount() { return refundAmount; }
    public void setRefundAmount(Double refundAmount) { this.refundAmount = refundAmount; }

    public String getEditReason() { return editReason; }
    public void setEditReason(String editReason) { this.editReason = editReason; }

    public String getPaymentMethod() { return paymentMethod; }  // Added getter
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }  // Added setter
}