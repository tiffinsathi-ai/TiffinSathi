package com.tiffin_sathi.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "vendors")
public class Vendor implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vendor_id")
    private Long vendorId;

    @Column(name = "owner_name", length = 150, nullable = false)
    private String ownerName;

    @Column(name = "business_name", length = 150, nullable = false)
    private String businessName;

    @Column(name = "phone", length = 10, nullable = false)
    private String phone;

    @Column(name = "business_email", length = 100, unique = true, nullable = false)
    private String businessEmail;

    @Column(name = "business_address", length = 255)
    private String businessAddress;

    @Column(name = "alternate_phone", length = 10)
    private String alternatePhone;

    @Column(name = "years_in_business", columnDefinition = "INT DEFAULT 0")
    private Integer yearsInBusiness = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VendorStatus status = VendorStatus.PENDING;

    @Column(name = "cuisine_type", length = 100)
    private String cuisineType;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "account_number", length = 50)
    private String accountNumber;

    @Column(name = "branch_name", length = 100)
    private String branchName;

    @Column(name = "account_holder_name", length = 100)
    private String accountHolderName;

    @Column(name = "pan_number", length = 20)
    private String panNumber;

    @Column(name = "vat_number", length = 20)
    private String vatNumber;

    @Column(name = "food_license_number", length = 50)
    private String foodLicenseNumber;

    @Column(name = "company_registration_number", length = 50)
    private String companyRegistrationNumber;

    @Lob
    @Column(name = "license_document", columnDefinition = "LONGBLOB")
    private byte[] licenseDocument;

    // 🔐 Add password field for login
    @Column(nullable = true)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.VENDOR;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    public Vendor() {
        this.status = VendorStatus.PENDING;
        this.yearsInBusiness = 0;
        this.role = Role.VENDOR;
    }


    // ------------------------
    // ✅ UserDetails methods
    // ------------------------

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(); // you can add roles if needed
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return businessEmail; // login using business email
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


    // ------------------------
    // Getters & Setters
    // ------------------------

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBusinessEmail() { return businessEmail; }
    public void setBusinessEmail(String businessEmail) { this.businessEmail = businessEmail; }

    public String getBusinessAddress() { return businessAddress; }
    public void setBusinessAddress(String businessAddress) { this.businessAddress = businessAddress; }

    public String getAlternatePhone() { return alternatePhone; }
    public void setAlternatePhone(String alternatePhone) { this.alternatePhone = alternatePhone; }

    public Integer getYearsInBusiness() { return yearsInBusiness; }
    public void setYearsInBusiness(Integer yearsInBusiness) { this.yearsInBusiness = yearsInBusiness; }

    public VendorStatus getStatus() { return status; }
    public void setStatus(VendorStatus status) { this.status = status; }

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

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public void setPassword(String password) { this.password = password; }
}
