package com.smartwatts.deviceservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class DeviceVerificationRequestDto {
    
    @NotNull(message = "Device ID is required")
    private UUID deviceId;
    
    @NotBlank(message = "Sample payload is required")
    private String samplePayload;
    
    private String notes;
    
    private String brand;
    
    private String model;
    
    private String preferredProtocol;
}
