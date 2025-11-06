package com.tiffin_sathi.controller;

import com.tiffin_sathi.dtos.JwtResponse;
import com.tiffin_sathi.dtos.LoginRequest;
import com.tiffin_sathi.dtos.SignupRequest;
import com.tiffin_sathi.model.User;
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

    @PostMapping("/signup")
    public ResponseEntity<User> register(@RequestBody SignupRequest registerUserDto) {
        User registeredUser = authenticationService.signup(registerUserDto);
        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticate(@RequestBody LoginRequest loginUserDto) {
        User authenticatedUser = authenticationService.authenticate(loginUserDto);

        // Generate JWT token
        String jwtToken = jwtService.generateToken(authenticatedUser);

        // Create response using record constructor
        JwtResponse loginResponse = new JwtResponse(jwtToken, jwtService.getExpirationTime());

        return ResponseEntity.ok(loginResponse);
    }
}
