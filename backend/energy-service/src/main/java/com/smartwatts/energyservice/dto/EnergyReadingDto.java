package com.smartwatts.energyservice.dto;

import com.smartwatts.energyservice.model.EnergyReading;
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
public class EnergyReadingDto {
    
    private UUID id;
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @NotNull(message = "Device ID is required")
    private String deviceId;
    
    private String meterNumber;
    
    @NotNull(message = "Reading timestamp is required")
    private LocalDateTime readingTimestamp;
    
    @DecimalMin(value = "0.0", message = "Voltage must be positive")
    @DecimalMax(value = "500.0", message = "Voltage must be reasonable")
    private BigDecimal voltage;
    
    @DecimalMin(value = "0.0", message = "Current must be positive")
    @DecimalMax(value = "1000.0", message = "Current must be reasonable")
    private BigDecimal current;
    
    @DecimalMin(value = "0.0", message = "Power must be positive")
    private BigDecimal power;
    
    @DecimalMin(value = "0.0", message = "Energy consumed must be positive")
    private BigDecimal energyConsumed;
    
    @DecimalMin(value = "0.0", message = "Frequency must be positive")
    @DecimalMax(value = "100.0", message = "Frequency must be reasonable")
    private BigDecimal frequency;
    
    @DecimalMin(value = "0.0", message = "Power factor must be positive")
    @DecimalMax(value = "1.0", message = "Power factor cannot exceed 1.0")
    private BigDecimal powerFactor;
    
    private EnergyReading.EnergySource sourceType;
    
    private EnergyReading.ReadingType readingType;
    
    @DecimalMin(value = "0.0", message = "Quality score must be positive")
    @DecimalMax(value = "1.0", message = "Quality score cannot exceed 1.0")
    private BigDecimal qualityScore;
    
    private Boolean isProcessed;
    
    private LocalDateTime processingTimestamp;
    
    private String rawData;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
} 