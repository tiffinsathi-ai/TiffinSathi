package com.tiffin_sathi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tiffin_sathi.dtos.LoginUserDto;
import com.tiffin_sathi.dtos.RegisterUserDto;
import com.tiffin_sathi.model.User;
import com.tiffin_sathi.services.AuthenticationService;
import com.tiffin_sathi.services.JwtServices;
import com.tiifin_sathi.response.LoginResponse;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {
    private final JwtServices jwtService;
    
    private final AuthenticationService authenticationService;

    public AuthenticationController(JwtServices jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<User> register(@RequestBody RegisterUserDto registerUserDto) {
        User registeredUser = authenticationService.signup(registerUserDto);

        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto) {
        User authenticatedUser = authenticationService.authenticate(loginUserDto);

        String jwtToken = jwtService.generateToken(authenticatedUser);

        LoginResponse loginResponse = new LoginResponse().setToken(jwtToken).setExpiresIn(jwtService.getExpirationTime());

        return ResponseEntity.ok(loginResponse);
    }
}