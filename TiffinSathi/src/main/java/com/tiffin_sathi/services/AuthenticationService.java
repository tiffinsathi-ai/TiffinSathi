package com.tiffin_sathi.services;

import com.tiffin_sathi.dtos.LoginRequest;
import com.tiffin_sathi.dtos.SignupRequest;
import com.tiffin_sathi.dtos.VendorSignupRequest;
import com.tiffin_sathi.model.*;
import com.tiffin_sathi.repository.UserRepository;
import com.tiffin_sathi.repository.VendorRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    
    @Autowired
    private EmailService emailService; 
    
    

    public AuthenticationService(UserRepository userRepository,
                                 VendorRepository vendorRepository,
                                 AuthenticationManager authenticationManager,
                                 PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.vendorRepository = vendorRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    // -------- Signup for normal user --------
    public User signupUser(SignupRequest input) {
        if (userRepository.existsByEmail(input.email()) && vendorRepository.findByBusinessEmail(input.email()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setUserName(input.userName());
        user.setEmail(input.email());
        user.setPassword(passwordEncoder.encode(input.password()));
        user.setPhoneNumber(input.phoneNumber());
        user.setRole(Role.USER);
        user.setStatus(Status.ACTIVE);
        user.setProfilePicture(input.profilePicture());

        return userRepository.save(user);
    }

    // -------- Signup for vendor --------
    public Vendor signupVendor(VendorSignupRequest input) {
        if (vendorRepository.findByBusinessEmail(input.getEmail()).isPresent() ||
                userRepository.existsByEmail(input.getEmail())) {
            throw new RuntimeException("Business email already in use");
        }

        Vendor vendor = new Vendor();
        vendor.setOwnerName(input.getUserName());
        vendor.setBusinessName(input.getBusinessName());
        vendor.setBusinessEmail(input.getEmail());
        vendor.setPhone(input.getPhoneNumber());

        // Password
        String rawPassword = input.getPassword() != null ? input.getPassword() : "defaultPassword123";
        vendor.setPassword(passwordEncoder.encode(rawPassword));

        vendor.setRole(Role.VENDOR);
        vendor.setProfilePicture(input.getProfilePicture());
        vendor.setStatus(VendorStatus.PENDING);

        // Other fields
        vendor.setBusinessAddress(input.getBusinessAddress());
        vendor.setAlternatePhone(input.getAlternatePhone());
        vendor.setCuisineType(input.getCuisineType());
        vendor.setCapacity(input.getCapacity());
        vendor.setDescription(input.getDescription());
        vendor.setBankName(input.getBankName());
        vendor.setAccountNumber(input.getAccountNumber());
        vendor.setBranchName(input.getBranchName());
        vendor.setAccountHolderName(input.getAccountHolderName());
        vendor.setPanNumber(input.getPanNumber());
        vendor.setVatNumber(input.getVatNumber());
        vendor.setFoodLicenseNumber(input.getFoodLicenseNumber());
        vendor.setCompanyRegistrationNumber(input.getCompanyRegistrationNumber());

        // Cloudinary documents
        vendor.setFssaiLicenseUrl(input.getFssaiLicenseUrl());
        vendor.setPanCardUrl(input.getPanCardUrl());
        vendor.setBankProofUrl(input.getBankProofUrl());
        vendor.setMenuCardUrl(input.getMenuCardUrl());

        // Save vendor (ONLY ONCE)
        Vendor savedVendor = vendorRepository.save(vendor);

        // Send emails after save
        sendEmails(savedVendor);

        return savedVendor;
    }

    
    private void sendEmails(Vendor vendor) {

        // Get all admins from DB
        List<User> admins = userRepository.findByRole(Role.ADMIN);

        // Notify each admin
        for (User admin : admins) {
            emailService.sendEmail(
                admin.getEmail(),
                "New Vendor Registration Request",
                "A new vendor has requested registration:\n\n" +
                        "Vendor: " + vendor.getBusinessName() + "\n" +
                        "Email: " + vendor.getBusinessEmail() + "\n" +
                        "Phone: " + vendor.getPhone()
            );
        }

        // Send confirmation email to vendor
        emailService.sendEmail(
            vendor.getBusinessEmail(),
            "Your Vendor Registration Request is Submitted",
            "Hello " + vendor.getOwnerName() + ",\n\n" +
                    "Your request to join our platform has been submitted.\n" +
                    "We will review your application and notify you soon."
        );
    }


    

    // -------- Authenticate both User and Vendor --------
    public Object authenticate(LoginRequest input) {
        // Perform authentication
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.email(),
                        input.password()
                )
        );

        // Try finding user
        User user = userRepository.findByEmail(input.email()).orElse(null);
        if (user != null) {
            if (user.getStatus() != Status.ACTIVE) {
                throw new RuntimeException("User account is not active");
            }
            return user;
        }

        // Try finding vendor
        Vendor vendor = vendorRepository.findByBusinessEmail(input.email()).orElse(null);
        if (vendor != null) {
            if (vendor.getStatus() != VendorStatus.APPROVED) {
                throw new RuntimeException("Vendor account is not approved");
            }
            return vendor;
        }

        throw new RuntimeException("User or Vendor not found");
    }
}