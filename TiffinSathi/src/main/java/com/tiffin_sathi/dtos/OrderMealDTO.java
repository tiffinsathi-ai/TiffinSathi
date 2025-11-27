package com.tiffin_sathi.dtos;

import com.tiffin_sathi.model.OrderMeal;

public class OrderMealDTO {
    private String mealSetName;
    private String mealSetType;
    private Integer quantity;

    public OrderMealDTO(OrderMeal orderMeal) {
        if (orderMeal.getMealSet() != null) {
            this.mealSetName = orderMeal.getMealSet().getName();
            this.mealSetType = orderMeal.getMealSet().getType().name();
        }
        this.quantity = orderMeal.getQuantity();
    }

    // Getters and setters

    public String getMealSetName() {
        return mealSetName;
    }

    public void setMealSetName(String mealSetName) {
        this.mealSetName = mealSetName;
    }

    public String getMealSetType() {
        return mealSetType;
    }

    public void setMealSetType(String mealSetType) {
        this.mealSetType = mealSetType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}