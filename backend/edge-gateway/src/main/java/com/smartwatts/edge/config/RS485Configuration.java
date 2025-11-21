package com.smartwatts.edge.config;

// import com.fazecast.jSerialComm.SerialPort; // Will be available at runtime
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * RS485 Serial Communication Configuration
 * Manages serial port settings and RS485 device configurations
 */
@Configuration
@ConfigurationProperties(prefix = "edge.rs485")
@ConditionalOnProperty(name = "edge.rs485.enabled", havingValue = "true", matchIfMissing = true)
public class RS485Configuration {

    private static final Logger logger = LoggerFactory.getLogger(RS485Configuration.class);

    @NotBlank(message = "Default serial port is required")
    private String defaultPort = "/dev/ttyUSB0";

    @Min(value = 1200, message = "Baud rate must be at least 1200")
    @Max(value = 115200, message = "Baud rate must be at most 115200")
    private int defaultBaudRate = 9600;

    @Min(value = 5, message = "Data bits must be at least 5")
    @Max(value = 8, message = "Data bits must be at most 8")
    private int defaultDataBits = 8;

    @NotNull(message = "Parity setting is required")
    private SerialPortParity defaultParity = SerialPortParity.NONE;

    @Min(value = 1, message = "Stop bits must be at least 1")
    @Max(value = 2, message = "Stop bits must be at most 2")
    private int defaultStopBits = 1;

    @Min(value = 100, message = "Read timeout must be at least 100ms")
    @Max(value = 10000, message = "Read timeout must be at most 10000ms")
    private int readTimeout = 1000;

    @Min(value = 100, message = "Write timeout must be at least 100ms")
    @Max(value = 10000, message = "Write timeout must be at most 10000ms")
    private int writeTimeout = 1000;

    @Min(value = 10, message = "Polling interval must be at least 10ms")
    @Max(value = 60000, message = "Polling interval must be at most 60000ms")
    private int pollingInterval = 5000;

    private boolean autoDiscovery = true;
    private boolean enableLogging = true;
    private int maxRetryAttempts = 3;
    private int retryDelay = 1000;

    // Device-specific configurations
    private Map<String, RS485DeviceConfig> devices = new ConcurrentHashMap<>();

    // Available serial ports
    private List<String> availablePorts;

    @Bean
    @Primary
    public RS485Configuration rs485Configuration() {
        return this;
    }

    @PostConstruct
    void applyDefaultsIfNeeded() {
        boolean adjusted = false;

        if (defaultPort == null || defaultPort.isBlank()) {
            defaultPort = "/dev/ttyS0";
            adjusted = true;
        }
        if (defaultBaudRate < 1200) {
            defaultBaudRate = 9600;
            adjusted = true;
        }
        if (defaultDataBits < 5) {
            defaultDataBits = 8;
            adjusted = true;
        }
        if (defaultParity == null) {
            defaultParity = SerialPortParity.NONE;
            adjusted = true;
        }
        if (defaultStopBits < 1) {
            defaultStopBits = 1;
            adjusted = true;
        }
        if (readTimeout < 100) {
            readTimeout = 1000;
            adjusted = true;
        }
        if (writeTimeout < 100) {
            writeTimeout = 1000;
            adjusted = true;
        }
        if (pollingInterval < 10) {
            pollingInterval = 5000;
            adjusted = true;
        }

        if (adjusted) {
            logger.warn("RS485 configuration contained invalid values. Falling back to safe defaults for Cloud Run.");
        }
    }

    /**
     * Get configuration for a specific device
     */
    public RS485DeviceConfig getDeviceConfig(String deviceId) {
        return devices.getOrDefault(deviceId, createDefaultDeviceConfig());
    }

    /**
     * Create default device configuration
     */
    private RS485DeviceConfig createDefaultDeviceConfig() {
        RS485DeviceConfig config = new RS485DeviceConfig();
        config.setPort(defaultPort);
        config.setBaudRate(defaultBaudRate);
        config.setDataBits(defaultDataBits);
        config.setParity(defaultParity);
        config.setStopBits(defaultStopBits);
        config.setReadTimeout(readTimeout);
        config.setWriteTimeout(writeTimeout);
        return config;
    }

    /**
     * Refresh available serial ports
     */
    public void refreshAvailablePorts() {
        try {
            // Use reflection to avoid compile-time dependency
            Class<?> serialPortClass = Class.forName("com.fazecast.jSerialComm.SerialPort");
            Object[] ports = (Object[]) serialPortClass.getMethod("getCommPorts").invoke(null);
            this.availablePorts = List.of(ports).stream()
                    .map(port -> {
                        try {
                            return (String) port.getClass().getMethod("getSystemPortName").invoke(port);
                        } catch (Exception e) {
                            return "unknown";
                        }
                    })
                    .toList();
        } catch (Exception e) {
            // Fallback to common port names
            this.availablePorts = List.of("/dev/ttyUSB0", "/dev/ttyUSB1", "/dev/ttyACM0", "/dev/ttyACM1");
        }
    }

