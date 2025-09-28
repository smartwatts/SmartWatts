package com.smartwatts.analyticsservice.dto;

import com.smartwatts.analyticsservice.model.EnergyAnalytics;
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
public class EnergyAnalyticsDto {
    
    private UUID id;
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    private UUID deviceId;
    
    @NotNull(message = "Analytics date is required")
    private LocalDateTime analyticsDate;
    
    @NotNull(message = "Period type is required")
    private EnergyAnalytics.PeriodType periodType;
    
    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalDateTime endTime;
    
    @DecimalMin(value = "0.0", message = "Total consumption must be positive")
    private BigDecimal totalConsumptionKwh;
    
    @DecimalMin(value = "0.0", message = "Peak consumption must be positive")
    private BigDecimal peakConsumptionKw;
    
    @DecimalMin(value = "0.0", message = "Average consumption must be positive")
    private BigDecimal averageConsumptionKw;
    
    @DecimalMin(value = "0.0", message = "Total cost must be positive")
    private BigDecimal totalCost;
    
    @DecimalMin(value = "0.0", message = "Cost per kWh must be positive")
    private BigDecimal costPerKwh;
    
    @DecimalMin(value = "0.0", message = "Efficiency score must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Efficiency score must be between 0 and 100")
    private BigDecimal efficiencyScore;
    
    @DecimalMin(value = "0.0", message = "Carbon footprint must be positive")
    private BigDecimal carbonFootprintKg;
    
    private Integer peakHoursCount;
    
    private Integer offPeakHoursCount;
    
    private Integer nightHoursCount;
    
    @DecimalMin(value = "0.0", message = "Peak consumption must be positive")
    private BigDecimal peakConsumptionKwh;
    
    @DecimalMin(value = "0.0", message = "Off-peak consumption must be positive")
    private BigDecimal offPeakConsumptionKwh;
    
    @DecimalMin(value = "0.0", message = "Night consumption must be positive")
    private BigDecimal nightConsumptionKwh;
    
    @DecimalMin(value = "0.0", message = "Peak cost must be positive")
    private BigDecimal peakCost;
    
    @DecimalMin(value = "0.0", message = "Off-peak cost must be positive")
    private BigDecimal offPeakCost;
    
    @DecimalMin(value = "0.0", message = "Night cost must be positive")
    private BigDecimal nightCost;
    
    @DecimalMin(value = "0.0", message = "Savings potential must be positive")
    private BigDecimal savingsPotential;
    
    private String optimizationRecommendations;
    
    private Integer anomalyCount;
    
    @DecimalMin(value = "0.0", message = "Quality score must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Quality score must be between 0 and 100")
    private BigDecimal qualityScore;
    
    private Integer dataPointsCount;
    
    @DecimalMin(value = "0.0", message = "Completeness percentage must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Completeness percentage must be between 0 and 100")
    private BigDecimal completenessPercentage;
    
    private String metadata;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
} 