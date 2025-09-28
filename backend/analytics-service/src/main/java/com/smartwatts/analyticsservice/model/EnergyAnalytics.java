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
@Table(name = "energy_analytics")
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class EnergyAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "device_id")
    private UUID deviceId;

    @Column(name = "analytics_date", nullable = false)
    private LocalDateTime analyticsDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false)
    private PeriodType periodType;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "total_consumption_kwh", precision = 10, scale = 4)
    private BigDecimal totalConsumptionKwh;

    @Column(name = "peak_consumption_kw", precision = 8, scale = 4)
    private BigDecimal peakConsumptionKw;

    @Column(name = "average_consumption_kw", precision = 8, scale = 4)
    private BigDecimal averageConsumptionKw;

    @Column(name = "total_cost", precision = 10, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "cost_per_kwh", precision = 6, scale = 4)
    private BigDecimal costPerKwh;

    @Column(name = "efficiency_score", precision = 5, scale = 2)
    private BigDecimal efficiencyScore;

    @Column(name = "carbon_footprint_kg", precision = 8, scale = 2)
    private BigDecimal carbonFootprintKg;

    @Column(name = "peak_hours_count")
    private Integer peakHoursCount;

    @Column(name = "off_peak_hours_count")
    private Integer offPeakHoursCount;

    @Column(name = "night_hours_count")
    private Integer nightHoursCount;

    @Column(name = "peak_consumption_kwh", precision = 10, scale = 4)
    private BigDecimal peakConsumptionKwh;

    @Column(name = "off_peak_consumption_kwh", precision = 10, scale = 4)
    private BigDecimal offPeakConsumptionKwh;

    @Column(name = "night_consumption_kwh", precision = 10, scale = 4)
    private BigDecimal nightConsumptionKwh;

    @Column(name = "peak_cost", precision = 10, scale = 2)
    private BigDecimal peakCost;

    @Column(name = "off_peak_cost", precision = 10, scale = 2)
    private BigDecimal offPeakCost;

    @Column(name = "night_cost", precision = 10, scale = 2)
    private BigDecimal nightCost;

    @Column(name = "savings_potential", precision = 10, scale = 2)
    private BigDecimal savingsPotential;

    @Column(name = "optimization_recommendations", columnDefinition = "TEXT")
    private String optimizationRecommendations;

    @Column(name = "anomaly_count")
    private Integer anomalyCount;

    @Column(name = "quality_score", precision = 5, scale = 2)
    private BigDecimal qualityScore;

    @Column(name = "data_points_count")
    private Integer dataPointsCount;

    @Column(name = "completeness_percentage", precision = 5, scale = 2)
    private BigDecimal completenessPercentage;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON string for additional analytics data

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
        HOURLY,
        DAILY,
        WEEKLY,
        MONTHLY,
        QUARTERLY,
        YEARLY,
        CUSTOM
    }
} 