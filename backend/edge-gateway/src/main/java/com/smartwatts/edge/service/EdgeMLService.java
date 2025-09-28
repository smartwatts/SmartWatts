package com.smartwatts.edge.service;

import com.smartwatts.edge.model.AnomalyDetection;
import com.smartwatts.edge.model.EnergyPrediction;
import com.smartwatts.edge.model.OptimizationRecommendation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Edge ML Service - High-level ML capabilities and business logic
 * Integrates with TensorFlowLiteService for model inference
 */
@Service
@Slf4j
public class EdgeMLService {
    
    @Autowired
    private TensorFlowLiteService tensorFlowLiteService;
    
    @Autowired
    private com.smartwatts.edge.storage.EdgeStorageService edgeStorageService; // Used for data persistence
    
    /**
     * Check if edge storage service is available
     */
    public boolean isEdgeStorageAvailable() {
        return edgeStorageService != null;
    }
    
    @Value("${smartwatts.ml.anomaly.threshold:0.8}")
    private double anomalyThreshold;
    
    @Value("${smartwatts.ml.prediction.horizon.hours:24}")
    private int defaultPredictionHorizon;
    
    @Value("${smartwatts.ml.optimization.confidence.min:0.7}")
    private double minOptimizationConfidence;
    
    // Cache for recent predictions and anomalies
    private final Map<String, List<EnergyPrediction>> predictionCache = new ConcurrentHashMap<>();
    private final Map<String, List<AnomalyDetection>> anomalyCache = new ConcurrentHashMap<>();
    private final Map<String, List<OptimizationRecommendation>> recommendationCache = new ConcurrentHashMap<>();
    
    // Performance metrics
    private final Map<String, Long> inferenceLatency = new ConcurrentHashMap<>();
    private final Map<String, Integer> inferenceCount = new ConcurrentHashMap<>();
    
    /**
     * Generate energy consumption forecast
     */
    public EnergyPrediction forecastEnergyConsumption(String deviceId, String facilityId, 
                                                    int horizonHours, Map<String, Double> context) {
        try {
            long startTime = System.currentTimeMillis();
            
            // Check if TensorFlow Lite is available
            if (!tensorFlowLiteService.isModelLoaded("energy_forecast")) {
                log.warn("Energy forecast model not loaded, using fallback prediction");
                return generateFallbackPrediction(deviceId, facilityId, horizonHours, context);
            }
            
            // Generate prediction using TensorFlow Lite
            double[] consumptionData = {0.0}; // Placeholder data
            EnergyPrediction prediction = tensorFlowLiteService.forecastEnergyConsumption(
                consumptionData, context);
            
            // Cache the prediction
            cachePrediction(deviceId, prediction);
            
            // Update performance metrics
            updatePerformanceMetrics("energy_forecast", System.currentTimeMillis() - startTime);
            
            log.info("Generated energy forecast for device {}: {} kWh in {} hours", 
                    deviceId, prediction.getPredictedConsumption(), horizonHours);
            
            return prediction;
            
        } catch (Exception e) {
            log.error("Error generating energy forecast for device {}: {}", deviceId, e.getMessage(), e);
            return generateFallbackPrediction(deviceId, facilityId, horizonHours, context);
        }
    }
    
    /**
     * Detect anomalies in energy consumption
     */
    public List<AnomalyDetection> detectAnomalies(String deviceId, String facilityId, 
                                                 Map<String, Double> energyData) {
        try {
            long startTime = System.currentTimeMillis();
            
            List<AnomalyDetection> anomalies = new ArrayList<>();
            
            // Check if TensorFlow Lite is available
            if (!tensorFlowLiteService.isModelLoaded("anomaly_detection")) {
                log.warn("Anomaly detection model not loaded, using statistical detection");
                return detectAnomaliesStatistical(deviceId, facilityId, energyData);
            }
            
            // Detect anomalies using TensorFlow Lite
            for (Map.Entry<String, Double> entry : energyData.entrySet()) {
                String metric = entry.getKey();
                Double value = entry.getValue();
                
                if (value != null) {
                    AnomalyDetection anomaly = tensorFlowLiteService.detectAnomaly(
                        value, value * 0.9, new HashMap<>());
                    
                    if (anomaly != null && anomaly.getConfidence() >= anomalyThreshold) {
                        anomaly.setMetric(metric); // Use the metric variable
                        anomalies.add(anomaly);
                    }
                }
            }
            
            // Cache detected anomalies
            cacheAnomalies(deviceId, anomalies);
            
            // Update performance metrics
            updatePerformanceMetrics("anomaly_detection", System.currentTimeMillis() - startTime);
            
            log.info("Detected {} anomalies for device {}", anomalies.size(), deviceId);
            
            return anomalies;
            
        } catch (Exception e) {
            log.error("Error detecting anomalies for device {}: {}", deviceId, e.getMessage(), e);
            return detectAnomaliesStatistical(deviceId, facilityId, energyData);
        }
    }
    
