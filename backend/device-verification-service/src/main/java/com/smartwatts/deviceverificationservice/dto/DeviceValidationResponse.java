package com.smartwatts.deviceverificationservice.dto;

import com.smartwatts.deviceverificationservice.model.DeviceTrustCategory;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceValidationResponse {

    private boolean valid;
    private String message;
    private String deviceId;
    private String customerType;
    private DeviceTrustCategory trustCategory;
    private LocalDateTime expiresAt;
    private String deviceType;
    private String hardwareId;
    
    // Additional validation data
    private String validationCode;
    private LocalDateTime validatedAt;
    private String ipAddress;
    private String userAgent;
    private boolean requiresRenewal;
    private int daysUntilExpiry;
}
