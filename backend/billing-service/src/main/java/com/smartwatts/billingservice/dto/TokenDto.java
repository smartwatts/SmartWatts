package com.smartwatts.billingservice.dto;

import com.smartwatts.billingservice.model.Token;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenDto {
    
    private UUID id;
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    private UUID deviceId;
    
    @NotBlank(message = "Token code is required")
    private String tokenCode;
    
    @NotBlank(message = "Meter number is required")
    private String meterNumber;
    
    @NotNull(message = "Amount paid is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    @DecimalMax(value = "999999.99", message = "Amount cannot exceed 999,999.99")
    private BigDecimal amountPaid;
    
    @NotNull(message = "Units purchased is required")
    @DecimalMin(value = "0.001", message = "Units must be at least 0.001")
    @DecimalMax(value = "999999.999", message = "Units cannot exceed 999,999.999")
    private BigDecimal unitsPurchased;
    
    private BigDecimal unitsConsumed;
    private BigDecimal unitsRemaining;
    
    @NotNull(message = "Rate per unit is required")
    @DecimalMin(value = "0.0001", message = "Rate must be at least 0.0001")
    @DecimalMax(value = "999.9999", message = "Rate cannot exceed 999.9999")
    private BigDecimal ratePerUnit;
    
    private Token.TokenStatus status;
    private LocalDateTime purchaseDate;
    private LocalDateTime activationDate;
    private LocalDateTime expiryDate;
    private String paymentMethod;
    private String transactionReference;
    private String discoReference;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 