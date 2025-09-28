package com.smartwatts.analyticsservice.model;

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
@Table(name = "energy_predictions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class EnergyPrediction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "prediction_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private PredictionType predictionType;
    
    @Column(name = "prediction_horizon", nullable = false)
    @Enumerated(EnumType.STRING)
    private PredictionHorizon predictionHorizon;
    
    @Column(name = "predicted_consumption_kwh", nullable = false, precision = 10, scale = 3)
    private BigDecimal predictedConsumptionKwh;
    
    @Column(name = "predicted_cost_ngn", precision = 12, scale = 2)
    private BigDecimal predictedCostNgn;
    
    @Column(name = "confidence_interval_lower", precision = 10, scale = 3)
    private BigDecimal confidenceIntervalLower;
    
    @Column(name = "confidence_interval_upper", precision = 10, scale = 3)
    private BigDecimal confidenceIntervalUpper;
    
    @Column(name = "confidence_level", precision = 3, scale = 2)
    private BigDecimal confidenceLevel;
    
    @Column(name = "model_version")
    private String modelVersion;
    
    @Column(name = "model_accuracy", precision = 3, scale = 2)
    private BigDecimal modelAccuracy;
    
    @Column(name = "prediction_date", nullable = false)
    private LocalDateTime predictionDate;
    
    @Column(name = "target_date", nullable = false)
    private LocalDateTime targetDate;
    
    @Column(name = "weather_conditions")
    private String weatherConditions; // JSON object with weather data
    
    @Column(name = "seasonal_factors")
    private String seasonalFactors; // JSON object with seasonal data
    
    @Column(name = "behavioral_factors")
    private String behavioralFactors; // JSON object with behavioral data
    
    @Column(name = "external_factors")
    private String externalFactors; // JSON object with external data
    
    @Column(name = "is_accurate")
    private Boolean isAccurate; // Null until actual data is available
    
    @Column(name = "actual_consumption_kwh", precision = 10, scale = 3)
    private BigDecimal actualConsumptionKwh;
    
    @Column(name = "actual_cost_ngn", precision = 12, scale = 2)
    private BigDecimal actualCostNgn;
    
    @Column(name = "prediction_error", precision = 10, scale = 3)
    private BigDecimal predictionError;
    
    @Column(name = "error_percentage", precision = 5, scale = 2)
    private BigDecimal errorPercentage;
    
    @Column(name = "notes")
    private String notes;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public enum PredictionType {
        CONSUMPTION, COST, PEAK_DEMAND, EFFICIENCY, MAINTENANCE
    }
    
    public enum PredictionHorizon {
        HOURLY, DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
    }
} 