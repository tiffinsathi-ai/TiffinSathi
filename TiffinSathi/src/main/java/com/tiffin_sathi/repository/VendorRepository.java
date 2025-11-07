package com.tiffin_sathi.repository;

import com.tiffin_sathi.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, Long> {
    Optional<Vendor> findByBusinessEmail(String businessEmail);
}
