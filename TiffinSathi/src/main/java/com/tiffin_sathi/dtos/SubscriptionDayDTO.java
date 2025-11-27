package com.tiffin_sathi.dtos;

import java.util.List;

public class SubscriptionDayDTO {
    private String dayOfWeek;
    private Boolean enabled;
    private List<SubscriptionDayMealDTO> meals;

    // Getters and Setters
    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public List<SubscriptionDayMealDTO> getMeals() { return meals; }
    public void setMeals(List<SubscriptionDayMealDTO> meals) { this.meals = meals; }
}
