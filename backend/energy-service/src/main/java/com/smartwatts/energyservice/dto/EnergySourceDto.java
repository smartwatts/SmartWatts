package com.smartwatts.energyservice.dto;

import com.smartwatts.energyservice.model.EnergySource;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnergySourceDto {
    
    private UUID id;
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @NotBlank(message = "Source name is required")
    private String sourceName;
    
    @NotNull(message = "Source type is required")
    private EnergySource.SourceType sourceType;
    
    @DecimalMin(value = "0.001", message = "Capacity must be at least 0.001 kW")
    @DecimalMax(value = "999999.999", message = "Capacity cannot exceed 999,999.999 kW")
    private BigDecimal capacityKw;
    
    @DecimalMin(value = "0.0", message = "Efficiency must be non-negative")
    @DecimalMax(value = "100.0", message = "Efficiency cannot exceed 100%")
    private BigDecimal efficiencyPercent;
    
    private LocalDateTime installationDate;
    
    private LocalDateTime lastMaintenanceDate;
    
    private LocalDateTime nextMaintenanceDate;
    
    private EnergySource.Status status;
    
    private BigDecimal locationLat;
    
    private BigDecimal locationLng;
    
    private String manufacturer;
    
    private String model;
    
    private String serialNumber;
    
    private LocalDateTime warrantyExpiry;
    
    private String notes;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
} 