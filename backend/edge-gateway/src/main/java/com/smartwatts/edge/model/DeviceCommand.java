package com.smartwatts.edge.model;

import java.time.LocalDateTime;
import java.util.Map;

public class DeviceCommand {
    private String id;
    private String deviceId;
    private String command;
    private String commandType;
    private Map<String, Object> parameters;
    private LocalDateTime timestamp;
    private String status;
    private String result;
    private LocalDateTime executedAt;
    private String executedBy;
    private int priority;
    private boolean requiresConfirmation;
    
    // Default constructor
    public DeviceCommand() {}
    
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
    
    public String getCommand() {
        return command;
    }
    
    public void setCommand(String command) {
        this.command = command;
    }
    
    public String getCommandType() {
        return commandType;
    }
    
    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getResult() {
        return result;
    }
    
    public void setResult(String result) {
        this.result = result;
    }
    
    public LocalDateTime getExecutedAt() {
        return executedAt;
    }
    
    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }
    
    public String getExecutedBy() {
        return executedBy;
    }
    
    public void setExecutedBy(String executedBy) {
        this.executedBy = executedBy;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    public boolean isRequiresConfirmation() {
        return requiresConfirmation;
    }
    
    public void setRequiresConfirmation(boolean requiresConfirmation) {
        this.requiresConfirmation = requiresConfirmation;
    }
    
    @Override
    public String toString() {
        return "DeviceCommand{" +
                "id='" + id + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", command='" + command + '\'' +
                ", commandType='" + commandType + '\'' +
                ", status='" + status + '\'' +
                ", priority=" + priority +
                '}';
    }
}
