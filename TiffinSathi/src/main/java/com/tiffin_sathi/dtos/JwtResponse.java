package com.tiffin_sathi.dtos;

public record JwtResponse(
	    String token,
	    long expiresIn,
	    String refreshToken,
	    long refreshExpiresIn
	) {}
