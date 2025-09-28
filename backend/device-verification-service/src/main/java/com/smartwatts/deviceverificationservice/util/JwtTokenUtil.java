package com.smartwatts.deviceverificationservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtTokenUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration.residential}")
    private long residentialExpirationSeconds;

    @Value("${jwt.expiration.commercial}")
    private long commercialExpirationSeconds;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate activation token with dual validity system
     * - Residential: 12 months (365 days)
     * - Commercial: 3 months initially (90 days), 12 months on renewal (365 days)
     */
    public String generateActivationToken(String deviceId, LocalDateTime activatedAt, 
                                       LocalDateTime expiresAt, String customerType, int validityDays) {
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("deviceId", deviceId);
        claims.put("customerType", customerType);
        claims.put("validityDays", validityDays);
        claims.put("tokenType", "ACTIVATION");
        claims.put("activatedAt", activatedAt.toString());
        claims.put("expiresAt", expiresAt.toString());

        return createToken(claims, deviceId, expiresAt);
    }

    /**
     * Create JWT token with claims and expiration
     */
    private String createToken(Map<String, Object> claims, String subject, LocalDateTime expiration) {
        Date expirationDate = Date.from(expiration.atZone(ZoneId.systemDefault()).toInstant());
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Validate activation token
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            
            // Check if token is expired
            if (claims.getExpiration().before(new Date())) {
                log.warn("Token expired for device: {}", claims.get("deviceId"));
                return false;
            }

            // Validate required claims
            if (claims.get("deviceId") == null || claims.get("customerType") == null) {
                log.warn("Token missing required claims for device: {}", claims.get("deviceId"));
                return false;
            }

            // Validate token type
            if (!"ACTIVATION".equals(claims.get("tokenType"))) {
                log.warn("Invalid token type for device: {}", claims.get("deviceId"));
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract all claims from token
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extract device ID from token
     */
    public String extractDeviceId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("deviceId", String.class);
    }

    /**
     * Extract customer type from token
     */
    public String extractCustomerType(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("customerType", String.class);
    }

    /**
     * Extract validity days from token
     */
    public Integer extractValidityDays(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("validityDays", Integer.class);
    }

    /**
     * Extract expiration date from token
     */
    public LocalDateTime extractExpirationDate(String token) {
        Claims claims = extractAllClaims(token);
        Date expiration = claims.getExpiration();
        return expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true; // Consider expired if we can't parse
        }
    }

    /**
     * Hash token for storage (security)
     */
    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("Error hashing token: {}", e.getMessage());
            return token; // Fallback to original token
        }
    }

    /**
     * Calculate expiration date based on customer type and activation type
     */
    public LocalDateTime calculateExpirationDate(LocalDateTime activatedAt, String customerType, boolean isRenewal) {
        int validityDays;
        
        if (isRenewal) {
            // All renewals get 12 months
            validityDays = 365;
        } else {
            // Initial activation: Residential gets 12 months, Commercial gets 3 months
            validityDays = "RESIDENTIAL".equalsIgnoreCase(customerType) ? 365 : 90;
        }
        
        return activatedAt.plusDays(validityDays);
    }

    /**
     * Get validity days for customer type and activation type
     */
    public int getValidityDays(String customerType, boolean isRenewal) {
        if (isRenewal) {
            return 365; // All renewals get 12 months
        } else {
            return "RESIDENTIAL".equalsIgnoreCase(customerType) ? 365 : 90;
        }
    }
}
