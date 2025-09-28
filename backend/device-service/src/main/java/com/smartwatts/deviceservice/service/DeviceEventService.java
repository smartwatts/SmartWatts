package com.smartwatts.deviceservice.service;

import com.smartwatts.deviceservice.model.Device;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceEventService {

    public void logDeviceEvent(UUID deviceId, String eventType, String description) {
        log.info("Device Event - Device ID: {}, Event Type: {}, Description: {}, Timestamp: {}", 
                deviceId, eventType, description, LocalDateTime.now());
    }

    public void logDeviceVerificationEvent(UUID deviceId, Device.VerificationStatus status, String notes) {
        log.info("Device Verification Event - Device ID: {}, Status: {}, Notes: {}, Timestamp: {}", 
                deviceId, status, notes, LocalDateTime.now());
    }

    public void logDeviceActivationEvent(UUID deviceId, boolean success, String reason) {
        log.info("Device Activation Event - Device ID: {}, Success: {}, Reason: {}, Timestamp: {}", 
                deviceId, success, reason, LocalDateTime.now());
    }
}
