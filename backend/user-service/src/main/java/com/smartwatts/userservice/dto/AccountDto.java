package com.smartwatts.userservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    
    private UUID id;
    
    @NotBlank(message = "Account name is required")
    private String name;
    
    @NotNull(message = "Account type is required")
    private AccountType type;
    
    @NotNull(message = "Account status is required")
    private AccountStatus status;
    
    @NotBlank(message = "Contact person is required")
    private String contactPerson;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Valid email is required")
    private String email;
    
    private String phone;
    
    private String address;
    private String city;
    private String state;
    
    private String subscriptionPlan;
    
    @PositiveOrZero(message = "Monthly revenue must be positive or zero")
    private Double monthlyRevenue;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastPayment;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate nextBilling;
    
    @PositiveOrZero(message = "Device count must be positive or zero")
    private Integer devices;
    
    @PositiveOrZero(message = "Energy savings must be positive or zero")
    private Double energySavings;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    public enum AccountType {
        ENTERPRISE, SME, RESIDENTIAL
    }
    
    public enum AccountStatus {
        ACTIVE, SUSPENDED, PENDING, CANCELLED
    }
}

