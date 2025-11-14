package com.tiffin_sathi.services;

import com.tiffin_sathi.dtos.*;
import com.tiffin_sathi.model.DeliveryPartner;
import com.tiffin_sathi.model.Vendor;
import com.tiffin_sathi.repository.DeliveryPartnerRepository;
import com.tiffin_sathi.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeliveryPartnerService {

    @Autowired
    private DeliveryPartnerRepository deliveryPartnerRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Transactional
    public DeliveryPartnerDTO createDeliveryPartner(Long vendorId, CreateDeliveryPartnerDTO createDeliveryPartnerDTO) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found with id: " + vendorId));

        // Check if phone number already exists for this vendor
        if (deliveryPartnerRepository.existsByPhoneNumberAndVendorVendorId(
                createDeliveryPartnerDTO.getPhoneNumber(), vendorId)) {
            throw new RuntimeException("Delivery partner with phone number '" +
                    createDeliveryPartnerDTO.getPhoneNumber() + "' already exists");
        }

        DeliveryPartner deliveryPartner = new DeliveryPartner();
        deliveryPartner.setVendor(vendor);
        deliveryPartner.setName(createDeliveryPartnerDTO.getName());
        deliveryPartner.setPhoneNumber(createDeliveryPartnerDTO.getPhoneNumber());
        deliveryPartner.setVehicleInfo(createDeliveryPartnerDTO.getVehicleInfo());
        deliveryPartner.setEmail(createDeliveryPartnerDTO.getEmail());
        deliveryPartner.setAddress(createDeliveryPartnerDTO.getAddress());
        deliveryPartner.setLicenseNumber(createDeliveryPartnerDTO.getLicenseNumber());
        deliveryPartner.setProfilePicture(createDeliveryPartnerDTO.getProfilePicture());
        deliveryPartner.setProfilePictureUrl(createDeliveryPartnerDTO.getProfilePictureUrl());
        deliveryPartner.setIsActive(createDeliveryPartnerDTO.getIsActive());

        DeliveryPartner savedPartner = deliveryPartnerRepository.save(deliveryPartner);
        return convertToDTO(savedPartner);
    }

    public List<DeliveryPartnerDTO> getDeliveryPartnersByVendor(Long vendorId) {
        List<DeliveryPartner> partners = deliveryPartnerRepository.findByVendorVendorId(vendorId);
        return partners.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<DeliveryPartnerDTO> getActiveDeliveryPartnersByVendor(Long vendorId) {
        List<DeliveryPartner> partners = deliveryPartnerRepository.findByVendorVendorIdAndIsActiveTrue(vendorId);
        return partners.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public DeliveryPartnerDTO getDeliveryPartnerByIdAndVendor(Long partnerId, Long vendorId) {
        DeliveryPartner partner = deliveryPartnerRepository.findByPartnerIdAndVendorVendorId(partnerId, vendorId)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found or you don't have permission to access it"));
        return convertToDTO(partner);
    }

    @Transactional
    public DeliveryPartnerDTO updateDeliveryPartner(Long partnerId, Long vendorId, UpdateDeliveryPartnerDTO updateDeliveryPartnerDTO) {
        DeliveryPartner partner = deliveryPartnerRepository.findByPartnerIdAndVendorVendorId(partnerId, vendorId)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found or you don't have permission to update it"));

        // Check if phone number already exists for another partner of the same vendor
        if (updateDeliveryPartnerDTO.getPhoneNumber() != null &&
                !updateDeliveryPartnerDTO.getPhoneNumber().equals(partner.getPhoneNumber())) {

            if (deliveryPartnerRepository.existsByPhoneNumberAndVendorVendorIdAndPartnerIdNot(
                    updateDeliveryPartnerDTO.getPhoneNumber(), vendorId, partnerId)) {
                throw new RuntimeException("Delivery partner with phone number '" +
                        updateDeliveryPartnerDTO.getPhoneNumber() + "' already exists");
            }
            partner.setPhoneNumber(updateDeliveryPartnerDTO.getPhoneNumber());
        }

        if (updateDeliveryPartnerDTO.getName() != null) {
            partner.setName(updateDeliveryPartnerDTO.getName());
        }
        if (updateDeliveryPartnerDTO.getVehicleInfo() != null) {
            partner.setVehicleInfo(updateDeliveryPartnerDTO.getVehicleInfo());
        }
        if (updateDeliveryPartnerDTO.getEmail() != null) {
            partner.setEmail(updateDeliveryPartnerDTO.getEmail());
        }
        if (updateDeliveryPartnerDTO.getAddress() != null) {
            partner.setAddress(updateDeliveryPartnerDTO.getAddress());
        }
        if (updateDeliveryPartnerDTO.getLicenseNumber() != null) {
            partner.setLicenseNumber(updateDeliveryPartnerDTO.getLicenseNumber());
        }
        if (updateDeliveryPartnerDTO.getProfilePicture() != null) {
            partner.setProfilePicture(updateDeliveryPartnerDTO.getProfilePicture());
        }
        if (updateDeliveryPartnerDTO.getProfilePictureUrl() != null) {
            partner.setProfilePictureUrl(updateDeliveryPartnerDTO.getProfilePictureUrl());
        }
        if (updateDeliveryPartnerDTO.getIsActive() != null) {
            partner.setIsActive(updateDeliveryPartnerDTO.getIsActive());
        }

        DeliveryPartner updatedPartner = deliveryPartnerRepository.save(partner);
        return convertToDTO(updatedPartner);
    }

    @Transactional
    public void deleteDeliveryPartner(Long partnerId, Long vendorId) {
        DeliveryPartner partner = deliveryPartnerRepository.findByPartnerIdAndVendorVendorId(partnerId, vendorId)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found or you don't have permission to delete it"));
        deliveryPartnerRepository.delete(partner);
    }

    @Transactional
    public void toggleDeliveryPartnerAvailability(Long partnerId, Long vendorId) {
        DeliveryPartner partner = deliveryPartnerRepository.findByPartnerIdAndVendorVendorId(partnerId, vendorId)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found or you don't have permission to update it"));
        partner.setIsActive(!partner.getIsActive());
        deliveryPartnerRepository.save(partner);
    }

    @Transactional
    public DeliveryPartnerDTO updateProfilePicture(Long partnerId, Long vendorId, byte[] profilePicture, String profilePictureUrl) {
        DeliveryPartner partner = deliveryPartnerRepository.findByPartnerIdAndVendorVendorId(partnerId, vendorId)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found or you don't have permission to update it"));

        if (profilePicture != null) {
            partner.setProfilePicture(profilePicture);
        }
        if (profilePictureUrl != null) {
            partner.setProfilePictureUrl(profilePictureUrl);
        }

        DeliveryPartner updatedPartner = deliveryPartnerRepository.save(partner);
        return convertToDTO(updatedPartner);
    }

    @Transactional
    public void removeProfilePicture(Long partnerId, Long vendorId) {
        DeliveryPartner partner = deliveryPartnerRepository.findByPartnerIdAndVendorVendorId(partnerId, vendorId)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found or you don't have permission to update it"));

        partner.setProfilePicture(null);
        partner.setProfilePictureUrl(null);

        deliveryPartnerRepository.save(partner);
    }

    public long getActivePartnersCount(Long vendorId) {
        return deliveryPartnerRepository.countActivePartnersByVendor(vendorId);
    }

    public List<DeliveryPartnerDTO> searchDeliveryPartnersByName(Long vendorId, String name) {
        List<DeliveryPartner> partners = deliveryPartnerRepository.findByVendorAndNameContaining(vendorId, name);
        return partners.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private DeliveryPartnerDTO convertToDTO(DeliveryPartner partner) {
        DeliveryPartnerDTO dto = new DeliveryPartnerDTO();
        dto.setPartnerId(partner.getPartnerId());
        dto.setName(partner.getName());
        dto.setPhoneNumber(partner.getPhoneNumber());
        dto.setVehicleInfo(partner.getVehicleInfo());
        dto.setEmail(partner.getEmail());
        dto.setAddress(partner.getAddress());
        dto.setLicenseNumber(partner.getLicenseNumber());
        dto.setProfilePicture(partner.getProfilePicture());
        dto.setProfilePictureUrl(partner.getProfilePictureUrl());
        dto.setIsActive(partner.getIsActive());
        dto.setVendorId(partner.getVendor().getVendorId());
        dto.setVendorName(partner.getVendor().getBusinessName());
        dto.setCreatedAt(partner.getCreatedAt());
        dto.setUpdatedAt(partner.getUpdatedAt());
        return dto;
    }
}