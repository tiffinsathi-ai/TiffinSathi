package com.tiffin_sathi.controller;

import com.tiffin_sathi.dtos.*;
import com.tiffin_sathi.model.DeliveryPartner;
import com.tiffin_sathi.model.User;
import com.tiffin_sathi.model.Vendor;
import com.tiffin_sathi.services.AuthenticationService;
import com.tiffin_sathi.services.DeliveryPartnerService;
import com.tiffin_sathi.services.EmailService;
import com.tiffin_sathi.services.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final JwtService jwtService;
    private final AuthenticationService authenticationService;
    private final EmailService emailService;
    private final DeliveryPartnerService deliveryPartnerService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationController(JwtService jwtService,
                                    AuthenticationService authenticationService,
                                    EmailService emailService,
                                    DeliveryPartnerService deliveryPartnerService,
                                    AuthenticationManager authenticationManager) { // Add this parameter
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.emailService = emailService;
        this.deliveryPartnerService = deliveryPartnerService;
        this.authenticationManager = authenticationManager;
    }

    // -------- User Signup --------
    @PostMapping("/signup/user")
    public ResponseEntity<User> registerUser(@RequestBody SignupRequest signupRequest) {
        User registeredUser = authenticationService.signupUser(signupRequest);
        return ResponseEntity.ok(registeredUser);
    }

    // -------- Vendor Signup --------
    @PostMapping("/signup/vendor")
    public ResponseEntity<Vendor> registerVendor(@RequestBody VendorSignupRequest vendorSignupRequest) {
        Vendor registeredVendor = authenticationService.signupVendor(vendorSignupRequest);
        return ResponseEntity.ok(registeredVendor);
    }

    // -------- Login (User, Vendor, or Delivery Partner) --------
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticate(@RequestBody LoginRequest loginRequest) {
        System.out.println("Login attempt for email: " + loginRequest.email());

        Object authResult = authenticationService.authenticate(loginRequest);

        String accessToken;
        String refreshToken;
        String role;
        String email;

        if (authResult instanceof User user) {
            accessToken = jwtService.generateToken(user);
            refreshToken = jwtService.generateRefreshToken(user);
            role = user.getRole().name();
            email = user.getEmail();
            System.out.println("User login successful - Email: " + email + ", Role: " + role);
        } else if (authResult instanceof Vendor vendor) {
            accessToken = jwtService.generateToken(vendor);
            refreshToken = jwtService.generateRefreshToken(vendor);
            role = vendor.getRole().name();
            email = vendor.getBusinessEmail();
            System.out.println("Vendor login successful - Email: " + email + ", Role: " + role);
        } else if (authResult instanceof DeliveryPartner deliveryPartner) {
            accessToken = jwtService.generateToken(deliveryPartner);
            refreshToken = jwtService.generateRefreshToken(deliveryPartner);
            role = "DELIVERY";
            email = deliveryPartner.getEmail();
            System.out.println("Delivery partner login successful - Email: " + email + ", Role: " + role);
        } else {
            throw new RuntimeException("Authentication failed");
        }

        JwtResponse response = new JwtResponse(
                accessToken,
                jwtService.getJwtExpiration(),
                refreshToken,
                jwtService.getRefreshExpiration()
        );

        // Log token details for debugging
        System.out.println("Generated Token - Role: " + jwtService.extractRole(accessToken) +
                ", Email: " + jwtService.extractEmail(accessToken));

        return ResponseEntity.ok(response);
    }
    // -------- Forgot Password --------
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        String token = jwtService.sendOtp(request.getEmail());
        return ResponseEntity.ok(token); // send token to frontend
    }

    // -------- Verify OTP --------
    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody VerifyOtpRequest request) {
        boolean isValid = jwtService.verifyOtp(request.getToken(), request.getOtp());
        if (isValid) {
            return ResponseEntity.ok("OTP verified successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired OTP");
        }
    }

    // -------- Reset Password --------
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Passwords do not match");
        }

        jwtService.resetPassword(request.getToken(), request.getEmail(), request.getNewPassword());
        return ResponseEntity.ok("Password reset successfully");
    }
}