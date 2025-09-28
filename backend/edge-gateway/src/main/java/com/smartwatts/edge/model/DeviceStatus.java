package com.smartwatts.edge.model;

import java.time.LocalDateTime;
import java.util.List;

public class DeviceStatus {
    private String deviceId;
    private String protocol;
    private String status;
    private LocalDateTime lastSeen;
    private String location;
    private List<String> capabilities;
    private DeviceReading lastReading;
    private String firmwareVersion;
    private String hardwareVersion;
    private String ipAddress;
    private int port;
    private String macAddress;
    private String manufacturer;
    private String model;
    
    // Default constructor
    public DeviceStatus() {}
    
    // Getters and Setters
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public String getProtocol() {
        return protocol;
    }
    
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getLastSeen() {
        return lastSeen;
    }
    
    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public List<String> getCapabilities() {
        return capabilities;
    }
    
    public void setCapabilities(List<String> capabilities) {
        this.capabilities = capabilities;
    }
    
    public DeviceReading getLastReading() {
        return lastReading;
    }
    
    public void setLastReading(DeviceReading lastReading) {
        this.lastReading = lastReading;
    }
    
    public String getFirmwareVersion() {
        return firmwareVersion;
    }
    
    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }
    
    public String getHardwareVersion() {
        return hardwareVersion;
    }
    
    public void setHardwareVersion(String hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public String getMacAddress() {
        return macAddress;
    }
    
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
    
    public String getManufacturer() {
        return manufacturer;
    }
    
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    @Override
    public String toString() {
        return "DeviceStatus{" +
                "deviceId='" + deviceId + '\'' +
                ", protocol='" + protocol + '\'' +
                ", status='" + status + '\'' +
                ", lastSeen=" + lastSeen +
                ", location='" + location + '\'' +
                ", capabilities=" + capabilities +
                '}';
    }
}
