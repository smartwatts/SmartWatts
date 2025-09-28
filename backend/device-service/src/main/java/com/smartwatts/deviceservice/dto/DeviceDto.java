package com.smartwatts.deviceservice.dto;

import com.smartwatts.deviceservice.model.Device;
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
public class DeviceDto {
    
    private UUID id;
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @NotBlank(message = "Device ID is required")
    private String deviceId;
    
    @NotBlank(message = "Device name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Device type is required")
    private Device.DeviceType deviceType;
    
    private Device.Protocol protocol;
    
    private String connectionString;
    
    private String ipAddress;
    
    private Integer port;
    
    private String username;
    
    private String password;
    
    private String mqttTopic;
    
    private Integer modbusAddress;
    
    private Integer modbusRegisterStart;
    
    private Integer modbusRegisterCount;
    
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private BigDecimal locationLat;
    
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private BigDecimal locationLng;
    
    private LocalDateTime installationDate;
    
    private LocalDateTime lastMaintenanceDate;
    
    private LocalDateTime nextMaintenanceDate;
    
    private Device.DeviceStatus status;
    
    private Device.ConnectionStatus connectionStatus;
    
    private LocalDateTime lastSeen;
    
    private String firmwareVersion;
    
    private String hardwareVersion;
    
    private String manufacturer;
    
    private String model;
    
    private String serialNumber;
    
    private LocalDateTime warrantyExpiry;
    
    private Boolean isCalibrated;
    
    private LocalDateTime calibrationDate;
    
    private LocalDateTime calibrationExpiry;
    
    @DecimalMin(value = "0.0", message = "Accuracy percentage must be positive")
    @DecimalMax(value = "100.0", message = "Accuracy percentage cannot exceed 100")
    private BigDecimal accuracyPercentage;
    
    @DecimalMin(value = "0.0", message = "Max voltage must be positive")
    private BigDecimal maxVoltage;
    
    @DecimalMin(value = "0.0", message = "Max current must be positive")
    private BigDecimal maxCurrent;
    
    @DecimalMin(value = "0.0", message = "Max power must be positive")
    private BigDecimal maxPower;
    
    private Boolean isVerified;
    
    private LocalDateTime verificationDate;
    
    private UUID verificationBy;

    private String trustLevel;
    
    private String deviceAuthSecret;
    
    private String verificationStatus;
    
    private String verificationNotes;
    
    private String samplePayload;
    
    private LocalDateTime verificationRequestDate;
    
    private LocalDateTime verificationReviewDate;
    
    private UUID verificationReviewer;
    
    private String notes;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
} 