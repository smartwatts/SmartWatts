package com.smartwatts.edge.model;

import java.time.LocalDateTime;

public class DeviceReading {
    private String id;
    private String deviceId;
    private LocalDateTime timestamp;
    private double energyConsumption;
    private double powerOutput;
    private double voltage;
    private double current;
    private double frequency;
    private double powerFactor;
    private double temperature;
    private double efficiency;
    private String status;
    private String location;
    
    // Default constructor
    public DeviceReading() {}
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public double getEnergyConsumption() {
        return energyConsumption;
    }
    
    public void setEnergyConsumption(double energyConsumption) {
        this.energyConsumption = energyConsumption;
    }
    
    public double getPowerOutput() {
        return powerOutput;
    }
    
    public void setPowerOutput(double powerOutput) {
        this.powerOutput = powerOutput;
    }
    
    public double getVoltage() {
        return voltage;
    }
    
    public void setVoltage(double voltage) {
        this.voltage = voltage;
    }
    
    public double getCurrent() {
        return current;
    }
    
    public void setCurrent(double current) {
        this.current = current;
    }
    
    public double getFrequency() {
        return frequency;
    }
    
    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }
    
    public double getPowerFactor() {
        return powerFactor;
    }
    
    public void setPowerFactor(double powerFactor) {
        this.powerFactor = powerFactor;
    }
    
    public double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
    
    public double getEfficiency() {
        return efficiency;
    }
    
    public void setEfficiency(double efficiency) {
        this.efficiency = efficiency;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    @Override
    public String toString() {
        return "DeviceReading{" +
                "id='" + id + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", timestamp=" + timestamp +
                ", energyConsumption=" + energyConsumption +
                ", powerOutput=" + powerOutput +
                ", efficiency=" + efficiency +
                ", status='" + status + '\'' +
                '}';
    }
}