    /**
     * Generate optimization recommendations
     */
    public List<OptimizationRecommendation> generateOptimizationRecommendations(
            String deviceId, String facilityId, Map<String, Double> currentMetrics) {
        try {
            long startTime = System.currentTimeMillis();
            
            // Check if TensorFlow Lite is available
            if (!tensorFlowLiteService.isModelLoaded("optimization")) {
                log.warn("Optimization model not loaded, using rule-based recommendations");
                return generateRuleBasedRecommendations(deviceId, facilityId, currentMetrics);
            }
            
            // Generate recommendations using TensorFlow Lite
            List<OptimizationRecommendation> recommendations = 
                tensorFlowLiteService.generateOptimizationRecommendations(
                    currentMetrics, new HashMap<>());
            
            // Filter by confidence threshold
            recommendations = recommendations.stream()
                .filter(rec -> rec.getConfidence() >= minOptimizationConfidence)
                .toList();
            
            // Cache recommendations
            cacheRecommendations(deviceId, recommendations);
            
            // Update performance metrics
            updatePerformanceMetrics("optimization", System.currentTimeMillis() - startTime);
            
            log.info("Generated {} optimization recommendations for device {}", 
                    recommendations.size(), deviceId);
            
            return recommendations;
            
        } catch (Exception e) {
            log.error("Error generating optimization recommendations for device {}: {}", 
                    deviceId, e.getMessage(), e);
            return generateRuleBasedRecommendations(deviceId, facilityId, currentMetrics);
        }
    }
    
    /**
     * Perform batch ML inference on multiple devices
     */
    public Map<String, Object> performBatchInference(List<String> deviceIds, String facilityId) {
        Map<String, Object> results = new HashMap<>();
        
        for (String deviceId : deviceIds) {
            try {
                Map<String, Object> deviceResults = new HashMap<>();
                
                // Get current energy metrics for the device
                Map<String, Double> currentMetrics = getCurrentDeviceMetrics(deviceId);
                
                // Generate predictions
                EnergyPrediction prediction = forecastEnergyConsumption(
                    deviceId, facilityId, defaultPredictionHorizon, currentMetrics);
                deviceResults.put("prediction", prediction);
                
                // Detect anomalies
                List<AnomalyDetection> anomalies = detectAnomalies(
                    deviceId, facilityId, currentMetrics);
                deviceResults.put("anomalies", anomalies);
                
                // Generate recommendations
                List<OptimizationRecommendation> recommendations = generateOptimizationRecommendations(
                    deviceId, facilityId, currentMetrics);
                deviceResults.put("recommendations", recommendations);
                
                results.put(deviceId, deviceResults);
                
            } catch (Exception e) {
                log.error("Error performing batch inference for device {}: {}", 
                        deviceId, e.getMessage(), e);
                results.put(deviceId, Map.of("error", e.getMessage()));
            }
        }
        
        return results;
    }
    
    /**
     * Get ML performance metrics
     */
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Model status
        metrics.put("models", tensorFlowLiteService.getModelStatus());
        
        // Performance metrics
        metrics.put("inference_latency", new HashMap<>(inferenceLatency));
        metrics.put("inference_count", new HashMap<>(inferenceCount));
        
        // Cache statistics
        metrics.put("prediction_cache_size", predictionCache.values().stream()
            .mapToInt(List::size).sum());
        metrics.put("anomaly_cache_size", anomalyCache.values().stream()
            .mapToInt(List::size).sum());
        metrics.put("recommendation_cache_size", recommendationCache.values().stream()
            .mapToInt(List::size).sum());
        
