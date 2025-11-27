package com.tiffin_sathi.repository;

import com.tiffin_sathi.model.Role;
import com.tiffin_sathi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);

    Optional<User> findByPhoneNumber(String phoneNumber);
    Boolean existsByPhoneNumber(String phoneNumber);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.subscriptions WHERE u.phoneNumber = :phoneNumber")
    Optional<User> findByPhoneNumberWithSubscriptions(@Param("phoneNumber") String phoneNumber);
}