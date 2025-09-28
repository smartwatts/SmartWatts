package com.smartwatts.userservice.dto;

import com.smartwatts.userservice.model.Role;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    
    private UUID id;
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+234[0-9]{10}$", message = "Phone number must be in Nigerian format (+234XXXXXXXXXX)")
    private String phoneNumber;
    
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;
    
    private String password;
    
    private Role.RoleName role;
    
    private boolean isActive;
    
    private boolean isEmailVerified;
    
    private boolean isPhoneVerified;
    
    private String address;
    
    private String city;
    
    private String state;
    
    private String postalCode;
    
    private String country;
    
    private LocalDateTime lastLoginAt;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
} 