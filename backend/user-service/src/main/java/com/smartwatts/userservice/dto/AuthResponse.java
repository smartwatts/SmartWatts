package com.smartwatts.userservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    
    private UUID userId;
    
    private String username;
    
    private String email;
    
    private String accessToken;
    
    private String refreshToken;
    
    private String tokenType;
    
    private LocalDateTime expiresAt;
    
    private String role;
    
    private boolean isActive;
} 