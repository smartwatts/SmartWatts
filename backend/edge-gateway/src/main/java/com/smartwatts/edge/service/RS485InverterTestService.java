package com.smartwatts.edge.service;

import com.smartwatts.edge.config.RS485Configuration;
import com.smartwatts.edge.model.DeviceReading;
import com.smartwatts.edge.protocol.ModbusProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RS485 Inverter Testing Service
 * Provides comprehensive testing capabilities for RS485-based inverters
 */
@Service
public class RS485InverterTestService {

    private static final Logger logger = LoggerFactory.getLogger(RS485InverterTestService.class);

    @Autowired
    private RS485SerialService rs485Service;

    @Autowired
    private ModbusProtocolHandler modbusHandler;

    @Autowired
    private RS485Configuration rs485Config;

    // Test results storage
    private final Map<String, InverterTestResult> testResults = new ConcurrentHashMap<>();
    private final Map<String, List<DeviceReading>> testReadings = new ConcurrentHashMap<>();

    // Common inverter configurations for testing
    private final Map<String, InverterTestConfig> knownInverters = new HashMap<>();

    @PostConstruct
    public void initialize() {
        initializeKnownInverters();
        logger.info("RS485 Inverter Test Service initialized with {} known inverter types", knownInverters.size());
    }

    /**
     * Initialize known inverter configurations for testing - production ready
     */
    private void initializeKnownInverters() {
        // Real device discovery - no mock configurations
        // Inverters will be discovered through actual RS485 scanning
        logger.info("RS485 inverter discovery enabled - no mock configurations");
    }

    /**
     * Test all available RS485 inverters through real device discovery
     */
    public Map<String, InverterTestResult> testAllInverters() {
        logger.info("Starting real RS485 inverter discovery and testing...");
        
        Map<String, InverterTestResult> results = new HashMap<>();
        
        // Discover real inverters through RS485 scanning
        List<String> availablePorts = rs485Config.getAvailablePorts();
        for (String port : availablePorts) {
            try {
                // Test each port for inverter communication
                InverterTestResult result = discoverAndTestInverter(port);
                if (result != null) {
                    results.put(port, result);
                    testResults.put(port, result);
                }
            } catch (Exception e) {
                logger.error("Failed to test port {}: {}", port, e.getMessage());
                InverterTestResult errorResult = new InverterTestResult();
                errorResult.setInverterType("UNKNOWN");
                errorResult.setSuccess(false);
                errorResult.setErrorMessage(e.getMessage());
                results.put(port, errorResult);
            }
        }
        
        logger.info("Completed RS485 inverter testing. {} inverters tested", results.size());
        return results;
    }

    /**
     * Discover and test inverter on a specific port
     */
    private InverterTestResult discoverAndTestInverter(String port) {
        logger.info("Discovering inverter on port: {}", port);
        
        InverterTestResult result = new InverterTestResult();
        result.setInverterType("DISCOVERED");
        result.setTestStartTime(LocalDateTime.now());
        
        try {
            // Test if port is available
            boolean portAvailable = testSerialPortAvailability(port);
            result.setPortAvailable(portAvailable);
            
            if (!portAvailable) {
                result.setSuccess(false);
                result.setErrorMessage("Port not available: " + port);
                return result;
            }
            
            // Try to identify inverter type through communication
            String inverterType = identifyInverterType(port);
            result.setInverterType(inverterType);
            
            // Create a basic test config from the port
            InverterTestConfig testConfig = createTestConfigFromPort(port, inverterType);
            
            // Test Modbus RTU communication
            boolean modbusWorking = testModbusRTUCommunication(testConfig);
            result.setModbusWorking(modbusWorking);
            
            if (!modbusWorking) {
                result.setSuccess(false);
                result.setErrorMessage("Modbus RTU communication failed");
                return result;
            }
            
            // Test register reading
            List<DeviceReading> readings = testRegisterReading(testConfig);
            result.setReadingsCount(readings.size());
            result.setTestReadings(readings);
            
            // Test data parsing
            boolean dataParsing = testDataParsing(readings, testConfig);
            result.setDataParsingWorking(dataParsing);
            
            // Test continuous polling
            boolean continuousPolling = testContinuousPolling(testConfig);
            result.setContinuousPollingWorking(continuousPolling);
            
            // Overall success
            result.setSuccess(portAvailable && modbusWorking && dataParsing && continuousPolling);
            result.setTestEndTime(LocalDateTime.now());
            
            logger.info("Inverter discovery completed for port {}: {}", port, result.isSuccess() ? "SUCCESS" : "FAILED");
            return result;
            
        } catch (Exception e) {
            logger.error("Error discovering inverter on port {}: {}", port, e.getMessage());
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            result.setTestEndTime(LocalDateTime.now());
            return result;
        }
    }

