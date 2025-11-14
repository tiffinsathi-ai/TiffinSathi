package com.tiffin_sathi.repository;

import com.tiffin_sathi.model.Vendor;
import com.tiffin_sathi.model.VendorStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, Long> {
    Optional<Vendor> findByBusinessEmail(String businessEmail);
    List<Vendor> findByStatus(VendorStatus status); // Add this method
}
