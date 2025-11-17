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

import com.tiffin_sathi.model.DeliveryPartner;
import com.tiffin_sathi.model.User;
import com.tiffin_sathi.model.Vendor;
import com.tiffin_sathi.repository.DeliveryPartnerRepository;
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
    private DeliveryPartnerRepository deliveryPartnerRepository;

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
        return extractClaim(token, Claims::getSubject);
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

    // ---------------- Token Generation for UserDetails ----------------
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
        } else if (userDetails instanceof DeliveryPartner deliveryPartner) {
            extraClaims.put("role", "DELIVERY");
            extraClaims.put("email", deliveryPartner.getEmail());
            extraClaims.put("partnerId", deliveryPartner.getPartnerId());
            extraClaims.put("vendorId", deliveryPartner.getVendor().getVendorId());
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
        } else if (userDetails instanceof DeliveryPartner deliveryPartner) {
            extraClaims.put("role", "DELIVERY");
            extraClaims.put("email", deliveryPartner.getEmail());
        }

        return generateToken(extraClaims, userDetails, refreshExpiration);
    }

    // ---------------- Token Generation for Specific Types ----------------
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("email", user.getEmail());
        claims.put("userId", user.getId());
        return createToken(claims, user.getEmail());
    }

    public String generateToken(Vendor vendor) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", vendor.getRole().name());
        claims.put("email", vendor.getBusinessEmail());
        claims.put("vendorId", vendor.getVendorId());
        return createToken(claims, vendor.getBusinessEmail());
    }

    public String generateToken(DeliveryPartner deliveryPartner) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "DELIVERY");
        claims.put("email", deliveryPartner.getEmail());
        claims.put("partnerId", deliveryPartner.getPartnerId());
        claims.put("vendorId", deliveryPartner.getVendor().getVendorId());
        return createToken(claims, deliveryPartner.getEmail());
    }

    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("email", user.getEmail());
        return createRefreshToken(claims, user.getEmail());
    }

    public String generateRefreshToken(Vendor vendor) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", vendor.getRole().name());
        claims.put("email", vendor.getBusinessEmail());
        return createRefreshToken(claims, vendor.getBusinessEmail());
    }

    public String generateRefreshToken(DeliveryPartner deliveryPartner) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "DELIVERY");
        claims.put("email", deliveryPartner.getEmail());
        return createRefreshToken(claims, deliveryPartner.getEmail());
    }

    // ---------------- Private Token Creation Methods ----------------
    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, long expirationTime) {

        String subject;

        if (userDetails instanceof User user) {
            subject = user.getEmail();
        } else if (userDetails instanceof Vendor vendor) {
            subject = vendor.getBusinessEmail();
        } else if (userDetails instanceof DeliveryPartner partner) {
            subject = partner.getEmail();
        } else {
            subject = userDetails.getUsername();
        }

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private String createRefreshToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
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
        // Lookup in all repositories
        if (!userRepository.findByEmail(email).isPresent() &&
                !vendorRepository.findByBusinessEmail(email).isPresent() &&
                !deliveryPartnerRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User, Vendor, or Delivery Partner not found");
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

            // Update password for User, Vendor, or DeliveryPartner
            if (userRepository.findByEmail(email).isPresent()) {
                User user = userRepository.findByEmail(email).get();
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
            } else if (vendorRepository.findByBusinessEmail(email).isPresent()) {
                Vendor vendor = vendorRepository.findByBusinessEmail(email).get();
                vendor.setPassword(passwordEncoder.encode(newPassword));
                vendorRepository.save(vendor);
            } else if (deliveryPartnerRepository.findByEmail(email).isPresent()) {
                DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmail(email).get();
                deliveryPartner.setPassword(passwordEncoder.encode(newPassword));
                deliveryPartnerRepository.save(deliveryPartner);
            } else {
                throw new RuntimeException("User, Vendor, or Delivery Partner not found");
            }

        } catch (Exception e) {
            throw new RuntimeException("Invalid or expired token");
        }
    }
}