    /**
     * Identify inverter type through communication
     */
    private String identifyInverterType(String port) {
        // Try to identify inverter by sending different Modbus requests
        // and analyzing responses
        try {
            // This would contain real inverter identification logic
            return "UNKNOWN_INVERTER";
        } catch (Exception e) {
            logger.debug("Could not identify inverter type on port {}: {}", port, e.getMessage());
            return "UNKNOWN";
        }
    }

    /**
     * Create a basic test configuration from a port
     */
    private InverterTestConfig createTestConfigFromPort(String port, String inverterType) {
        // Create a default configuration for testing discovered inverters
        // These are default values that should work for most inverters
        return new InverterTestConfig(
            "DISCOVERED",
            inverterType,
            port,
            9600,  // Default baud rate
            1,     // Default unit ID
            0,     // Default start address
            10,    // Default register count
            "INVERTER"
        );
    }

    /**
     * Test a specific inverter type
     */
    public InverterTestResult testInverter(String inverterType, InverterTestConfig config) {
        logger.info("Testing inverter: {} ({})", config.getManufacturer(), config.getModel());
        
        InverterTestResult result = new InverterTestResult();
        result.setInverterType(inverterType);
        result.setManufacturer(config.getManufacturer());
        result.setModel(config.getModel());
        result.setTestStartTime(LocalDateTime.now());
        
        try {
            // Step 1: Test serial port availability
            boolean portAvailable = testSerialPortAvailability(config.getSerialPort());
            result.setPortAvailable(portAvailable);
            
            if (!portAvailable) {
                result.setSuccess(false);
                result.setErrorMessage("Serial port not available: " + config.getSerialPort());
                return result;
            }
            
            // Step 2: Test Modbus RTU communication
            boolean modbusWorking = testModbusRTUCommunication(config);
            result.setModbusWorking(modbusWorking);
            
            if (!modbusWorking) {
                result.setSuccess(false);
                result.setErrorMessage("Modbus RTU communication failed");
                return result;
            }
            
            // Step 3: Test register reading
            List<DeviceReading> readings = testRegisterReading(config);
            result.setReadingsCount(readings.size());
            result.setTestReadings(readings);
            testReadings.put(inverterType, readings);
            
            // Step 4: Test data parsing
            boolean dataParsing = testDataParsing(readings, config);
            result.setDataParsingWorking(dataParsing);
            
            // Step 5: Test continuous polling
            boolean continuousPolling = testContinuousPolling(config);
            result.setContinuousPollingWorking(continuousPolling);
            
            // Overall success
            result.setSuccess(portAvailable && modbusWorking && dataParsing && continuousPolling);
            result.setTestEndTime(LocalDateTime.now());
            
            if (result.isSuccess()) {
                logger.info("Inverter test PASSED: {} {}", config.getManufacturer(), config.getModel());
            } else {
                logger.warn("Inverter test FAILED: {} {}", config.getManufacturer(), config.getModel());
            }
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMessage("Test failed with exception: " + e.getMessage());
            logger.error("Inverter test failed for {}: {}", inverterType, e.getMessage(), e);
        }
        
        return result;
    }

