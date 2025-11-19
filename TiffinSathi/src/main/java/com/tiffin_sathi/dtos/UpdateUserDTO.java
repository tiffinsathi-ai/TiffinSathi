package com.tiffin_sathi.dtos;


public class UpdateUserDTO {

    private String userName;
    private String phoneNumber;
    private String profilePicture;

    // ------------------------
    // Constructors
    // ------------------------

    public UpdateUserDTO() {
    }

    public UpdateUserDTO(String userName, String email, String password, String phoneNumber, String profilePicture) {
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.profilePicture = profilePicture;
    }

    // ------------------------
    // Getters and Setters
    // ------------------------

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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
