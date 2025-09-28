package com.smartwatts.edge.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Optimization Recommendation Model for ML Optimization Results
 * Represents energy optimization recommendations with implementation details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationRecommendation {
    
    /**
     * Unique identifier for the recommendation
     */
    private String id;
    
    /**
     * Timestamp when the recommendation was generated
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    /**
     * Device ID for which the recommendation was made
     */
    private String deviceId;
    
    /**
     * Facility ID for which the recommendation was made
     */
    private String facilityId;
    
    /**
     * Type of optimization recommendation
     */
    private OptimizationType optimizationType;
    
    /**
     * Priority level of the recommendation
     */
    private PriorityLevel priority;
    
    /**
     * Category of the optimization
     */
    private OptimizationCategory category;
    
    /**
     * Title of the recommendation
     */
    private String title;
    
    /**
     * Detailed description of the recommendation
     */
    private String description;
    
    /**
     * Expected energy savings in kWh
     */
    private double expectedEnergySavings;
    
    /**
     * Expected cost savings in local currency
     */
    private double expectedCostSavings;
    
    /**
     * Expected percentage improvement
     */
    private double expectedImprovementPercentage;
    
    /**
     * Implementation difficulty (1-10 scale)
     */
    private int implementationDifficulty;
    
    /**
     * Estimated implementation time in hours
     */
    private double estimatedImplementationTime;
    
    /**
     * Estimated implementation cost
     */
    private double estimatedImplementationCost;
    
    /**
     * Payback period in months
     */
    private double paybackPeriod;
    
    /**
     * Return on investment percentage
     */
    private double returnOnInvestment;
    
    /**
     * Current energy consumption baseline
     */
    private double currentBaseline;
    
    /**
     * Optimized energy consumption target
     */
    private double optimizedTarget;
    
    /**
     * Confidence score of the recommendation (0.0 - 1.0)
     */
    private double confidence;
    
    /**
     * Model version used for the recommendation
     */
    private String modelVersion;
    
    /**
     * Implementation steps
     */
    private List<String> implementationSteps;
    
    /**
     * Required resources
     */
    private List<String> requiredResources;
    
    /**
     * Potential risks
     */
    private List<String> potentialRisks;
    
    /**
     * Success metrics
     */
    private List<String> successMetrics;
    
    /**
     * Additional context data
     */
    private Map<String, Object> context;
    
    /**
     * Whether the recommendation has been implemented
     */
    private boolean implemented;
    
    /**
     * Implementation timestamp
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime implementedAt;
    
    /**
     * User who implemented the recommendation
     */
    private String implementedBy;
    
    /**
     * Implementation notes
     */
    private String implementationNotes;
    
    /**
     * Actual energy savings achieved
     */
    private Double actualEnergySavings;
    
    /**
     * Actual cost savings achieved
     */
    private Double actualCostSavings;
    
    /**
     * Whether the recommendation is still active
     */
    private boolean active;
    
    /**
     * Whether the recommendation is actionable
     */
    private boolean isActionable;
    
    /**
     * Expiration timestamp
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;
    
    /**
     * Get confidence as percentage
     */
    public double getConfidencePercentage() {
        return confidence * 100.0;
    }
    
    /**
     * Check if recommendation is high confidence
     */
    public boolean isHighConfidence() {
        return confidence >= 0.8;
    }
    
    /**
     * Check if recommendation is high priority
     */
    public boolean isHighPriority() {
        return priority == PriorityLevel.HIGH || priority == PriorityLevel.CRITICAL;
    }
    
    /**
     * Check if recommendation is cost-effective
     */
    public boolean isCostEffective() {
        return paybackPeriod <= 12.0; // Less than 1 year payback
    }
    
    /**
     * Check if recommendation is easy to implement
     */
    public boolean isEasyToImplement() {
        return implementationDifficulty <= 3;
    }
    
    /**
     * Get formatted expected savings
     */
    public String getFormattedExpectedSavings() {
        return String.format("%.2f kWh (%.2f%%)", expectedEnergySavings, expectedImprovementPercentage);
    }
    
    /**
     * Get formatted payback period
     */
    public String getFormattedPaybackPeriod() {
        if (paybackPeriod < 1.0) {
            return String.format("%.1f months", paybackPeriod * 12);
        }
        return String.format("%.1f months", paybackPeriod);
    }
    
    /**
     * Check if recommendation is expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Get time until expiration
     */
    public long getTimeUntilExpiration() {
        if (expiresAt != null) {
            return java.time.Duration.between(LocalDateTime.now(), expiresAt).toDays();
        }
        return -1;
    }
    
    /**
     * Optimization types
     */
    public enum OptimizationType {
        LOAD_SHIFTING,
        PEAK_DEMAND_REDUCTION,
        ENERGY_EFFICIENCY,
        EQUIPMENT_OPTIMIZATION,
        SCHEDULING_OPTIMIZATION,
        MAINTENANCE_OPTIMIZATION,
        RENEWABLE_INTEGRATION,
        STORAGE_OPTIMIZATION,
        GRID_OPTIMIZATION,
        BEHAVIORAL_CHANGE,
        EQUIPMENT_UPGRADE,
        PROCESS_OPTIMIZATION,
        TEMPERATURE_OPTIMIZATION,
        LIGHTING_OPTIMIZATION,
        HVAC_OPTIMIZATION,
        PRODUCTION_OPTIMIZATION,
        DEMAND_RESPONSE,
        DEMAND_MANAGEMENT,
        TIME_OF_USE_OPTIMIZATION,
        SEASONAL_OPTIMIZATION,
        WEATHER_ADAPTIVE,
        PREDICTIVE_MAINTENANCE,
        ANOMALY_PREVENTION,
        PERFORMANCE_TUNING,
        CONFIGURATION_OPTIMIZATION
    }
    
    /**
     * Priority levels
     */
    public enum PriorityLevel {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
    
    /**
     * Optimization categories
     */
    public enum OptimizationCategory {
        OPERATIONAL,
        TECHNICAL,
        BEHAVIORAL,
        INFRASTRUCTURE,
        MAINTENANCE,
        SCHEDULING,
        EQUIPMENT,
        PROCESS,
        ENERGY_SOURCE,
        STORAGE,
        DISTRIBUTION,
        MONITORING,
        AUTOMATION,
        INTEGRATION,
        UPGRADE,
        REPLACEMENT,
        RETROFIT,
        NEW_INSTALLATION
    }
}
