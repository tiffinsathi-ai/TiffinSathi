package com.tiffin_sathi.model;

import jakarta.persistence.*;

@Entity
@Table(name = "package_sets")
public class PackageSet {

    @EmbeddedId
    private PackageSetId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("packageId")
    @JoinColumn(name = "package_id")
    private MealPackage mealPackage;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("setId")
    @JoinColumn(name = "set_id")
    private MealSet mealSet;

    @Column(name = "frequency", nullable = false)
    private Integer frequency = 1;

    // Constructors
    public PackageSet() {}

    public PackageSet(MealPackage mealPackage, MealSet mealSet, Integer frequency) {
        this.mealPackage = mealPackage;
        this.mealSet = mealSet;
        this.frequency = frequency;
        this.id = new PackageSetId(mealPackage.getPackageId(), mealSet.getSetId());
    }

    // Getters and Setters
    public PackageSetId getId() { return id; }
    public void setId(PackageSetId id) { this.id = id; }

    public MealPackage getMealPackage() { return mealPackage; }
    public void setMealPackage(MealPackage mealPackage) { this.mealPackage = mealPackage; }

    public MealSet getMealSet() { return mealSet; }
    public void setMealSet(MealSet mealSet) { this.mealSet = mealSet; }

    public Integer getFrequency() { return frequency; }
    public void setFrequency(Integer frequency) { this.frequency = frequency; }
}