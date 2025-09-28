package com.smartwatts.analyticsservice.dto;

import com.smartwatts.analyticsservice.model.UsagePattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsagePatternDto {
    
    private UUID id;
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    private UUID deviceId;
    
    @NotNull(message = "Pattern date is required")
    private LocalDateTime patternDate;
    
    @NotNull(message = "Pattern type is required")
    private UsagePattern.PatternType patternType;
    
    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalDateTime endTime;
    
    @DecimalMin(value = "0.0", message = "Duration must be positive")
    private BigDecimal durationHours;
    
    @DecimalMin(value = "0.0", message = "Total consumption must be positive")
    private BigDecimal totalConsumptionKwh;
    
    @DecimalMin(value = "0.0", message = "Average power must be positive")
    private BigDecimal averagePowerKw;
    
    @DecimalMin(value = "0.0", message = "Peak power must be positive")
    private BigDecimal peakPowerKw;
    
    @DecimalMin(value = "0.0", message = "Total cost must be positive")
    private BigDecimal totalCost;
    
    private Integer frequencyCount;
    
    @DecimalMin(value = "0.0", message = "Frequency percentage must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Frequency percentage must be between 0 and 100")
    private BigDecimal frequencyPercentage;
    
    @DecimalMin(value = "0.0", message = "Confidence score must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Confidence score must be between 0 and 100")
    private BigDecimal confidenceScore;
    
    private Boolean isAnomaly;
    
    @DecimalMin(value = "0.0", message = "Anomaly score must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Anomaly score must be between 0 and 100")
    private BigDecimal anomalyScore;
    
    private String patternDescription;
    
    private String category;
    
    private String subcategory;
    
    private String tags;
    
    @DecimalMin(value = "0.0", message = "Seasonal factor must be positive")
    private BigDecimal seasonalFactor;
    
    @DecimalMin(value = "-1.0", message = "Weather correlation must be between -1 and 1")
    @DecimalMax(value = "1.0", message = "Weather correlation must be between -1 and 1")
    private BigDecimal weatherCorrelation;
    
    @DecimalMin(value = "-1.0", message = "Occupancy correlation must be between -1 and 1")
    @DecimalMax(value = "1.0", message = "Occupancy correlation must be between -1 and 1")
    private BigDecimal occupancyCorrelation;
    
    @DecimalMin(value = "0.0", message = "Efficiency rating must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Efficiency rating must be between 0 and 100")
    private BigDecimal efficiencyRating;
    
    @DecimalMin(value = "0.0", message = "Optimization potential must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Optimization potential must be between 0 and 100")
    private BigDecimal optimizationPotential;
    
    private String recommendations;
    
    private String metadata;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
} 