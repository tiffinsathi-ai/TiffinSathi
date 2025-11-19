package com.tiffin_sathi.dtos;

public class VerifyOtpRequest {
    private String email;
    private String otp;
    private String token; // JWT token received from server


	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getOtp() {
		return otp;
	}
	public void setOtp(String otp) {
		this.otp = otp;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	
}