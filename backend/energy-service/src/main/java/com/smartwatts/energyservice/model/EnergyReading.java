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
@Table(name = "energy_readings")
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class EnergyReading {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "meter_number")
    private String meterNumber;

    @Column(name = "reading_timestamp", nullable = false)
    private LocalDateTime readingTimestamp;

    @Column(name = "voltage", precision = 10, scale = 2)
    private BigDecimal voltage;

    @Column(name = "current", precision = 10, scale = 2)
    private BigDecimal current;

    @Column(name = "power", precision = 10, scale = 2)
    private BigDecimal power;

    @Column(name = "energy_consumed", precision = 10, scale = 4)
    private BigDecimal energyConsumed;

    @Column(name = "frequency", precision = 5, scale = 2)
    private BigDecimal frequency;

    @Column(name = "power_factor", precision = 3, scale = 2)
    private BigDecimal powerFactor;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private EnergySource sourceType = EnergySource.GRID;

    @Enumerated(EnumType.STRING)
    @Column(name = "reading_type", nullable = false)
    private ReadingType readingType = ReadingType.REAL_TIME;

    @Column(name = "quality_score", precision = 3, scale = 2)
    private BigDecimal qualityScore;

    @Column(name = "is_processed")
    private Boolean isProcessed = false;

    @Column(name = "processing_timestamp")
    private LocalDateTime processingTimestamp;

    @Column(name = "raw_data", columnDefinition = "TEXT")
    private String rawData;

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

    public enum EnergySource {
        GRID, SOLAR, GENERATOR, BATTERY, HYBRID
    }

    public enum ReadingType {
        REAL_TIME, CUMULATIVE, PEAK, AVERAGE, MINIMUM
    }
} 