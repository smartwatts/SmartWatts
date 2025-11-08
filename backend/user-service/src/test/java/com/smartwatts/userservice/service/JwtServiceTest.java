package com.smartwatts.userservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private String secretKey;
    private long jwtExpiration;
    private long refreshExpiration;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        secretKey = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
        jwtExpiration = 86400000L; // 24 hours
        refreshExpiration = 604800000L; // 7 days

        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", jwtExpiration);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", refreshExpiration);

        userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities("USER")
                .build();
    }

    @Test
    void extractUsername_ValidToken_ReturnsUsername() {
        // Given
        String token = jwtService.generateToken(userDetails);

        // When
        String username = jwtService.extractUsername(token);

        // Then
        assertEquals("testuser", username);
    }

    @Test
    void generateToken_WithUserDetails_ReturnsToken() {
        // When
        String token = jwtService.generateToken(userDetails);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void generateToken_WithExtraClaims_ReturnsToken() {
        // Given
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ADMIN");

        // When
        String token = jwtService.generateToken(extraClaims, userDetails);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void generateRefreshToken_WithUserDetails_ReturnsToken() {
        // When
        String token = jwtService.generateRefreshToken(userDetails);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void isTokenValid_ValidToken_ReturnsTrue() {
        // Given
        String token = jwtService.generateToken(userDetails);

        // When
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Then
        assertTrue(isValid);
    }

    @Test
    void isTokenValid_InvalidUsername_ReturnsFalse() {
        // Given
        String token = jwtService.generateToken(userDetails);
        UserDetails differentUser = User.builder()
                .username("differentuser")
                .password("password")
                .authorities("USER")
                .build();

        // When
        boolean isValid = jwtService.isTokenValid(token, differentUser);

        // Then
        assertFalse(isValid);
    }

    @Test
    void isTokenValid_ExpiredToken_ReturnsFalse() {
        // Given
        // Create an expired token manually
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(new Date(System.currentTimeMillis() - 100000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(key)
                .compact();

        // When & Then
        // Expired tokens should throw ExpiredJwtException, which means they're invalid
        try {
            boolean isValid = jwtService.isTokenValid(expiredToken, userDetails);
            assertFalse(isValid);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // Expected behavior - expired tokens should throw exception
            assertTrue(true);
        }
    }

    @Test
    void isTokenValid_WithoutUserDetails_ValidToken_ReturnsTrue() {
        // Given
        String token = jwtService.generateToken(userDetails);

        // When
        boolean isValid = jwtService.isTokenValid(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void isTokenValid_WithoutUserDetails_ExpiredToken_ReturnsFalse() {
        // Given
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(new Date(System.currentTimeMillis() - 100000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(key)
                .compact();

        // When
        boolean isValid = jwtService.isTokenValid(expiredToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void generatePasswordResetToken_WithEmail_ReturnsToken() {
        // Given
        String email = "test@example.com";

        // When
        String token = jwtService.generatePasswordResetToken(email);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void isPasswordResetTokenValid_ValidToken_ReturnsTrue() {
        // Given
        String email = "test@example.com";
        String token = jwtService.generatePasswordResetToken(email);

        // When
        boolean isValid = jwtService.isPasswordResetTokenValid(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void isPasswordResetTokenValid_InvalidToken_ReturnsFalse() {
        // Given
        String regularToken = jwtService.generateToken(userDetails);

        // When
        boolean isValid = jwtService.isPasswordResetTokenValid(regularToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void isPasswordResetTokenValid_ExpiredToken_ReturnsFalse() {
        // Given
        // Create an expired password reset token manually
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "password_reset");
        String expiredToken = Jwts.builder()
                .setClaims(claims)
                .setSubject("test@example.com")
                .setIssuedAt(new Date(System.currentTimeMillis() - 7200000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(key)
                .compact();

        // When
        boolean isValid = jwtService.isPasswordResetTokenValid(expiredToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void extractEmailFromResetToken_ValidToken_ReturnsEmail() {
        // Given
        String email = "test@example.com";
        String token = jwtService.generatePasswordResetToken(email);

        // When
        String extractedEmail = jwtService.extractEmailFromResetToken(token);

        // Then
        assertEquals(email, extractedEmail);
    }

    @Test
    void extractClaim_ValidToken_ReturnsClaim() {
        // Given
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ADMIN");
        String token = jwtService.generateToken(extraClaims, userDetails);

        // When
        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));

        // Then
        assertEquals("ADMIN", role);
    }

    @Test
    void tokenExpiration_AccessToken_ExpiresAfter24Hours() {
        // Given
        String token = jwtService.generateToken(userDetails);

        // When
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();

        Date expiration = claims.getExpiration();
        Date issuedAt = claims.getIssuedAt();
        long expirationTime = expiration.getTime() - issuedAt.getTime();

        // Then
        assertTrue(expirationTime >= jwtExpiration - 1000 && expirationTime <= jwtExpiration + 1000);
    }

    @Test
    void tokenExpiration_RefreshToken_ExpiresAfter7Days() {
        // Given
        String token = jwtService.generateRefreshToken(userDetails);

        // When
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();

        Date expiration = claims.getExpiration();
        Date issuedAt = claims.getIssuedAt();
        long expirationTime = expiration.getTime() - issuedAt.getTime();

        // Then
        assertTrue(expirationTime >= refreshExpiration - 1000 && expirationTime <= refreshExpiration + 1000);
    }

    @Test
    void passwordResetToken_ContainsCorrectType() {
        // Given
        String email = "test@example.com";
        String token = jwtService.generatePasswordResetToken(email);

        // When
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();

        String type = claims.get("type", String.class);

        // Then
        assertEquals("password_reset", type);
    }

    @Test
    void passwordResetToken_ExpiresAfter1Hour() {
        // Given
        String email = "test@example.com";
        String token = jwtService.generatePasswordResetToken(email);

        // When
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();

        Date expiration = claims.getExpiration();
        Date issuedAt = claims.getIssuedAt();
        long expirationTime = expiration.getTime() - issuedAt.getTime();
        long oneHour = 3600000L; // 1 hour in milliseconds

        // Then
        assertTrue(expirationTime >= oneHour - 1000 && expirationTime <= oneHour + 1000);
    }
}