    /**
     * Test serial port availability
     */
    private boolean testSerialPortAvailability(String portName) {
        try {
            List<String> availablePorts = rs485Config.getAvailablePorts();
            boolean available = availablePorts.contains(portName);
            
            logger.debug("Serial port {} availability: {}", portName, available);
            return available;
        } catch (Exception e) {
            logger.error("Error checking serial port availability: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Test Modbus RTU communication
     */
    private boolean testModbusRTUCommunication(InverterTestConfig config) {
        try {
            // Create device configuration
            RS485Configuration.RS485DeviceConfig deviceConfig = new RS485Configuration.RS485DeviceConfig();
            deviceConfig.setPort(config.getSerialPort());
            deviceConfig.setBaudRate(config.getBaudRate());
            deviceConfig.setUnitId(config.getUnitId());
            deviceConfig.setStartAddress(config.getStartAddress());
            deviceConfig.setRegisterCount(config.getRegisterCount());
            deviceConfig.setDeviceType(config.getDeviceType());
            deviceConfig.setManufacturer(config.getManufacturer());
            deviceConfig.setModel(config.getModel());
            
            // Add device to RS485 service
            String deviceId = config.getManufacturer() + "_" + config.getModel() + "_" + System.currentTimeMillis();
            rs485Service.addDevice(deviceId, deviceConfig);
            
            // Test communication
            boolean success = rs485Service.testDeviceCommunication(deviceId);
            
            // Clean up
            rs485Service.removeDevice(deviceId);
            
            logger.debug("Modbus RTU communication test for {}: {}", config.getModel(), success);
            return success;
        } catch (Exception e) {
            logger.error("Modbus RTU communication test failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Test register reading
     */
    private List<DeviceReading> testRegisterReading(InverterTestConfig config) {
        List<DeviceReading> readings = new ArrayList<>();
        
        try {
            // Create Modbus device configuration
            ModbusProtocolHandler.ModbusDeviceConfig modbusConfig = new ModbusProtocolHandler.ModbusDeviceConfig(
                config.getSerialPort(),
                config.getBaudRate(),
                config.getUnitId(),
                config.getStartAddress(),
                config.getRegisterCount(),
                0,
                true // RTU mode
            );
            
            // Read data multiple times to test consistency
            for (int i = 0; i < 5; i++) {
                DeviceReading reading = modbusHandler.readDeviceData("test_device", modbusConfig);
                if (reading != null) {
                    readings.add(reading);
                }
                Thread.sleep(1000); // Wait between reads
            }
            
            logger.debug("Register reading test completed. {} readings collected", readings.size());
        } catch (Exception e) {
            logger.error("Register reading test failed: {}", e.getMessage());
        }
        
        return readings;
    }

    /**
     * Test data parsing
     */
    private boolean testDataParsing(List<DeviceReading> readings, InverterTestConfig config) {
        if (readings.isEmpty()) {
            return false;
        }
        
        try {
            // Check if readings have valid data
            boolean validData = readings.stream().allMatch(reading -> 
                reading.getEnergyConsumption() >= 0 &&
                reading.getVoltage() > 0 &&
                reading.getCurrent() >= 0 &&
                reading.getPowerFactor() > 0 &&
                reading.getPowerFactor() <= 1
            );
            
            logger.debug("Data parsing test for {}: {}", config.getModel(), validData);
            return validData;
        } catch (Exception e) {
            logger.error("Data parsing test failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Test continuous polling
     */
    private boolean testContinuousPolling(InverterTestConfig config) {
        try {
            // Simulate continuous polling for 30 seconds
            long startTime = System.currentTimeMillis();
            int successfulReads = 0;
            int totalReads = 0;
            
            while (System.currentTimeMillis() - startTime < 30000) { // 30 seconds
                // Create device configuration
                ModbusProtocolHandler.ModbusDeviceConfig modbusConfig = new ModbusProtocolHandler.ModbusDeviceConfig(
                    config.getSerialPort(),
                    config.getBaudRate(),
                    config.getUnitId(),
                    config.getStartAddress(),
                    config.getRegisterCount(),
                    0,
                    true
                );
                
                DeviceReading reading = modbusHandler.readDeviceData("test_device", modbusConfig);
                totalReads++;
                
                if (reading != null) {
                    successfulReads++;
                }
                
                Thread.sleep(2000); // Poll every 2 seconds
            }
            
            double successRate = (double) successfulReads / totalReads;
            boolean success = successRate >= 0.8; // 80% success rate required
            
            logger.debug("Continuous polling test for {}: {}% success rate", config.getModel(), successRate * 100);
            return success;
        } catch (Exception e) {
            logger.error("Continuous polling test failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get test results for a specific inverter type
     */
    public InverterTestResult getTestResult(String inverterType) {
        return testResults.get(inverterType);
    }

    /**
     * Get all test results
     */
    public Map<String, InverterTestResult> getAllTestResults() {
        return new HashMap<>(testResults);
    }

    /**
     * Get test readings for a specific inverter type
     */
    public List<DeviceReading> getTestReadings(String inverterType) {
        return testReadings.getOrDefault(inverterType, new ArrayList<>());
    }

    /**
     * Generate test report
     */
    public String generateTestReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== RS485 Inverter Test Report ===\n");
        report.append("Generated: ").append(LocalDateTime.now()).append("\n\n");
        
        int totalTests = testResults.size();
        int successfulTests = (int) testResults.values().stream().filter(InverterTestResult::isSuccess).count();
        
        report.append("Summary:\n");
        report.append("- Total Inverters Tested: ").append(totalTests).append("\n");
        report.append("- Successful Tests: ").append(successfulTests).append("\n");
        report.append("- Failed Tests: ").append(totalTests - successfulTests).append("\n");
        report.append("- Success Rate: ").append(String.format("%.1f%%", (double) successfulTests / totalTests * 100)).append("\n\n");
        
        report.append("Detailed Results:\n");
        testResults.forEach((inverterType, result) -> {
            report.append("\n--- ").append(inverterType).append(" ---\n");
            report.append("Manufacturer: ").append(result.getManufacturer()).append("\n");
            report.append("Model: ").append(result.getModel()).append("\n");
            report.append("Success: ").append(result.isSuccess() ? "YES" : "NO").append("\n");
            report.append("Port Available: ").append(result.isPortAvailable() ? "YES" : "NO").append("\n");
            report.append("Modbus Working: ").append(result.isModbusWorking() ? "YES" : "NO").append("\n");
            report.append("Data Parsing: ").append(result.isDataParsingWorking() ? "YES" : "NO").append("\n");
            report.append("Continuous Polling: ").append(result.isContinuousPollingWorking() ? "YES" : "NO").append("\n");
            report.append("Readings Count: ").append(result.getReadingsCount()).append("\n");
            
            if (result.getErrorMessage() != null) {
                report.append("Error: ").append(result.getErrorMessage()).append("\n");
            }
        });
        
        return report.toString();
    }

    /**
     * Inverter Test Configuration
     */
    public static class InverterTestConfig {
        private String manufacturer;
        private String model;
        private String serialPort;
        private int baudRate;
        private int unitId;
        private int startAddress;
        private int registerCount;
        private String deviceType;

        public InverterTestConfig(String manufacturer, String model, String serialPort, int baudRate, 
                                int unitId, int startAddress, int registerCount, String deviceType) {
            this.manufacturer = manufacturer;
            this.model = model;
            this.serialPort = serialPort;
            this.baudRate = baudRate;
            this.unitId = unitId;
            this.startAddress = startAddress;
            this.registerCount = registerCount;
            this.deviceType = deviceType;
        }

        // Getters
        public String getManufacturer() { return manufacturer; }
        public String getModel() { return model; }
        public String getSerialPort() { return serialPort; }
        public int getBaudRate() { return baudRate; }
        public int getUnitId() { return unitId; }
        public int getStartAddress() { return startAddress; }
        public int getRegisterCount() { return registerCount; }
        public String getDeviceType() { return deviceType; }
    }

    /**
     * Inverter Test Result
     */
    public static class InverterTestResult {
        private String inverterType;
        private String manufacturer;
        private String model;
        private boolean success;
        private boolean portAvailable;
        private boolean modbusWorking;
        private boolean dataParsingWorking;
        private boolean continuousPollingWorking;
        private int readingsCount;
        private List<DeviceReading> testReadings;
        private String errorMessage;
        private LocalDateTime testStartTime;
        private LocalDateTime testEndTime;

        // Getters and Setters
        public String getInverterType() { return inverterType; }
        public void setInverterType(String inverterType) { this.inverterType = inverterType; }

        public String getManufacturer() { return manufacturer; }
        public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public boolean isPortAvailable() { return portAvailable; }
        public void setPortAvailable(boolean portAvailable) { this.portAvailable = portAvailable; }

        public boolean isModbusWorking() { return modbusWorking; }
        public void setModbusWorking(boolean modbusWorking) { this.modbusWorking = modbusWorking; }

        public boolean isDataParsingWorking() { return dataParsingWorking; }
        public void setDataParsingWorking(boolean dataParsingWorking) { this.dataParsingWorking = dataParsingWorking; }

        public boolean isContinuousPollingWorking() { return continuousPollingWorking; }
        public void setContinuousPollingWorking(boolean continuousPollingWorking) { this.continuousPollingWorking = continuousPollingWorking; }

        public int getReadingsCount() { return readingsCount; }
        public void setReadingsCount(int readingsCount) { this.readingsCount = readingsCount; }

        public List<DeviceReading> getTestReadings() { return testReadings; }
        public void setTestReadings(List<DeviceReading> testReadings) { this.testReadings = testReadings; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        public LocalDateTime getTestStartTime() { return testStartTime; }
        public void setTestStartTime(LocalDateTime testStartTime) { this.testStartTime = testStartTime; }

        public LocalDateTime getTestEndTime() { return testEndTime; }
        public void setTestEndTime(LocalDateTime testEndTime) { this.testEndTime = testEndTime; }
    }
}
