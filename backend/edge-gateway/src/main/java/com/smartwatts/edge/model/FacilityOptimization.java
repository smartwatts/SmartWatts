package com.smartwatts.edge.model;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Model representing a facility optimization recommendation for edge processing
 */
public class FacilityOptimization {
    
    private String optimizationId;
    private String assetId;
    private String optimizationType;
    private String recommendation;
    private String description;
    private Double estimatedSavings;
    private String currency;
    private Integer estimatedImplementationTime;
    private String priority;
    private String status;
    private LocalDateTime timestamp;
    private Map<String, Object> parameters;
    private String createdBy;
    
    // Default constructor
    public FacilityOptimization() {}
    
    // Getters and Setters
    public String getOptimizationId() {
        return optimizationId;
    }
    
    public void setOptimizationId(String optimizationId) {
        this.optimizationId = optimizationId;
    }
    
    public String getAssetId() {
        return assetId;
    }
    
    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }
    
    public String getOptimizationType() {
        return optimizationType;
    }
    
    public void setOptimizationType(String optimizationType) {
        this.optimizationType = optimizationType;
    }
    
    public String getRecommendation() {
        return recommendation;
    }
    
    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Double getEstimatedSavings() {
        return estimatedSavings;
    }
    
    public void setEstimatedSavings(Double estimatedSavings) {
        this.estimatedSavings = estimatedSavings;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public Integer getEstimatedImplementationTime() {
        return estimatedImplementationTime;
    }
    
    public void setEstimatedImplementationTime(Integer estimatedImplementationTime) {
        this.estimatedImplementationTime = estimatedImplementationTime;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    @Override
    public String toString() {
        return "FacilityOptimization{" +
                "optimizationId='" + optimizationId + '\'' +
                ", assetId='" + assetId + '\'' +
                ", optimizationType='" + optimizationType + '\'' +
                ", recommendation='" + recommendation + '\'' +
                ", estimatedSavings=" + estimatedSavings +
                ", priority='" + priority + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
