package com.tiffin_sathi.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @Column(name = "subscription_id", length = 50)
    private String subscriptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    @JsonIgnore
    private MealPackage mealPackage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SubscriptionStatus status;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "subtotal_amount", nullable = false)
    private Double subtotalAmount;

    @Column(name = "delivery_fee", nullable = false)
    private Double deliveryFee;

    @Column(name = "tax_amount", nullable = false)
    private Double taxAmount;

    @Column(name = "discount_amount", nullable = false)
    private Double discountAmount = 0.0;

    @Column(name = "discount_code", length = 50)
    private String discountCode;

    @Column(name = "delivery_address", nullable = false, columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "landmark", length = 200)
    private String landmark;

    @Column(name = "preferred_delivery_time", length = 20)
    private String preferredDeliveryTime;

    @Column(name = "dietary_notes", columnDefinition = "TEXT")
    private String dietaryNotes;

    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions;

    @Column(name = "include_packaging", nullable = false)
    private Boolean includePackaging = true;

    @Column(name = "include_cutlery", nullable = false)
    private Boolean includeCutlery = false;

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference  // <-- add this
    private List<SubscriptionDay> subscriptionDays = new ArrayList<>();

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Payment> payments = new ArrayList<>();  // Changed from single Payment to List<Payment>

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum SubscriptionStatus {
        ACTIVE,
        PAUSED,
        COMPLETED,
        CANCELLED
    }

    // Constructors
    public Subscription() {}

    public Subscription(String subscriptionId, User user, MealPackage mealPackage, SubscriptionStatus status) {
        this.subscriptionId = subscriptionId;
        this.user = user;
        this.mealPackage = mealPackage;
        this.status = status;
    }

    // Helper method to get the latest payment (useful for backward compatibility)
    public Payment getPayment() {
        if (payments == null || payments.isEmpty()) {
            return null;
        }
        // Return the most recent payment by creation date
        return payments.stream()
                .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                .findFirst()
                .orElse(null);
    }

    // Helper method to set/add a payment (maintains backward compatibility)
    public void setPayment(Payment payment) {
        if (payments == null) {
            payments = new ArrayList<>();
        }
        // Remove existing payments with same ID to avoid duplicates
        payments.removeIf(p -> p.getPaymentId().equals(payment.getPaymentId()));
        payments.add(payment);
        payment.setSubscription(this);
    }

    // Getters and Setters
    public String getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(String subscriptionId) { this.subscriptionId = subscriptionId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public MealPackage getMealPackage() { return mealPackage; }
    public void setMealPackage(MealPackage mealPackage) { this.mealPackage = mealPackage; }

    public SubscriptionStatus getStatus() { return status; }
    public void setStatus(SubscriptionStatus status) { this.status = status; }

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

    public String getDiscountCode() { return discountCode; }
    public void setDiscountCode(String discountCode) { this.discountCode = discountCode; }

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

    public List<SubscriptionDay> getSubscriptionDays() { return subscriptionDays; }
    public void setSubscriptionDays(List<SubscriptionDay> subscriptionDays) { this.subscriptionDays = subscriptionDays; }

    public List<Payment> getPayments() { return payments; }
    public void setPayments(List<Payment> payments) { this.payments = payments; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}