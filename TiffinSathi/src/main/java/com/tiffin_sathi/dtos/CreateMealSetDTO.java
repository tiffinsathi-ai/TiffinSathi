package com.tiffin_sathi.dtos;

import com.tiffin_sathi.model.MealSet;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateMealSetDTO {

    @NotBlank(message = "Set ID is required")
    private String setId;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Type is required")
    private MealSet.MealSetType type;

    @NotBlank(message = "Meal items text is required")
    private String mealItemsText;

    // Getters and Setters
    public String getSetId() { return setId; }
    public void setSetId(String setId) { this.setId = setId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public MealSet.MealSetType getType() { return type; }
    public void setType(MealSet.MealSetType type) { this.type = type; }

    public String getMealItemsText() { return mealItemsText; }
    public void setMealItemsText(String mealItemsText) { this.mealItemsText = mealItemsText; }
}