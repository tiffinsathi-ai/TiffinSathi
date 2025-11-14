package com.tiffin_sathi.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.JwtException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import com.tiffin_sathi.model.User;
import com.tiffin_sathi.model.Vendor;
import com.tiffin_sathi.repository.UserRepository;
import com.tiffin_sathi.repository.VendorRepository;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    @Value("${security.jwt.refresh-expiration-time}")
    private long refreshExpiration;
    
    @Autowired
    private UserRepository userRepository; 
    
    @Autowired
    private VendorRepository vendorRepository;

    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    // ---------------- Extract Claims ----------------
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject); // Subject is email
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    // ---------------- Token Generation ----------------
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();

        if (userDetails instanceof User user) {
            extraClaims.put("role", user.getRole().name());
            extraClaims.put("email", user.getEmail());
            extraClaims.put("userId", user.getId());
        } else if (userDetails instanceof Vendor vendor) {
            extraClaims.put("role", vendor.getRole().name());
            extraClaims.put("email", vendor.getBusinessEmail());
            extraClaims.put("vendorId", vendor.getVendorId());
        }

        return generateToken(extraClaims, userDetails, jwtExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();

        if (userDetails instanceof User user) {
            extraClaims.put("role", user.getRole().name());
            extraClaims.put("email", user.getEmail());
        } else if (userDetails instanceof Vendor vendor) {
            extraClaims.put("role", vendor.getRole().name());
            extraClaims.put("email", vendor.getBusinessEmail());
        }

        return generateToken(extraClaims, userDetails, refreshExpiration);
    }

    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, long expirationTime) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername()) // username is email
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ---------------- Token Validation ----------------
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        final String role = extractRole(token);

        boolean validUsername = username.equals(userDetails.getUsername());
        boolean notExpired = !isTokenExpired(token);

        System.out.println("Token validation - Username: " + username + ", Role: " + role + ", Valid: " + validUsername + ", Not Expired: " + notExpired);

        return validUsername && notExpired;
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // ---------------- Signing Key ----------------
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public long getJwtExpiration() {
        return jwtExpiration;
    }

    public long getRefreshExpiration() {
        return refreshExpiration;
    }
    
 // ------------------ Send OTP ------------------
    public String sendOtp(String email) {
        // Lookup in both repositories
        if (!userRepository.findByEmail(email).isPresent() && !vendorRepository.findByBusinessEmail(email).isPresent()) {
            throw new RuntimeException("User or Vendor not found");
        }

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000); // 6-digit OTP

        // Create JWT with OTP
        String token = Jwts.builder()
                .claim("email", email)
                .claim("otp", otp)
                .setExpiration(Date.from(Instant.now().plus(5, ChronoUnit.MINUTES)))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        // Send OTP via email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password Reset OTP");
        message.setText("Your OTP is: " + otp + "\nIt is valid for 5 minutes.");
        mailSender.send(message);

        return token;
    }

    // ------------------ Verify OTP ------------------
    public boolean verifyOtp(String token, String inputOtp) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();

            String otp = claims.get("otp", String.class);
            return otp.equals(inputOtp);
        } catch (Exception e) {
            return false; // expired or invalid
        }
    }

    // ------------------ Reset Password ------------------
    public void resetPassword(String token, String email, String newPassword) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();

            String tokenEmail = claims.get("email", String.class);
            if (!tokenEmail.equals(email)) {
                throw new RuntimeException("Invalid token for email");
            }

            // Update password for User or Vendor
            if (userRepository.findByEmail(email).isPresent()) {
                User user = userRepository.findByEmail(email).get();
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
            } else if (vendorRepository.findByBusinessEmail(email).isPresent()) {
                Vendor vendor = vendorRepository.findByBusinessEmail(email).get();
                vendor.setPassword(passwordEncoder.encode(newPassword));
                vendorRepository.save(vendor);
            } else {
                throw new RuntimeException("User or Vendor not found");
            }

        } catch (Exception e) {
            throw new RuntimeException("Invalid or expired token");
        }
    }

}
