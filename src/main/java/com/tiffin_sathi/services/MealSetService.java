package com.tiffin_sathi.services;

import com.tiffin_sathi.dtos.*;
import com.tiffin_sathi.model.MealSet;
import com.tiffin_sathi.model.Vendor;
import com.tiffin_sathi.repository.MealSetRepository;
import com.tiffin_sathi.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MealSetService {

    @Autowired
    private MealSetRepository mealSetRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Transactional
    public MealSetDTO createMealSet(Long vendorId, CreateMealSetDTO createMealSetDTO) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found with id: " + vendorId));

        // Check if set ID already exists
        if (mealSetRepository.existsBySetIdAndVendorVendorId(createMealSetDTO.getSetId(), vendorId)) {
            throw new RuntimeException("Meal set with ID '" + createMealSetDTO.getSetId() + "' already exists");
        }

        MealSet mealSet = new MealSet();
        mealSet.setSetId(createMealSetDTO.getSetId());
        mealSet.setName(createMealSetDTO.getName());
        mealSet.setType(createMealSetDTO.getType());
        mealSet.setMealItemsText(createMealSetDTO.getMealItemsText());
        mealSet.setVendor(vendor);

        MealSet savedMealSet = mealSetRepository.save(mealSet);
        return convertToDTO(savedMealSet);
    }

    public List<MealSetDTO> getMealSetsByVendor(Long vendorId) {
        List<MealSet> mealSets = mealSetRepository.findByVendorVendorId(vendorId);
        return mealSets.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<MealSetDTO> getAvailableMealSetsByVendor(Long vendorId) {
        List<MealSet> mealSets = mealSetRepository.findByVendorVendorIdAndIsAvailableTrue(vendorId);
        return mealSets.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public MealSetDTO getMealSetById(String setId) {
        MealSet mealSet = mealSetRepository.findById(setId)
                .orElseThrow(() -> new RuntimeException("Meal set not found with id: " + setId));
        return convertToDTO(mealSet);
    }

    public MealSetDTO getMealSetByIdAndVendor(String setId, Long vendorId) {
        MealSet mealSet = mealSetRepository.findBySetIdAndVendorVendorId(setId, vendorId)
                .orElseThrow(() -> new RuntimeException("Meal set not found or you don't have permission to access it"));
        return convertToDTO(mealSet);
    }

    @Transactional
    public MealSetDTO updateMealSet(String setId, Long vendorId, UpdateMealSetDTO updateMealSetDTO) {
        MealSet mealSet = mealSetRepository.findBySetIdAndVendorVendorId(setId, vendorId)
                .orElseThrow(() -> new RuntimeException("Meal set not found or you don't have permission to update it"));

        if (updateMealSetDTO.getName() != null) {
            mealSet.setName(updateMealSetDTO.getName());
        }
        if (updateMealSetDTO.getType() != null) {
            mealSet.setType(updateMealSetDTO.getType());
        }
        if (updateMealSetDTO.getMealItemsText() != null) {
            mealSet.setMealItemsText(updateMealSetDTO.getMealItemsText());
        }
        if (updateMealSetDTO.getIsAvailable() != null) {
            mealSet.setIsAvailable(updateMealSetDTO.getIsAvailable());
        }

        MealSet updatedMealSet = mealSetRepository.save(mealSet);
        return convertToDTO(updatedMealSet);
    }

    @Transactional
    public void deleteMealSet(String setId, Long vendorId) {
        MealSet mealSet = mealSetRepository.findBySetIdAndVendorVendorId(setId, vendorId)
                .orElseThrow(() -> new RuntimeException("Meal set not found or you don't have permission to delete it"));
        mealSetRepository.delete(mealSet);
    }

    @Transactional
    public void toggleMealSetAvailability(String setId, Long vendorId) {
        MealSet mealSet = mealSetRepository.findBySetIdAndVendorVendorId(setId, vendorId)
                .orElseThrow(() -> new RuntimeException("Meal set not found or you don't have permission to update it"));
        mealSet.setIsAvailable(!mealSet.getIsAvailable());
        mealSetRepository.save(mealSet);
    }

    // Public endpoints
    public List<MealSetDTO> getAllAvailableMealSets() {
        List<MealSet> mealSets = mealSetRepository.findByIsAvailableTrue();
        return mealSets.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<MealSetDTO> getMealSetsByType(MealSet.MealSetType type) {
        List<MealSet> mealSets = mealSetRepository.findByTypeAndIsAvailableTrue(type);
        return mealSets.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private MealSetDTO convertToDTO(MealSet mealSet) {
        MealSetDTO dto = new MealSetDTO();
        dto.setSetId(mealSet.getSetId());
        dto.setName(mealSet.getName());
        dto.setType(mealSet.getType());
        dto.setMealItemsText(mealSet.getMealItemsText());
        dto.setIsAvailable(mealSet.getIsAvailable());
        dto.setVendorId(mealSet.getVendor().getVendorId());
        dto.setVendorName(mealSet.getVendor().getBusinessName());
        return dto;
    }
}