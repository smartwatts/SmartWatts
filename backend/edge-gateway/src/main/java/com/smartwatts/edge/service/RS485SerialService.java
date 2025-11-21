package com.smartwatts.edge.service;

import com.fazecast.jSerialComm.SerialPort;
import com.smartwatts.edge.config.RS485Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * RS485 Serial Communication Service
 * Manages serial port connections and RS485 device communication
 * 
 * Note: This is a framework implementation. For production use, 
 * integrate with actual serial communication libraries like jSerialComm
 */
@Service
@ConditionalOnProperty(name = "edge.rs485.enabled", havingValue = "true", matchIfMissing = true)
public class RS485SerialService {

    private static final Logger logger = LoggerFactory.getLogger(RS485SerialService.class);

    @Autowired
    private RS485Configuration rs485Config;

    private final Map<String, SerialPort> openPorts = new ConcurrentHashMap<>();
    private final Map<String, RS485Configuration.RS485DeviceConfig> deviceConfigs = new ConcurrentHashMap<>();
    private final Map<String, Consumer<byte[]>> dataCallbacks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    @PostConstruct
    public void initialize() {
        try {
            logger.info("Initializing RS485 Serial Service...");
            
            // Refresh available ports
            rs485Config.refreshAvailablePorts();
            logger.info("Available serial ports: {}", rs485Config.getAvailablePorts());
            
            // Initialize device configurations
            initializeDeviceConfigurations();
            
            // Start auto-discovery if enabled
            if (rs485Config.isAutoDiscovery()) {
                startAutoDiscovery();
            }
            
            logger.info("RS485 Serial Service initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize RS485 Serial Service", e);
        }
    }

    /**
     * Initialize device configurations from config
     */
    private void initializeDeviceConfigurations() {
        rs485Config.getDevices().forEach((deviceId, config) -> {
            deviceConfigs.put(deviceId, config);
            logger.info("Loaded device configuration for {}: {}:{}", deviceId, config.getPort(), config.getBaudRate());
        });
    }

