package com.tiffin_sathi.repository;

import com.tiffin_sathi.model.MealPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MealPackageRepository extends JpaRepository<MealPackage, String> {

    List<MealPackage> findByVendorVendorId(Long vendorId);

    List<MealPackage> findByVendorVendorIdAndIsAvailableTrue(Long vendorId);

    Optional<MealPackage> findByPackageIdAndVendorVendorId(String packageId, Long vendorId);

    List<MealPackage> findByBasePackageTypeAndIsAvailableTrue(String basePackageType);

    List<MealPackage> findByIsAvailableTrue();

    @Query("SELECT mp FROM MealPackage mp WHERE mp.pricePerSet BETWEEN :minPrice AND :maxPrice AND mp.isAvailable = true")
    List<MealPackage> findByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);

    @Query("SELECT mp FROM MealPackage mp WHERE mp.durationDays = :duration AND mp.isAvailable = true")
    List<MealPackage> findByDurationDays(@Param("duration") Integer duration);
}