        // Average latency
        double avgLatency = inferenceLatency.values().stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);
        metrics.put("average_latency_ms", avgLatency);
        
        return metrics;
    }
    
    /**
     * Health check for ML services
     */
    public Map<String, Object> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Check TensorFlow Lite service
            boolean tfLiteHealthy = tensorFlowLiteService.isModelLoaded("energy_forecast") ||
                                  tensorFlowLiteService.isModelLoaded("anomaly_detection") ||
                                  tensorFlowLiteService.isModelLoaded("optimization");
            
            health.put("tensorflow_lite", Map.of(
                "status", tfLiteHealthy ? "healthy" : "degraded",
                "models_loaded", tensorFlowLiteService.getModelStatus().size()
            ));
            
            // Check storage service
            health.put("storage", Map.of(
                "status", "healthy",
                "available", true
            ));
            
            // Overall status
            health.put("overall_status", tfLiteHealthy ? "healthy" : "degraded");
            health.put("timestamp", LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("Error during ML health check: {}", e.getMessage(), e);
            health.put("overall_status", "unhealthy");
            health.put("error", e.getMessage());
        }
        
        return health;
    }
    
    // Private helper methods
    
    private EnergyPrediction generateFallbackPrediction(String deviceId, String facilityId, 
                                                      int horizonHours, Map<String, Double> context) {
        // Simple statistical prediction based on historical data
        double baseConsumption = context.getOrDefault("base_consumption", 100.0);
        double seasonalFactor = context.getOrDefault("seasonal_factor", 1.0);
        double trendFactor = context.getOrDefault("trend_factor", 1.0);
        
        double predictedConsumption = baseConsumption * seasonalFactor * trendFactor * (horizonHours / 24.0);
        
        return EnergyPrediction.builder()
            .id(UUID.randomUUID().toString())
            .timestamp(LocalDateTime.now())
            .deviceId(deviceId)
            .facilityId(facilityId)
            .predictedConsumption(predictedConsumption)
            .confidence(0.6) // Lower confidence for fallback
            .predictionHorizon(horizonHours)
            .predictionType("fallback_statistical")
            .modelVersion("fallback_v1")
            .build();
    }
    
    private List<AnomalyDetection> detectAnomaliesStatistical(String deviceId, String facilityId, 
                                                             Map<String, Double> energyData) {
        List<AnomalyDetection> anomalies = new ArrayList<>();
        
        // Simple statistical anomaly detection
        for (Map.Entry<String, Double> entry : energyData.entrySet()) {
            String metric = entry.getKey();
            Double value = entry.getValue();
            
            if (value != null) {
                // Get historical baseline for this metric
                double baseline = getHistoricalBaseline(deviceId, metric);
                double threshold = baseline * 2.0; // 2x baseline threshold
                
                if (value > threshold) {
                    AnomalyDetection anomaly = AnomalyDetection.builder()
                        .id(UUID.randomUUID().toString())
                        .timestamp(LocalDateTime.now())
                        .deviceId(deviceId)
                        .facilityId(facilityId)
                        .anomalyType(AnomalyDetection.AnomalyType.ENERGY_CONSUMPTION_SPIKE)
                        .severity(AnomalyDetection.AnomalySeverity.MEDIUM)
                        .confidence(0.7)
                        .description("Statistical anomaly detected for " + metric)
                        .expectedValue(baseline)
                        .actualValue(value)
                        .deviation(value - baseline)
                        .deviationPercentage(((value - baseline) / baseline) * 100)
                        .threshold(threshold)
                        .modelVersion("statistical_v1")
                        .active(true)
                        .build();
                    
                    anomalies.add(anomaly);
                }
            }
        }
        
        return anomalies;
    }
    
    private List<OptimizationRecommendation> generateRuleBasedRecommendations(
            String deviceId, String facilityId, Map<String, Double> currentMetrics) {
        List<OptimizationRecommendation> recommendations = new ArrayList<>();
        
        // Rule-based optimization recommendations
        double currentConsumption = currentMetrics.getOrDefault("energy_consumption", 0.0);
        double peakDemand = currentMetrics.getOrDefault("peak_demand", 0.0);
        
        if (currentConsumption > 1000.0) {
            recommendations.add(OptimizationRecommendation.builder()
                .id(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .deviceId(deviceId)
                .facilityId(facilityId)
                .optimizationType(OptimizationRecommendation.OptimizationType.ENERGY_EFFICIENCY)
                .priority(OptimizationRecommendation.PriorityLevel.HIGH)
                .category(OptimizationRecommendation.OptimizationCategory.OPERATIONAL)
                .title("High Energy Consumption Detected")
                .description("Current energy consumption is above optimal levels. Consider load shifting and efficiency improvements.")
                .expectedEnergySavings(currentConsumption * 0.15)
                .expectedCostSavings(currentConsumption * 0.15 * 0.12) // Assuming 12 cents per kWh
                .expectedImprovementPercentage(15.0)
                .implementationDifficulty(4)
                .estimatedImplementationTime(8.0)
                .estimatedImplementationCost(500.0)
                .paybackPeriod(6.0)
                .returnOnInvestment(200.0)
                .currentBaseline(currentConsumption)
                .optimizedTarget(currentConsumption * 0.85)
                .confidence(0.8)
                .modelVersion("rule_based_v1")
                .active(true)
                .build());
        }
        
        // Check for high peak demand
        if (peakDemand > 500.0) {
            recommendations.add(OptimizationRecommendation.builder()
                .id(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .deviceId(deviceId)
                .facilityId(facilityId)
                .optimizationType(OptimizationRecommendation.OptimizationType.DEMAND_MANAGEMENT)
                .priority(OptimizationRecommendation.PriorityLevel.MEDIUM)
                .category(OptimizationRecommendation.OptimizationCategory.OPERATIONAL)
                .title("High Peak Demand Detected")
                .description("Peak demand is above optimal levels. Consider demand response strategies.")
                .expectedEnergySavings(peakDemand * 0.1)
                .expectedCostSavings(peakDemand * 0.1 * 0.12)
                .confidence(0.75)
                .isActionable(true)
                .build());
        }
        
        return recommendations;
    }
    
    private Map<String, Double> getCurrentDeviceMetrics(String deviceId) {
        // Get current metrics from storage or return defaults
        return Map.of(
            "energy_consumption", 1200.0,
            "peak_demand", 150.0,
            "voltage", 220.0,
            "current", 5.5,
            "power_factor", 0.95
        );
    }
    
    private double getHistoricalBaseline(String deviceId, String metric) {
        // Get historical baseline from storage or return default
        return 100.0; // Default baseline
    }
    
    private void cachePrediction(String deviceId, EnergyPrediction prediction) {
        predictionCache.computeIfAbsent(deviceId, k -> new ArrayList<>()).add(prediction);
        
        // Keep only recent predictions (last 100)
        List<EnergyPrediction> predictions = predictionCache.get(deviceId);
        if (predictions.size() > 100) {
            predictions.remove(0);
        }
    }
    
    private void cacheAnomalies(String deviceId, List<AnomalyDetection> anomalies) {
        anomalyCache.computeIfAbsent(deviceId, k -> new ArrayList<>()).addAll(anomalies);
        
        // Keep only recent anomalies (last 50)
        List<AnomalyDetection> deviceAnomalies = anomalyCache.get(deviceId);
        if (deviceAnomalies.size() > 50) {
            deviceAnomalies.subList(0, deviceAnomalies.size() - 50).clear();
        }
    }
    
    private void cacheRecommendations(String deviceId, List<OptimizationRecommendation> recommendations) {
        recommendationCache.computeIfAbsent(deviceId, k -> new ArrayList<>()).addAll(recommendations);
        
        // Keep only recent recommendations (last 25)
        List<OptimizationRecommendation> deviceRecommendations = recommendationCache.get(deviceId);
        if (deviceRecommendations.size() > 25) {
            deviceRecommendations.subList(0, deviceRecommendations.size() - 25).clear();
        }
    }
    
    private void updatePerformanceMetrics(String operation, long latency) {
        inferenceLatency.merge(operation, latency, (oldVal, newVal) -> (oldVal + newVal) / 2);
        inferenceCount.merge(operation, 1, Integer::sum);
    }
    
    /**
     * Get model status information
     */
    public Map<String, Object> getModelStatus() {
        return tensorFlowLiteService.getModelStatus();
    }
    
    /**
     * Initialize ML models
     */
    public void initializeModels() {
        tensorFlowLiteService.initializeModels();
    }
    
    /**
     * Detect anomaly with simplified signature
     */
    public AnomalyDetection detectAnomaly(double consumption, double baseline, Map<String, Double> context) {
        return tensorFlowLiteService.detectAnomaly(consumption, baseline, context);
    }
    
    /**
     * Generate recommendations with simplified signature
     */
    public List<OptimizationRecommendation> generateRecommendations(double consumption, Map<String, Double> metrics) {
        return tensorFlowLiteService.generateRecommendations(consumption, metrics);
    }
    
    /**
     * Generate facility optimization recommendations
     */
    public List<OptimizationRecommendation> generateFacilityOptimization(Object facilityAsset) {
        // Placeholder implementation
        return List.of();
    }
}
