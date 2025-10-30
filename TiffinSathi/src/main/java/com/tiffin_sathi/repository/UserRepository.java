package com.tiffin_sathi.repository;

import com.tiffin_sathi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Method to find a user by email, crucial for login
    Optional<User> findByEmail(String email);
    
    // Method to check if a user with the given email already exists
    Boolean existsByEmail(String email);
}
