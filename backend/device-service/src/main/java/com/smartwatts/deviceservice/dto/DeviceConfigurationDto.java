package com.smartwatts.deviceservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceConfigurationDto {
    
    private UUID id;
    
    @NotNull(message = "Device ID is required")
    private UUID deviceId;
    
    @NotBlank(message = "Configuration key is required")
    private String configKey;
    
    private String configValue;
    
    @NotBlank(message = "Data type is required")
    private String dataType;
    
    private String description;
    
    private Boolean isRequired;
    
    private Boolean isEncrypted;
    
    private String defaultValue;
    
    @DecimalMin(value = "0.0", message = "Min value must be positive")
    private BigDecimal minValue;
    
    @DecimalMin(value = "0.0", message = "Max value must be positive")
    private BigDecimal maxValue;
    
    private String validationRegex;
    
    private Boolean isActive;
    
    private LocalDateTime lastUpdated;
    
    private UUID updatedBy;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
} 