    /**
     * Start auto-discovery of RS485 devices
     */
    private void startAutoDiscovery() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                discoverRS485Devices();
            } catch (Exception e) {
                logger.error("Error during RS485 device discovery", e);
            }
        }, 10, 30, TimeUnit.SECONDS);
    }

    /**
     * Discover RS485 devices on available ports
     */
    private void discoverRS485Devices() {
        List<String> availablePorts = rs485Config.getAvailablePorts();
        
        for (String portName : availablePorts) {
            if (!openPorts.containsKey(portName)) {
                try {
                    // REAL serial port discovery
                    SerialPort port = SerialPort.getCommPort(portName);
                    if (port != null && port.openPort()) {
                        logger.info("Discovered RS485 device on port: {}", portName);
                        
                        // Configure the port
                        configureSerialPort(port, createDefaultDeviceConfig());
                        
                        // Test communication
                        if (testPortCommunication(port)) {
                            openPorts.put(portName, port);
                            startPortReading(portName, port);
                            logger.info("Successfully connected to RS485 device on port: {}", portName);
                        } else {
                            port.closePort();
                            logger.warn("Failed communication test on port: {}", portName);
                        }
                    }
                } catch (Exception e) {
                    logger.debug("Failed to discover port {}: {}", portName, e.getMessage());
                }
            }
        }
    }
    
    /**
     * Configure serial port with device settings
     */
    private void configureSerialPort(SerialPort port, RS485Configuration.RS485DeviceConfig config) {
        port.setBaudRate(config.getBaudRate());
        port.setNumDataBits(config.getDataBits());
        port.setParity(config.getParity().getValue());
        port.setNumStopBits(config.getStopBits());
        port.setComPortTimeouts(
            SerialPort.TIMEOUT_READ_SEMI_BLOCKING,
            config.getReadTimeout(),
            config.getWriteTimeout()
        );
        
        // RS485 mode configuration (jSerialComm handles this automatically)
        logger.debug("RS485 port configured: {}", port.getSystemPortName());
    }
    
    /**
     * Test communication on a serial port
     */
    private boolean testPortCommunication(SerialPort port) {
        try {
            // Send a simple Modbus RTU request (Read Holding Registers)
            byte[] testRequest = createModbusRTURequest(1, 3, 0, 1); // Read 1 register from address 0
            port.writeBytes(testRequest, testRequest.length);
            
            // Wait for response
            Thread.sleep(100);
            
            // Check if we received data
            byte[] response = new byte[256];
            int bytesRead = port.readBytes(response, response.length);
            
            return bytesRead > 0;
        } catch (Exception e) {
            logger.debug("Port communication test failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Start reading data from a serial port
     */
    private void startPortReading(String portName, SerialPort port) {
        scheduler.submit(() -> {
            byte[] buffer = new byte[1024];
            
            while (port.isOpen()) {
                try {
                    int bytesRead = port.readBytes(buffer, buffer.length);
                    if (bytesRead > 0) {
                        byte[] data = Arrays.copyOf(buffer, bytesRead);
                        
                        // Process received data
                        processReceivedData(portName, data);
                        
                        // Notify callbacks
                        Consumer<byte[]> callback = dataCallbacks.get(portName);
                        if (callback != null) {
                            callback.accept(data);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error reading from port {}: {}", portName, e.getMessage());
                    break;
                }
            }
        });
    }
    
    /**
     * Process received RS485 data
     */
    private void processReceivedData(String portName, byte[] data) {
        try {
            // Parse Modbus RTU response
            if (isValidModbusRTUResponse(data)) {
                int unitId = data[0] & 0xFF;
                int functionCode = data[1] & 0xFF;
                
                logger.debug("Received Modbus RTU data from port {}: UnitId={}, FunctionCode={}", 
                           portName, unitId, functionCode);
                
                // Find device configuration for this unit ID
                String deviceId = findDeviceByUnitId(unitId);
                if (deviceId != null) {
                    processDeviceData(deviceId, data);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing RS485 data from port {}: {}", portName, e.getMessage());
        }
    }
    
    /**
     * Check if data is a valid Modbus RTU response
     */
    private boolean isValidModbusRTUResponse(byte[] data) {
        if (data.length < 4) return false;
        
        // Check CRC (simplified check)
        int crc = calculateCRC16(data, 0, data.length - 2);
        int receivedCRC = ((data[data.length - 2] & 0xFF) << 8) | (data[data.length - 1] & 0xFF);
        
        return crc == receivedCRC;
    }
    
    /**
     * Find device configuration by unit ID
     */
    private String findDeviceByUnitId(int unitId) {
        return deviceConfigs.entrySet().stream()
                .filter(entry -> entry.getValue().getUnitId() == unitId)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Process device-specific data
     */
    private void processDeviceData(String deviceId, byte[] data) {
        RS485Configuration.RS485DeviceConfig config = deviceConfigs.get(deviceId);
        if (config != null) {
            logger.info("Processing data for device {} ({}): {} bytes", 
                       deviceId, config.getDeviceType(), data.length);
            
            // Here you would parse the specific device data format
            // and convert it to DeviceReading objects
        }
    }
    
    /**
     * Create default device configuration
     */
    private RS485Configuration.RS485DeviceConfig createDefaultDeviceConfig() {
        RS485Configuration.RS485DeviceConfig config = new RS485Configuration.RS485DeviceConfig();
        config.setBaudRate(rs485Config.getDefaultBaudRate());
        config.setDataBits(rs485Config.getDefaultDataBits());
        config.setParity(rs485Config.getDefaultParity());
        config.setStopBits(rs485Config.getDefaultStopBits());
        config.setReadTimeout(rs485Config.getReadTimeout());
        config.setWriteTimeout(rs485Config.getWriteTimeout());
        return config;
    }

    /**
     * Send data to a specific device
     */
    public boolean sendDataToDevice(String deviceId, byte[] data) {
        try {
            RS485Configuration.RS485DeviceConfig config = deviceConfigs.get(deviceId);
            if (config == null) {
                logger.warn("No configuration found for device: {}", deviceId);
                return false;
            }
            
            SerialPort port = openPorts.get(config.getPort());
            if (port == null || !port.isOpen()) {
                logger.warn("Port {} is not open for device: {}", config.getPort(), deviceId);
                return false;
            }
            
            // REAL data sending
            int bytesWritten = port.writeBytes(data, data.length);
            logger.debug("Sent {} bytes to device {} on port {}", bytesWritten, deviceId, config.getPort());
            
            return bytesWritten == data.length;
        } catch (Exception e) {
            logger.error("Failed to send data to device {}: {}", deviceId, e.getMessage());
            return false;
        }
    }

    /**
     * Send Modbus RTU command to device
     */
    public boolean sendModbusRTUCommand(String deviceId, int functionCode, int startAddress, int quantity) {
        RS485Configuration.RS485DeviceConfig config = deviceConfigs.get(deviceId);
        if (config == null) {
            logger.warn("No configuration found for device: {}", deviceId);
            return false;
        }
        
        byte[] command = createModbusRTURequest(config.getUnitId(), functionCode, startAddress, quantity);
        return sendDataToDevice(deviceId, command);
    }

    /**
     * Create Modbus RTU request
     */
    private byte[] createModbusRTURequest(int unitId, int functionCode, int startAddress, int quantity) {
        byte[] request = new byte[8];
        request[0] = (byte) unitId;
        request[1] = (byte) functionCode;
        request[2] = (byte) (startAddress >> 8);
        request[3] = (byte) (startAddress & 0xFF);
        request[4] = (byte) (quantity >> 8);
        request[5] = (byte) (quantity & 0xFF);
        
        // Calculate CRC
        int crc = calculateCRC16(request, 0, 6);
        request[6] = (byte) (crc & 0xFF);
        request[7] = (byte) (crc >> 8);
        
        return request;
    }

    /**
     * Calculate CRC16 for Modbus RTU
     */
    private int calculateCRC16(byte[] data, int offset, int length) {
        int crc = 0xFFFF;
        
        for (int i = offset; i < offset + length; i++) {
            crc ^= (data[i] & 0xFF);
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x0001) != 0) {
                    crc = (crc >> 1) ^ 0xA001;
                } else {
                    crc = crc >> 1;
                }
            }
        }
        
        return crc;
    }

    /**
     * Register callback for data from specific port
     */
    public void registerDataCallback(String portName, Consumer<byte[]> callback) {
        dataCallbacks.put(portName, callback);
        logger.info("Registered data callback for port: {}", portName);
    }

    /**
     * Unregister data callback
     */
    public void unregisterDataCallback(String portName) {
        dataCallbacks.remove(portName);
        logger.info("Unregistered data callback for port: {}", portName);
    }

    /**
     * Add device configuration
     */
    public void addDevice(String deviceId, RS485Configuration.RS485DeviceConfig config) {
        deviceConfigs.put(deviceId, config);
        logger.info("Added device configuration: {} on port {}", deviceId, config.getPort());
    }

    /**
     * Remove device configuration
     */
    public void removeDevice(String deviceId) {
        RS485Configuration.RS485DeviceConfig config = deviceConfigs.remove(deviceId);
        if (config != null) {
            logger.info("Removed device configuration: {}", deviceId);
        }
    }

    /**
     * Get device status
     */
    public Map<String, Object> getDeviceStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("total_devices", deviceConfigs.size());
        status.put("open_ports", openPorts.size());
        status.put("available_ports", rs485Config.getAvailablePorts());
        
        List<Map<String, Object>> deviceStatuses = new ArrayList<>();
        deviceConfigs.forEach((deviceId, config) -> {
            Map<String, Object> deviceStatus = new HashMap<>();
            deviceStatus.put("device_id", deviceId);
            deviceStatus.put("port", config.getPort());
            deviceStatus.put("enabled", config.isEnabled());
            deviceStatus.put("unit_id", config.getUnitId());
            deviceStatus.put("device_type", config.getDeviceType());
            deviceStatus.put("manufacturer", config.getManufacturer());
            deviceStatus.put("model", config.getModel());
            
            Object port = openPorts.get(config.getPort());
            deviceStatus.put("port_open", port != null);
            
            deviceStatuses.add(deviceStatus);
        });
        status.put("devices", deviceStatuses);
        
        return status;
    }

    /**
     * Test communication with a specific device
     */
    public boolean testDeviceCommunication(String deviceId) {
        try {
            RS485Configuration.RS485DeviceConfig config = deviceConfigs.get(deviceId);
            if (config == null) {
                logger.warn("No configuration found for device: {}", deviceId);
                return false;
            }
            
            // REAL communication test
            SerialPort port = openPorts.get(config.getPort());
            if (port == null || !port.isOpen()) {
                logger.warn("Port {} is not open for device: {}", config.getPort(), deviceId);
                return false;
            }
            
            // Send a test Modbus RTU request
            byte[] testRequest = createModbusRTURequest(config.getUnitId(), 3, 0, 1);
            int bytesWritten = port.writeBytes(testRequest, testRequest.length);
            
            if (bytesWritten != testRequest.length) {
                logger.warn("Failed to write test request to device: {}", deviceId);
                return false;
            }
            
            // Wait for response
            Thread.sleep(200);
            
            // Check for response
            byte[] response = new byte[256];
            int bytesRead = port.readBytes(response, response.length);
            
            boolean success = bytesRead > 0 && isValidModbusRTUResponse(response);
            logger.info("Communication test for device {}: {}", deviceId, success ? "PASSED" : "FAILED");
            
            return success;
        } catch (Exception e) {
            logger.error("Failed to test communication with device {}: {}", deviceId, e.getMessage());
            return false;
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            logger.info("Cleaning up RS485 Serial Service...");
            
            // Close all open ports
            openPorts.forEach((portName, port) -> {
                if (port.isOpen()) {
                    port.closePort();
                    logger.info("Closed port: {}", portName);
                }
            });
            openPorts.clear();
            
            // Shutdown scheduler
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            
            logger.info("RS485 Serial Service cleaned up successfully");
        } catch (Exception e) {
            logger.error("Error during RS485 Serial Service cleanup", e);
        }
    }
}