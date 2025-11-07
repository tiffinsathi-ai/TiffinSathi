package com.tiffin_sathi.controller;

import com.tiffin_sathi.dtos.JwtResponse;
import com.tiffin_sathi.dtos.LoginRequest;
import com.tiffin_sathi.dtos.SignupRequest;
import com.tiffin_sathi.dtos.VendorSignupRequest;
import com.tiffin_sathi.model.User;
import com.tiffin_sathi.model.Vendor;
import com.tiffin_sathi.services.AuthenticationService;
import com.tiffin_sathi.services.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
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

    // -------- Login (User or Vendor) --------
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticate(@RequestBody LoginRequest loginRequest) {
        Object authResult = authenticationService.authenticate(loginRequest);

        String accessToken;
        String refreshToken;

        if (authResult instanceof User user) {
            accessToken = jwtService.generateToken(user);
            refreshToken = jwtService.generateRefreshToken(user);
        } else if (authResult instanceof Vendor vendor) {
            accessToken = jwtService.generateToken(vendor);
            refreshToken = jwtService.generateRefreshToken(vendor);
        } else {
            throw new RuntimeException("Authentication failed");
        }

        JwtResponse response = new JwtResponse(
                accessToken,
                jwtService.getJwtExpiration(),
                refreshToken,
                jwtService.getRefreshExpiration()
        );

        return ResponseEntity.ok(response);
    }
}
