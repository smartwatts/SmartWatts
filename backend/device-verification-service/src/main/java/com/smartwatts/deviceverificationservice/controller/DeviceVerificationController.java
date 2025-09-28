package com.smartwatts.deviceverificationservice.controller;

import com.smartwatts.deviceverificationservice.dto.DeviceActivationRequest;
import com.smartwatts.deviceverificationservice.dto.DeviceActivationResponse;
import com.smartwatts.deviceverificationservice.dto.DeviceValidationResponse;
import com.smartwatts.deviceverificationservice.service.DeviceVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/device-verification")
@RequiredArgsConstructor
@Slf4j
public class DeviceVerificationController {

    private final DeviceVerificationService deviceVerificationService;

    /**
     * Device Activation Endpoint
     * POST /api/device-verification/activate
     * 
     * Implements the dual token validity system:
     * - Residential: 12 months initially
     * - Commercial: 3 months initially, 12 months on renewal
     */
    @PostMapping("/activate")
    public ResponseEntity<DeviceActivationResponse> activateDevice(
            @Valid @RequestBody DeviceActivationRequest request) {
        
        log.info("Received device activation request for device: {}", request.getDeviceId());
        
        DeviceActivationResponse response = deviceVerificationService.activateDevice(request);
        
        if (response.isSuccess()) {
            log.info("Device {} activated successfully. Customer type: {}, Validity: {} days", 
                request.getDeviceId(), response.getCustomerType(), response.getValidityDays());
            return ResponseEntity.ok(response);
        } else {
            log.warn("Device {} activation failed: {}", request.getDeviceId(), response.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Device Validation Endpoint
     * POST /api/device-verification/validate
     * 
     * Validates device access for telemetry ingestion or dashboard access
     */
    @PostMapping("/validate")
    public ResponseEntity<DeviceValidationResponse> validateDevice(
            @RequestParam String deviceId,
            @RequestParam String token) {
        
        log.info("Validating device access for device: {}", deviceId);
        
        DeviceValidationResponse response = deviceVerificationService.validateDeviceAccess(deviceId, token);
        
        if (response.isValid()) {
            log.info("Device {} access validated successfully", deviceId);
            return ResponseEntity.ok(response);
        } else {
            log.warn("Device {} access validation failed: {}", deviceId, response.getMessage());
            return ResponseEntity.status(403).body(response);
        }
    }

    /**
     * Device Status Check Endpoint
     * GET /api/device-verification/status/{deviceId}
     */
    @GetMapping("/status/{deviceId}")
    public ResponseEntity<DeviceValidationResponse> getDeviceStatus(@PathVariable String deviceId) {
        log.info("Checking device status for device: {}", deviceId);
        
        // This would return device status without requiring a token
        // Useful for dashboard status displays
        DeviceValidationResponse response = deviceVerificationService.validateDeviceAccess(deviceId, "STATUS_CHECK");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Health Check Endpoint
     * GET /api/device-verification/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Device Verification Service is running");
    }

    /**
     * Service Information Endpoint
     * GET /api/device-verification/info
     */
    @GetMapping("/info")
    public ResponseEntity<Object> getServiceInfo() {
        return ResponseEntity.ok(Map.of(
            "service", "Device Verification & Activation Service",
            "version", "1.0.0",
            "description", "SmartWatts device verification and activation service with dual token validity system",
            "features", List.of(
                "Device activation with dual validity (12 months residential, 3 months commercial initially)",
                "Automatic renewal with 12-month validity for all customers",
                "Trust category management (OEM_LOCKED, OFFLINE_LOCKED, UNVERIFIED)",
                "Tamper detection and audit logging",
                "Docker startup validation",
                "Offline activation support"
            ),
            "tokenValidity", Map.of(
                "residential_initial", "365 days",
                "commercial_initial", "90 days", 
                "all_renewals", "365 days"
            )
        ));
    }
}
