package com.tiffin_sathi.dtos;

public class VendorSignupRequest {

    private String userName;               // Owner name
    private String businessName;
    private String email;
    private String phoneNumber;
    private String password;

    // Optional
    private String businessAddress;
    private String alternatePhone;
    private String cuisineType;
    private Integer capacity;
    private String description;
    private String bankName;
    private String accountNumber;
    private String branchName;
    private String accountHolderName;
    private String panNumber;
    private String vatNumber;
    private String foodLicenseNumber;
    private String companyRegistrationNumber;
    private byte[] licenseDocument;

    // -------------------- Constructors --------------------
    public VendorSignupRequest() {}

    // You can add a constructor with required fields if needed
    public VendorSignupRequest(String userName, String businessName, String email, String phoneNumber, String password) {
        this.userName = userName;
        this.businessName = businessName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
    }

    // -------------------- Getters and Setters --------------------
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getBusinessAddress() { return businessAddress; }
    public void setBusinessAddress(String businessAddress) { this.businessAddress = businessAddress; }

    public String getAlternatePhone() { return alternatePhone; }
    public void setAlternatePhone(String alternatePhone) { this.alternatePhone = alternatePhone; }

    public String getCuisineType() { return cuisineType; }
    public void setCuisineType(String cuisineType) { this.cuisineType = cuisineType; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public String getAccountHolderName() { return accountHolderName; }
    public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }

    public String getPanNumber() { return panNumber; }
    public void setPanNumber(String panNumber) { this.panNumber = panNumber; }

    public String getVatNumber() { return vatNumber; }
    public void setVatNumber(String vatNumber) { this.vatNumber = vatNumber; }

    public String getFoodLicenseNumber() { return foodLicenseNumber; }
    public void setFoodLicenseNumber(String foodLicenseNumber) { this.foodLicenseNumber = foodLicenseNumber; }

    public String getCompanyRegistrationNumber() { return companyRegistrationNumber; }
    public void setCompanyRegistrationNumber(String companyRegistrationNumber) { this.companyRegistrationNumber = companyRegistrationNumber; }

    public byte[] getLicenseDocument() { return licenseDocument; }
    public void setLicenseDocument(byte[] licenseDocument) { this.licenseDocument = licenseDocument; }
}
