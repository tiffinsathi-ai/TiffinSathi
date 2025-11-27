package com.tiffin_sathi.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "meal_sets")
public class MealSet {

    @Id
    @Column(name = "set_id", length = 50)
    private String setId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private MealSetType type;

    @Column(name = "meal_items_text", nullable = false, columnDefinition = "TEXT")
    private String mealItemsText;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum MealSetType {
        VEG("Veg"),
        NON_VEG("Non-Veg");

        private final String displayName;

        MealSetType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Constructors
    public MealSet() {}

    // Getters and Setters
    public String getSetId() { return setId; }
    public void setSetId(String setId) { this.setId = setId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public MealSetType getType() { return type; }
    public void setType(MealSetType type) { this.type = type; }

    public String getMealItemsText() { return mealItemsText; }
    public void setMealItemsText(String mealItemsText) { this.mealItemsText = mealItemsText; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }

    public Vendor getVendor() { return vendor; }
    public void setVendor(Vendor vendor) { this.vendor = vendor; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getDisplayName() {
        return this.type != null ? this.type.getDisplayName() : "";
    }
}