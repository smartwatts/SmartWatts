package com.smartwatts.facilityservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartwatts.facilityservice.model.Fleet;
import com.smartwatts.facilityservice.model.FleetStatus;
import com.smartwatts.facilityservice.service.FleetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FleetController.class)
class FleetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FleetService fleetService;

    @Autowired
    private ObjectMapper objectMapper;

    private Fleet testFleet;
    private Long testFleetId;

    @BeforeEach
    void setUp() {
        testFleetId = 1L;
        
        testFleet = new Fleet();
        testFleet.setId(testFleetId);
        testFleet.setVehicleId("VEHICLE-001");
        testFleet.setName("Test Vehicle");
        testFleet.setStatus(FleetStatus.OPERATIONAL);
        testFleet.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createFleet_Success_ReturnsCreated() throws Exception {
        // Given
        when(fleetService.createFleet(any(Fleet.class))).thenReturn(testFleet);

        // When & Then
        mockMvc.perform(post("/api/v1/fleet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testFleet)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Vehicle"));

        verify(fleetService).createFleet(any(Fleet.class));
    }

    @Test
    void getFleetById_Success_ReturnsFleet() throws Exception {
        // Given
        when(fleetService.getFleetById(testFleetId)).thenReturn(Optional.of(testFleet));

        // When & Then
        mockMvc.perform(get("/api/v1/fleet/{id}", testFleetId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testFleetId))
                .andExpect(jsonPath("$.name").value("Test Vehicle"));

        verify(fleetService).getFleetById(testFleetId);
    }

    @Test
    void getFleetById_NotFound_ReturnsNotFound() throws Exception {
        // Given
        when(fleetService.getFleetById(testFleetId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/fleet/{id}", testFleetId))
                .andExpect(status().isNotFound());

        verify(fleetService).getFleetById(testFleetId);
    }

    @Test
    void getFleetByVehicleId_Success_ReturnsFleet() throws Exception {
        // Given
        when(fleetService.getFleetByVehicleId("VEHICLE-001")).thenReturn(Optional.of(testFleet));

        // When & Then
        mockMvc.perform(get("/api/v1/fleet/vehicle/{vehicleId}", "VEHICLE-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleId").value("VEHICLE-001"));

        verify(fleetService).getFleetByVehicleId("VEHICLE-001");
    }

    @Test
    void getAllFleet_Success_ReturnsPage() throws Exception {
        // Given
        Page<Fleet> page = new PageImpl<>(Arrays.asList(testFleet));
        when(fleetService.getFleet(any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/fleet")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Test Vehicle"));

        verify(fleetService).getFleet(any(Pageable.class));
    }

    @Test
    void updateFleet_Success_ReturnsUpdatedFleet() throws Exception {
        // Given
        testFleet.setName("Updated Vehicle");
        when(fleetService.updateFleet(eq(testFleetId), any(Fleet.class))).thenReturn(testFleet);

        // When & Then
        mockMvc.perform(put("/api/v1/fleet/{id}", testFleetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testFleet)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Vehicle"));

        verify(fleetService).updateFleet(eq(testFleetId), any(Fleet.class));
    }

    @Test
    void deleteFleet_Success_ReturnsNoContent() throws Exception {
        // Given
        doNothing().when(fleetService).deleteFleet(testFleetId);

        // When & Then
        mockMvc.perform(delete("/api/v1/fleet/{id}", testFleetId))
                .andExpect(status().isNoContent());

        verify(fleetService).deleteFleet(testFleetId);
    }
}

