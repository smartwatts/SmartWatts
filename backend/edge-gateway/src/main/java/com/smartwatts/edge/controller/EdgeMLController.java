package com.smartwatts.edge.controller;

import com.smartwatts.edge.model.EnergyPrediction;
import com.smartwatts.edge.model.AnomalyDetection;
import com.smartwatts.edge.model.OptimizationRecommendation;
import com.smartwatts.edge.service.TensorFlowLiteService;
import com.smartwatts.edge.service.EdgeMLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Edge ML Controller for TensorFlow Lite Inference and ML Analytics
 * Provides REST API endpoints for edge-based machine learning capabilities
 */
@RestController
@RequestMapping("/api/edge/ml")
@CrossOrigin(origins = "*")
public class EdgeMLController {
    
    private static final Logger logger = LoggerFactory.getLogger(EdgeMLController.class);
    
    private final TensorFlowLiteService tensorFlowLiteService;
    private final EdgeMLService edgeMLService;
    
    @Autowired
    public EdgeMLController(TensorFlowLiteService tensorFlowLiteService, 
                          EdgeMLService edgeMLService) {
        this.tensorFlowLiteService = tensorFlowLiteService;
        this.edgeMLService = edgeMLService;
    }
    
    /**
     * Get ML service status and model information
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getMLStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            
            // TensorFlow Lite service status
            Map<String, Object> tfliteStatus = tensorFlowLiteService.getModelStatus();
            status.put("tensorflow_lite", tfliteStatus);
            
            // Edge ML service status
            Map<String, Object> edgeMLStatus = edgeMLService.getModelStatus();
            status.put("edge_ml", edgeMLStatus);
            
            // Overall ML status
            status.put("timestamp", java.time.LocalDateTime.now());
            status.put("status", "operational");
            status.put("total_models", tfliteStatus.size() + edgeMLStatus.size());
            
            logger.info("Retrieved ML service status");
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            logger.error("Failed to get ML status", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Perform energy consumption forecasting using TensorFlow Lite
     */
    @PostMapping("/forecast/energy")
    public ResponseEntity<EnergyPrediction> forecastEnergyConsumption(
            @RequestBody Map<String, Object> request) {
        try {
            // Extract historical data
            @SuppressWarnings("unchecked")
            List<Double> historicalList = (List<Double>) request.get("historicalData");
            double[] historicalData = historicalList.stream()
                    .mapToDouble(Double::doubleValue)
                    .toArray();
            
            // Extract factors
            @SuppressWarnings("unchecked")
            Map<String, Double> factors = (Map<String, Double>) request.get("factors");
            
            // Validate input
            if (historicalData.length == 0) {
                return ResponseEntity.badRequest().build();
            }
            
            // Perform forecasting
            EnergyPrediction prediction = tensorFlowLiteService.forecastEnergyConsumption(
                    historicalData, factors != null ? factors : new HashMap<>());
            
            logger.info("Energy forecast generated for {} data points", historicalData.length);
            return ResponseEntity.ok(prediction);
            
        } catch (Exception e) {
            logger.error("Failed to generate energy forecast", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Detect energy consumption anomalies using TensorFlow Lite
     */
    @PostMapping("/detect/anomaly")
    public ResponseEntity<AnomalyDetection> detectAnomaly(
            @RequestBody Map<String, Object> request) {
        try {
            // Extract consumption data
            @SuppressWarnings("unchecked")
            List<Double> consumptionList = (List<Double>) request.get("consumptionData");
            double[] consumptionData = consumptionList.stream()
                    .mapToDouble(Double::doubleValue)
                    .toArray();
            
            // Extract context
            @SuppressWarnings("unchecked")
            Map<String, Double> context = (Map<String, Double>) request.get("context");
            
            // Validate input
            if (consumptionData.length == 0) {
                return ResponseEntity.badRequest().build();
            }
            
            // Perform anomaly detection
            AnomalyDetection anomaly = tensorFlowLiteService.detectAnomaly(
                    consumptionData, context != null ? context : new HashMap<>());
            
            logger.info("Anomaly detection completed for {} data points", consumptionData.length);
            return ResponseEntity.ok(anomaly);
            
        } catch (Exception e) {
            logger.error("Failed to detect anomaly", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Generate optimization recommendations using TensorFlow Lite
     */
    @PostMapping("/optimize/facility")
    public ResponseEntity<List<OptimizationRecommendation>> generateOptimizationRecommendations(
            @RequestBody Map<String, Object> request) {
        try {
            // Extract facility metrics
            @SuppressWarnings("unchecked")
            Map<String, Double> facilityMetrics = (Map<String, Double>) request.get("facilityMetrics");
            
            // Extract historical data
            @SuppressWarnings("unchecked")
            Map<String, Double> historicalData = (Map<String, Double>) request.get("historicalData");
            
            // Validate input
            if (facilityMetrics == null || facilityMetrics.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            // Generate recommendations
            List<OptimizationRecommendation> recommendations = 
                    tensorFlowLiteService.generateOptimizationRecommendations(
                            facilityMetrics, 
                            historicalData != null ? historicalData : new HashMap<>());
            
            logger.info("Generated {} optimization recommendations", recommendations.size());
            return ResponseEntity.ok(recommendations);
            
        } catch (Exception e) {
            logger.error("Failed to generate optimization recommendations", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get specific model information
     */
    @GetMapping("/models/{modelName}")
    public ResponseEntity<Map<String, Object>> getModelInfo(@PathVariable String modelName) {
        try {
            Map<String, Object> modelInfo = new HashMap<>();
            
            // Check if model is loaded
            boolean isLoaded = tensorFlowLiteService.isModelLoaded(modelName);
            modelInfo.put("name", modelName);
            modelInfo.put("loaded", isLoaded);
            modelInfo.put("status", isLoaded ? "READY" : "NOT_LOADED");
            modelInfo.put("timestamp", java.time.LocalDateTime.now());
            
            if (isLoaded) {
                Map<String, Object> tfliteStatus = tensorFlowLiteService.getModelStatus();
                if (tfliteStatus.containsKey(modelName)) {
                    modelInfo.put("details", tfliteStatus.get(modelName));
                }
            }
            
            logger.info("Retrieved model info for: {}", modelName);
            return ResponseEntity.ok(modelInfo);
            
        } catch (Exception e) {
            logger.error("Failed to get model info for: {}", modelName, e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Reload a specific model
     */
    @PostMapping("/models/{modelName}/reload")
    public ResponseEntity<Map<String, Object>> reloadModel(@PathVariable String modelName) {
        try {
            boolean success = tensorFlowLiteService.reloadModel(modelName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("model", modelName);
            response.put("reloaded", success);
            response.put("timestamp", java.time.LocalDateTime.now());
            
            if (success) {
                logger.info("Model reloaded successfully: {}", modelName);
                response.put("message", "Model reloaded successfully");
            } else {
                logger.warn("Failed to reload model: {}", modelName);
                response.put("message", "Failed to reload model");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to reload model: {}", modelName, e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Perform batch ML inference on multiple data points
     */
    @PostMapping("/inference/batch")
    public ResponseEntity<Map<String, Object>> performBatchInference(
            @RequestBody Map<String, Object> request) {
        try {
            // Extract batch data
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> batchData = (List<Map<String, Object>>) request.get("data");
            
            if (batchData == null || batchData.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Batch data is required"));
            }
            
            Map<String, Object> results = new HashMap<>();
            results.put("timestamp", java.time.LocalDateTime.now());
            results.put("total_processed", batchData.size());
            results.put("results", new HashMap<>());
            
            int successCount = 0;
            int errorCount = 0;
            
            for (int i = 0; i < batchData.size(); i++) {
                Map<String, Object> dataPoint = batchData.get(i);
                String dataId = (String) dataPoint.getOrDefault("id", "data_" + i);
                
                try {
                    // Process based on type
                    String type = (String) dataPoint.get("type");
                    Map<String, Object> result = new HashMap<>();
                    
                    switch (type) {
                        case "energy_forecast":
                            @SuppressWarnings("unchecked")
                            List<Double> historical = (List<Double>) dataPoint.get("historicalData");
                            @SuppressWarnings("unchecked")
                            Map<String, Double> factors = (Map<String, Double>) dataPoint.get("factors");
                            
                            double[] historicalArray = historical.stream()
                                    .mapToDouble(Double::doubleValue)
                                    .toArray();
                            
                            EnergyPrediction prediction = tensorFlowLiteService.forecastEnergyConsumption(
                                    historicalArray, factors != null ? factors : new HashMap<>());
                            
                            result.put("type", "energy_forecast");
                            result.put("prediction", prediction);
                            break;
                            
                        case "anomaly_detection":
                            @SuppressWarnings("unchecked")
                            List<Double> consumption = (List<Double>) dataPoint.get("consumptionData");
                            @SuppressWarnings("unchecked")
                            Map<String, Double> context = (Map<String, Double>) dataPoint.get("context");
                            
                            double[] consumptionArray = consumption.stream()
                                    .mapToDouble(Double::doubleValue)
                                    .toArray();
                            
                            AnomalyDetection anomaly = tensorFlowLiteService.detectAnomaly(
                                    consumptionArray, context != null ? context : new HashMap<>());
                            
                            result.put("type", "anomaly_detection");
                            result.put("anomaly", anomaly);
                            break;
                            
                        default:
                            result.put("error", "Unknown inference type: " + type);
                            errorCount++;
                            break;
                    }
                    
                    if (!result.containsKey("error")) {
                        successCount++;
                    }
                    
                    @SuppressWarnings("unchecked")
                    Map<String, Object> resultsMap = (Map<String, Object>) results.get("results");
                    resultsMap.put(dataId, result);
                    
                } catch (Exception e) {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("error", e.getMessage());
                    @SuppressWarnings("unchecked")
                    Map<String, Object> resultsMap = (Map<String, Object>) results.get("results");
                    resultsMap.put(dataId, errorResult);
                    errorCount++;
                }
            }
            
            results.put("success_count", successCount);
            results.put("error_count", errorCount);
            
            logger.info("Batch inference completed: {} success, {} errors", successCount, errorCount);
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            logger.error("Failed to perform batch inference", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get ML performance metrics
     */
    @GetMapping("/metrics/performance")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
        try {
            Map<String, Object> metrics = new HashMap<>();
            
            // Get model status for performance metrics
            Map<String, Object> modelStatus = tensorFlowLiteService.getModelStatus();
            
            // Calculate performance metrics
            long totalInferences = 0;
            double totalInferenceTime = 0.0;
            int activeModels = 0;
            
            for (Map.Entry<String, Object> entry : modelStatus.entrySet()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> modelInfo = (Map<String, Object>) entry.getValue();
                
                if ("LOADED".equals(modelInfo.get("status"))) {
                    activeModels++;
                    totalInferences += (Long) modelInfo.getOrDefault("inferenceCount", 0L);
                    totalInferenceTime += (Double) modelInfo.getOrDefault("averageInferenceTime", 0.0);
                }
            }
            
            metrics.put("timestamp", java.time.LocalDateTime.now());
            metrics.put("active_models", activeModels);
            metrics.put("total_inferences", totalInferences);
            metrics.put("average_inference_time_ms", activeModels > 0 ? totalInferenceTime / activeModels : 0.0);
            metrics.put("total_models", modelStatus.size());
            
            logger.info("Retrieved ML performance metrics");
            return ResponseEntity.ok(metrics);
            
        } catch (Exception e) {
            logger.error("Failed to get performance metrics", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Health check for ML services
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            Map<String, Object> health = new HashMap<>();
            
            // Check TensorFlow Lite service
            boolean tfliteHealthy = !tensorFlowLiteService.getModelStatus().isEmpty();
            
            // Check Edge ML service
            boolean edgeMLHealthy = !edgeMLService.getModelStatus().isEmpty();
            
            health.put("timestamp", java.time.LocalDateTime.now());
            health.put("status", (tfliteHealthy && edgeMLHealthy) ? "HEALTHY" : "DEGRADED");
            health.put("tensorflow_lite", tfliteHealthy ? "HEALTHY" : "UNHEALTHY");
            health.put("edge_ml", edgeMLHealthy ? "HEALTHY" : "UNHEALTHY");
            health.put("message", "ML services operational");
            
            logger.debug("ML health check completed");
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            logger.error("ML health check failed", e);
            return ResponseEntity.status(503).body(Map.of(
                    "status", "UNHEALTHY",
                    "error", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now()
            ));
        }
    }
}
