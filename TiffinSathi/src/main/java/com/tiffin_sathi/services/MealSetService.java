package com.tiffin_sathi.services;

import com.tiffin_sathi.dtos.*;
import com.tiffin_sathi.model.MealSet;
import com.tiffin_sathi.model.Vendor;
import com.tiffin_sathi.repository.MealSetRepository;
import com.tiffin_sathi.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MealSetService {

    @Autowired
    private MealSetRepository mealSetRepository;

    @Autowired
    private VendorRepository vendorRepository;

    private String generateMealSetId() {
        // Generate unique ID: MSET_<timestamp>_<random_chars>
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "MSET_" + timestamp + "_" + random;
    }

    @Transactional
    public MealSetDTO createMealSet(Long vendorId, CreateMealSetDTO createMealSetDTO) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found with id: " + vendorId));

        // Generate unique ID
        String setId = generateMealSetId();

        // Check if ID already exists (extremely rare but possible)
        int attempt = 0;
        while (mealSetRepository.existsById(setId) && attempt < 5) {
            setId = generateMealSetId();
            attempt++;
        }

        if (mealSetRepository.existsById(setId)) {
            throw new RuntimeException("Failed to generate unique meal set ID");
        }

        MealSet mealSet = new MealSet();
        mealSet.setSetId(setId);
        mealSet.setName(createMealSetDTO.getName());
        mealSet.setType(createMealSetDTO.getType());
        mealSet.setMealItemsText(createMealSetDTO.getMealItemsText());
        mealSet.setVendor(vendor);

        MealSet savedMealSet = mealSetRepository.save(mealSet);
        return convertToDTO(savedMealSet);
    }

    // Update other methods to handle auto-generated IDs...
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

        // Check if meal set is used in any packages
        // You may want to add this check in a real implementation
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