    /**
     * Get available serial ports
     */
    public List<String> getAvailablePorts() {
        if (availablePorts == null) {
            refreshAvailablePorts();
        }
        return availablePorts;
    }

    // Getters and Setters
    public String getDefaultPort() { return defaultPort; }
    public void setDefaultPort(String defaultPort) { this.defaultPort = defaultPort; }

    public int getDefaultBaudRate() { return defaultBaudRate; }
    public void setDefaultBaudRate(int defaultBaudRate) { this.defaultBaudRate = defaultBaudRate; }

    public int getDefaultDataBits() { return defaultDataBits; }
    public void setDefaultDataBits(int defaultDataBits) { this.defaultDataBits = defaultDataBits; }

    public SerialPortParity getDefaultParity() { return defaultParity; }
    public void setDefaultParity(SerialPortParity defaultParity) { this.defaultParity = defaultParity; }

    public int getDefaultStopBits() { return defaultStopBits; }
    public void setDefaultStopBits(int defaultStopBits) { this.defaultStopBits = defaultStopBits; }

    public int getReadTimeout() { return readTimeout; }
    public void setReadTimeout(int readTimeout) { this.readTimeout = readTimeout; }

    public int getWriteTimeout() { return writeTimeout; }
    public void setWriteTimeout(int writeTimeout) { this.writeTimeout = writeTimeout; }

    public int getPollingInterval() { return pollingInterval; }
    public void setPollingInterval(int pollingInterval) { this.pollingInterval = pollingInterval; }

    public boolean isAutoDiscovery() { return autoDiscovery; }
    public void setAutoDiscovery(boolean autoDiscovery) { this.autoDiscovery = autoDiscovery; }

    public boolean isEnableLogging() { return enableLogging; }
    public void setEnableLogging(boolean enableLogging) { this.enableLogging = enableLogging; }

    public int getMaxRetryAttempts() { return maxRetryAttempts; }
    public void setMaxRetryAttempts(int maxRetryAttempts) { this.maxRetryAttempts = maxRetryAttempts; }

    public int getRetryDelay() { return retryDelay; }
    public void setRetryDelay(int retryDelay) { this.retryDelay = retryDelay; }

    public Map<String, RS485DeviceConfig> getDevices() { return devices; }
    public void setDevices(Map<String, RS485DeviceConfig> devices) { this.devices = devices; }

    /**
     * RS485 Device Configuration
     */
    public static class RS485DeviceConfig {
        private String port;
        private int baudRate = 9600;
        private int dataBits = 8;
        private SerialPortParity parity = SerialPortParity.NONE;
        private int stopBits = 1;
        private int readTimeout = 1000;
        private int writeTimeout = 1000;
        private int unitId = 1;
        private int startAddress = 0;
        private int registerCount = 10;
        private int controlAddress = 0;
        private String deviceType = "INVERTER";
        private String manufacturer = "UNKNOWN";
        private String model = "UNKNOWN";
        private boolean enabled = true;

        // Getters and Setters
        public String getPort() { return port; }
        public void setPort(String port) { this.port = port; }

        public int getBaudRate() { return baudRate; }
        public void setBaudRate(int baudRate) { this.baudRate = baudRate; }

        public int getDataBits() { return dataBits; }
        public void setDataBits(int dataBits) { this.dataBits = dataBits; }

        public SerialPortParity getParity() { return parity; }
        public void setParity(SerialPortParity parity) { this.parity = parity; }

        public int getStopBits() { return stopBits; }
        public void setStopBits(int stopBits) { this.stopBits = stopBits; }

        public int getReadTimeout() { return readTimeout; }
        public void setReadTimeout(int readTimeout) { this.readTimeout = readTimeout; }

        public int getWriteTimeout() { return writeTimeout; }
        public void setWriteTimeout(int writeTimeout) { this.writeTimeout = writeTimeout; }

        public int getUnitId() { return unitId; }
        public void setUnitId(int unitId) { this.unitId = unitId; }

        public int getStartAddress() { return startAddress; }
        public void setStartAddress(int startAddress) { this.startAddress = startAddress; }

        public int getRegisterCount() { return registerCount; }
        public void setRegisterCount(int registerCount) { this.registerCount = registerCount; }

        public int getControlAddress() { return controlAddress; }
        public void setControlAddress(int controlAddress) { this.controlAddress = controlAddress; }

        public String getDeviceType() { return deviceType; }
        public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

        public String getManufacturer() { return manufacturer; }
        public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    /**
     * Serial Port Parity Options
     */
    public enum SerialPortParity {
        NONE(0),
        ODD(1),
        EVEN(2),
        MARK(3),
        SPACE(4);

        private final int value;

        SerialPortParity(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
