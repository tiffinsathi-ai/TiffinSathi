package com.tiffin_sathi.dtos;

import jakarta.validation.constraints.Future;
import java.time.LocalDate;
import java.util.List;

public class SubscriptionRequestDTO {
    private String packageId;
    private String phoneNumber;
    private String fullName;
    private String email;
    private String deliveryAddress;
    private String landmark;
    private String preferredDeliveryTime;
    private String dietaryNotes;
    private String specialInstructions;
    private Boolean includePackaging;
    private Boolean includeCutlery;
    private String discountCode;
    private String paymentMethod;

    @Future(message = "Start date must be in the future")
    private LocalDate startDate;

    private List<SubscriptionDayDTO> schedule;

    // Getters and Setters
    public String getPackageId() { return packageId; }
    public void setPackageId(String packageId) { this.packageId = packageId; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

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

    public String getDiscountCode() { return discountCode; }
    public void setDiscountCode(String discountCode) { this.discountCode = discountCode; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public List<SubscriptionDayDTO> getSchedule() { return schedule; }
    public void setSchedule(List<SubscriptionDayDTO> schedule) { this.schedule = schedule; }
}