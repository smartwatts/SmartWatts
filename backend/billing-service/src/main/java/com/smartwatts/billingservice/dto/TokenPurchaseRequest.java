package com.smartwatts.billingservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenPurchaseRequest {
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @NotBlank(message = "Token amount is required")
    private String tokenAmount;
    
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
    
    @NotBlank(message = "DisCo is required")
    private String disco;
    
    private String meterNumber;
    
    private String accountNumber;
    
    private String customerName;
    
    private String customerPhone;
    
    private String customerEmail;
    
    private String vendingAgent;
    
    private String transactionReference;
}









