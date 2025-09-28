package com.smartwatts.energyservice.dto;

import com.smartwatts.energyservice.model.EnergyAlert;
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
public class EnergyAlertDto {
    
    private UUID id;
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    private String deviceId;
    
    @NotNull(message = "Alert type is required")
    private EnergyAlert.AlertType alertType;
    
    private EnergyAlert.Severity severity;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Message is required")
    private String message;
    
    @DecimalMin(value = "0.0", message = "Threshold value must be positive")
    private BigDecimal thresholdValue;
    
    @DecimalMin(value = "0.0", message = "Actual value must be positive")
    private BigDecimal actualValue;
    
    @NotNull(message = "Alert timestamp is required")
    private LocalDateTime alertTimestamp;
    
    private Boolean isAcknowledged;
    
    private LocalDateTime acknowledgedAt;
    
    private UUID acknowledgedBy;
    
    private Boolean isResolved;
    
    private LocalDateTime resolvedAt;
    
    private UUID resolvedBy;
    
    private String resolutionNotes;
    
    private Boolean notificationSent;
    
    private LocalDateTime notificationSentAt;
    
    private String notificationChannels;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
} 