package com.tiffin_sathi.repository;

import com.tiffin_sathi.model.SubscriptionEditHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionEditHistoryRepository extends JpaRepository<SubscriptionEditHistory, String> {

    List<SubscriptionEditHistory> findBySubscriptionSubscriptionIdOrderByCreatedAtDesc(String subscriptionId);

    // FIXED: Add this missing method
    @Query("SELECT seh FROM SubscriptionEditHistory seh WHERE seh.subscription.subscriptionId = :subscriptionId AND seh.status = :status")
    List<SubscriptionEditHistory> findBySubscriptionSubscriptionIdAndStatus(
            @Param("subscriptionId") String subscriptionId,
            @Param("status") String status);

    Optional<SubscriptionEditHistory> findTopBySubscriptionSubscriptionIdAndStatusOrderByCreatedAtDesc(
            String subscriptionId, String status);

    List<SubscriptionEditHistory> findBySubscriptionUserEmail(String userEmail);

    List<SubscriptionEditHistory> findByPaymentId(String paymentId);
}