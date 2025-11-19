package com.tiffin_sathi.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meal_packages")
public class MealPackage {

    @Id
    @Column(name = "package_id", length = 50)
    private String packageId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "base_package_type", nullable = false, length = 50)
    private String basePackageType;

    @Column(name = "price_per_set", nullable = false)
    private Double pricePerSet;

    @Column(name = "features", columnDefinition = "TEXT")
    private String features;

    @Lob
    @Column(name = "image", columnDefinition = "LONGTEXT") // Changed to LONGTEXT for base64
    private String image;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @OneToMany(mappedBy = "mealPackage", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PackageSet> packageSets = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public MealPackage() {}

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

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }

    public Vendor getVendor() { return vendor; }
    public void setVendor(Vendor vendor) { this.vendor = vendor; }

    public List<PackageSet> getPackageSets() { return packageSets; }
    public void setPackageSets(List<PackageSet> packageSets) { this.packageSets = packageSets; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}