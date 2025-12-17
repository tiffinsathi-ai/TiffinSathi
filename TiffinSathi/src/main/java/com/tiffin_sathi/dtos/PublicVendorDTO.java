package com.tiffin_sathi.dtos;

import java.util.Objects;

public class PublicVendorDTO {
    private Long vendorId;
    private String businessName;
    private String ownerName;
    private String phone;
    private String businessAddress;
    private String cuisineType;
    private String description;
    private Integer yearsInBusiness;
    private String profilePicture;
    private String status;

    // Getters and Setters
    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBusinessAddress() {
        return businessAddress;
    }

    public void setBusinessAddress(String businessAddress) {
        this.businessAddress = businessAddress;
    }

    public String getCuisineType() {
        return cuisineType;
    }

    public void setCuisineType(String cuisineType) {
        this.cuisineType = cuisineType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getYearsInBusiness() {
        return yearsInBusiness;
    }

    public void setYearsInBusiness(Integer yearsInBusiness) {
        this.yearsInBusiness = yearsInBusiness;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // equals() and hashCode()
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicVendorDTO that = (PublicVendorDTO) o;
        return Objects.equals(vendorId, that.vendorId) &&
                Objects.equals(businessName, that.businessName) &&
                Objects.equals(ownerName, that.ownerName) &&
                Objects.equals(phone, that.phone) &&
                Objects.equals(businessAddress, that.businessAddress) &&
                Objects.equals(cuisineType, that.cuisineType) &&
                Objects.equals(description, that.description) &&
                Objects.equals(yearsInBusiness, that.yearsInBusiness) &&
                Objects.equals(profilePicture, that.profilePicture) &&
                Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vendorId, businessName, ownerName, phone, businessAddress, cuisineType, description, yearsInBusiness, profilePicture, status);
    }

    // toString()
    @Override
    public String toString() {
        return "PublicVendorDTO{" +
                "vendorId=" + vendorId +
                ", businessName='" + businessName + '\'' +
                ", ownerName='" + ownerName + '\'' +
                ", phone='" + phone + '\'' +
                ", businessAddress='" + businessAddress + '\'' +
                ", cuisineType='" + cuisineType + '\'' +
                ", description='" + description + '\'' +
                ", yearsInBusiness=" + yearsInBusiness +
                ", profilePicture='" + profilePicture + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}