package com.smartwatts.deviceservice.controller;

import com.smartwatts.deviceservice.dto.DeviceDto;
import com.smartwatts.deviceservice.dto.DeviceConfigurationDto;
import com.smartwatts.deviceservice.dto.DeviceEventDto;
import com.smartwatts.deviceservice.model.Device;
import com.smartwatts.deviceservice.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Device Management", description = "APIs for IoT device management and monitoring")
public class DeviceController {
    
    private final DeviceService deviceService;
    
    @GetMapping
    @Operation(summary = "Get all devices", description = "Retrieves all devices (for testing)")
    public ResponseEntity<Page<DeviceDto>> getAllDevices(Pageable pageable) {
        log.info("Fetching all devices");
        Page<DeviceDto> devices = deviceService.getAllDevices(pageable);
        return ResponseEntity.ok(devices);
    }
    
    @PostMapping("/register")
    @Operation(summary = "Register device", description = "Registers a new IoT device")
    public ResponseEntity<DeviceDto> registerDevice(@Valid @RequestBody DeviceDto deviceDto) {
        log.info("Registering device: {} for user: {}", deviceDto.getDeviceId(), deviceDto.getUserId());
        DeviceDto registeredDevice = deviceService.registerDevice(deviceDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredDevice);
    }
    
    @GetMapping("/{deviceId}")
    @Operation(summary = "Get device by ID", description = "Retrieves a specific device by its ID")
    @PreAuthorize("hasRole('ADMIN') or #deviceId == authentication.principal.username")
    public ResponseEntity<DeviceDto> getDeviceById(
            @Parameter(description = "Device ID") @PathVariable UUID deviceId) {
        log.info("Fetching device with ID: {}", deviceId);
        DeviceDto device = deviceService.getDeviceById(deviceId);
        return ResponseEntity.ok(device);
    }
    
    @GetMapping("/device-id/{deviceId}")
    @Operation(summary = "Get device by device ID", description = "Retrieves a specific device by its device ID")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeviceDto> getDeviceByDeviceId(
            @Parameter(description = "Device ID string") @PathVariable String deviceId) {
        log.info("Fetching device with device ID: {}", deviceId);
        DeviceDto device = deviceService.getDeviceByDeviceId(deviceId);
        return ResponseEntity.ok(device);
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get devices by user", description = "Retrieves all devices for a specific user")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public ResponseEntity<Page<DeviceDto>> getDevicesByUserId(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            Pageable pageable) {
        log.info("Fetching devices for user: {}", userId);
        Page<DeviceDto> devices = deviceService.getDevicesByUserId(userId, pageable);
        return ResponseEntity.ok(devices);
    }
    
    @GetMapping("/user/{userId}/active")
    @Operation(summary = "Get active devices by user", description = "Retrieves all active devices for a specific user")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public ResponseEntity<List<DeviceDto>> getActiveDevicesByUserId(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        log.info("Fetching active devices for user: {}", userId);
        List<DeviceDto> devices = deviceService.getActiveDevicesByUserId(userId);
        return ResponseEntity.ok(devices);
    }
    
    @PutMapping("/{deviceId}")
    @Operation(summary = "Update device", description = "Updates an existing device")
    @PreAuthorize("hasRole('ADMIN') or #deviceId == authentication.principal.username")
    public ResponseEntity<DeviceDto> updateDevice(
            @Parameter(description = "Device ID") @PathVariable UUID deviceId,
            @Valid @RequestBody DeviceDto deviceDto) {
        log.info("Updating device with ID: {}", deviceId);
        DeviceDto updatedDevice = deviceService.updateDevice(deviceId, deviceDto);
        return ResponseEntity.ok(updatedDevice);
    }
    
    @PutMapping("/{deviceId}/status")
    @Operation(summary = "Update device status", description = "Updates the status of a specific device")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeviceDto> updateDeviceStatus(
            @Parameter(description = "Device ID") @PathVariable UUID deviceId,
            @Parameter(description = "New status") @RequestParam Device.DeviceStatus status) {
        log.info("Updating device status to: {} for device: {}", status, deviceId);
        DeviceDto updatedDevice = deviceService.updateDeviceStatus(deviceId, status);
        return ResponseEntity.ok(updatedDevice);
    }
    
    @PutMapping("/{deviceId}/connection-status")
    @Operation(summary = "Update connection status", description = "Updates the connection status of a specific device")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeviceDto> updateConnectionStatus(
            @Parameter(description = "Device ID") @PathVariable UUID deviceId,
            @Parameter(description = "New connection status") @RequestParam Device.ConnectionStatus connectionStatus) {
        log.info("Updating connection status to: {} for device: {}", connectionStatus, deviceId);
        DeviceDto updatedDevice = deviceService.updateConnectionStatus(deviceId, connectionStatus);
        return ResponseEntity.ok(updatedDevice);
    }
    
