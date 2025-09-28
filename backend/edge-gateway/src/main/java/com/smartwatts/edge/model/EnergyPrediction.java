package com.smartwatts.edge.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Energy Prediction Model for ML Forecasting Results
 * Represents predicted energy consumption with confidence metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyPrediction {
    
    /**
     * Unique identifier for the prediction
     */
    private String id;
    
    /**
     * Timestamp when the prediction was generated
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    /**
     * Predicted energy consumption in kWh
     */
    private double predictedConsumption;
    
    /**
     * Confidence level of the prediction (0.0 - 1.0)
     */
    private double confidence;
    
    /**
     * Prediction horizon in hours
     */
    private int predictionHorizon;
    
    /**
     * Model version used for prediction
     */
    private String modelVersion;
    
    /**
     * Input factors that influenced the prediction
     */
    private Map<String, Double> factors;
    
    /**
     * Prediction type (e.g., "hourly", "daily", "weekly")
     */
    private String predictionType;
    
    /**
     * Facility ID for which the prediction was made
     */
    private String facilityId;
    
    /**
     * Device ID if prediction is device-specific
     */
    private String deviceId;
    
    /**
     * Additional metadata about the prediction
     */
    private Map<String, Object> metadata;
    
    /**
     * Prediction accuracy metrics
     */
    @JsonProperty("accuracy_metrics")
    private AccuracyMetrics accuracyMetrics;
    
    /**
     * Confidence intervals for the prediction
     */
    @JsonProperty("confidence_intervals")
    private ConfidenceIntervals confidenceIntervals;
    
    /**
     * Model performance indicators
     */
    @JsonProperty("model_performance")
    private ModelPerformance modelPerformance;
    
    /**
     * Get confidence as percentage
     */
    public double getConfidencePercentage() {
        return confidence * 100.0;
    }
    
    /**
     * Check if prediction is high confidence
     */
    public boolean isHighConfidence() {
        return confidence >= 0.8;
    }
    
    /**
     * Check if prediction is low confidence
     */
    public boolean isLowConfidence() {
        return confidence < 0.6;
    }
    
    /**
     * Get formatted prediction time
     */
    public String getFormattedPredictionTime() {
        if (timestamp != null) {
            return timestamp.plusHours(predictionHorizon).format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            );
        }
        return "Unknown";
    }
    
    /**
     * Inner class for accuracy metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccuracyMetrics {
        private double meanAbsoluteError;
        private double rootMeanSquareError;
        private double meanAbsolutePercentageError;
        private double rSquared;
        private int trainingDataPoints;
        private int validationDataPoints;
    }
    
    /**
     * Inner class for confidence intervals
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfidenceIntervals {
        private double lowerBound95;
        private double upperBound95;
        private double lowerBound90;
        private double upperBound90;
        private double standardDeviation;
    }
    
    /**
     * Inner class for model performance
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModelPerformance {
        private double trainingAccuracy;
        private double validationAccuracy;
        private double testAccuracy;
        private long trainingTimeMs;
        private long inferenceTimeMs;
        private String modelType;
        private int modelParameters;
    }
}
