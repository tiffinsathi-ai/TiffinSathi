package com.tiffin_sathi.dtos;

public class UpdateProfilePictureDTO {

    private byte[] profilePicture;
    private String profilePictureUrl;

    // Getters and Setters
    public byte[] getProfilePicture() { return profilePicture; }
    public void setProfilePicture(byte[] profilePicture) { this.profilePicture = profilePicture; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }
}