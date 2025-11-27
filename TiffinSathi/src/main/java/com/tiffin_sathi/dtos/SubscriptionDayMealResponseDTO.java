package com.tiffin_sathi.dtos;

public class SubscriptionDayMealResponseDTO {
    private String setId;
    private String mealSetName;
    private String mealSetType;
    private Integer quantity;
    private Double unitPrice;

    // Getters and Setters
    public String getSetId() { return setId; }
    public void setSetId(String setId) { this.setId = setId; }

    public String getMealSetName() { return mealSetName; }
    public void setMealSetName(String mealSetName) { this.mealSetName = mealSetName; }

    public String getMealSetType() { return mealSetType; }
    public void setMealSetType(String mealSetType) { this.mealSetType = mealSetType; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }
}
