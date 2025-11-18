package com.tiffin_sathi.dtos;

import com.tiffin_sathi.model.MealSet;

public class UpdateMealSetDTO {

    private String name;
    private MealSet.MealSetType type;
    private String mealItemsText;
    private Boolean isAvailable;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public MealSet.MealSetType getType() { return type; }
    public void setType(MealSet.MealSetType type) { this.type = type; }

    public String getMealItemsText() { return mealItemsText; }
    public void setMealItemsText(String mealItemsText) { this.mealItemsText = mealItemsText; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
}