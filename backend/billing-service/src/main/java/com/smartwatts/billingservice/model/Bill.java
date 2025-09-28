package com.smartwatts.billingservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bills")
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "bill_number", nullable = false, unique = true)
    private String billNumber;

    @Column(name = "bill_title")
    private String billTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "bill_type", nullable = false)
    private BillType billType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BillStatus status = BillStatus.PENDING;

    @Column(name = "billing_period_start", nullable = false)
    private LocalDateTime billingPeriodStart;

    @Column(name = "billing_period_end", nullable = false)
    private LocalDateTime billingPeriodEnd;

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Column(name = "issued_date", nullable = false)
    private LocalDateTime issuedDate;

    @Column(name = "paid_date")
    private LocalDateTime paidDate;

    @Column(name = "total_consumption_kwh", precision = 10, scale = 4)
    private BigDecimal totalConsumptionKwh;

    @Column(name = "consumption_kwh", precision = 10, scale = 4)
    private BigDecimal consumptionKwh;

    @Column(name = "rate_per_kwh", precision = 8, scale = 4)
    private BigDecimal ratePerKwh;

    @Column(name = "base_amount", precision = 12, scale = 2)
    private BigDecimal baseAmount;

    @Column(name = "service_charge", precision = 12, scale = 2)
    private BigDecimal serviceCharge;

    @Column(name = "vat_amount", precision = 12, scale = 2)
    private BigDecimal vatAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "energy_source")
    private EnergySource energySource;

    @Column(name = "total_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "tax_amount", precision = 12, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "final_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal finalAmount;

    @Column(name = "amount_paid", precision = 12, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "balance_due", precision = 12, scale = 2)
    private BigDecimal balanceDue;

    @Column(name = "currency", nullable = false)
    private String currency = "NGN";

    @Column(name = "exchange_rate", precision = 8, scale = 4)
    private BigDecimal exchangeRate = BigDecimal.ONE;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "disco_reference")
    private String discoReference;

    @Column(name = "meter_number")
    private String meterNumber;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_address", columnDefinition = "TEXT")
    private String customerAddress;

    @Column(name = "customer_phone")
    private String customerPhone;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "billing_address", columnDefinition = "TEXT")
    private String billingAddress;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "terms_conditions", columnDefinition = "TEXT")
    private String termsConditions;

    @Column(name = "is_recurring")
    private Boolean isRecurring = false;

    @Column(name = "recurring_frequency")
    private String recurringFrequency;

    @Column(name = "next_billing_date")
    private LocalDateTime nextBillingDate;

    @Column(name = "is_estimated")
    private Boolean isEstimated = false;

    @Column(name = "estimation_reason")
    private String estimationReason;

    @Column(name = "is_disputed")
    private Boolean isDisputed = false;

    @Column(name = "dispute_reason")
    private String disputeReason;

    @Column(name = "dispute_date")
    private LocalDateTime disputeDate;

    @Column(name = "dispute_resolved_date")
    private LocalDateTime disputeResolvedDate;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON string for additional billing data

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum BillType {
        GRID_ELECTRICITY,
        SOLAR_GENERATION,
        GENERATOR_FUEL,
        HYBRID_SYSTEM,
        TOKEN_PURCHASE,
        MAINTENANCE_FEE,
        SERVICE_FEE,
        CUSTOM_BILL
    }

    public enum BillStatus {
        DRAFT,
        PENDING,
        ISSUED,
        PAID,
        OVERDUE,
        CANCELLED,
        DISPUTED,
        REFUNDED
    }

    public enum EnergySource {
        GRID,
        SOLAR,
        GENERATOR,
        BATTERY,
        HYBRID
    }
} 