package com.tiffin_sathi.dtos;


public class UpdateUserDTO {

    private String userName;
    private String phoneNumber;
    private byte[] profilePicture;

    // ------------------------
    // Constructors
    // ------------------------

    public UpdateUserDTO() {
    }

    public UpdateUserDTO(String userName, String email, String password, String phoneNumber, byte[] profilePicture) {
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

    public byte[] getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }
}
