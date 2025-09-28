package com.smartwatts.billingservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Token {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "device_id")
    private UUID deviceId;
    
    @Column(name = "token_code", nullable = false, unique = true)
    private String tokenCode;
    
    @Column(name = "meter_number", nullable = false)
    private String meterNumber;
    
    @Column(name = "amount_paid", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountPaid;
    
    @Column(name = "units_purchased", nullable = false, precision = 10, scale = 3)
    private BigDecimal unitsPurchased;
    
    @Column(name = "units_consumed", nullable = false, precision = 10, scale = 3)
    private BigDecimal unitsConsumed;
    
    @Column(name = "units_remaining", nullable = false, precision = 10, scale = 3)
    private BigDecimal unitsRemaining;
    
    @Column(name = "rate_per_unit", nullable = false, precision = 10, scale = 4)
    private BigDecimal ratePerUnit;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TokenStatus status;
    
    @Column(name = "purchase_date", nullable = false)
    private LocalDateTime purchaseDate;
    
    @Column(name = "activation_date")
    private LocalDateTime activationDate;
    
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    
    @Column(name = "payment_method")
    private String paymentMethod;
    
    @Column(name = "transaction_reference")
    private String transactionReference;
    
    @Column(name = "disco_reference")
    private String discoReference;
    
    @Column(name = "notes")
    private String notes;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public enum TokenStatus {
        PENDING, ACTIVE, EXPIRED, CONSUMED, CANCELLED
    }
} 