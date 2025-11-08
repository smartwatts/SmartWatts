package com.smartwatts.deviceservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartwatts.deviceservice.model.Circuit;
import com.smartwatts.deviceservice.model.SubPanel;
import com.smartwatts.deviceservice.service.CircuitManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CircuitManagementController.class)
class CircuitManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CircuitManagementService circuitManagementService;

    @Autowired
    private ObjectMapper objectMapper;

    private Circuit testCircuit;
    private SubPanel testSubPanel;
    private UUID testDeviceId;
    private UUID testCircuitId;

    @BeforeEach
    void setUp() {
        testDeviceId = UUID.randomUUID();
        testCircuitId = UUID.randomUUID();
        
        testCircuit = new Circuit();
        testCircuit.setId(testCircuitId);
        testCircuit.setName("Test Circuit");
        testCircuit.setSubPanelId(testDeviceId);
        testCircuit.setCurrentReading(new BigDecimal("10.5"));
        testCircuit.setVoltageReading(new BigDecimal("220.0"));
        
        testSubPanel = new SubPanel();
        testSubPanel.setId(UUID.randomUUID());
        testSubPanel.setName("Test SubPanel");
        testSubPanel.setDeviceId(testDeviceId);
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void createCircuit_Success_ReturnsCircuit() throws Exception {
        // Given
        when(circuitManagementService.createCircuit(any(Circuit.class))).thenReturn(testCircuit);

        // When & Then
        mockMvc.perform(post("/api/v1/circuits")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCircuit)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Circuit"));

        verify(circuitManagementService).createCircuit(any(Circuit.class));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void createCircuit_Error_ReturnsInternalServerError() throws Exception {
        // Given
        when(circuitManagementService.createCircuit(any(Circuit.class)))
                .thenThrow(new RuntimeException("Error creating circuit"));

        // When & Then
        mockMvc.perform(post("/api/v1/circuits")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCircuit)))
                .andExpect(status().isInternalServerError());

        verify(circuitManagementService).createCircuit(any(Circuit.class));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void createSubPanel_Success_ReturnsSubPanel() throws Exception {
        // Given
        when(circuitManagementService.createSubPanel(any(SubPanel.class))).thenReturn(testSubPanel);

        // When & Then
        mockMvc.perform(post("/api/v1/circuits/sub-panels")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testSubPanel)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test SubPanel"));

        verify(circuitManagementService).createSubPanel(any(SubPanel.class));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void createSubPanel_Error_ReturnsInternalServerError() throws Exception {
        // Given
        when(circuitManagementService.createSubPanel(any(SubPanel.class)))
                .thenThrow(new RuntimeException("Error creating sub-panel"));

        // When & Then
        mockMvc.perform(post("/api/v1/circuits/sub-panels")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testSubPanel)))
                .andExpect(status().isInternalServerError());

        verify(circuitManagementService).createSubPanel(any(SubPanel.class));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getCircuitHierarchy_Success_ReturnsHierarchy() throws Exception {
        // Given
        Map<String, Object> hierarchy = new HashMap<>();
        hierarchy.put("deviceId", testDeviceId.toString());
        hierarchy.put("circuits", Arrays.asList(testCircuit));
        when(circuitManagementService.getCircuitHierarchy(testDeviceId)).thenReturn(hierarchy);

        // When & Then
        mockMvc.perform(get("/api/v1/circuits/devices/{deviceId}/hierarchy", testDeviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId").value(testDeviceId.toString()));

        verify(circuitManagementService).getCircuitHierarchy(testDeviceId);
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getCircuitHierarchy_Error_ReturnsInternalServerError() throws Exception {
        // Given
        when(circuitManagementService.getCircuitHierarchy(testDeviceId))
                .thenThrow(new RuntimeException("Error getting hierarchy"));

        // When & Then
        mockMvc.perform(get("/api/v1/circuits/devices/{deviceId}/hierarchy", testDeviceId))
                .andExpect(status().isInternalServerError());

        verify(circuitManagementService).getCircuitHierarchy(testDeviceId);
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getCircuitTreeView_Success_ReturnsTreeView() throws Exception {
        // Given
        Map<String, Object> treeNode = new HashMap<>();
        treeNode.put("id", testCircuitId.toString());
        treeNode.put("name", "Test Circuit");
        List<Map<String, Object>> treeView = Arrays.asList(treeNode);
        when(circuitManagementService.getCircuitTreeView(testDeviceId)).thenReturn(treeView);

        // When & Then
        mockMvc.perform(get("/api/v1/circuits/devices/{deviceId}/tree", testDeviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Circuit"));

        verify(circuitManagementService).getCircuitTreeView(testDeviceId);
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getCircuitTreeView_Error_ReturnsInternalServerError() throws Exception {
        // Given
        when(circuitManagementService.getCircuitTreeView(testDeviceId))
                .thenThrow(new RuntimeException("Error getting tree view"));

        // When & Then
        mockMvc.perform(get("/api/v1/circuits/devices/{deviceId}/tree", testDeviceId))
                .andExpect(status().isInternalServerError());

        verify(circuitManagementService).getCircuitTreeView(testDeviceId);
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getCircuitLoadData_Success_ReturnsLoadData() throws Exception {
        // Given
        Map<String, Object> loadData = new HashMap<>();
        loadData.put("circuitId", testCircuitId.toString());
        loadData.put("currentReading", 10.5);
        loadData.put("voltageReading", 220.0);
        when(circuitManagementService.getCircuitLoadData(testCircuitId)).thenReturn(loadData);

        // When & Then
        mockMvc.perform(get("/api/v1/circuits/{circuitId}/load", testCircuitId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.circuitId").value(testCircuitId.toString()));

        verify(circuitManagementService).getCircuitLoadData(testCircuitId);
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getCircuitLoadData_Error_ReturnsInternalServerError() throws Exception {
        // Given
        when(circuitManagementService.getCircuitLoadData(testCircuitId))
                .thenThrow(new RuntimeException("Error getting load data"));

        // When & Then
        mockMvc.perform(get("/api/v1/circuits/{circuitId}/load", testCircuitId))
                .andExpect(status().isInternalServerError());

        verify(circuitManagementService).getCircuitLoadData(testCircuitId);
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getAllCircuitsStatus_Success_ReturnsStatusList() throws Exception {
        // Given
        Map<String, Object> status = new HashMap<>();
        status.put("circuitId", testCircuitId.toString());
        status.put("status", "ACTIVE");
        List<Map<String, Object>> statusList = Arrays.asList(status);
        when(circuitManagementService.getAllCircuitsStatus(testDeviceId)).thenReturn(statusList);

        // When & Then
        mockMvc.perform(get("/api/v1/circuits/devices/{deviceId}/status", testDeviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].circuitId").value(testCircuitId.toString()));

        verify(circuitManagementService).getAllCircuitsStatus(testDeviceId);
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getAllCircuitsStatus_Error_ReturnsInternalServerError() throws Exception {
        // Given
        when(circuitManagementService.getAllCircuitsStatus(testDeviceId))
                .thenThrow(new RuntimeException("Error getting status"));

        // When & Then
        mockMvc.perform(get("/api/v1/circuits/devices/{deviceId}/status", testDeviceId))
                .andExpect(status().isInternalServerError());

        verify(circuitManagementService).getAllCircuitsStatus(testDeviceId);
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void updateCircuitReadings_Success_ReturnsOk() throws Exception {
        // Given
        doNothing().when(circuitManagementService).updateCircuitReadings(
                eq(testCircuitId), any(BigDecimal.class), any(BigDecimal.class));

        CircuitManagementController.UpdateCircuitReadingsRequest request = 
                new CircuitManagementController.UpdateCircuitReadingsRequest();
        request.setCurrentReading(new BigDecimal("15.0"));
        request.setVoltageReading(new BigDecimal("230.0"));

        // When & Then
        mockMvc.perform(put("/api/v1/circuits/{circuitId}/readings", testCircuitId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(circuitManagementService).updateCircuitReadings(
                eq(testCircuitId), any(BigDecimal.class), any(BigDecimal.class));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void updateCircuitReadings_Error_ReturnsInternalServerError() throws Exception {
        // Given
        doThrow(new RuntimeException("Error updating readings"))
                .when(circuitManagementService).updateCircuitReadings(
                        eq(testCircuitId), any(BigDecimal.class), any(BigDecimal.class));

        CircuitManagementController.UpdateCircuitReadingsRequest request = 
                new CircuitManagementController.UpdateCircuitReadingsRequest();
        request.setCurrentReading(new BigDecimal("15.0"));
        request.setVoltageReading(new BigDecimal("230.0"));

        // When & Then
        mockMvc.perform(put("/api/v1/circuits/{circuitId}/readings", testCircuitId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        verify(circuitManagementService).updateCircuitReadings(
                eq(testCircuitId), any(BigDecimal.class), any(BigDecimal.class));
    }
}

