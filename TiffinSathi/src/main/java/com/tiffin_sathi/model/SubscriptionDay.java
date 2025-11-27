package com.tiffin_sathi.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subscription_days")
public class SubscriptionDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_day_id")
    private Long subscriptionDayId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Column(name = "day_of_week", nullable = false, length = 20)
    private String dayOfWeek; // MONDAY, TUESDAY, etc.

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled;

    @OneToMany(mappedBy = "subscriptionDay", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SubscriptionDayMeal> subscriptionDayMeals = new ArrayList<>();

    // Constructors
    public SubscriptionDay() {}

    public SubscriptionDay(Subscription subscription, String dayOfWeek, Boolean isEnabled) {
        this.subscription = subscription;
        this.dayOfWeek = dayOfWeek;
        this.isEnabled = isEnabled;
    }

    // Getters and Setters
    public Long getSubscriptionDayId() { return subscriptionDayId; }
    public void setSubscriptionDayId(Long subscriptionDayId) { this.subscriptionDayId = subscriptionDayId; }

    public Subscription getSubscription() { return subscription; }
    public void setSubscription(Subscription subscription) { this.subscription = subscription; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public Boolean getIsEnabled() { return isEnabled; }
    public void setIsEnabled(Boolean isEnabled) { this.isEnabled = isEnabled; }

    public List<SubscriptionDayMeal> getSubscriptionDayMeals() { return subscriptionDayMeals; }
    public void setSubscriptionDayMeals(List<SubscriptionDayMeal> subscriptionDayMeals) { this.subscriptionDayMeals = subscriptionDayMeals; }
}