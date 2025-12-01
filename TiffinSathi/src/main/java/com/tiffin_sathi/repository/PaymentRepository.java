package com.tiffin_sathi.repository;

import com.tiffin_sathi.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findBySubscriptionSubscriptionId(String subscritionId);
}