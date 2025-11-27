package com.tiffin_sathi.model;

import jakarta.persistence.*;

@Entity
@Table(name = "subscription_day_meals")
public class SubscriptionDayMeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_day_meal_id")
    private Long subscriptionDayMealId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_day_id", nullable = false)
    private SubscriptionDay subscriptionDay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_id", nullable = false)
    private MealSet mealSet;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;

    // Constructors
    public SubscriptionDayMeal() {}

    public SubscriptionDayMeal(SubscriptionDay subscriptionDay, MealSet mealSet, Integer quantity, Double unitPrice) {
        this.subscriptionDay = subscriptionDay;
        this.mealSet = mealSet;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Getters and Setters
    public Long getSubscriptionDayMealId() { return subscriptionDayMealId; }
    public void setSubscriptionDayMealId(Long subscriptionDayMealId) { this.subscriptionDayMealId = subscriptionDayMealId; }

    public SubscriptionDay getSubscriptionDay() { return subscriptionDay; }
    public void setSubscriptionDay(SubscriptionDay subscriptionDay) { this.subscriptionDay = subscriptionDay; }

    public MealSet getMealSet() { return mealSet; }
    public void setMealSet(MealSet mealSet) { this.mealSet = mealSet; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }
}

