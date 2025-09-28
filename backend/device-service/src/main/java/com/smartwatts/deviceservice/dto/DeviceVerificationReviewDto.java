package com.smartwatts.deviceservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class DeviceVerificationReviewDto {
    
    @NotNull(message = "Device ID is required")
    private UUID deviceId;
    
    @NotNull(message = "Verification status is required")
    private String verificationStatus; // APPROVED, REJECTED, SUSPENDED
    
    private String notes;
    
    @NotNull(message = "Reviewer ID is required")
    private UUID reviewerId;
}
