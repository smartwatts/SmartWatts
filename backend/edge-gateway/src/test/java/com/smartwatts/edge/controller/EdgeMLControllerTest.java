package com.smartwatts.edge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartwatts.edge.model.AnomalyDetection;
import com.smartwatts.edge.model.EnergyPrediction;
import com.smartwatts.edge.model.OptimizationRecommendation;
import com.smartwatts.edge.service.EdgeMLService;
import com.smartwatts.edge.service.TensorFlowLiteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EdgeMLControllerTest {

    @Mock
    private TensorFlowLiteService tensorFlowLiteService;

    @Mock
    private EdgeMLService edgeMLService;

    @InjectMocks
    private EdgeMLController edgeMLController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(edgeMLController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetMLStatus_Success() throws Exception {
        // Mock service response
        Map<String, Object> mockStatus = Map.of(
            "models", Map.of("energy_forecast", "loaded"),
            "overall_status", "healthy"
        );
        when(edgeMLService.getPerformanceMetrics()).thenReturn(mockStatus);

        // Perform request
        mockMvc.perform(get("/api/edge/ml/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.models").exists())
            .andExpect(jsonPath("$.overall_status").value("healthy"));

        verify(edgeMLService, times(1)).getPerformanceMetrics();
    }

    @Test
    void testForecastEnergyConsumption_Success() throws Exception {
        // Mock service response
        EnergyPrediction mockPrediction = EnergyPrediction.builder()
            .id(UUID.randomUUID().toString())
            .timestamp(LocalDateTime.now())
            .deviceId("test-device-001")
            .facilityId("test-facility-001")
            .predictedConsumption(1200.0)
            .confidence(0.85)
            .predictionHorizon(24)
            .build();

        when(edgeMLService.forecastEnergyConsumption(
            anyString(), anyString(), anyInt(), anyMap()))
            .thenReturn(mockPrediction);

        // Create request payload
        Map<String, Object> request = Map.of(
            "deviceId", "test-device-001",
            "facilityId", "test-facility-001",
            "horizonHours", 24,
            "context", Map.of("base_consumption", 1000.0)
        );

        // Perform request
        mockMvc.perform(post("/api/edge/ml/forecast")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.deviceId").value("test-device-001"))
            .andExpect(jsonPath("$.facilityId").value("test-facility-001"))
            .andExpect(jsonPath("$.predictedConsumption").value(1200.0))
            .andExpect(jsonPath("$.confidence").value(0.85));

        verify(edgeMLService, times(1)).forecastEnergyConsumption(
            "test-device-001", "test-facility-001", 24, anyMap());
    }

    @Test
    void testForecastEnergyConsumption_InvalidRequest() throws Exception {
        // Create invalid request payload (missing required fields)
        Map<String, Object> request = Map.of(
            "deviceId", "test-device-001"
            // Missing facilityId and horizonHours
        );

        // Perform request
        mockMvc.perform(post("/api/edge/ml/forecast")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(edgeMLService, never()).forecastEnergyConsumption(any(), any(), anyInt(), anyMap());
    }

    @Test
    void testDetectAnomaly_Success() throws Exception {
        // Mock service response
        AnomalyDetection mockAnomaly = AnomalyDetection.builder()
            .id(UUID.randomUUID().toString())
            .timestamp(LocalDateTime.now())
            .deviceId("test-device-001")
            .facilityId("test-facility-001")
            .anomalyType(AnomalyDetection.AnomalyType.ENERGY_CONSUMPTION_SPIKE)
            .severity(AnomalyDetection.AnomalySeverity.HIGH)
            .confidence(0.9)
            .description("High energy consumption detected")
            .actualValue(1500.0)
            .expectedValue(1000.0)
            .build();

        when(edgeMLService.detectAnomalies(
            anyString(), anyString(), anyMap()))
            .thenReturn(List.of(mockAnomaly));

        // Create request payload
        Map<String, Object> request = Map.of(
            "deviceId", "test-device-001",
            "facilityId", "test-facility-001",
            "energyData", Map.of("energy_consumption", 1500.0)
        );

        // Perform request
        mockMvc.perform(post("/api/edge/ml/anomaly")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].deviceId").value("test-device-001"))
            .andExpect(jsonPath("$[0].facilityId").value("test-facility-001"))
            .andExpect(jsonPath("$[0].confidence").value(0.9))
            .andExpect(jsonPath("$[0].severity").value("HIGH"));

        verify(edgeMLService, times(1)).detectAnomalies(
            "test-device-001", "test-facility-001", anyMap());
    }

    @Test
    void testDetectAnomaly_InvalidRequest() throws Exception {
        // Create invalid request payload
        Map<String, Object> request = Map.of(
            "deviceId", "test-device-001"
            // Missing facilityId and energyData
        );

        // Perform request
        mockMvc.perform(post("/api/edge/ml/anomaly")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(edgeMLService, never()).detectAnomalies(any(), any(), anyMap());
    }

    @Test
    void testGenerateOptimizationRecommendations_Success() throws Exception {
        // Mock service response
        OptimizationRecommendation mockRecommendation = OptimizationRecommendation.builder()
            .id(UUID.randomUUID().toString())
            .timestamp(LocalDateTime.now())
            .deviceId("test-device-001")
            .facilityId("test-facility-001")
            .optimizationType(OptimizationRecommendation.OptimizationType.ENERGY_EFFICIENCY)
            .priority(OptimizationRecommendation.PriorityLevel.HIGH)
            .title("Improve Energy Efficiency")
            .description("Implement load balancing strategies")
            .expectedEnergySavings(200.0)
            .confidence(0.8)
            .build();

        when(edgeMLService.generateOptimizationRecommendations(
            anyString(), anyString(), anyMap()))
            .thenReturn(List.of(mockRecommendation));

        // Create request payload
        Map<String, Object> request = Map.of(
            "deviceId", "test-device-001",
            "facilityId", "test-facility-001",
            "currentMetrics", Map.of("energy_consumption", 1200.0)
        );

        // Perform request
        mockMvc.perform(post("/api/edge/ml/optimize")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].deviceId").value("test-device-001"))
            .andExpect(jsonPath("$[0].facilityId").value("test-facility-001"))
            .andExpect(jsonPath("$[0].title").value("Improve Energy Efficiency"))
            .andExpect(jsonPath("$[0].confidence").value(0.8));

        verify(edgeMLService, times(1)).generateOptimizationRecommendations(
            "test-device-001", "test-facility-001", anyMap());
    }

    @Test
    void testGenerateOptimizationRecommendations_InvalidRequest() throws Exception {
        // Create invalid request payload
        Map<String, Object> request = Map.of(
            "deviceId", "test-device-001"
            // Missing facilityId and currentMetrics
        );

        // Perform request
        mockMvc.perform(post("/api/edge/ml/optimize")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(edgeMLService, never()).generateOptimizationRecommendations(any(), any(), anyMap());
    }

    @Test
    void testGetModelInfo_Success() throws Exception {
        // Mock service response
        Map<String, Object> mockModelInfo = Map.of(
            "energy_forecast", Map.of("status", "loaded", "version", "v1.0"),
            "anomaly_detection", Map.of("status", "loaded", "version", "v1.0"),
            "optimization", Map.of("status", "loaded", "version", "v1.0")
        );
        when(tensorFlowLiteService.getModelStatus()).thenReturn(mockModelInfo);

        // Perform request
        mockMvc.perform(get("/api/edge/ml/models"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.energy_forecast").exists())
            .andExpect(jsonPath("$.anomaly_detection").exists())
            .andExpect(jsonPath("$.optimization").exists());

        verify(tensorFlowLiteService, times(1)).getModelStatus();
    }

    @Test
    void testReloadModel_Success() throws Exception {
        // Mock service response
        when(tensorFlowLiteService.reloadModel("energy_forecast")).thenReturn(true);

        // Create request payload
        Map<String, Object> request = Map.of("modelName", "energy_forecast");

        // Perform request
        mockMvc.perform(post("/api/edge/ml/models/reload")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Model energy_forecast reloaded successfully"));

        verify(tensorFlowLiteService, times(1)).reloadModel("energy_forecast");
    }

    @Test
    void testReloadModel_ModelNotFound() throws Exception {
        // Mock service response
        when(tensorFlowLiteService.reloadModel("non_existent_model")).thenReturn(false);

        // Create request payload
        Map<String, Object> request = Map.of("modelName", "non_existent_model");

        // Perform request
        mockMvc.perform(post("/api/edge/ml/models/reload")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Model non_existent_model not found"));

        verify(tensorFlowLiteService, times(1)).reloadModel("non_existent_model");
    }

    @Test
    void testReloadModel_InvalidRequest() throws Exception {
        // Create invalid request payload
        Map<String, Object> request = Map.of("invalidField", "value");

        // Perform request
        mockMvc.perform(post("/api/edge/ml/models/reload")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(tensorFlowLiteService, never()).reloadModel(any());
    }

    @Test
    void testPerformBatchInference_Success() throws Exception {
        // Mock service response
        Map<String, Object> mockResults = Map.of(
            "device-001", Map.of(
                "prediction", Map.of("predictedConsumption", 1200.0),
                "anomalies", List.of(),
                "recommendations", List.of()
            ),
            "device-002", Map.of(
                "prediction", Map.of("predictedConsumption", 800.0),
                "anomalies", List.of(),
                "recommendations", List.of()
            )
        );

        when(edgeMLService.performBatchInference(
            anyList(), anyString()))
            .thenReturn(mockResults);

        // Create request payload
        Map<String, Object> request = Map.of(
            "deviceIds", List.of("device-001", "device-002"),
            "facilityId", "test-facility-001"
        );

        // Perform request
        mockMvc.perform(post("/api/edge/ml/batch-inference")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.device-001").exists())
            .andExpect(jsonPath("$.device-002").exists());

        verify(edgeMLService, times(1)).performBatchInference(
            List.of("device-001", "device-002"), "test-facility-001");
    }

    @Test
    void testPerformBatchInference_InvalidRequest() throws Exception {
        // Create invalid request payload
        Map<String, Object> request = Map.of(
            "deviceIds", List.of()
            // Missing facilityId
        );

        // Perform request
        mockMvc.perform(post("/api/edge/ml/batch-inference")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(edgeMLService, never()).performBatchInference(anyList(), anyString());
    }

    @Test
    void testGetPerformanceMetrics_Success() throws Exception {
        // Mock service response
        Map<String, Object> mockMetrics = Map.of(
            "models", Map.of("energy_forecast", "loaded"),
            "inference_latency", Map.of("energy_forecast", 150L),
            "inference_count", Map.of("energy_forecast", 100),
            "average_latency_ms", 150.0
        );
        when(edgeMLService.getPerformanceMetrics()).thenReturn(mockMetrics);

        // Perform request
        mockMvc.perform(get("/api/edge/ml/performance"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.models").exists())
            .andExpect(jsonPath("$.inference_latency").exists())
            .andExpect(jsonPath("$.inference_count").exists())
            .andExpect(jsonPath("$.average_latency_ms").value(150.0));

        verify(edgeMLService, times(1)).getPerformanceMetrics();
    }

    @Test
    void testHealthCheck_Success() throws Exception {
        // Mock service response
        Map<String, Object> mockHealth = Map.of(
            "tensorflow_lite", Map.of("status", "healthy", "models_loaded", 3),
            "storage", Map.of("status", "healthy", "available", true),
            "overall_status", "healthy"
        );
        when(edgeMLService.healthCheck()).thenReturn(mockHealth);

        // Perform request
        mockMvc.perform(get("/api/edge/ml/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tensorflow_lite.status").value("healthy"))
            .andExpect(jsonPath("$.storage.status").value("healthy"))
            .andExpect(jsonPath("$.overall_status").value("healthy"));

        verify(edgeMLService, times(1)).healthCheck();
    }

    @Test
    void testHealthCheck_ServiceUnhealthy() throws Exception {
        // Mock service response for unhealthy state
        Map<String, Object> mockHealth = Map.of(
            "tensorflow_lite", Map.of("status", "degraded", "models_loaded", 1),
            "storage", Map.of("status", "healthy", "available", true),
            "overall_status", "degraded"
        );
        when(edgeMLService.healthCheck()).thenReturn(mockHealth);

        // Perform request
        mockMvc.perform(get("/api/edge/ml/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.overall_status").value("degraded"));

        verify(edgeMLService, times(1)).healthCheck();
    }
}
