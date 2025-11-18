package com.tiffin_sathi.dtos;

import java.util.List;

public class UpdateMealPackageDTO {

    private String name;
    private Integer durationDays;
    private String basePackageType;
    private Double pricePerSet;
    private String features;
    private byte[] image;
    private String imageUrl;
    private Boolean isAvailable;
    private List<PackageSetDTO> packageSets;

    // Getters and Setters
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

    public byte[] getImage() { return image; }
    public void setImage(byte[] image) { this.image = image; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }

    public List<PackageSetDTO> getPackageSets() { return packageSets; }
    public void setPackageSets(List<PackageSetDTO> packageSets) { this.packageSets = packageSets; }
}