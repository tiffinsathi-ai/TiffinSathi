package com.tiffin_sathi.dtos;

import com.tiffin_sathi.model.User;

public class OrderCustomerDTO {
    private String userId;
    private String userName;
    private String email;
    private String phoneNumber;
    private String profilePicture;

    public OrderCustomerDTO() {}

    public OrderCustomerDTO(User user) {
        this.userId = String.valueOf(user.getId());
        this.userName = user.getUserName();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();
        this.profilePicture = user.getProfilePicture();
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfilePicture() {
        return profilePicture;
    }
    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
}