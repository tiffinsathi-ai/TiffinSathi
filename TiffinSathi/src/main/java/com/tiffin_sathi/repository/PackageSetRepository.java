package com.tiffin_sathi.repository;

import com.tiffin_sathi.model.PackageSet;
import com.tiffin_sathi.model.PackageSetId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackageSetRepository extends JpaRepository<PackageSet, PackageSetId> {

    List<PackageSet> findByMealPackagePackageId(String packageId);

    @Modifying
    @Query("DELETE FROM PackageSet ps WHERE ps.mealPackage.packageId = :packageId")
    void deleteByMealPackagePackageId(@Param("packageId") String packageId);

    @Query("SELECT ps FROM PackageSet ps WHERE ps.mealPackage.packageId = :packageId AND ps.mealSet.setId = :setId")
    Optional<PackageSet> findByPackageIdAndSetId(@Param("packageId") String packageId, @Param("setId") String setId);
}