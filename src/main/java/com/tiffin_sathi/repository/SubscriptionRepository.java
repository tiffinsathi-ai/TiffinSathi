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

    @Query("SELECT s FROM Subscription s JOIN FETCH s.user u WHERE u.email = :email")
    List<Subscription> findByUserEmail(@Param("email") String email);

    List<Subscription> findByStatus(Subscription.SubscriptionStatus status);

    @Query("SELECT s FROM Subscription s JOIN FETCH s.user WHERE s.subscriptionId = :subscriptionId")
    Optional<Subscription> findByIdWithUser(@Param("subscriptionId") String subscriptionId);

    @Query("SELECT s FROM Subscription s JOIN FETCH s.user WHERE s.user.phoneNumber = :phoneNumber")
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

    @Query("SELECT s FROM Subscription s WHERE s.mealPackage.vendor.vendorId = :vendorId")
    List<Subscription> findByMealPackageVendorVendorId(@Param("vendorId") Long vendorId);

    @Query("SELECT s FROM Subscription s WHERE s.mealPackage.vendor.vendorId = :vendorId AND s.status = :status")
    List<Subscription> findByMealPackageVendorVendorIdAndStatus(
            @Param("vendorId") Long vendorId,
            @Param("status") Subscription.SubscriptionStatus status);



}