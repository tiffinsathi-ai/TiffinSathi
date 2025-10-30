package com.tiifin_sathi.response;

public class LoginResponse {
    private String token;
    private long expiresIn;

    // Getter & Setter for token
    public String getToken() { return token; }

    public LoginResponse setToken(String token) { // return this instead of void
        this.token = token;
        return this;
    }

    // Getter & Setter for expiresIn
    public long getExpiresIn() { return expiresIn; }

    public LoginResponse setExpiresIn(long expiresIn) { // return this instead of void
        this.expiresIn = expiresIn;
        return this;
    }
}
