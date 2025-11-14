package com.tiffin_sathi.dtos;

import java.util.List;

public class MealPackageDTO {

    private String packageId;
    private String name;
    private Integer durationDays;
    private String basePackageType;
    private Double pricePerSet;
    private String features;
    private String imageUrl;
    private Boolean isAvailable;
    private Long vendorId;
    private String vendorName;
    private List<PackageSetDTO> packageSets;

    // Getters and Setters
    public String getPackageId() { return packageId; }
    public void setPackageId(String packageId) { this.packageId = packageId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }

    public String getBasePackageType() { return basePackageType; }
    public void setBasePackageType(String basePackageType) { this.basePackageType = basePackageType; }

    public Double getPricePerSet() { return pricePerSet; }
    public void setPricePerSet(Double pricePerSet) { this.pricePerSet = pricePerSet; }

    public String getFeatures() { return features; }
    public void setFeatures(String features) { this.features = features; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }

    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }

    public List<PackageSetDTO> getPackageSets() { return packageSets; }
    public void setPackageSets(List<PackageSetDTO> packageSets) { this.packageSets = packageSets; }
}