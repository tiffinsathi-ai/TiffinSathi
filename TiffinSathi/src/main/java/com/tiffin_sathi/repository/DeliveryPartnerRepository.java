package com.tiffin_sathi.repository;

import com.tiffin_sathi.model.DeliveryPartner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, Long> {

    Optional<DeliveryPartner> findByEmail(String email);

    boolean existsByPhoneNumberAndVendorVendorId(String phoneNumber, Long vendorId);
    boolean existsByEmail(String email);

    List<DeliveryPartner> findByVendorVendorId(Long vendorId);
    List<DeliveryPartner> findByVendorVendorIdAndIsActiveTrue(Long vendorId);
    Optional<DeliveryPartner> findByPartnerIdAndVendorVendorId(Long partnerId, Long vendorId);

    boolean existsByPhoneNumberAndVendorVendorIdAndPartnerIdNot(String phoneNumber, Long vendorId, Long partnerId);


    @Query("SELECT COUNT(dp) FROM DeliveryPartner dp WHERE dp.vendor.vendorId = :vendorId AND dp.isActive = true")
    long countActivePartnersByVendor(@Param("vendorId") Long vendorId);

    @Query("SELECT dp FROM DeliveryPartner dp WHERE dp.vendor.vendorId = :vendorId AND LOWER(dp.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<DeliveryPartner> findByVendorAndNameContaining(@Param("vendorId") Long vendorId, @Param("name") String name);
}