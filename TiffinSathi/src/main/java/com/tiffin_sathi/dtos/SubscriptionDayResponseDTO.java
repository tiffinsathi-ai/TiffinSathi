package com.tiffin_sathi.dtos;

import java.util.List;

public class SubscriptionDayResponseDTO {
    private String dayOfWeek;
    private Boolean enabled;
    private List<SubscriptionDayMealResponseDTO> meals;

    // Getters and Setters
    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public List<SubscriptionDayMealResponseDTO> getMeals() { return meals; }
    public void setMeals(List<SubscriptionDayMealResponseDTO> meals) { this.meals = meals; }
}
