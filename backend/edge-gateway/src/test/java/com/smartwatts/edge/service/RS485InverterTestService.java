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
     * Initialize known inverter configurations for testing
     */
    private void initializeKnownInverters() {
        // SMA Inverters
        knownInverters.put("SMA_SUNNY_BOY", new InverterTestConfig(
            "SMA", "Sunny Boy", "/dev/ttyUSB0", 9600, 1, 40000, 20, "SOLAR_INVERTER"
        ));

        // Fronius Inverters
        knownInverters.put("FRONIUS_SYMO", new InverterTestConfig(
            "Fronius", "Symo", "/dev/ttyUSB1", 19200, 1, 50000, 25, "SOLAR_INVERTER"
        ));

        // Growatt Inverters
        knownInverters.put("GROWATT_SPH", new InverterTestConfig(
            "Growatt", "SPH", "/dev/ttyUSB2", 9600, 1, 1000, 10, "SOLAR_INVERTER"
        ));

        // Solis Inverters
        knownInverters.put("SOLIS_RHI", new InverterTestConfig(
            "Solis", "RHI", "/dev/ttyUSB3", 9600, 1, 0, 15, "SOLAR_INVERTER"
        ));

        // Deye Inverters
        knownInverters.put("DEYE_SUN", new InverterTestConfig(
            "Deye", "SUN", "/dev/ttyUSB4", 9600, 1, 0, 12, "SOLAR_INVERTER"
        ));

        // Generic Modbus RTU Inverter
        knownInverters.put("GENERIC_MODBUS_RTU", new InverterTestConfig(
            "Generic", "Modbus RTU", "/dev/ttyUSB5", 9600, 1, 0, 8, "SOLAR_INVERTER"
        ));
    }

    /**
     * Test all available RS485 inverters
     */
    public Map<String, InverterTestResult> testAllInverters() {
        logger.info("Starting comprehensive RS485 inverter testing...");
        
        Map<String, InverterTestResult> results = new HashMap<>();
        
        // Test each known inverter type
        knownInverters.forEach((inverterType, config) -> {
            try {
                InverterTestResult result = testInverter(inverterType, config);
                results.put(inverterType, result);
                testResults.put(inverterType, result);
            } catch (Exception e) {
                logger.error("Failed to test inverter type {}: {}", inverterType, e.getMessage());
                InverterTestResult errorResult = new InverterTestResult();
                errorResult.setInverterType(inverterType);
                errorResult.setSuccess(false);
                errorResult.setErrorMessage(e.getMessage());
                results.put(inverterType, errorResult);
            }
        });
        
        logger.info("Completed RS485 inverter testing. {} inverters tested", results.size());
        return results;
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




