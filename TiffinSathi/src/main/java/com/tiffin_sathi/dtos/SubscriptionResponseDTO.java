package com.tiffin_sathi.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class SubscriptionResponseDTO {
    private String subscriptionId;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double totalAmount;
    private Double subtotalAmount;
    private Double deliveryFee;
    private Double taxAmount;
    private Double discountAmount;
    private String deliveryAddress;
    private String landmark;
    private String preferredDeliveryTime;
    private String dietaryNotes;
    private String specialInstructions;
    private Boolean includePackaging;
    private Boolean includeCutlery;
    private LocalDateTime createdAt;
    private List<SubscriptionDayResponseDTO> schedule;
    private PaymentResponseDTO payment;
    private OrderCustomerDTO customer;

    // Getters and Setters
    public String getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(String subscriptionId) { this.subscriptionId = subscriptionId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public Double getSubtotalAmount() { return subtotalAmount; }
    public void setSubtotalAmount(Double subtotalAmount) { this.subtotalAmount = subtotalAmount; }

    public Double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(Double deliveryFee) { this.deliveryFee = deliveryFee; }

    public Double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(Double taxAmount) { this.taxAmount = taxAmount; }

    public Double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(Double discountAmount) { this.discountAmount = discountAmount; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getLandmark() { return landmark; }
    public void setLandmark(String landmark) { this.landmark = landmark; }

    public String getPreferredDeliveryTime() { return preferredDeliveryTime; }
    public void setPreferredDeliveryTime(String preferredDeliveryTime) { this.preferredDeliveryTime = preferredDeliveryTime; }

    public String getDietaryNotes() { return dietaryNotes; }
    public void setDietaryNotes(String dietaryNotes) { this.dietaryNotes = dietaryNotes; }

    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }

    public Boolean getIncludePackaging() { return includePackaging; }
    public void setIncludePackaging(Boolean includePackaging) { this.includePackaging = includePackaging; }

    public Boolean getIncludeCutlery() { return includeCutlery; }
    public void setIncludeCutlery(Boolean includeCutlery) { this.includeCutlery = includeCutlery; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<SubscriptionDayResponseDTO> getSchedule() { return schedule; }
    public void setSchedule(List<SubscriptionDayResponseDTO> schedule) { this.schedule = schedule; }

    public PaymentResponseDTO getPayment() { return payment; }
    public void setPayment(PaymentResponseDTO payment) { this.payment = payment; }

    public OrderCustomerDTO getCustomer() { return customer; }
    public void setCustomer(OrderCustomerDTO customer) { this.customer = customer; }
}




