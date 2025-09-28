package com.smartwatts.analyticsservice.dto;

import com.smartwatts.analyticsservice.model.EnergyPrediction;
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
public class EnergyPredictionDto {
    
    private UUID id;
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @NotNull(message = "Prediction type is required")
    private EnergyPrediction.PredictionType predictionType;
    
    @NotNull(message = "Prediction horizon is required")
    private EnergyPrediction.PredictionHorizon predictionHorizon;
    
    @NotNull(message = "Predicted consumption is required")
    @DecimalMin(value = "0.001", message = "Predicted consumption must be at least 0.001 kWh")
    private BigDecimal predictedConsumptionKwh;
    
    @DecimalMin(value = "0.0", message = "Predicted cost must be non-negative")
    private BigDecimal predictedCostNgn;
    
    @DecimalMin(value = "0.0", message = "Confidence interval lower must be non-negative")
    private BigDecimal confidenceIntervalLower;
    
    @DecimalMin(value = "0.0", message = "Confidence interval upper must be non-negative")
    private BigDecimal confidenceIntervalUpper;
    
    @DecimalMin(value = "0.0", message = "Confidence level must be non-negative")
    @DecimalMax(value = "1.0", message = "Confidence level cannot exceed 1.0")
    private BigDecimal confidenceLevel;
    
    private String modelVersion;
    
    @DecimalMin(value = "0.0", message = "Model accuracy must be non-negative")
    @DecimalMax(value = "1.0", message = "Model accuracy cannot exceed 1.0")
    private BigDecimal modelAccuracy;
    
    @NotNull(message = "Prediction date is required")
    private LocalDateTime predictionDate;
    
    @NotNull(message = "Target date is required")
    private LocalDateTime targetDate;
    
    private String weatherConditions;
    
    private String seasonalFactors;
    
    private String behavioralFactors;
    
    private String externalFactors;
    
    private Boolean isAccurate;
    
    @DecimalMin(value = "0.0", message = "Actual consumption must be non-negative")
    private BigDecimal actualConsumptionKwh;
    
    @DecimalMin(value = "0.0", message = "Actual cost must be non-negative")
    private BigDecimal actualCostNgn;
    
    private BigDecimal predictionError;
    
    @DecimalMin(value = "0.0", message = "Error percentage must be non-negative")
    @DecimalMax(value = "100.0", message = "Error percentage cannot exceed 100%")
    private BigDecimal errorPercentage;
    
    private String notes;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
} 