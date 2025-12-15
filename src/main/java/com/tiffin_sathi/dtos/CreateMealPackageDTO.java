package com.tiffin_sathi.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public class CreateMealPackageDTO {

    // Removed packageId field - will be auto-generated
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Duration days is required")
    @Positive(message = "Duration must be positive")
    private Integer durationDays;

    @NotBlank(message = "Base package type is required")
    private String basePackageType;

    @NotNull(message = "Price per set is required")
    @Positive(message = "Price must be positive")
    private Double pricePerSet;

    private String features;
    private String image;
    private Boolean isAvailable = true;

    @NotNull(message = "At least one meal set is required")
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

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }

    public List<PackageSetDTO> getPackageSets() { return packageSets; }
    public void setPackageSets(List<PackageSetDTO> packageSets) { this.packageSets = packageSets; }
}