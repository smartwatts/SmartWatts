package com.smartwatts.deviceverificationservice.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceActivationRequest {

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotBlank(message = "Device type is required")
    private String deviceType;

    @NotBlank(message = "Hardware ID is required")
    private String hardwareId;

    private String firmwareHash;
    private String firmwareVersion;

    @NotNull(message = "Customer type is required")
    private String customerType; // RESIDENTIAL or COMMERCIAL

    private UUID customerId;
    private String installerId;
    private BigDecimal locationLat;
    private BigDecimal locationLng;
    @Builder.Default
    private boolean offlineActivation = false;

    // Additional metadata for verification
    private String macAddress;
    private String serialNumber;
    private String modelNumber;
    private String manufacturer;
    private String firmwareChecksum;
    private String dockerImageHash;
}
