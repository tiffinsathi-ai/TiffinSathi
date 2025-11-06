package com.tiffin_sathi.services;


import com.tiffin_sathi.dtos.LoginRequest;
import com.tiffin_sathi.dtos.SignupRequest;
import com.tiffin_sathi.model.Role;
import com.tiffin_sathi.model.Status;
import com.tiffin_sathi.model.User;
import com.tiffin_sathi.repository.UserRepository;


import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepository userRepository,
                       AuthenticationManager authenticationManager,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    public User signup(SignupRequest input) {
        if (userRepository.existsByEmail(input.email())) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setUserName(input.userName());
        user.setEmail(input.email());
        user.setPassword(passwordEncoder.encode(input.password()));
        user.setPhoneNumber(input.phoneNumber());
        user.setRole(Role.USER);       // default
        user.setStatus(Status.ACTIVE); // default
        user.setProfilePicture(input.profilePicture()); // optional, can be null

        // JPA will automatically populate createdAt and updatedAt
        return userRepository.save(user);
    }

    public User authenticate(LoginRequest input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.email(),
                        input.password()
                )
        );

        User user = userRepository.findByEmail(input.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() != Status.ACTIVE) {
            throw new RuntimeException("User account is not active");
        }

        return user;
    }
}
