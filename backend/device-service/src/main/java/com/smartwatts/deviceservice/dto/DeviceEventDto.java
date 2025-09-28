package com.smartwatts.deviceservice.dto;

import com.smartwatts.deviceservice.model.DeviceEvent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceEventDto {
    
    private UUID id;
    
    @NotNull(message = "Device ID is required")
    private UUID deviceId;
    
    @NotNull(message = "Event type is required")
    private DeviceEvent.EventType eventType;
    
    private DeviceEvent.Severity severity;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Message is required")
    private String message;
    
    @NotNull(message = "Event timestamp is required")
    private LocalDateTime eventTimestamp;
    
    private String source;
    
    private String errorCode;
    
    private String errorMessage;
    
    private String stackTrace;
    
    private String metadata;
    
    private Boolean isAcknowledged;
    
    private LocalDateTime acknowledgedAt;
    
    private UUID acknowledgedBy;
    
    private Boolean isResolved;
    
    private LocalDateTime resolvedAt;
    
    private UUID resolvedBy;
    
    private String resolutionNotes;
    
    private Boolean notificationSent;
    
    private LocalDateTime notificationSentAt;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
} 