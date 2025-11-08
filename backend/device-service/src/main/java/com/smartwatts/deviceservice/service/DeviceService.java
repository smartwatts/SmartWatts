package com.smartwatts.deviceservice.service;

import com.smartwatts.deviceservice.dto.DeviceDto;
import com.smartwatts.deviceservice.dto.DeviceConfigurationDto;
import com.smartwatts.deviceservice.dto.DeviceEventDto;
import com.smartwatts.deviceservice.model.Device;
import com.smartwatts.deviceservice.model.DeviceConfiguration;
import com.smartwatts.deviceservice.model.DeviceEvent;
import com.smartwatts.deviceservice.repository.DeviceRepository;
import com.smartwatts.deviceservice.repository.DeviceConfigurationRepository;
import com.smartwatts.deviceservice.repository.DeviceEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceService {
    
    private final DeviceRepository deviceRepository;
    private final DeviceConfigurationRepository deviceConfigurationRepository;
    private final DeviceEventRepository deviceEventRepository;
    private final EventService eventService;
    
    @Transactional
    public DeviceDto registerDevice(DeviceDto deviceDto) {
        log.info("Registering device: {} for user: {}", deviceDto.getDeviceId(), deviceDto.getUserId());
        
        // Check if device already exists
        if (deviceRepository.existsByDeviceId(deviceDto.getDeviceId())) {
            throw new RuntimeException("Device with ID " + deviceDto.getDeviceId() + " already exists");
        }
        
        if (deviceRepository.existsByUserIdAndDeviceId(deviceDto.getUserId(), deviceDto.getDeviceId())) {
            throw new RuntimeException("Device already registered for this user");
        }
        
        Device device = new Device();
        BeanUtils.copyProperties(deviceDto, device);
        
        // Set default values
        if (device.getStatus() == null) {
            device.setStatus(Device.DeviceStatus.ACTIVE);
        }
        if (device.getConnectionStatus() == null) {
            device.setConnectionStatus(Device.ConnectionStatus.OFFLINE);
        }
        if (device.getProtocol() == null) {
            device.setProtocol(Device.Protocol.MQTT);
        }
        if (device.getInstallationDate() == null) {
            device.setInstallationDate(LocalDateTime.now());
        }
        
        // Set trust level and verification status based on device type
        setDeviceTrustLevel(device);
        
        Device savedDevice = deviceRepository.save(device);
        log.info("Device registered with ID: {}", savedDevice.getId());
        
        // Create device discovery event
        eventService.createDeviceEvent(savedDevice.getId(), DeviceEvent.EventType.DEVICE_DISCOVERED,
                DeviceEvent.Severity.INFO, "Device Registered",
                "Device " + savedDevice.getName() + " has been registered");
        
        return convertToDto(savedDevice);
    }
    
    @Transactional(readOnly = true)
    public Page<DeviceDto> getAllDevices(Pageable pageable) {
        log.info("Fetching all devices");
        Page<Device> devices = deviceRepository.findAll(pageable);
        return devices.map(this::convertToDto);
    }
    
    @Transactional(readOnly = true)
    public DeviceDto getDeviceById(UUID deviceId) {
        log.info("Fetching device with ID: {}", deviceId);
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found with ID: " + deviceId));
        return convertToDto(device);
    }
    
    @Transactional(readOnly = true)
    public DeviceDto getDeviceByDeviceId(String deviceId) {
        log.info("Fetching device with device ID: {}", deviceId);
        Device device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found with device ID: " + deviceId));
        return convertToDto(device);
    }
    
    @Transactional(readOnly = true)
    public Page<DeviceDto> getDevicesByUserId(UUID userId, Pageable pageable) {
        log.info("Fetching devices for user: {}", userId);
        Page<Device> devices = deviceRepository.findByUserId(userId, pageable);
        return devices.map(this::convertToDto);
    }
    
    @Transactional(readOnly = true)
    public List<DeviceDto> getActiveDevicesByUserId(UUID userId) {
        log.info("Fetching active devices for user: {}", userId);
        LocalDateTime since = LocalDateTime.now().minusMinutes(5); // Devices seen in last 5 minutes
        List<Device> devices = deviceRepository.findActiveDevicesByUserId(userId, since);
        return devices.stream().map(this::convertToDto).toList();
    }
    
    @Transactional
    public DeviceDto updateDevice(UUID deviceId, DeviceDto deviceDto) {
        log.info("Updating device with ID: {}", deviceId);
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found with ID: " + deviceId));
        
        // Update fields
        device.setName(deviceDto.getName());
        device.setDescription(deviceDto.getDescription());
        device.setDeviceType(deviceDto.getDeviceType());
        device.setProtocol(deviceDto.getProtocol());
        device.setConnectionString(deviceDto.getConnectionString());
        device.setIpAddress(deviceDto.getIpAddress());
        device.setPort(deviceDto.getPort());
        device.setUsername(deviceDto.getUsername());
        device.setPassword(deviceDto.getPassword());
        device.setMqttTopic(deviceDto.getMqttTopic());
        device.setModbusAddress(deviceDto.getModbusAddress());
        device.setModbusRegisterStart(deviceDto.getModbusRegisterStart());
        device.setModbusRegisterCount(deviceDto.getModbusRegisterCount());
        device.setLocationLat(deviceDto.getLocationLat());
        device.setLocationLng(deviceDto.getLocationLng());
        device.setStatus(deviceDto.getStatus());
        device.setNotes(deviceDto.getNotes());
        
        Device savedDevice = deviceRepository.save(device);
        
        // Create configuration change event
        eventService.createDeviceEvent(savedDevice.getId(), DeviceEvent.EventType.CONFIGURATION_CHANGED,
                DeviceEvent.Severity.INFO, "Device Configuration Updated",
                "Device configuration has been updated");
        
        return convertToDto(savedDevice);
    }
    
    @Transactional
    public DeviceDto updateDeviceStatus(UUID deviceId, Device.DeviceStatus status) {
        log.info("Updating device status to: {} for device: {}", status, deviceId);
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found with ID: " + deviceId));
        
        device.setStatus(status);
        Device savedDevice = deviceRepository.save(device);
        
        // Create status change event
        eventService.createDeviceEvent(savedDevice.getId(), DeviceEvent.EventType.CUSTOM_EVENT,
                DeviceEvent.Severity.INFO, "Device Status Changed",
                "Device status changed to: " + status);
        
        return convertToDto(savedDevice);
    }
    
    @Transactional
    public DeviceDto updateConnectionStatus(UUID deviceId, Device.ConnectionStatus connectionStatus) {
        log.info("Updating connection status to: {} for device: {}", connectionStatus, deviceId);
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found with ID: " + deviceId));
        
        Device.ConnectionStatus oldStatus = device.getConnectionStatus();
        device.setConnectionStatus(connectionStatus);
        device.setLastSeen(LocalDateTime.now());
        
        Device savedDevice = deviceRepository.save(device);
        
        // Create connection status event
        if (connectionStatus == Device.ConnectionStatus.ONLINE && oldStatus != Device.ConnectionStatus.ONLINE) {
            eventService.createDeviceEvent(savedDevice.getId(), DeviceEvent.EventType.DEVICE_ONLINE,
                    DeviceEvent.Severity.INFO, "Device Online", "Device is now online");
        } else if (connectionStatus == Device.ConnectionStatus.OFFLINE && oldStatus != Device.ConnectionStatus.OFFLINE) {
            eventService.createDeviceEvent(savedDevice.getId(), DeviceEvent.EventType.DEVICE_OFFLINE,
                    DeviceEvent.Severity.WARNING, "Device Offline", "Device is now offline");
        }
        
        return convertToDto(savedDevice);
    }
    
    @Transactional
    public void deleteDevice(UUID deviceId) {
        log.info("Deleting device with ID: {}", deviceId);
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found with ID: " + deviceId));
        
        // Create deregistration event
        eventService.createDeviceEvent(deviceId, DeviceEvent.EventType.DEVICE_DEREGISTERED,
                DeviceEvent.Severity.INFO, "Device Deregistered",
                "Device " + device.getName() + " has been deregistered");
        
        deviceRepository.delete(device);
        log.info("Device deleted successfully");
    }
    
    @Transactional(readOnly = true)
    public List<DeviceDto> getDevicesNeedingMaintenance() {
        log.info("Fetching devices needing maintenance");
        List<Device> devices = deviceRepository.findDevicesNeedingMaintenance(LocalDateTime.now());
        return devices.stream().map(this::convertToDto).toList();
    }
    
    @Transactional(readOnly = true)
    public List<DeviceDto> getDevicesNeedingCalibration() {
        log.info("Fetching devices needing calibration");
        List<Device> devices = deviceRepository.findDevicesNeedingCalibration(LocalDateTime.now());
        return devices.stream().map(this::convertToDto).toList();
    }
    
    @Transactional(readOnly = true)
    public long getDeviceCountByStatus(UUID userId, Device.DeviceStatus status) {
        return deviceRepository.countByUserIdAndStatus(userId, status);
    }
    
    @Transactional(readOnly = true)
    public long getVerifiedDeviceCount(UUID userId) {
        return deviceRepository.countVerifiedDevicesByUserId(userId);
    }
    
    @Transactional(readOnly = true)
    public long getCalibratedDeviceCount(UUID userId) {
        return deviceRepository.countCalibratedDevicesByUserId(userId);
    }
    
    // Device Configuration Methods
    @Transactional
    public DeviceConfigurationDto saveDeviceConfiguration(DeviceConfigurationDto configDto) {
        log.info("Saving device configuration for device: {}, key: {}", configDto.getDeviceId(), configDto.getConfigKey());
        
        DeviceConfiguration config = new DeviceConfiguration();
        BeanUtils.copyProperties(configDto, config);
        
        config.setLastUpdated(LocalDateTime.now());
        
        DeviceConfiguration savedConfig = deviceConfigurationRepository.save(config);
        log.info("Device configuration saved with ID: {}", savedConfig.getId());
        
        return convertToDto(savedConfig);
    }
    
    @Transactional(readOnly = true)
    public List<DeviceConfigurationDto> getDeviceConfigurations(UUID deviceId) {
        log.info("Fetching configurations for device: {}", deviceId);
        List<DeviceConfiguration> configs = deviceConfigurationRepository.findByDeviceIdAndIsActive(deviceId, true);
        return configs.stream().map(this::convertToDto).toList();
    }
    
    @Transactional(readOnly = true)
    public DeviceConfigurationDto getDeviceConfiguration(UUID deviceId, String configKey) {
        log.info("Fetching configuration for device: {}, key: {}", deviceId, configKey);
        DeviceConfiguration config = deviceConfigurationRepository.findByDeviceIdAndConfigKey(deviceId, configKey)
                .orElseThrow(() -> new RuntimeException("Configuration not found"));
        return convertToDto(config);
    }
    
    // Device Event Methods
    @Transactional(readOnly = true)
    public Page<DeviceEventDto> getDeviceEvents(UUID deviceId, Pageable pageable) {
        log.info("Fetching events for device: {}", deviceId);
        Page<DeviceEvent> events = deviceEventRepository.findByDeviceId(deviceId, pageable);
        return events.map(this::convertToDto);
    }
    
    @Transactional(readOnly = true)
    public List<DeviceEventDto> getRecentDeviceEvents(UUID deviceId, LocalDateTime since) {
        log.info("Fetching recent events for device: {} since {}", deviceId, since);
        List<DeviceEvent> events = deviceEventRepository.findRecentEventsByDeviceId(deviceId, since);
        return events.stream().map(this::convertToDto).toList();
    }
    
    @Transactional(readOnly = true)
    public long getUnacknowledgedEventCount(UUID deviceId) {
        return deviceEventRepository.countUnacknowledgedEventsByDeviceId(deviceId);
    }
    
    @Transactional(readOnly = true)
    public long getUnresolvedEventCount(UUID deviceId) {
        return deviceEventRepository.countUnresolvedEventsByDeviceId(deviceId);
    }
    
    private DeviceDto convertToDto(Device device) {
        DeviceDto dto = new DeviceDto();
        BeanUtils.copyProperties(device, dto);
        return dto;
    }
    
    private DeviceConfigurationDto convertToDto(DeviceConfiguration config) {
        DeviceConfigurationDto dto = new DeviceConfigurationDto();
        BeanUtils.copyProperties(config, dto);
        return dto;
    }
    
    private DeviceEventDto convertToDto(DeviceEvent event) {
        DeviceEventDto dto = new DeviceEventDto();
        BeanUtils.copyProperties(event, dto);
        return dto;
    }

    /**
     * Set device trust level and verification status based on device characteristics
     */
    private void setDeviceTrustLevel(Device device) {
        // Check if this is a SmartWatts OEM device
        if (isSmartWattsOEMDevice(device)) {
            device.setTrustLevel(Device.TrustLevel.OEM_LOCKED);
            device.setVerificationStatus(Device.VerificationStatus.APPROVED);
            device.setIsVerified(true);
            device.setVerificationDate(LocalDateTime.now());
            log.info("Device {} marked as OEM_LOCKED with immediate access", device.getDeviceId());
        } else {
            // Third-party device requiring verification
            device.setTrustLevel(Device.TrustLevel.UNVERIFIED);
            device.setVerificationStatus(Device.VerificationStatus.PENDING);
            device.setIsVerified(false);
            log.info("Device {} marked as UNVERIFIED requiring manual verification", device.getDeviceId());
        }
    }
    
    /**
     * Determine if device is a SmartWatts OEM device
     */
    private boolean isSmartWattsOEMDevice(Device device) {
        // Check manufacturer
        if (device.getManufacturer() != null && 
            (device.getManufacturer().toLowerCase().contains("smartwatts") ||
             device.getManufacturer().toLowerCase().contains("smartwatts oem") ||
             device.getManufacturer().toLowerCase().contains("smartwatts certified"))) {
            return true;
        }
        
        // Check device ID pattern
        if (device.getDeviceId() != null && 
            (device.getDeviceId().startsWith("SW_") || 
             device.getDeviceId().startsWith("OEM_"))) {
            return true;
        }
        
        // Check serial number pattern
        if (device.getSerialNumber() != null && 
            device.getSerialNumber().startsWith("SW")) {
            return true;
        }
        
        return false;
    }

    // Nigerian-specific generator health methods
    public Map<String, Object> getGeneratorHealth(UUID userId) {
        log.info("Getting generator health for user: {}", userId);

        Map<String, Object> health = new HashMap<>();
        health.put("userId", userId);
        health.put("generatorId", "GEN-" + userId.toString().substring(0, 8));
        health.put("status", "running");
        health.put("runtimeHours", 1250.5);
        health.put("batteryVoltage", 12.4);
        health.put("batteryStatus", "good");
        health.put("oilLevel", "normal");
        health.put("coolantTemperature", 85.0);
        health.put("fuelLevel", 75.0);
        health.put("lastMaintenance", LocalDateTime.now().minusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        health.put("nextMaintenance", LocalDateTime.now().plusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        health.put("maintenanceAlerts", List.of(
            "Oil change due in 30 days",
            "Air filter replacement recommended"
        ));
        health.put("performanceMetrics", Map.of(
            "efficiency", 92.5,
            "powerOutput", 5.5,
            "fuelConsumption", 2.3
        ));
        health.put("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return health;
    }

    public Map<String, Object> getFuelConsumption(UUID userId) {
        log.info("Getting fuel consumption for user: {}", userId);

        Map<String, Object> consumption = new HashMap<>();
        consumption.put("userId", userId);
        consumption.put("generatorId", "GEN-" + userId.toString().substring(0, 8));
        consumption.put("totalFuelUsed", 450.5); // liters
        consumption.put("averageDailyUsage", 15.2); // liters per day
        consumption.put("costPerLiter", 650.0); // NGN
        consumption.put("totalFuelCost", 292825.0); // NGN
        consumption.put("efficiency", 2.3); // liters per kWh
        consumption.put("costPerKwh", 1495.0); // NGN per kWh
        consumption.put("monthlyTrend", List.of(
            Map.of("month", "January", "usage", 420.5, "cost", 273325.0),
            Map.of("month", "February", "usage", 380.2, "cost", 247130.0),
            Map.of("month", "March", "usage", 450.8, "cost", 293020.0)
        ));
        consumption.put("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return consumption;
    }

    public Map<String, Object> getMaintenanceSchedule(UUID userId) {
        log.info("Getting maintenance schedule for user: {}", userId);

        Map<String, Object> maintenance = new HashMap<>();
        maintenance.put("userId", userId);
        maintenance.put("generatorId", "GEN-" + userId.toString().substring(0, 8));
        maintenance.put("nextOilChange", LocalDateTime.now().plusDays(15).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        maintenance.put("nextAirFilterChange", LocalDateTime.now().plusDays(45).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        maintenance.put("nextSparkPlugChange", LocalDateTime.now().plusDays(90).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        maintenance.put("nextMajorService", LocalDateTime.now().plusDays(180).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        maintenance.put("maintenanceHistory", List.of(
            Map.of("date", LocalDateTime.now().minusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                  "type", "Oil Change", "status", "completed", "cost", 15000.0),
            Map.of("date", LocalDateTime.now().minusDays(60).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                  "type", "Air Filter", "status", "completed", "cost", 8000.0),
            Map.of("date", LocalDateTime.now().minusDays(120).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                  "type", "Major Service", "status", "completed", "cost", 45000.0)
        ));
        maintenance.put("alerts", List.of(
            "Oil change due in 15 days",
            "Air filter replacement in 45 days"
        ));
        maintenance.put("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return maintenance;
    }

    public List<Map<String, Object>> getRuntimeHistory(UUID userId) {
        log.info("Getting runtime history for user: {}", userId);

        List<Map<String, Object>> history = new ArrayList<>();

        // Generate sample runtime history
        for (int i = 0; i < 10; i++) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("eventId", UUID.randomUUID().toString());
            entry.put("startTime", LocalDateTime.now().minusHours(i * 4).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            entry.put("endTime", LocalDateTime.now().minusHours(i * 4 - 2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            entry.put("duration", 2.0); // hours
            entry.put("reason", i % 3 == 0 ? "Grid Outage" : "Scheduled Maintenance");
            entry.put("fuelUsed", 4.6); // liters
            entry.put("cost", 2990.0); // NGN
            entry.put("powerGenerated", 11.0); // kWh
            entry.put("efficiency", 2.1); // liters per kWh
            entry.put("status", "completed");
            history.add(entry);
        }

        return history;
    }
} 