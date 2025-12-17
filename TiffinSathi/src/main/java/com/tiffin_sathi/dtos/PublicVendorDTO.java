package com.tiffin_sathi.dtos;

import lombok.Data;

@Data
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
}