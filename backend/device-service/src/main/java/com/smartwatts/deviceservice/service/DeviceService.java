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
import java.util.List;
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
} 