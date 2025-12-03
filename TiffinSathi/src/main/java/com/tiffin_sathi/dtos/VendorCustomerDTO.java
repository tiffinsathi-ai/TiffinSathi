package com.tiffin_sathi.dtos;

import java.time.LocalDate;
import java.util.List;

public class VendorCustomerDTO {
    private String userId;
    private String userName;
    private String email;
    private String phoneNumber;
    private String profilePicture;
    private String userStatus;

    // Customer stats
    private int totalSubscriptions;
    private int activeSubscriptions;
    private int totalOrders;
    private double totalSpent;
    private LocalDate firstOrderDate;
    private LocalDate lastOrderDate;

    // Current subscription info
    private String currentSubscriptionId;
    private String currentSubscriptionStatus;
    private LocalDate currentSubscriptionStart;
    private LocalDate currentSubscriptionEnd;
    private String currentPackageName;

    // Meal preferences
    private List<String> preferredMealTypes;
    private String dietaryNotes;

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public String getUserStatus() { return userStatus; }
    public void setUserStatus(String userStatus) { this.userStatus = userStatus; }

    public int getTotalSubscriptions() { return totalSubscriptions; }
    public void setTotalSubscriptions(int totalSubscriptions) { this.totalSubscriptions = totalSubscriptions; }

    public int getActiveSubscriptions() { return activeSubscriptions; }
    public void setActiveSubscriptions(int activeSubscriptions) { this.activeSubscriptions = activeSubscriptions; }

    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

    public double getTotalSpent() { return totalSpent; }
    public void setTotalSpent(double totalSpent) { this.totalSpent = totalSpent; }

    public LocalDate getFirstOrderDate() { return firstOrderDate; }
    public void setFirstOrderDate(LocalDate firstOrderDate) { this.firstOrderDate = firstOrderDate; }

    public LocalDate getLastOrderDate() { return lastOrderDate; }
    public void setLastOrderDate(LocalDate lastOrderDate) { this.lastOrderDate = lastOrderDate; }

    public String getCurrentSubscriptionId() { return currentSubscriptionId; }
    public void setCurrentSubscriptionId(String currentSubscriptionId) { this.currentSubscriptionId = currentSubscriptionId; }

    public String getCurrentSubscriptionStatus() { return currentSubscriptionStatus; }
    public void setCurrentSubscriptionStatus(String currentSubscriptionStatus) { this.currentSubscriptionStatus = currentSubscriptionStatus; }

    public LocalDate getCurrentSubscriptionStart() { return currentSubscriptionStart; }
    public void setCurrentSubscriptionStart(LocalDate currentSubscriptionStart) { this.currentSubscriptionStart = currentSubscriptionStart; }

    public LocalDate getCurrentSubscriptionEnd() { return currentSubscriptionEnd; }
    public void setCurrentSubscriptionEnd(LocalDate currentSubscriptionEnd) { this.currentSubscriptionEnd = currentSubscriptionEnd; }

    public String getCurrentPackageName() { return currentPackageName; }
    public void setCurrentPackageName(String currentPackageName) { this.currentPackageName = currentPackageName; }

    public List<String> getPreferredMealTypes() { return preferredMealTypes; }
    public void setPreferredMealTypes(List<String> preferredMealTypes) { this.preferredMealTypes = preferredMealTypes; }

    public String getDietaryNotes() { return dietaryNotes; }
    public void setDietaryNotes(String dietaryNotes) { this.dietaryNotes = dietaryNotes; }
}