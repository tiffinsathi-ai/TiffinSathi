package com.tiffin_sathi.repository;

import com.tiffin_sathi.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByTransactionUuid(String transactionUuid);
}
