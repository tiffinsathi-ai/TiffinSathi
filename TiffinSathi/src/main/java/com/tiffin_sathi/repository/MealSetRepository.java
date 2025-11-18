package com.tiffin_sathi.repository;

import com.tiffin_sathi.model.MealSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MealSetRepository extends JpaRepository<MealSet, String> {

    List<MealSet> findByVendorVendorId(Long vendorId);

    List<MealSet> findByVendorVendorIdAndIsAvailableTrue(Long vendorId);

    Optional<MealSet> findBySetIdAndVendorVendorId(String setId, Long vendorId);

    List<MealSet> findByTypeAndIsAvailableTrue(MealSet.MealSetType type);

    List<MealSet> findByIsAvailableTrue();

    @Query("SELECT ms FROM MealSet ms WHERE ms.vendor.vendorId = :vendorId AND ms.name LIKE %:name%")
    List<MealSet> findByVendorAndNameContaining(@Param("vendorId") Long vendorId, @Param("name") String name);

    boolean existsBySetIdAndVendorVendorId(String setId, Long vendorId);
}