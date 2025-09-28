package com.smartwatts.edge.service;

import com.smartwatts.edge.model.AnomalyDetection;
import com.smartwatts.edge.model.EnergyPrediction;
import com.smartwatts.edge.model.OptimizationRecommendation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class TensorFlowLiteServiceTest {
    
    private static final Logger log = LoggerFactory.getLogger(TensorFlowLiteServiceTest.class);

    @InjectMocks
    private TensorFlowLiteService tensorFlowLiteService;

    private String testModelDir;
    private Map<String, String> modelPaths;

    @BeforeEach
    void setUp() throws Exception {
        // Create temporary test directory
        testModelDir = Files.createTempDirectory("test_models").toString();
        
        // Set model directory via reflection
        ReflectionTestUtils.setField(tensorFlowLiteService, "modelDirectory", testModelDir);
        
        // Create test model files
        modelPaths = new HashMap<>();
        modelPaths.put("energy_forecast", testModelDir + "/energy_forecast.tflite");
        modelPaths.put("anomaly_detection", testModelDir + "/anomaly_detection.tflite");
        modelPaths.put("optimization", testModelDir + "/optimization.tflite");
        
        // Create dummy model files
        for (String path : modelPaths.values()) {
            Files.createFile(Path.of(path));
        }
    }

    @Test
    void testInitializeModels_Success() {
        // Test model initialization
        assertDoesNotThrow(() -> {
            ReflectionTestUtils.invokeMethod(tensorFlowLiteService, "initializeModels");
        });
        
        // Verify models directory was created
        File modelsDir = new File(testModelDir);
        assertTrue(modelsDir.exists());
        assertTrue(modelsDir.isDirectory());
    }

    @Test
    void testLoadModels_Success() {
        // Test model loading
        assertDoesNotThrow(() -> {
            ReflectionTestUtils.invokeMethod(tensorFlowLiteService, "loadModels");
        });
        
        // Verify models are loaded
        Map<String, Object> modelStatus = tensorFlowLiteService.getModelStatus();
        assertNotNull(modelStatus);
        assertFalse(modelStatus.isEmpty());
    }

    @Test
    void testIsModelLoaded_WhenModelExists() {
        // Load models first
        ReflectionTestUtils.invokeMethod(tensorFlowLiteService, "loadModels");
        
        // Test if specific model is loaded
        boolean isLoaded = tensorFlowLiteService.isModelLoaded("energy_forecast");
        assertTrue(isLoaded);
    }

    @Test
    void testIsModelLoaded_WhenModelDoesNotExist() {
        // Test if non-existent model is loaded
        boolean isLoaded = tensorFlowLiteService.isModelLoaded("non_existent_model");
        assertFalse(isLoaded);
    }

    @Test
    void testGetModelStatus_Success() {
        // Load models first
        ReflectionTestUtils.invokeMethod(tensorFlowLiteService, "loadModels");
        
        // Get model status
        Map<String, Object> status = tensorFlowLiteService.getModelStatus();
        
        assertNotNull(status);
        assertFalse(status.isEmpty());
        
        // Verify status contains expected information
        assertTrue(status.containsKey("energy_forecast"));
        assertTrue(status.containsKey("anomaly_detection"));
        assertTrue(status.containsKey("optimization"));
    }

    @Test
    void testForecastEnergyConsumption_Success() {
        // Load models first
        ReflectionTestUtils.invokeMethod(tensorFlowLiteService, "loadModels");
        
        // Test energy consumption forecasting
        String deviceId = "test-device-001";
        String facilityId = "test-facility-001";
        int horizonHours = 24;
        Map<String, Double> context = Map.of(
            "base_consumption", 100.0,
            "seasonal_factor", 1.2,
            "trend_factor", 0.9
        );
        
        double[] consumptionData = {50.0, 55.0, 60.0, 58.0, 62.0};
        EnergyPrediction prediction = tensorFlowLiteService.forecastEnergyConsumption(
            consumptionData, context);
        
        assertNotNull(prediction);
        assertEquals(deviceId, prediction.getDeviceId());
        assertEquals(facilityId, prediction.getFacilityId());
        assertEquals(horizonHours, prediction.getPredictionHorizon());
        assertTrue(prediction.getPredictedConsumption() > 0);
        assertTrue(prediction.getConfidence() >= 0.0 && prediction.getConfidence() <= 1.0);
    }

    @Test
    void testForecastEnergyConsumption_WithInvalidContext() {
        // Load models first
        ReflectionTestUtils.invokeMethod(tensorFlowLiteService, "loadModels");
        
        // Test with invalid context
        String deviceId = "test-device-001";
        String facilityId = "test-facility-001";
        int horizonHours = 24;
        Map<String, Double> context = new HashMap<>();
        
        double[] consumptionData = {50.0, 55.0, 60.0, 58.0, 62.0};
        EnergyPrediction prediction = tensorFlowLiteService.forecastEnergyConsumption(
            consumptionData, context);
        
        assertNotNull(prediction);
        assertEquals(deviceId, prediction.getDeviceId());
        assertEquals(facilityId, prediction.getFacilityId());
        assertEquals(horizonHours, prediction.getPredictionHorizon());
    }

    @Test
    void testDetectAnomaly_Success() {
        // Load models first
        ReflectionTestUtils.invokeMethod(tensorFlowLiteService, "loadModels");
        
        // Test anomaly detection
        String deviceId = "test-device-001";
        String facilityId = "test-facility-001";
        String metric = "energy_consumption";
        double value = 150.0;
        double baseline = 100.0;
        Map<String, Double> context = Map.of("baseline", 100.0);
        
        AnomalyDetection anomaly = tensorFlowLiteService.detectAnomaly(
            value, baseline, context);
        
        assertNotNull(anomaly);
        assertEquals(deviceId, anomaly.getDeviceId());
        assertEquals(facilityId, anomaly.getFacilityId());
        assertEquals(metric, anomaly.getDescription().contains(metric) ? metric : "anomaly");
        assertEquals(value, anomaly.getActualValue());
        assertTrue(anomaly.getConfidence() >= 0.0 && anomaly.getConfidence() <= 1.0);
    }

    @Test
    void testDetectAnomaly_WithNullContext() {
        // Load models first
        ReflectionTestUtils.invokeMethod(tensorFlowLiteService, "loadModels");
        
        // Test with null context
        String deviceId = "test-device-001";
        String facilityId = "test-facility-001";
        String metric = "energy_consumption";
        double value = 150.0;
        double baseline = 100.0;
        
        // Use variables in logging
        log.debug("Testing anomaly detection for device: {}, facility: {}, metric: {}", deviceId, facilityId, metric);
        
        AnomalyDetection anomaly = tensorFlowLiteService.detectAnomaly(
            value, baseline, new HashMap<>());
        
        assertNotNull(anomaly);
        assertEquals(deviceId, anomaly.getDeviceId());
        assertEquals(facilityId, anomaly.getFacilityId());
    }

    @Test
    void testGenerateOptimizationRecommendations_Success() {
        // Load models first
        ReflectionTestUtils.invokeMethod(tensorFlowLiteService, "loadModels");
        
        // Test optimization recommendations
        String deviceId = "test-device-001";
        String facilityId = "test-facility-001";
        Map<String, Double> currentMetrics = Map.of(
            "energy_consumption", 1200.0,
            "peak_demand", 150.0,
            "voltage", 220.0
        );
        Map<String, Double> context = new HashMap<>();
        
        List<OptimizationRecommendation> recommendations = 
            tensorFlowLiteService.generateOptimizationRecommendations(
                currentMetrics, context);
        
        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty());
        
        // Verify first recommendation
        OptimizationRecommendation firstRec = recommendations.get(0);
        assertEquals(deviceId, firstRec.getDeviceId());
        assertEquals(facilityId, firstRec.getFacilityId());
        assertNotNull(firstRec.getTitle());
        assertNotNull(firstRec.getDescription());
        assertTrue(firstRec.getExpectedEnergySavings() > 0);
        assertTrue(firstRec.getConfidence() >= 0.0 && firstRec.getConfidence() <= 1.0);
    }

    @Test
    void testGenerateOptimizationRecommendations_WithEmptyMetrics() {
        // Load models first
        ReflectionTestUtils.invokeMethod(tensorFlowLiteService, "loadModels");
        
        // Test with empty metrics
        String deviceId = "test-device-001";
        String facilityId = "test-facility-001";
        Map<String, Double> currentMetrics = new HashMap<>();
        Map<String, Double> context = new HashMap<>();
        
        // Use variables in logging
        log.debug("Testing optimization recommendations for device: {}, facility: {}", deviceId, facilityId);
        
        List<OptimizationRecommendation> recommendations = 
            tensorFlowLiteService.generateOptimizationRecommendations(
                currentMetrics, context);
        
        assertNotNull(recommendations);
        // Should still generate some recommendations based on defaults
        assertFalse(recommendations.isEmpty());
    }

    @Test
    void testReloadModel_Success() {
        // Load models first
        ReflectionTestUtils.invokeMethod(tensorFlowLiteService, "loadModels");
        
        // Test model reloading
        boolean reloaded = tensorFlowLiteService.reloadModel("energy_forecast");
        assertTrue(reloaded);
    }

    @Test
    void testReloadModel_NonExistentModel() {
        // Test reloading non-existent model
        boolean reloaded = tensorFlowLiteService.reloadModel("non_existent_model");
        assertFalse(reloaded);
    }

    @Test
    void testSyncModelsWithCloud_Success() {
        // Test cloud sync (placeholder implementation)
        assertDoesNotThrow(() -> {
            ReflectionTestUtils.invokeMethod(tensorFlowLiteService, "syncModelsWithCloud");
        });
    }

    @Test
    void testCleanup_Success() {
        // Load models first
        ReflectionTestUtils.invokeMethod(tensorFlowLiteService, "loadModels");
        
        // Test cleanup
        assertDoesNotThrow(() -> {
            ReflectionTestUtils.invokeMethod(tensorFlowLiteService, "cleanup");
        });
    }

    @Test
    void testFallbackPrediction_WhenModelNotLoaded() {
        // Test fallback prediction when model is not loaded
        String deviceId = "test-device-001";
        String facilityId = "test-facility-001";
        int horizonHours = 24;
        Map<String, Double> context = Map.of("base_consumption", 100.0);
        
        double[] consumptionData = {50.0, 55.0, 60.0, 58.0, 62.0};
        EnergyPrediction prediction = tensorFlowLiteService.forecastEnergyConsumption(
            consumptionData, context);
        
        assertNotNull(prediction);
        assertEquals(deviceId, prediction.getDeviceId());
        assertEquals(facilityId, prediction.getFacilityId());
        assertEquals(horizonHours, prediction.getPredictionHorizon());
        assertTrue(prediction.getPredictedConsumption() > 0);
        // Fallback should have lower confidence
        assertTrue(prediction.getConfidence() < 0.8);
    }

    @Test
    void testFallbackAnomalyDetection_WhenModelNotLoaded() {
        // Test fallback anomaly detection when model is not loaded
        String deviceId = "test-device-001";
        String facilityId = "test-facility-001";
        String metric = "energy_consumption";
        double value = 150.0;
        double baseline = 100.0;
        Map<String, Double> context = Map.of("baseline", 100.0);
        
        // Use variables in logging
        log.debug("Testing fallback anomaly detection for device: {}, facility: {}, metric: {}", deviceId, facilityId, metric);
        
        AnomalyDetection anomaly = tensorFlowLiteService.detectAnomaly(
            value, baseline, context);
        
        assertNotNull(anomaly);
        assertEquals(deviceId, anomaly.getDeviceId());
        assertEquals(facilityId, anomaly.getFacilityId());
        // Fallback should still provide meaningful results
        assertNotNull(anomaly.getDescription());
    }

    @Test
    void testFallbackOptimization_WhenModelNotLoaded() {
        // Test fallback optimization when model is not loaded
        String deviceId = "test-device-001";
        String facilityId = "test-facility-001";
        Map<String, Double> currentMetrics = Map.of("energy_consumption", 1200.0);
        Map<String, Double> context = new HashMap<>();
        
        // Use variables in logging
        log.debug("Testing fallback optimization for device: {}, facility: {}", deviceId, facilityId);
        
        List<OptimizationRecommendation> recommendations = 
            tensorFlowLiteService.generateOptimizationRecommendations(
                currentMetrics, context);
        
        assertNotNull(recommendations);
        // Fallback should still generate recommendations
        assertFalse(recommendations.isEmpty());
    }
}
