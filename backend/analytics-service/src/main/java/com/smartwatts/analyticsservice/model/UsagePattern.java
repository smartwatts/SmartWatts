package com.smartwatts.analyticsservice.model;

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
@Table(name = "usage_patterns")
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class UsagePattern {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "device_id")
    private UUID deviceId;

    @Column(name = "pattern_date", nullable = false)
    private LocalDateTime patternDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "pattern_type", nullable = false)
    private PatternType patternType;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "duration_hours", precision = 5, scale = 2)
    private BigDecimal durationHours;

    @Column(name = "total_consumption_kwh", precision = 10, scale = 4)
    private BigDecimal totalConsumptionKwh;

    @Column(name = "average_power_kw", precision = 8, scale = 4)
    private BigDecimal averagePowerKw;

    @Column(name = "peak_power_kw", precision = 8, scale = 4)
    private BigDecimal peakPowerKw;

    @Column(name = "total_cost", precision = 10, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "frequency_count")
    private Integer frequencyCount;

    @Column(name = "frequency_percentage", precision = 5, scale = 2)
    private BigDecimal frequencyPercentage;

    @Column(name = "confidence_score", precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    @Column(name = "is_anomaly")
    private Boolean isAnomaly = false;

    @Column(name = "anomaly_score", precision = 5, scale = 2)
    private BigDecimal anomalyScore;

    @Column(name = "pattern_description", columnDefinition = "TEXT")
    private String patternDescription;

    @Column(name = "category")
    private String category;

    @Column(name = "subcategory")
    private String subcategory;

    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags; // JSON array of tags

    @Column(name = "seasonal_factor", precision = 5, scale = 2)
    private BigDecimal seasonalFactor;

    @Column(name = "weather_correlation", precision = 5, scale = 2)
    private BigDecimal weatherCorrelation;

    @Column(name = "occupancy_correlation", precision = 5, scale = 2)
    private BigDecimal occupancyCorrelation;

    @Column(name = "efficiency_rating", precision = 5, scale = 2)
    private BigDecimal efficiencyRating;

    @Column(name = "optimization_potential", precision = 5, scale = 2)
    private BigDecimal optimizationPotential;

    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON string for additional pattern data

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

    public enum PatternType {
        DAILY_RHYTHM,
        WEEKLY_PATTERN,
        MONTHLY_TREND,
        SEASONAL_VARIATION,
        PEAK_USAGE,
        OFF_PEAK_USAGE,
        NIGHT_USAGE,
        WEEKEND_PATTERN,
        HOLIDAY_PATTERN,
        ANOMALOUS_USAGE,
        EFFICIENT_USAGE,
        INEFFICIENT_USAGE,
        CUSTOM_PATTERN
    }
} 