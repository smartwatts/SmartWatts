package com.smartwatts.billingservice.dto;

import com.smartwatts.billingservice.model.Bill;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillDto {
    
    private UUID id;
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @NotBlank(message = "Bill number is required")
    private String billNumber;
    
    private String billTitle;
    
    @NotNull(message = "Bill type is required")
    private Bill.BillType billType;
    
    private Bill.BillStatus status;
    
    @NotNull(message = "Billing period start is required")
    private LocalDateTime billingPeriodStart;
    
    @NotNull(message = "Billing period end is required")
    private LocalDateTime billingPeriodEnd;
    
    @NotNull(message = "Due date is required")
    private LocalDateTime dueDate;
    
    private LocalDateTime issuedDate;
    
    private LocalDateTime paidDate;
    
    @DecimalMin(value = "0.0", message = "Total consumption must be positive")
    private BigDecimal totalConsumptionKwh;
    
    @DecimalMin(value = "0.0", message = "Total amount must be positive")
    private BigDecimal totalAmount;
    
    @DecimalMin(value = "0.0", message = "Tax amount must be positive")
    private BigDecimal taxAmount;
    
    @DecimalMin(value = "0.0", message = "Discount amount must be positive")
    private BigDecimal discountAmount;
    
    @DecimalMin(value = "0.0", message = "Final amount must be positive")
    private BigDecimal finalAmount;
    
    @DecimalMin(value = "0.0", message = "Amount paid must be positive")
    private BigDecimal amountPaid;
    
    @DecimalMin(value = "0.0", message = "Balance due must be positive")
    private BigDecimal balanceDue;
    
    private String currency;
    
    @DecimalMin(value = "0.0", message = "Exchange rate must be positive")
    private BigDecimal exchangeRate;
    
    private String paymentMethod;
    
    private String paymentReference;
    
    private String discoReference;
    
    private String meterNumber;
    
    private String accountNumber;
    
    private String customerName;
    
    private String customerAddress;
    
    private String customerPhone;
    
    private String customerEmail;
    
    private String billingAddress;
    
    private String notes;
    
    private String termsConditions;
    
    private Boolean isRecurring;
    
    private String recurringFrequency;
    
    private LocalDateTime nextBillingDate;
    
    private Boolean isEstimated;
    
    private String estimationReason;
    
    private Boolean isDisputed;
    
    private String disputeReason;
    
    private LocalDateTime disputeDate;
    
    private LocalDateTime disputeResolvedDate;
    
    private String metadata;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
} 