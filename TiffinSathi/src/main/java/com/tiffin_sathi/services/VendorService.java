package com.tiffin_sathi.services;

import com.tiffin_sathi.dtos.UpdateVendorDTO;
import com.tiffin_sathi.model.Vendor;
import com.tiffin_sathi.model.VendorStatus;
import com.tiffin_sathi.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class VendorService {

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    public List<Vendor> getAllVendors() {
        return vendorRepository.findAll();
    }

    public List<Vendor> getVendorsByStatus(VendorStatus status) {
        return vendorRepository.findByStatus(status);
    }

    public Optional<Vendor> getVendorById(Long vendorId) {
        return vendorRepository.findById(vendorId);
    }

    public Optional<Vendor> getVendorByEmail(String email) {
        return vendorRepository.findByBusinessEmail(email);
    }

    public Vendor updateVendor(Long vendorId, UpdateVendorDTO updateVendorDTO) {
        try {
            Vendor vendor = vendorRepository.findById(vendorId)
                    .orElseThrow(() -> new RuntimeException("Vendor not found with id: " + vendorId));

            // Update fields if provided
            if (updateVendorDTO.getOwnerName() != null) {
                vendor.setOwnerName(updateVendorDTO.getOwnerName());
            }
            if (updateVendorDTO.getBusinessName() != null) {
                vendor.setBusinessName(updateVendorDTO.getBusinessName());
            }
            if (updateVendorDTO.getPhone() != null) {
                if (updateVendorDTO.getPhone().length() != 10) {
                    throw new RuntimeException("Phone number must be exactly 10 digits");
                }
                vendor.setPhone(updateVendorDTO.getPhone());
            }
            if (updateVendorDTO.getBusinessAddress() != null) {
                vendor.setBusinessAddress(updateVendorDTO.getBusinessAddress());
            }
            if (updateVendorDTO.getAlternatePhone() != null) {
                if (updateVendorDTO.getAlternatePhone().length() != 10) {
                    throw new RuntimeException("Alternate phone number must be exactly 10 digits");
                }
                vendor.setAlternatePhone(updateVendorDTO.getAlternatePhone());
            }
            if (updateVendorDTO.getYearsInBusiness() != null) {
                vendor.setYearsInBusiness(updateVendorDTO.getYearsInBusiness());
            }
            if (updateVendorDTO.getCuisineType() != null) {
                vendor.setCuisineType(updateVendorDTO.getCuisineType());
            }
            if (updateVendorDTO.getCapacity() != null) {
                vendor.setCapacity(updateVendorDTO.getCapacity());
            }
            if (updateVendorDTO.getDescription() != null) {
                vendor.setDescription(updateVendorDTO.getDescription());
            }
            if (updateVendorDTO.getBusinessImage() != null) {
                vendor.setProfilePicture(updateVendorDTO.getBusinessImage());
            }

            return vendorRepository.save(vendor);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Database error: " + e.getRootCause().getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error updating vendor: " + e.getMessage());
        }
    }

    public Vendor updateVendorStatus(Long vendorId, VendorStatus status, String reason) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found with id: " + vendorId));

        VendorStatus oldStatus = vendor.getStatus();
        vendor.setStatus(status);

        // If status changed to approved, send approval email (keep existing password)
        if (status == VendorStatus.APPROVED && oldStatus != VendorStatus.APPROVED) {
            vendorRepository.save(vendor);
            // Send approval email - vendor uses the temp password from registration
            emailService.sendVendorApprovalEmail(
                    vendor.getBusinessEmail(),
                    vendor.getBusinessName(),
                    "Use the temporary password sent during registration"
            );
        }
        // If status changed to rejected, send rejection email
        else if (status == VendorStatus.REJECTED && oldStatus != VendorStatus.REJECTED) {
            vendorRepository.save(vendor);
            emailService.sendVendorRejectionEmail(vendor.getBusinessEmail(), vendor.getBusinessName(), reason);
        }
        else {
            vendorRepository.save(vendor);
        }

        return vendor;
    }

    public void deleteVendor(Long vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found with id: " + vendorId));
        vendorRepository.delete(vendor);
    }

    public String changeVendorPassword(Long vendorId, String currentPassword, String newPassword, String confirmPassword) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found with id: " + vendorId));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, vendor.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Check new and confirm password match
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("New password and confirm password do not match");
        }

        // Prevent same password reuse
        if (passwordEncoder.matches(newPassword, vendor.getPassword())) {
            throw new RuntimeException("New password must be different from current password");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        vendor.setPassword(encodedPassword);
        vendorRepository.save(vendor);
        return "Password changed successfully";
    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
