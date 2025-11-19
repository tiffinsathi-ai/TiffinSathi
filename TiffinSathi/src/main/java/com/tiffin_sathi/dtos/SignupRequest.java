package com.tiffin_sathi.dtos;

public record SignupRequest(
        String email,
        String password,
        String userName,
        String phoneNumber,       // optional
        String profilePicture     // optional, can be null if no file uploaded
) {}
