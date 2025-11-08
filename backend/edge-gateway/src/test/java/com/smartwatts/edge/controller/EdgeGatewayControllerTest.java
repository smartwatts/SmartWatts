package com.smartwatts.edge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartwatts.edge.model.AnomalyDetection;
import com.smartwatts.edge.model.EnergyPrediction;
import com.smartwatts.edge.service.EdgeDeviceService;
import com.smartwatts.edge.service.EdgeMLService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EdgeGatewayController.class)
class EdgeGatewayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EdgeMLService edgeMLService;

    @MockBean
    private EdgeDeviceService edgeDeviceService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Setup common mocks
    }

    @Test
    void healthCheck_Success_ReturnsHealth() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/edge/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("healthy"))
                .andExpect(jsonPath("$.service").value("SmartWatts Edge Gateway"));
    }

    @Test
    void getGatewayStats_Success_ReturnsStats() throws Exception {
        // Given
        Map<String, Object> deviceStats = new HashMap<>();
        deviceStats.put("connectedDevices", 5);
        deviceStats.put("totalReadings", 1000);
        
        Map<String, Object> mlStatus = new HashMap<>();
        mlStatus.put("modelsLoaded", 2);
        mlStatus.put("status", "ready");
        
        when(edgeDeviceService.getGatewayStats()).thenReturn(deviceStats);
        when(edgeMLService.getModelStatus()).thenReturn(mlStatus);

        // When & Then
        mockMvc.perform(get("/api/edge/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connectedDevices").value(5))
                .andExpect(jsonPath("$.mlModels").exists());

        verify(edgeDeviceService).getGatewayStats();
        verify(edgeMLService).getModelStatus();
    }

    @Test
    void getGatewayStats_Error_ReturnsInternalServerError() throws Exception {
        // Given
        when(edgeDeviceService.getGatewayStats()).thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(get("/api/edge/stats"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed to get gateway stats"));

        verify(edgeDeviceService).getGatewayStats();
    }

    @Test
    void forecastEnergyConsumption_Success_ReturnsPrediction() throws Exception {
        // Given
        EnergyPrediction prediction = new EnergyPrediction();
        prediction.setDeviceId("device123");
        prediction.setFacilityId("facility456");
        prediction.setPredictedConsumption(1500.0);
        prediction.setConfidence(0.85);
        
        Map<String, Object> request = new HashMap<>();
        request.put("currentConsumption", 1000.0);
        request.put("historicalAverage", 1200.0);
        Map<String, Double> factors = new HashMap<>();
        factors.put("temperature", 25.0);
        factors.put("humidity", 60.0);
        request.put("factors", factors);
        
        when(edgeMLService.forecastEnergyConsumption(anyString(), anyString(), anyInt(), anyMap())).thenReturn(prediction);

        // When & Then
        mockMvc.perform(post("/api/edge/ml/forecast")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId").value("device123"))
                .andExpect(jsonPath("$.predictedConsumption").value(1500.0));

        verify(edgeMLService).forecastEnergyConsumption(anyString(), anyString(), anyInt(), anyMap());
    }

    @Test
    void forecastEnergyConsumption_Error_ReturnsBadRequest() throws Exception {
        // Given
        Map<String, Object> invalidRequest = new HashMap<>();
        when(edgeMLService.forecastEnergyConsumption(anyString(), anyString(), anyInt(), anyMap()))
                .thenThrow(new RuntimeException("Invalid request"));

        // When & Then
        mockMvc.perform(post("/api/edge/ml/forecast")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(edgeMLService, never()).forecastEnergyConsumption(anyString(), anyString(), anyInt(), anyMap());
    }

    @Test
    void detectAnomaly_Success_ReturnsAnomalyDetection() throws Exception {
        // Given
        AnomalyDetection anomaly = AnomalyDetection.builder()
                .active(true)
                .confidence(0.92)
                .severity(AnomalyDetection.AnomalySeverity.HIGH)
                .build();
        
        Map<String, Object> request = new HashMap<>();
        request.put("currentConsumption", 2000.0);
        request.put("baseline", 1000.0);
        Map<String, Double> context = new HashMap<>();
        context.put("temperature", 30.0);
        request.put("context", context);
        
        when(edgeMLService.detectAnomaly(anyDouble(), anyDouble(), anyMap())).thenReturn(anomaly);

        // When & Then
        mockMvc.perform(post("/api/edge/ml/anomaly")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.confidence").value(0.92))
                .andExpect(jsonPath("$.severity").value("HIGH"));

        verify(edgeMLService).detectAnomaly(anyDouble(), anyDouble(), anyMap());
    }

    @Test
    void detectAnomaly_Error_ReturnsBadRequest() throws Exception {
        // Given
        Map<String, Object> invalidRequest = new HashMap<>();
        when(edgeMLService.detectAnomaly(anyDouble(), anyDouble(), anyMap()))
                .thenThrow(new RuntimeException("Invalid request"));

        // When & Then
        mockMvc.perform(post("/api/edge/ml/anomaly")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(edgeMLService, never()).detectAnomaly(anyDouble(), anyDouble(), anyMap());
    }
}

