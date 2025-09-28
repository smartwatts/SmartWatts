package com.smartwatts.edge.model;

import java.time.LocalDateTime;

/**
 * Model representing a facility event for edge processing
 */
public class FacilityEvent {
    
    private String eventId;
    private String assetId;
    private String eventType;
    private LocalDateTime timestamp;
    private String description;
    private String severity;
    private String location;
    private String operator;
    private String notes;
    
    // Default constructor
    public FacilityEvent() {}
    
    // Getters and Setters
    public String getEventId() {
        return eventId;
    }
    
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
    
    public String getAssetId() {
        return assetId;
    }
    
    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getSeverity() {
        return severity;
    }
    
    public void setSeverity(String severity) {
        this.severity = severity;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getOperator() {
        return operator;
    }
    
    public void setOperator(String operator) {
        this.operator = operator;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    @Override
    public String toString() {
        return "FacilityEvent{" +
                "eventId='" + eventId + '\'' +
                ", assetId='" + assetId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", timestamp=" + timestamp +
                ", severity='" + severity + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
