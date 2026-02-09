package com.tiffin_sathi.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tiffin_sathi.dtos.ChangePasswordDTO;
import com.tiffin_sathi.dtos.UpdateRoleRequest;
import com.tiffin_sathi.dtos.UpdateUserDTO;
import com.tiffin_sathi.model.Status;
import com.tiffin_sathi.model.User;
import com.tiffin_sathi.services.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    // Admin only - Get all users
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Get user by ID (user can access their own, admin can access any)
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        Optional<User> user = userService.getUserById(userId);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Get current user profile
    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile(Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<User> user = userService.getUserByEmail(email);
            if (user.isPresent()) {
                return ResponseEntity.ok(user.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching profile: " + e.getMessage());
        }
    }

    // Get user by email
    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        Optional<User> user = userService.getUserByEmail(email);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Update user profile
    @PutMapping("/profile")
    public ResponseEntity<?> updateCurrentUserProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateUserDTO updateUserDTO) {
        try {
            String email = authentication.getName();
            Optional<User> existingUser = userService.getUserByEmail(email);

            if (existingUser.isPresent()) {
                User user = existingUser.get();
                user.setUserName(updateUserDTO.getUserName());
                user.setPhoneNumber(updateUserDTO.getPhoneNumber());
                user.setProfilePicture(updateUserDTO.getProfilePicture());

                User updatedUser = userService.updateUser(user.getId(), user);
                return ResponseEntity.ok(updatedUser);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Update user profile by ID
    @PutMapping("profile/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @Valid @RequestBody UpdateUserDTO updateUserDTO) {
        try {
            User user = new User();
            user.setUserName(updateUserDTO.getUserName());
            user.setPhoneNumber(updateUserDTO.getPhoneNumber());
            user.setProfilePicture(updateUserDTO.getProfilePicture());

            User updatedUser = userService.updateUser(userId, user);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Admin only - Update user status
    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long userId, @RequestBody Status status) {
        try {
            User updatedUser = userService.updateUserStatus(userId, status);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Change password for current user
    @PutMapping("/change-password")
    public ResponseEntity<?> changeCurrentUserPassword(
            Authentication authentication,
            @RequestBody @Valid ChangePasswordDTO dto) {
        try {
            String email = authentication.getName();
            Optional<User> user = userService.getUserByEmail(email);

            if (user.isPresent()) {
                String message = userService.changeUserPassword(
                        user.get().getId(),
                        dto.getCurrentPassword(),
                        dto.getNewPassword(),
                        dto.getConfirmPassword()
                );
                return ResponseEntity.ok(message);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Change password
    @PutMapping("/{userId}/change-password")
    public ResponseEntity<?> changeUserPassword(@PathVariable Long userId,
                                                @RequestBody @Valid ChangePasswordDTO dto) {
        String message = userService.changeUserPassword(userId, dto.getCurrentPassword(),
                dto.getNewPassword(), dto.getConfirmPassword());
        return ResponseEntity.ok(message);
    }

    // Admin only - Delete user
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok("User deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Admin only - Update user role
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{userId}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long userId, @Valid @RequestBody UpdateRoleRequest updateRoleDTO) {
        try {
            User updatedUser = userService.updateUserRole(userId, updateRoleDTO.getRole());
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}