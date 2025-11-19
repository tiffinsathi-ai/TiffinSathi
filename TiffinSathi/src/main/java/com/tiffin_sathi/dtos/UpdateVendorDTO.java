package com.tiffin_sathi.dtos;


public class UpdateVendorDTO {
    private String ownerName;
    private String businessName;
    private String phone;
    private String businessAddress;
    private String alternatePhone;
    private Integer yearsInBusiness;
    private String cuisineType;
    private Integer capacity;
    private String description;
    private String businessImage; // Changed to String for base64/URL
	public String getOwnerName() {
		return ownerName;
	}
	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}
	public String getBusinessName() {
		return businessName;
	}
	public void setBusinessName(String businessName) {
		this.businessName = businessName;
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
	public String getAlternatePhone() {
		return alternatePhone;
	}
	public void setAlternatePhone(String alternatePhone) {
		this.alternatePhone = alternatePhone;
	}
	public Integer getYearsInBusiness() {
		return yearsInBusiness;
	}
	public void setYearsInBusiness(Integer yearsInBusiness) {
		this.yearsInBusiness = yearsInBusiness;
	}
	public String getCuisineType() {
		return cuisineType;
	}
	public void setCuisineType(String cuisineType) {
		this.cuisineType = cuisineType;
	}
	public Integer getCapacity() {
		return capacity;
	}
	public void setCapacity(Integer capacity) {
		this.capacity = capacity;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getBusinessImage() {
		return businessImage;
	}
	public void setBusinessImage(String businessImage) {
		this.businessImage = businessImage;
	}

    
}
