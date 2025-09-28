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
@Table(name = "tariffs")
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class Tariff {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tariff_name", nullable = false)
    private String tariffName;

    @Column(name = "tariff_code", nullable = false, unique = true)
    private String tariffCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "tariff_type", nullable = false)
    private TariffType tariffType;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_category", nullable = false)
    private CustomerCategory customerCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "energy_source", nullable = false)
    private EnergySource energySource;

    @Column(name = "effective_date", nullable = false)
    private LocalDateTime effectiveDate;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_approved", nullable = false)
    private Boolean isApproved = false;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TariffStatus status = TariffStatus.DRAFT;

    @Column(name = "vat_rate", precision = 5, scale = 2)
    private BigDecimal vatRate;

    @Column(name = "approved_by_authority")
    private String approvedByAuthority;

    @Column(name = "approval_reference")
    private String approvalReference;

    @Column(name = "base_rate", precision = 8, scale = 4)
    private BigDecimal baseRate;

    @Column(name = "peak_rate", precision = 8, scale = 4)
    private BigDecimal peakRate;

    @Column(name = "off_peak_rate", precision = 8, scale = 4)
    private BigDecimal offPeakRate;

    @Column(name = "night_rate", precision = 8, scale = 4)
    private BigDecimal nightRate;

    @Column(name = "service_charge", precision = 12, scale = 2)
    private BigDecimal serviceCharge;

    @Column(name = "meter_rental", precision = 12, scale = 2)
    private BigDecimal meterRental;

    @Column(name = "demand_charge", precision = 12, scale = 2)
    private BigDecimal demandCharge;

    @Column(name = "capacity_charge", precision = 12, scale = 2)
    private BigDecimal capacityCharge;

    @Column(name = "transmission_charge", precision = 12, scale = 2)
    private BigDecimal transmissionCharge;

    @Column(name = "distribution_charge", precision = 12, scale = 2)
    private BigDecimal distributionCharge;

    @Column(name = "regulatory_charge", precision = 12, scale = 2)
    private BigDecimal regulatoryCharge;

    @Column(name = "environmental_charge", precision = 12, scale = 2)
    private BigDecimal environmentalCharge;

    @Column(name = "fuel_adjustment_rate", precision = 8, scale = 4)
    private BigDecimal fuelAdjustmentRate;

    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate;

    @Column(name = "minimum_charge", precision = 12, scale = 2)
    private BigDecimal minimumCharge;

    @Column(name = "maximum_charge", precision = 12, scale = 2)
    private BigDecimal maximumCharge;

    @Column(name = "peak_hours_start")
    private String peakHoursStart;

    @Column(name = "peak_hours_end")
    private String peakHoursEnd;

    @Column(name = "off_peak_hours_start")
    private String offPeakHoursStart;

    @Column(name = "off_peak_hours_end")
    private String offPeakHoursEnd;

    @Column(name = "night_hours_start")
    private String nightHoursStart;

    @Column(name = "night_hours_end")
    private String nightHoursEnd;

    @Column(name = "minimum_consumption_kwh", precision = 10, scale = 4)
    private BigDecimal minimumConsumptionKwh;

    @Column(name = "maximum_consumption_kwh", precision = 10, scale = 4)
    private BigDecimal maximumConsumptionKwh;

    @Column(name = "tier_1_limit", precision = 10, scale = 4)
    private BigDecimal tier1Limit;

    @Column(name = "tier_1_rate", precision = 8, scale = 4)
    private BigDecimal tier1Rate;

    @Column(name = "tier_2_limit", precision = 10, scale = 4)
    private BigDecimal tier2Limit;

    @Column(name = "tier_2_rate", precision = 8, scale = 4)
    private BigDecimal tier2Rate;

    @Column(name = "tier_3_limit", precision = 10, scale = 4)
    private BigDecimal tier3Limit;

    @Column(name = "tier_3_rate", precision = 8, scale = 4)
    private BigDecimal tier3Rate;

    @Column(name = "tier_4_rate", precision = 8, scale = 4)
    private BigDecimal tier4Rate;

    @Column(name = "currency", nullable = false)
    private String currency = "NGN";

    @Column(name = "disco_code")
    private String discoCode;

    @Column(name = "disco_name")
    private String discoName;

    @Column(name = "region")
    private String region;

    @Column(name = "state")
    private String state;

    @Column(name = "city")
    private String city;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON string for additional tariff data

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

    public enum TariffType {
        RESIDENTIAL,
        COMMERCIAL,
        INDUSTRIAL,
        AGRICULTURAL,
        SPECIAL_LOAD,
        STREET_LIGHTING,
        BULK_SUPPLY,
        EMBASSY,
        MILITARY,
        CUSTOM
    }

    public enum CustomerCategory {
        R1_SMALL_RESIDENTIAL,
        R2_MEDIUM_RESIDENTIAL,
        R3_LARGE_RESIDENTIAL,
        C1_SMALL_COMMERCIAL,
        C2_MEDIUM_COMMERCIAL,
        C3_LARGE_COMMERCIAL,
        D1_SMALL_INDUSTRIAL,
        D2_MEDIUM_INDUSTRIAL,
        D3_LARGE_INDUSTRIAL,
        A1_AGRICULTURAL,
        S1_SPECIAL_LOAD,
        E1_EMBASSY,
        M1_MILITARY,
        B1_BULK_SUPPLY,
        ST1_STREET_LIGHTING
    }

    public enum TariffStatus {
        DRAFT,
        PENDING_APPROVAL,
        APPROVED,
        ACTIVE,
        EXPIRED,
        SUSPENDED,
        REJECTED
    }

    public enum CustomerType {
        RESIDENTIAL,
        COMMERCIAL,
        INDUSTRIAL,
        AGRICULTURAL,
        SPECIAL_LOAD,
        STREET_LIGHTING,
        BULK_SUPPLY,
        EMBASSY,
        MILITARY
    }

    public enum EnergySource {
        GRID,
        SOLAR,
        GENERATOR,
        BATTERY,
        HYBRID
    }
} 