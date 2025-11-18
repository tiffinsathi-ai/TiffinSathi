package com.tiffin_sathi.dtos;

import com.tiffin_sathi.model.MealSet;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class PackageSetDTO {

    @NotBlank(message = "Set ID is required")
    private String setId;

    @NotNull(message = "Frequency is required")
    @Positive(message = "Frequency must be positive")
    private Integer frequency;

    // Additional details for response
    private String setName;
    private String mealItemsText;
    private MealSet.MealSetType type;

    // Getters and Setters
    public String getSetId() { return setId; }
    public void setSetId(String setId) { this.setId = setId; }

    public Integer getFrequency() { return frequency; }
    public void setFrequency(Integer frequency) { this.frequency = frequency; }

    public String getSetName() { return setName; }
    public void setSetName(String setName) { this.setName = setName; }

    public String getMealItemsText() { return mealItemsText; }
    public void setMealItemsText(String mealItemsText) { this.mealItemsText = mealItemsText; }

    public MealSet.MealSetType getType() { return type; }
    public void setType(MealSet.MealSetType type) { this.type = type; }
}