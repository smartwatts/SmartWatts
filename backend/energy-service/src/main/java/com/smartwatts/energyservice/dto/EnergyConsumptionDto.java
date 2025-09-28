package com.smartwatts.energyservice.dto;

import com.smartwatts.energyservice.model.EnergyConsumption;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnergyConsumptionDto {
    
    private UUID id;
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @NotNull(message = "Device ID is required")
    private String deviceId;
    
    private String meterNumber;
    
    @NotNull(message = "Period start is required")
    private LocalDateTime periodStart;
    
    @NotNull(message = "Period end is required")
    private LocalDateTime periodEnd;
    
    @NotNull(message = "Period type is required")
    private EnergyConsumption.PeriodType periodType;
    
    private EnergyConsumption.EnergySource sourceType;
    
    @NotNull(message = "Total energy is required")
    @DecimalMin(value = "0.0", message = "Total energy must be positive")
    private BigDecimal totalEnergy;
    
    @DecimalMin(value = "0.0", message = "Peak power must be positive")
    private BigDecimal peakPower;
    
    @DecimalMin(value = "0.0", message = "Average power must be positive")
    private BigDecimal averagePower;
    
    @DecimalMin(value = "0.0", message = "Minimum power must be positive")
    private BigDecimal minimumPower;
    
    @DecimalMin(value = "0.0", message = "Total cost must be positive")
    private BigDecimal totalCost;
    
    @DecimalMin(value = "0.0", message = "Tariff rate must be positive")
    private BigDecimal tariffRate;
    
    private Integer readingCount;
    
    @DecimalMin(value = "0.0", message = "Quality score must be positive")
    private BigDecimal qualityScore;
    
    private Boolean isBilled;
    
    private String billingReference;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
} 