    @DeleteMapping("/{deviceId}")
    @Operation(summary = "Delete device", description = "Deletes a specific device")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDevice(
            @Parameter(description = "Device ID") @PathVariable UUID deviceId) {
        log.info("Deleting device with ID: {}", deviceId);
        deviceService.deleteDevice(deviceId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/maintenance/needed")
    @Operation(summary = "Get devices needing maintenance", description = "Retrieves all devices that need maintenance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DeviceDto>> getDevicesNeedingMaintenance() {
        log.info("Fetching devices needing maintenance");
        List<DeviceDto> devices = deviceService.getDevicesNeedingMaintenance();
        return ResponseEntity.ok(devices);
    }
    
    @GetMapping("/calibration/needed")
    @Operation(summary = "Get devices needing calibration", description = "Retrieves all devices that need calibration")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DeviceDto>> getDevicesNeedingCalibration() {
        log.info("Fetching devices needing calibration");
        List<DeviceDto> devices = deviceService.getDevicesNeedingCalibration();
        return ResponseEntity.ok(devices);
    }
    
    @GetMapping("/user/{userId}/count/status/{status}")
    @Operation(summary = "Get device count by status", description = "Returns the count of devices with a specific status")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public ResponseEntity<Long> getDeviceCountByStatus(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Device status") @PathVariable Device.DeviceStatus status) {
        log.info("Counting devices with status: {} for user: {}", status, userId);
        long count = deviceService.getDeviceCountByStatus(userId, status);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/user/{userId}/count/verified")
    @Operation(summary = "Get verified device count", description = "Returns the count of verified devices")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public ResponseEntity<Long> getVerifiedDeviceCount(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        log.info("Counting verified devices for user: {}", userId);
        long count = deviceService.getVerifiedDeviceCount(userId);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/user/{userId}/count/calibrated")
    @Operation(summary = "Get calibrated device count", description = "Returns the count of calibrated devices")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public ResponseEntity<Long> getCalibratedDeviceCount(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        log.info("Counting calibrated devices for user: {}", userId);
        long count = deviceService.getCalibratedDeviceCount(userId);
        return ResponseEntity.ok(count);
    }
    
    // Device Configuration Endpoints
    @PostMapping("/{deviceId}/configurations")
    @Operation(summary = "Save device configuration", description = "Saves a device configuration")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeviceConfigurationDto> saveDeviceConfiguration(
            @Parameter(description = "Device ID") @PathVariable UUID deviceId,
            @Valid @RequestBody DeviceConfigurationDto configDto) {
        log.info("Saving configuration for device: {}", deviceId);
        configDto.setDeviceId(deviceId);
        DeviceConfigurationDto savedConfig = deviceService.saveDeviceConfiguration(configDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedConfig);
    }
    
    @GetMapping("/{deviceId}/configurations")
    @Operation(summary = "Get device configurations", description = "Retrieves all configurations for a specific device")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DeviceConfigurationDto>> getDeviceConfigurations(
            @Parameter(description = "Device ID") @PathVariable UUID deviceId) {
        log.info("Fetching configurations for device: {}", deviceId);
        List<DeviceConfigurationDto> configs = deviceService.getDeviceConfigurations(deviceId);
        return ResponseEntity.ok(configs);
    }
    
    @GetMapping("/{deviceId}/configurations/{configKey}")
    @Operation(summary = "Get device configuration", description = "Retrieves a specific configuration for a device")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeviceConfigurationDto> getDeviceConfiguration(
            @Parameter(description = "Device ID") @PathVariable UUID deviceId,
            @Parameter(description = "Configuration key") @PathVariable String configKey) {
        log.info("Fetching configuration for device: {}, key: {}", deviceId, configKey);
        DeviceConfigurationDto config = deviceService.getDeviceConfiguration(deviceId, configKey);
        return ResponseEntity.ok(config);
    }
    
    // Device Event Endpoints
    @GetMapping("/{deviceId}/events")
    @Operation(summary = "Get device events", description = "Retrieves all events for a specific device")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<DeviceEventDto>> getDeviceEvents(
            @Parameter(description = "Device ID") @PathVariable UUID deviceId,
            Pageable pageable) {
        log.info("Fetching events for device: {}", deviceId);
        Page<DeviceEventDto> events = deviceService.getDeviceEvents(deviceId, pageable);
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/{deviceId}/events/recent")
    @Operation(summary = "Get recent device events", description = "Retrieves recent events for a specific device")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DeviceEventDto>> getRecentDeviceEvents(
            @Parameter(description = "Device ID") @PathVariable UUID deviceId,
            @Parameter(description = "Since timestamp") @RequestParam LocalDateTime since) {
        log.info("Fetching recent events for device: {} since {}", deviceId, since);
        List<DeviceEventDto> events = deviceService.getRecentDeviceEvents(deviceId, since);
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/{deviceId}/events/unacknowledged-count")
    @Operation(summary = "Get unacknowledged event count", description = "Returns the count of unacknowledged events")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> getUnacknowledgedEventCount(
            @Parameter(description = "Device ID") @PathVariable UUID deviceId) {
        log.info("Counting unacknowledged events for device: {}", deviceId);
        long count = deviceService.getUnacknowledgedEventCount(deviceId);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/{deviceId}/events/unresolved-count")
    @Operation(summary = "Get unresolved event count", description = "Returns the count of unresolved events")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> getUnresolvedEventCount(
            @Parameter(description = "Device ID") @PathVariable UUID deviceId) {
        log.info("Counting unresolved events for device: {}", deviceId);
        long count = deviceService.getUnresolvedEventCount(deviceId);
        return ResponseEntity.ok(count);
    }
} 