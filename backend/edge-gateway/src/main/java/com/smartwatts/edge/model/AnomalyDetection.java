package com.smartwatts.edge.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Anomaly Detection Model for ML Anomaly Detection Results
 * Represents detected energy consumption anomalies with severity and context
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyDetection {
    
    /**
     * Unique identifier for the anomaly detection
     */
    private String id;
    
    /**
     * Timestamp when the anomaly was detected
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    /**
     * Device ID where the anomaly was detected
     */
    private String deviceId;
    
    /**
     * Facility ID where the anomaly was detected
     */
    private String facilityId;
    
    /**
     * Metric name that triggered the anomaly
     */
    private String metric;
    
    /**
     * Type of anomaly detected
     */
    private AnomalyType anomalyType;
    
    /**
     * Severity level of the anomaly
     */
    private AnomalySeverity severity;
    
    /**
     * Confidence score of the detection (0.0 - 1.0)
     */
    private double confidence;
    
    /**
     * Description of the anomaly
     */
    private String description;
    
    /**
     * Expected value (normal behavior)
     */
    private double expectedValue;
    
    /**
     * Actual value (anomalous behavior)
     */
    private double actualValue;
    
    /**
     * Deviation from expected value
     */
    private double deviation;
    
    /**
     * Percentage deviation from expected value
     */
    private double deviationPercentage;
    
    /**
     * Threshold that was exceeded
     */
    private double threshold;
    
    /**
     * Additional context data
     */
    private Map<String, Object> context;
    
    /**
     * Model version used for detection
     */
    private String modelVersion;
    
    /**
     * Whether the anomaly has been acknowledged
     */
    private boolean acknowledged;
    
    /**
     * Timestamp when anomaly was acknowledged
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime acknowledgedAt;
    
    /**
     * User who acknowledged the anomaly
     */
    private String acknowledgedBy;
    
    /**
     * Recommended actions to resolve the anomaly
     */
    private String recommendedActions;
    
    /**
     * Whether the anomaly is still active
     */
    private boolean active;
    
    /**
     * Resolution timestamp if anomaly is resolved
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime resolvedAt;
    
    /**
     * Resolution notes
     */
    private String resolutionNotes;
    
    /**
     * Get confidence as percentage
     */
    public double getConfidencePercentage() {
        return confidence * 100.0;
    }
    
    /**
     * Check if anomaly is high confidence
     */
    public boolean isHighConfidence() {
        return confidence >= 0.8;
    }
    
    /**
     * Check if anomaly is critical
     */
    public boolean isCritical() {
        return severity == AnomalySeverity.CRITICAL;
    }
    
    /**
     * Check if anomaly requires immediate attention
     */
    public boolean requiresImmediateAttention() {
        return severity == AnomalySeverity.CRITICAL || severity == AnomalySeverity.HIGH;
    }
    
    /**
     * Get formatted deviation percentage
     */
    public String getFormattedDeviationPercentage() {
        return String.format("%.2f%%", deviationPercentage);
    }
    
    /**
     * Get time since detection
     */
    public long getTimeSinceDetection() {
        if (timestamp != null) {
            return java.time.Duration.between(timestamp, LocalDateTime.now()).toMinutes();
        }
        return 0;
    }
    
    /**
     * Anomaly types
     */
    public enum AnomalyType {
        VOLTAGE_SPIKE,
        VOLTAGE_DROP,
        CURRENT_SURGE,
        CURRENT_DROP,
        POWER_ANOMALY,
        ENERGY_CONSUMPTION_SPIKE,
        ENERGY_CONSUMPTION_DROP,
        FREQUENCY_DEVIATION,
        POWER_FACTOR_ANOMALY,
        HARMONIC_DISTORTION,
        TEMPERATURE_ANOMALY,
        COMMUNICATION_FAILURE,
        DEVICE_MALFUNCTION,
        GRID_ANOMALY,
        SOLAR_PANEL_ISSUE,
        INVERTER_FAULT,
        GENERATOR_ANOMALY,
        BATTERY_ANOMALY,
        LOAD_SHIFTING,
        PEAK_DEMAND_ANOMALY,
        OFF_PEAK_ANOMALY,
        SEASONAL_ANOMALY,
        WEATHER_RELATED,
        MAINTENANCE_REQUIRED,
        UNKNOWN
    }
    
    /**
     * Anomaly severity levels
     */
    public enum AnomalySeverity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}
