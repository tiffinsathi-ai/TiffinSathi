package com.tiffin_sathi.services;

import com.tiffin_sathi.dtos.*;
import com.tiffin_sathi.model.*;
import com.tiffin_sathi.repository.MealPackageRepository;
import com.tiffin_sathi.repository.MealSetRepository;
import com.tiffin_sathi.repository.PackageSetRepository;
import com.tiffin_sathi.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MealPackageService {

    @Autowired
    private MealPackageRepository mealPackageRepository;

    @Autowired
    private MealSetRepository mealSetRepository;

    @Autowired
    private PackageSetRepository packageSetRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Transactional
    public MealPackageDTO createMealPackage(Long vendorId, CreateMealPackageDTO createMealPackageDTO) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found with id: " + vendorId));

        // Check if package ID already exists
        if (mealPackageRepository.existsById(createMealPackageDTO.getPackageId())) {
            throw new RuntimeException("Meal package with ID '" + createMealPackageDTO.getPackageId() + "' already exists");
        }

        MealPackage mealPackage = new MealPackage();
        mealPackage.setPackageId(createMealPackageDTO.getPackageId());
        mealPackage.setName(createMealPackageDTO.getName());
        mealPackage.setDurationDays(createMealPackageDTO.getDurationDays());
        mealPackage.setBasePackageType(createMealPackageDTO.getBasePackageType());
        mealPackage.setPricePerSet(createMealPackageDTO.getPricePerSet());
        mealPackage.setFeatures(createMealPackageDTO.getFeatures());
        mealPackage.setImage(createMealPackageDTO.getImage());
        mealPackage.setVendor(vendor);

        // CHANGED: Only require at least 1 meal set (removed the 7-meal-set requirement)
        if (createMealPackageDTO.getPackageSets() == null || createMealPackageDTO.getPackageSets().isEmpty()) {
            throw new RuntimeException("At least one meal set is required for a meal package");
        }

        MealPackage savedMealPackage = mealPackageRepository.save(mealPackage);

        // Create package-set relationships
        for (PackageSetDTO packageSetDTO : createMealPackageDTO.getPackageSets()) {
            MealSet mealSet = mealSetRepository.findBySetIdAndVendorVendorId(packageSetDTO.getSetId(), vendorId)
                    .orElseThrow(() -> new RuntimeException("Meal set not found: " + packageSetDTO.getSetId()));

            PackageSet packageSet = new PackageSet();
            packageSet.setId(new PackageSetId(savedMealPackage.getPackageId(), packageSetDTO.getSetId()));
            packageSet.setMealPackage(savedMealPackage);
            packageSet.setMealSet(mealSet);
            packageSet.setFrequency(packageSetDTO.getFrequency());

            packageSetRepository.save(packageSet);
        }

        return convertToDTO(savedMealPackage);
    }

    public List<MealPackageDTO> getMealPackagesByVendor(Long vendorId) {
        List<MealPackage> mealPackages = mealPackageRepository.findByVendorVendorId(vendorId);
        return mealPackages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<MealPackageDTO> getAvailableMealPackagesByVendor(Long vendorId) {
        List<MealPackage> mealPackages = mealPackageRepository.findByVendorVendorIdAndIsAvailableTrue(vendorId);
        return mealPackages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public MealPackageDTO getMealPackageById(String packageId) {
        MealPackage mealPackage = mealPackageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Meal package not found with id: " + packageId));
        return convertToDTO(mealPackage);
    }

    public MealPackageDTO getMealPackageByIdAndVendor(String packageId, Long vendorId) {
        MealPackage mealPackage = mealPackageRepository.findByPackageIdAndVendorVendorId(packageId, vendorId)
                .orElseThrow(() -> new RuntimeException("Meal package not found or you don't have permission to access it"));
        return convertToDTO(mealPackage);
    }

    @Transactional
    public MealPackageDTO updateMealPackage(String packageId, Long vendorId, UpdateMealPackageDTO updateMealPackageDTO) {
        MealPackage mealPackage = mealPackageRepository.findByPackageIdAndVendorVendorId(packageId, vendorId)
                .orElseThrow(() -> new RuntimeException("Meal package not found or you don't have permission to update it"));

        if (updateMealPackageDTO.getName() != null) {
            mealPackage.setName(updateMealPackageDTO.getName());
        }
        if (updateMealPackageDTO.getDurationDays() != null) {
            mealPackage.setDurationDays(updateMealPackageDTO.getDurationDays());
        }
        if (updateMealPackageDTO.getBasePackageType() != null) {
            mealPackage.setBasePackageType(updateMealPackageDTO.getBasePackageType());
        }
        if (updateMealPackageDTO.getPricePerSet() != null) {
            mealPackage.setPricePerSet(updateMealPackageDTO.getPricePerSet());
        }
        if (updateMealPackageDTO.getFeatures() != null) {
            mealPackage.setFeatures(updateMealPackageDTO.getFeatures());
        }
        if (updateMealPackageDTO.getImage() != null) {
            mealPackage.setImage(updateMealPackageDTO.getImage());
        }
        if (updateMealPackageDTO.getIsAvailable() != null) {
            mealPackage.setIsAvailable(updateMealPackageDTO.getIsAvailable());
        }

        // Update package sets if provided
        if (updateMealPackageDTO.getPackageSets() != null) {
            // CHANGED: Only require at least 1 meal set (removed the 7-meal-set requirement)
            if (updateMealPackageDTO.getPackageSets().isEmpty()) {
                throw new RuntimeException("At least one meal set is required for a meal package");
            }

            // Delete existing package sets
            packageSetRepository.deleteByMealPackagePackageId(packageId);

            // Create new package-set relationships
            for (PackageSetDTO packageSetDTO : updateMealPackageDTO.getPackageSets()) {
                MealSet mealSet = mealSetRepository.findBySetIdAndVendorVendorId(packageSetDTO.getSetId(), vendorId)
                        .orElseThrow(() -> new RuntimeException("Meal set not found: " + packageSetDTO.getSetId()));

                PackageSet packageSet = new PackageSet();
                packageSet.setId(new PackageSetId(packageId, packageSetDTO.getSetId()));
                packageSet.setMealPackage(mealPackage);
                packageSet.setMealSet(mealSet);
                packageSet.setFrequency(packageSetDTO.getFrequency());

                packageSetRepository.save(packageSet);
            }
        }

        MealPackage updatedMealPackage = mealPackageRepository.save(mealPackage);
        return convertToDTO(updatedMealPackage);
    }

    @Transactional
    public void deleteMealPackage(String packageId, Long vendorId) {
        MealPackage mealPackage = mealPackageRepository.findByPackageIdAndVendorVendorId(packageId, vendorId)
                .orElseThrow(() -> new RuntimeException("Meal package not found or you don't have permission to delete it"));
        mealPackageRepository.delete(mealPackage);
    }

    @Transactional
    public void toggleMealPackageAvailability(String packageId, Long vendorId) {
        MealPackage mealPackage = mealPackageRepository.findByPackageIdAndVendorVendorId(packageId, vendorId)
                .orElseThrow(() -> new RuntimeException("Meal package not found or you don't have permission to update it"));
        mealPackage.setIsAvailable(!mealPackage.getIsAvailable());
        mealPackageRepository.save(mealPackage);
    }

    // Public endpoints
    public List<MealPackageDTO> getAllAvailableMealPackages() {
        List<MealPackage> mealPackages = mealPackageRepository.findByIsAvailableTrue();
        return mealPackages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<MealPackageDTO> getMealPackagesByType(String basePackageType) {
        List<MealPackage> mealPackages = mealPackageRepository.findByBasePackageTypeAndIsAvailableTrue(basePackageType);
        return mealPackages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<MealPackageDTO> getMealPackagesByPriceRange(Double minPrice, Double maxPrice) {
        List<MealPackage> mealPackages = mealPackageRepository.findByPriceRange(minPrice, maxPrice);
        return mealPackages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<MealPackageDTO> getMealPackagesByDuration(Integer duration) {
        List<MealPackage> mealPackages = mealPackageRepository.findByDurationDays(duration);
        return mealPackages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private MealPackageDTO convertToDTO(MealPackage mealPackage) {
        MealPackageDTO dto = new MealPackageDTO();
        dto.setPackageId(mealPackage.getPackageId());
        dto.setName(mealPackage.getName());
        dto.setDurationDays(mealPackage.getDurationDays());
        dto.setBasePackageType(mealPackage.getBasePackageType());
        dto.setPricePerSet(mealPackage.getPricePerSet());
        dto.setFeatures(mealPackage.getFeatures());
        dto.setImage(mealPackage.getImage()); // Make sure this is included
        dto.setIsAvailable(mealPackage.getIsAvailable());
        dto.setVendorId(mealPackage.getVendor().getVendorId());
        dto.setVendorName(mealPackage.getVendor().getBusinessName());

        // Convert package sets to DTOs
        List<PackageSet> packageSets = packageSetRepository.findByMealPackagePackageId(mealPackage.getPackageId());
        List<PackageSetDTO> packageSetDTOs = packageSets.stream()
                .map(this::convertPackageSetToDTO)
                .collect(Collectors.toList());
        dto.setPackageSets(packageSetDTOs);

        return dto;
    }

    private PackageSetDTO convertPackageSetToDTO(PackageSet packageSet) {
        PackageSetDTO dto = new PackageSetDTO();
        dto.setSetId(packageSet.getMealSet().getSetId());
        dto.setFrequency(packageSet.getFrequency());
        dto.setSetName(packageSet.getMealSet().getName());
        dto.setMealItemsText(packageSet.getMealSet().getMealItemsText());
        dto.setType(packageSet.getMealSet().getType());
        return dto;
    }
}