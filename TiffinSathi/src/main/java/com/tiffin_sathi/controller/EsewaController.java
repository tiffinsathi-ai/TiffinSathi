package com.tiffin_sathi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Payload coming from the React frontend.
 * Uses Java Records (Java 17+).
 */
record SignatureRequest(
        @NotNull String total_amount,
        @NotBlank String transaction_uuid,
        @NotBlank String product_code
) {}

@RestController
@RequestMapping("/api/esewa")
@CrossOrigin(origins = "http://localhost:5173") // Allow calls from React dev server
public class EsewaController {

    // Sandbox secret key (Do not hardcode in production)
    private static final String ESEWA_SECRET_KEY = "8gBm/:&EnhH.1/q";
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    /**
     * Generates the HMAC-SHA256 signature for eSewa.
     * @param request incoming JSON data from frontend
     * @return JSON with "signature"
     */
    @PostMapping("/signature")
    public ResponseEntity<?> generateSignature(@RequestBody SignatureRequest request) {
        try {

            // 1. Build hash string in EXACT order required by eSewa
            String hashString = String.format(
                    "total_amount=%s,transaction_uuid=%s,product_code=%s",
                    request.total_amount(),
                    request.transaction_uuid(),
                    request.product_code()
            );

            // 2. Initialize HMAC with secret key
            SecretKeySpec secretKey = new SecretKeySpec(
                    ESEWA_SECRET_KEY.getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGORITHM
            );

            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(secretKey);

            // 3. Generate HMAC hash
            byte[] hmacBytes = mac.doFinal(hashString.getBytes(StandardCharsets.UTF_8));

            // 4. Convert hash to Base64
            String signature = Base64.getEncoder().encodeToString(hmacBytes);

            // 5. Return JSON response
            Map<String, String> response = new HashMap<>();
            response.put("signature", signature);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error generating signature");
        }
    }
}
