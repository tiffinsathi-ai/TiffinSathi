package com.tiffin_sathi.config;

import com.tiffin_sathi.model.Vendor;
import com.tiffin_sathi.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class VendorContext {

    @Autowired
    private VendorRepository vendorRepository;

    public Long getCurrentVendorId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Vendor) {
            Vendor vendor = (Vendor) authentication.getPrincipal();
            return vendor.getVendorId();
        } else if (authentication != null && authentication.getPrincipal() instanceof String) {
            // If it's a String (username), look up the vendor by email
            String email = (String) authentication.getPrincipal();
            return vendorRepository.findByBusinessEmail(email)
                    .map(Vendor::getVendorId)
                    .orElse(null);
        }
        return null;
    }

    public Vendor getCurrentVendor() {
        Long vendorId = getCurrentVendorId();
        if (vendorId != null) {
            return vendorRepository.findById(vendorId).orElse(null);
        }
        return null;
    }
}