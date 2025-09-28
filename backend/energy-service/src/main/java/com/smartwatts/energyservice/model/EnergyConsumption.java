package com.smartwatts.energyservice.model;

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
@Table(name = "energy_consumption")
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class EnergyConsumption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "meter_number")
    private String meterNumber;

    @Column(name = "period_start", nullable = false)
    private LocalDateTime periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDateTime periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false)
    private PeriodType periodType;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private EnergySource sourceType = EnergySource.GRID;

    @Column(name = "total_energy", precision = 12, scale = 4, nullable = false)
    private BigDecimal totalEnergy;

    @Column(name = "peak_power", precision = 10, scale = 2)
    private BigDecimal peakPower;

    @Column(name = "average_power", precision = 10, scale = 2)
    private BigDecimal averagePower;

    @Column(name = "minimum_power", precision = 10, scale = 2)
    private BigDecimal minimumPower;

    @Column(name = "total_cost", precision = 10, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "tariff_rate", precision = 8, scale = 4)
    private BigDecimal tariffRate;

    @Column(name = "reading_count")
    private Integer readingCount;

    @Column(name = "quality_score", precision = 3, scale = 2)
    private BigDecimal qualityScore;

    @Column(name = "is_billed")
    private Boolean isBilled = false;

    @Column(name = "billing_reference")
    private String billingReference;

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

    public enum PeriodType {
        MINUTE, HOUR, DAY, WEEK, MONTH, YEAR
    }

    public enum EnergySource {
        GRID, SOLAR, GENERATOR, BATTERY, HYBRID
    }
} 