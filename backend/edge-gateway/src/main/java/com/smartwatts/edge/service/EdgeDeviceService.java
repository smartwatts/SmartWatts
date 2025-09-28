package com.smartwatts.edge.service;

import com.smartwatts.edge.model.DeviceReading;
import com.smartwatts.edge.model.DeviceStatus;
import com.smartwatts.edge.model.DeviceCommand;
import com.smartwatts.edge.protocol.MQTTProtocolHandler;
import com.smartwatts.edge.protocol.ModbusProtocolHandler;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EdgeDeviceService {
    
    private static final Logger logger = LoggerFactory.getLogger(EdgeDeviceService.class);
    
    // Device registry and status tracking
    private final Map<String, DeviceStatus> deviceRegistry = new ConcurrentHashMap<>();
    private final Map<String, List<DeviceReading>> deviceReadings = new ConcurrentHashMap<>();
    private final Map<String, Queue<DeviceCommand>> commandQueue = new ConcurrentHashMap<>();
    
    // Protocol handlers for different IoT protocols
    private final Map<String, Object> protocolHandlers = new HashMap<>();
    
    // Inject real protocol handlers
    private final MQTTProtocolHandler mqttHandler;
    private final ModbusProtocolHandler modbusHandler;
    
    public EdgeDeviceService(MQTTProtocolHandler mqttHandler, ModbusProtocolHandler modbusHandler) {
        this.mqttHandler = mqttHandler;
        this.modbusHandler = modbusHandler;
    }
    
    /**
     * Initialize edge device service
     */
    public void initialize() {
        try {
            logger.info("Initializing edge device service...");
            
            // Initialize protocol handlers
            initializeProtocolHandlers();
            
            // Start device discovery
            discoverDevices();
            
            logger.info("Edge device service initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize edge device service", e);
        }
    }
    
    /**
     * Initialize protocol handlers for different IoT protocols
     */
    private void initializeProtocolHandlers() {
        // Register real protocol handlers
        protocolHandlers.put("mqtt", mqttHandler);
        protocolHandlers.put("modbus", modbusHandler);
        
        logger.info("Protocol handlers initialized: {}", protocolHandlers.keySet());
    }
    
    /**
     * Discover IoT devices on the network
     */
    private void discoverDevices() {
        try {
            logger.info("Starting device discovery...");
            
            // Simulate device discovery for different protocols
            discoverMQTTDevices();
            discoverModbusDevices();
            discoverHTTPDevices();
            
            logger.info("Device discovery completed. Found {} devices", deviceRegistry.size());
        } catch (Exception e) {
            logger.error("Device discovery failed", e);
        }
    }
    
    /**
     * Discover MQTT devices
     */
    private void discoverMQTTDevices() {
        // Simulate MQTT device discovery
        String[] mqttDevices = {"smart_meter_001", "solar_inverter_001", "generator_monitor_001"};
        
        for (String deviceId : mqttDevices) {
            DeviceStatus status = new DeviceStatus();
            status.setDeviceId(deviceId);
            status.setProtocol("mqtt");
            status.setStatus("online");
            status.setLastSeen(LocalDateTime.now());
            status.setLocation("Edge Gateway");
            status.setCapabilities(Arrays.asList("energy_monitoring", "real_time_data"));
            
            deviceRegistry.put(deviceId, status);
            deviceReadings.put(deviceId, new ArrayList<>());
            commandQueue.put(deviceId, new LinkedList<>());
            
            logger.info("Discovered MQTT device: {}", deviceId);
        }
    }
    
    /**
     * Discover Modbus devices
     */
    private void discoverModbusDevices() {
        // Simulate Modbus device discovery
        String[] modbusDevices = {"power_meter_001", "load_controller_001"};
        
        for (String deviceId : modbusDevices) {
            DeviceStatus status = new DeviceStatus();
            status.setDeviceId(deviceId);
            status.setProtocol("modbus");
            status.setStatus("online");
            status.setLastSeen(LocalDateTime.now());
            status.setLocation("Edge Gateway");
            status.setCapabilities(Arrays.asList("power_measurement", "load_control"));
            
            deviceRegistry.put(deviceId, status);
            deviceReadings.put(deviceId, new ArrayList<>());
            commandQueue.put(deviceId, new LinkedList<>());
            
            logger.info("Discovered Modbus device: {}", deviceId);
        }
    }
    
    /**
     * Discover HTTP devices
     */
    private void discoverHTTPDevices() {
        // Simulate HTTP device discovery
        String[] httpDevices = {"smart_plug_001", "energy_monitor_001"};
        
        for (String deviceId : httpDevices) {
            DeviceStatus status = new DeviceStatus();
            status.setDeviceId(deviceId);
            status.setProtocol("http");
            status.setStatus("online");
            status.setLastSeen(LocalDateTime.now());
            status.setLocation("Edge Gateway");
            status.setCapabilities(Arrays.asList("on_off_control", "energy_monitoring"));
            
            deviceRegistry.put(deviceId, status);
            deviceReadings.put(deviceId, new ArrayList<>());
            commandQueue.put(deviceId, new LinkedList<>());
            
            logger.info("Discovered HTTP device: {}", deviceId);
        }
    }
    
    /**
     * Process device reading from IoT device
     */
    public void processDeviceReading(String deviceId, DeviceReading reading) {
        try {
            if (!deviceRegistry.containsKey(deviceId)) {
                logger.warn("Received reading from unknown device: {}", deviceId);
                return;
            }
            
            // Add timestamp if not present
            if (reading.getTimestamp() == null) {
                reading.setTimestamp(LocalDateTime.now());
            }
            
            // Store reading
            List<DeviceReading> readings = deviceReadings.get(deviceId);
            readings.add(reading);
            
            // Keep only last 1000 readings per device
            if (readings.size() > 1000) {
                readings.remove(0);
            }
            
            // Update device status
            DeviceStatus status = deviceRegistry.get(deviceId);
            status.setLastSeen(LocalDateTime.now());
            status.setLastReading(reading);
            
            logger.debug("Processed reading from device {}: {} kWh", deviceId, reading.getEnergyConsumption());
            
        } catch (Exception e) {
            logger.error("Failed to process device reading from {}", deviceId, e);
        }
    }
    
    /**
     * Send command to IoT device
     */
    public boolean sendDeviceCommand(String deviceId, DeviceCommand command) {
        try {
            if (!deviceRegistry.containsKey(deviceId)) {
                logger.warn("Attempted to send command to unknown device: {}", deviceId);
                return false;
            }
            
            DeviceStatus status = deviceRegistry.get(deviceId);
            String protocol = status.getProtocol();
            
            boolean success = false;
            
            switch (protocol) {
                case "mqtt":
                    success = mqttHandler.sendCommand(deviceId, command);
                    break;
                case "modbus":
                    // For Modbus, we need device config
                    try {
                        Object config = modbusHandler.getDeviceConfigs().get(deviceId);
                        if (config != null) {
                            success = modbusHandler.writeDeviceCommand(deviceId, command, config);
                        } else {
                            logger.warn("No Modbus configuration found for device: {}", deviceId);
                            return false;
                        }
                    } catch (Exception e) {
                        logger.error("Error accessing Modbus device config for device: {}", deviceId, e);
                        return false;
                    }
                    break;
                default:
                    logger.warn("Unsupported protocol: {}", protocol);
                    return false;
            }
            
            if (success) {
                logger.info("Command sent successfully to device {}: {}", deviceId, command.getCommand());
            } else {
                logger.warn("Failed to send command to device {}: {}", deviceId, command.getCommand());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Failed to send command to device {}", deviceId, e);
            return false;
        }
    }
    
    /**
     * Get device status
     */
    public DeviceStatus getDeviceStatus(String deviceId) {
        return deviceRegistry.get(deviceId);
    }
    
    /**
     * Get all device statuses
     */
    public List<DeviceStatus> getAllDeviceStatuses() {
        return new ArrayList<>(deviceRegistry.values());
    }
    
    /**
     * Get device readings
     */
    public List<DeviceReading> getDeviceReadings(String deviceId, int limit) {
        List<DeviceReading> readings = deviceReadings.get(deviceId);
        if (readings == null) {
            return new ArrayList<>();
        }
        
        int startIndex = Math.max(0, readings.size() - limit);
        return readings.subList(startIndex, readings.size());
    }
    
    /**
     * Update device status
     */
    public void updateDeviceStatus(String deviceId, String status, String location) {
        DeviceStatus deviceStatus = deviceRegistry.get(deviceId);
        if (deviceStatus != null) {
            deviceStatus.setStatus(status);
            deviceStatus.setLocation(location);
            deviceStatus.setLastSeen(LocalDateTime.now());
            logger.info("Updated device {} status to: {}", deviceId, status);
        }
    }
    
    /**
     * Scheduled task to check device health
     */
    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void checkDeviceHealth() {
        try {
            LocalDateTime now = LocalDateTime.now();
            int offlineCount = 0;
            
            for (Map.Entry<String, DeviceStatus> entry : deviceRegistry.entrySet()) {
                String deviceId = entry.getKey();
                DeviceStatus status = entry.getValue();
                
                // Check if device is offline (no activity for 5 minutes)
                if (status.getLastSeen().plusMinutes(5).isBefore(now)) {
                    status.setStatus("offline");
                    offlineCount++;
                    logger.warn("Device {} marked as offline", deviceId);
                }
            }
            
            if (offlineCount > 0) {
                logger.info("Device health check completed. {} devices offline", offlineCount);
            }
            
        } catch (Exception e) {
            logger.error("Device health check failed", e);
        }
    }
    
    /**
     * Get edge gateway statistics
     */
    public Map<String, Object> getGatewayStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDevices", deviceRegistry.size());
        stats.put("onlineDevices", deviceRegistry.values().stream()
                .filter(d -> "online".equals(d.getStatus())).count());
        stats.put("offlineDevices", deviceRegistry.values().stream()
                .filter(d -> "offline".equals(d.getStatus())).count());
        stats.put("totalReadings", deviceReadings.values().stream()
                .mapToInt(List::size).sum());
        stats.put("totalCommands", commandQueue.values().stream()
                .mapToInt(Queue::size).sum());
        stats.put("protocols", new ArrayList<>(protocolHandlers.keySet()));
        
        return stats;
    }
    
    // Protocol handler interface and implementations removed - using real protocol handlers from protocol package
}
