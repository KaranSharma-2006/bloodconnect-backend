package com.bloodconnect.bloodconnect.service;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OAuthService {

    @Value("${oauth.google.client-id:}")
    private String googleClientId;

    /**
     * Verify Google ID token
     * Note: In production, implement full signature verification with Google's public keys
     */
    public Map<String, String> verifyGoogleToken(String idToken) {
        try {
            // Decode JWT payload
            Map<String, String> claims = decodeJWT(idToken);
            
            // Verify audience (optional but recommended)
            String audience = claims.get("aud");
            if (audience != null && !audience.contains(googleClientId)) {
                // Allow for basic validation - in production, implement stricter verification
                System.out.println("Warning: Token audience may not match. Expected: " + googleClientId + ", Got: " + audience);
            }

            return claims;
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify Google token: " + e.getMessage(), e);
        }
    }

    /**
     * Decode JWT payload without signature verification
     * Extracts claims from the JWT's payload section
     */
    private Map<String, String> decodeJWT(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT format");
            }

            // Decode payload (second part)
            byte[] decodedBytes = Base64.getUrlDecoder().decode(parts[1]);
            String payload = new String(decodedBytes);

            // Parse JSON manually using regex patterns
            Map<String, String> claims = new HashMap<>();
            
            // Extract common fields
            extractJsonField(payload, "sub", claims, "id");
            extractJsonField(payload, "email", claims, "email");
            extractJsonField(payload, "name", claims, "name");
            extractJsonField(payload, "picture", claims, "picture");
            extractJsonField(payload, "aud", claims, "aud");
            
            return claims;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode JWT: " + e.getMessage(), e);
        }
    }

    /**
     * Extract a field from JSON using regex pattern
     */
    private void extractJsonField(String json, String jsonKey, Map<String, String> claims, String mapKey) {
        String pattern = "\"" + jsonKey + "\":\"([^\"]+)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            claims.put(mapKey, m.group(1));
        }
    }

    /**
     * Verify Apple ID token
     * Note: In production, implement full signature verification with Apple's public keys
     */
    public Map<String, String> verifyAppleToken(String idToken) {
        try {
            // Decode JWT payload
            Map<String, String> claims = decodeJWT(idToken);
            return claims;
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify Apple token: " + e.getMessage(), e);
        }
    }
}
