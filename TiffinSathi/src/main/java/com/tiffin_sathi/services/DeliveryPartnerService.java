package com.tiffin_sathi.services;

import com.tiffin_sathi.dtos.*;
import com.tiffin_sathi.model.DeliveryPartner;
import com.tiffin_sathi.model.Vendor;
import com.tiffin_sathi.repository.DeliveryPartnerRepository;
import com.tiffin_sathi.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class DeliveryPartnerService {

    @Autowired
    private DeliveryPartnerRepository deliveryPartnerRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Transactional
    public Map<String, Object> createDeliveryPartner(Long vendorId, CreateDeliveryPartnerDTO createDeliveryPartnerDTO) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found with id: " + vendorId));

        // Check if phone number already exists for this vendor
        if (deliveryPartnerRepository.existsByPhoneNumberAndVendorVendorId(
                createDeliveryPartnerDTO.getPhoneNumber(), vendorId)) {
            throw new RuntimeException("Delivery partner with phone number '" +
                    createDeliveryPartnerDTO.getPhoneNumber() + "' already exists");
        }

        // Check if email already exists
        if (deliveryPartnerRepository.existsByEmail(createDeliveryPartnerDTO.getEmail())) {
            throw new RuntimeException("Delivery partner with email '" +
                    createDeliveryPartnerDTO.getEmail() + "' already exists");
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
        deliveryPartner.setIsActive(createDeliveryPartnerDTO.getIsActive() != null ? createDeliveryPartnerDTO.getIsActive() : true);
        deliveryPartner.setAvailabilityStatus(DeliveryPartner.AvailabilityStatus.AVAILABLE);

        // Generate temporary password and encode it
        String tempPassword = generateTempPassword();
        deliveryPartner.setPassword(passwordEncoder.encode(tempPassword));

        DeliveryPartner savedPartner = deliveryPartnerRepository.save(deliveryPartner);

        // Send email with credentials to delivery partner (INCLUDING password)
        try {
            emailService.sendDeliveryPartnerCredentials(
                    savedPartner.getEmail(),
                    savedPartner.getName(),
                    tempPassword,
                    vendor.getBusinessName()
            );
        } catch (Exception e) {
            System.err.println("Failed to send email to delivery partner: " + e.getMessage());
        }

        DeliveryPartnerDTO dto = convertToDTO(savedPartner);
        Map<String, Object> response = new HashMap<>();
        response.put("partner", dto);
        response.put("tempPassword", tempPassword);
        return response;
    }

    // Change Delivery Partner Password (for delivery partners themselves)
    @Transactional
    public String changePassword(String email, ChangePasswordDTO changePasswordDTO) {
        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found"));

        // Verify current password
        if (!passwordEncoder.matches(changePasswordDTO.getCurrentPassword(), deliveryPartner.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Check if new password matches confirm password
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            throw new RuntimeException("New password and confirm password do not match");
        }

        // Update password
        deliveryPartner.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        deliveryPartnerRepository.save(deliveryPartner);

        // Send confirmation email
        try {
            emailService.sendPasswordResetConfirmation(email, deliveryPartner.getName());
        } catch (Exception e) {
            System.err.println("Failed to send password reset confirmation email: " + e.getMessage());
        }

        return "Password changed successfully";
    }

    // Reset password for delivery partner (Vendor operation)
    @Transactional
    public Map<String, Object> resetDeliveryPartnerPassword(Long partnerId, Long vendorId) {
        DeliveryPartner partner = deliveryPartnerRepository.findByPartnerIdAndVendorVendorId(partnerId, vendorId)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found or you don't have permission"));

        String newPassword = generateTempPassword();
        partner.setPassword(passwordEncoder.encode(newPassword));
        deliveryPartnerRepository.save(partner);

        // Send email with new password to delivery partner
        try {
            emailService.sendDeliveryPartnerCredentials(
                    partner.getEmail(),
                    partner.getName(),
                    newPassword,
                    partner.getVendor().getBusinessName()
            );
        } catch (Exception e) {
            System.err.println("Failed to send password reset email: " + e.getMessage());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Password reset successfully. New password has been sent to the delivery partner's email.");
        response.put("tempPassword", newPassword);
        return response;
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
    public DeliveryPartnerDTO toggleDeliveryPartnerAvailability(Long partnerId, Long vendorId) {
        DeliveryPartner partner = deliveryPartnerRepository.findByPartnerIdAndVendorVendorId(partnerId, vendorId)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found or you don't have permission to update it"));

        partner.setIsActive(!partner.getIsActive());

        // If deactivating, set status to OFFLINE
        if (!partner.getIsActive()) {
            partner.setAvailabilityStatus(DeliveryPartner.AvailabilityStatus.OFFLINE);
        } else {
            partner.setAvailabilityStatus(DeliveryPartner.AvailabilityStatus.AVAILABLE);
        }

        DeliveryPartner updatedPartner = deliveryPartnerRepository.save(partner);
        return convertToDTO(updatedPartner);
    }

    @Transactional
    public DeliveryPartnerDTO updateProfilePicture(Long partnerId, Long vendorId, String profilePicture, String profilePictureUrl) {
        DeliveryPartner partner = deliveryPartnerRepository.findByPartnerIdAndVendorVendorId(partnerId, vendorId)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found or you don't have permission to update it"));

        if (profilePicture != null) {
            partner.setProfilePicture(profilePicture);
        }

        DeliveryPartner updatedPartner = deliveryPartnerRepository.save(partner);
        return convertToDTO(updatedPartner);
    }

    @Transactional
    public void removeProfilePicture(Long partnerId, Long vendorId) {
        DeliveryPartner partner = deliveryPartnerRepository.findByPartnerIdAndVendorVendorId(partnerId, vendorId)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found or you don't have permission to update it"));

        partner.setProfilePicture(null);

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

    public DeliveryPartnerDTO getDeliveryPartnerByEmail(String email) {
        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found with email: " + email));
        return convertToDTO(deliveryPartner);
    }

    public DeliveryPartnerDTO updateDeliveryPartnerSelf(String email, UpdateDeliveryPartnerDTO dto) {
        DeliveryPartner partner = deliveryPartnerRepository
                .findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found"));

        partner.setName(dto.getName());
        partner.setPhoneNumber(dto.getPhoneNumber());
        partner.setAddress(dto.getAddress());
        partner.setVehicleInfo(dto.getVehicleInfo());
        partner.setLicenseNumber(dto.getLicenseNumber());

        if (dto.getProfilePicture() != null) {
            partner.setProfilePicture(dto.getProfilePicture());
        }

        deliveryPartnerRepository.save(partner);

        return convertToDTO(partner);
    }

    public String toggleAvailability(String email) {
        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found"));

        if (deliveryPartner.getAvailabilityStatus() == DeliveryPartner.AvailabilityStatus.AVAILABLE) {
            deliveryPartner.setAvailabilityStatus(DeliveryPartner.AvailabilityStatus.BUSY);
        } else {
            deliveryPartner.setAvailabilityStatus(DeliveryPartner.AvailabilityStatus.AVAILABLE);
        }

        deliveryPartnerRepository.save(deliveryPartner);
        return "Availability updated to " + deliveryPartner.getAvailabilityStatus();
    }

    public List<DeliveryPartner> getAvailableDeliveryPartners(Long vendorId) {
        return deliveryPartnerRepository.findByVendorVendorIdAndIsActiveTrue(vendorId).stream()
                .filter(partner -> partner.getAvailabilityStatus() == DeliveryPartner.AvailabilityStatus.AVAILABLE)
                .collect(Collectors.toList());
    }

    // Generate temporary password
    private String generateTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
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
        dto.setIsActive(partner.getIsActive());
        dto.setAvailabilityStatus(partner.getAvailabilityStatus().name());
        dto.setVendorId(partner.getVendor().getVendorId());
        dto.setVendorName(partner.getVendor().getBusinessName());
        dto.setCreatedAt(partner.getCreatedAt());
        dto.setUpdatedAt(partner.getUpdatedAt());
        return dto;
    }
}