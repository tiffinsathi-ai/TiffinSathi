package com.tiffin_sathi.dtos;


import lombok.Data;

@Data
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

}
