package com.tiffin_sathi.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tiffin_sathi.model.Role;
import com.tiffin_sathi.model.Status;
import com.tiffin_sathi.model.User;
import com.tiffin_sathi.model.Vendor;
import com.tiffin_sathi.repository.UserRepository;
import com.tiffin_sathi.repository.VendorRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User updateUser(Long userId, User userDetails) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (userDetails.getUserName() != null) {
            user.setUserName(userDetails.getUserName());
        }
        if (userDetails.getPhoneNumber() != null) {
            user.setPhoneNumber(userDetails.getPhoneNumber());
        }
        if (userDetails.getProfilePicture() != null) {
            user.setProfilePicture(userDetails.getProfilePicture());
        }

        return userRepository.save(user);
    }

    public User updateUserStatus(Long userId, Status status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.setStatus(status);
        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        userRepository.delete(user);
    }

    // Change password for User
    public String changeUserPassword(Long userId, String currentPassword, String newPassword, String confirmPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        return changePasswordLogic(user.getPassword(), currentPassword, newPassword, confirmPassword, (encoded) -> {
            user.setPassword(encoded);
            userRepository.save(user);
        });
    }

    // Change password for Vendor
    public String changeVendorPassword(Long vendorId, String currentPassword, String newPassword, String confirmPassword) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found with id: " + vendorId));

        return changePasswordLogic(vendor.getPassword(), currentPassword, newPassword, confirmPassword, (encoded) -> {
            vendor.setPassword(encoded);
            vendorRepository.save(vendor);
        });
    }

    // Common logic to avoid duplication
    private String changePasswordLogic(String existingPassword, String currentPassword,
                                       String newPassword, String confirmPassword,
                                       java.util.function.Consumer<String> saveFunction) {

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, existingPassword)) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Check new and confirm password match
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("New password and confirm password do not match");
        }

        // Prevent same password reuse
        if (passwordEncoder.matches(newPassword, existingPassword)) {
            throw new RuntimeException("New password must be different from current password");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        saveFunction.accept(encodedPassword);
        return "Password changed successfully";
    }

    public User updateUserRole(Long userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.setRole(role);
        return userRepository.save(user);
    }

    @Transactional
    public User findOrCreateUser(String phoneNumber, String fullName, String email) {
        // Try to find user by phone number
        Optional<User> existingUser = userRepository.findByPhoneNumber(phoneNumber);

        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        // Create new user
        User newUser = new User();
        newUser.setUserName(fullName);
        newUser.setPhoneNumber(phoneNumber);
        newUser.setEmail(email != null ? email : phoneNumber + "@tiffinsathi.com");

        // Generate a random password for the user
        String tempPassword = generateTempPassword();
        newUser.setPassword(passwordEncoder.encode(tempPassword));
        newUser.setRole(Role.USER);
        newUser.setStatus(Status.ACTIVE);

        return userRepository.save(newUser);
    }
    private String generateTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        java.util.Random random = new java.util.Random();
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
