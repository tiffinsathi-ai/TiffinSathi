package com.tiffin_sathi.repository;

import com.tiffin_sathi.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, String> {

    // Updated to fetch meal package eagerly
    @Query("SELECT s FROM Subscription s JOIN FETCH s.user u JOIN FETCH s.mealPackage mp WHERE u.email = :email")
    List<Subscription> findByUserEmail(@Param("email") String email);

    List<Subscription> findByStatus(Subscription.SubscriptionStatus status);

    // Updated to fetch meal package eagerly
    @Query("SELECT s FROM Subscription s JOIN FETCH s.user JOIN FETCH s.mealPackage WHERE s.subscriptionId = :subscriptionId")
    Optional<Subscription> findByIdWithUser(@Param("subscriptionId") String subscriptionId);

    // Updated to fetch meal package eagerly
    @Query("SELECT s FROM Subscription s JOIN FETCH s.user JOIN FETCH s.mealPackage WHERE s.user.phoneNumber = :phoneNumber")
    List<Subscription> findByUserPhoneNumber(@Param("phoneNumber") String phoneNumber);

    @Query("SELECT s FROM Subscription s WHERE s.endDate >= :date AND s.status = 'ACTIVE'")
    List<Subscription> findActiveSubscriptionsOnDate(@Param("date") LocalDate date);

    @Query("SELECT s FROM Subscription s WHERE s.startDate <= :date AND s.endDate >= :date AND s.status = 'ACTIVE'")
    List<Subscription> findActiveSubscriptionsBetweenDates(@Param("date") LocalDate date);

    @Query("SELECT s FROM Subscription s WHERE s.status = :status AND s.startDate <= :endDate AND s.endDate >= :startDate")
    List<Subscription> findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            @Param("status") Subscription.SubscriptionStatus status,
            @Param("endDate") LocalDate endDate,
            @Param("startDate") LocalDate startDate);

    // Updated to fetch meal package eagerly
    @Query("SELECT s FROM Subscription s JOIN FETCH s.mealPackage mp WHERE mp.vendor.vendorId = :vendorId")
    List<Subscription> findByMealPackageVendorVendorId(@Param("vendorId") Long vendorId);

    // Updated to fetch meal package eagerly
    @Query("SELECT s FROM Subscription s JOIN FETCH s.mealPackage mp WHERE mp.vendor.vendorId = :vendorId AND s.status = :status")
    List<Subscription> findByMealPackageVendorVendorIdAndStatus(
            @Param("vendorId") Long vendorId,
            @Param("status") Subscription.SubscriptionStatus status);
}