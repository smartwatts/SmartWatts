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
public class DeviceActivationResponse {

    private boolean success;
    private String message;
    private String deviceId;
    private String activationToken;
    private LocalDateTime activatedAt;
    private LocalDateTime expiresAt;
    private int validityDays;
    private String customerType;
    private DeviceTrustCategory trustCategory;
    private String deviceType;
    private String hardwareId;
    private String installerId;
    
    // Additional response data
    private String tokenType; // ONLINE or OFFLINE
    private String renewalInstructions;
    private String supportContact;
}
