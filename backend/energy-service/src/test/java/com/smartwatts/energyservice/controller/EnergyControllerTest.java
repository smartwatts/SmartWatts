package com.smartwatts.energyservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartwatts.energyservice.dto.EnergyReadingDto;
import com.smartwatts.energyservice.dto.EnergyConsumptionDto;
import com.smartwatts.energyservice.model.EnergyConsumption;
import com.smartwatts.energyservice.service.EnergyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EnergyController.class)
class EnergyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EnergyService energyService;

    @Autowired
    private ObjectMapper objectMapper;

    private EnergyReadingDto testReadingDto;
    private UUID testReadingId;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testReadingId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        
        testReadingDto = EnergyReadingDto.builder()
                .id(testReadingId)
                .userId(testUserId)
                .deviceId("DEVICE-001")
                .voltage(new BigDecimal("220.0"))
                .current(new BigDecimal("10.5"))
                .power(new BigDecimal("2310.0"))
                .energyConsumed(new BigDecimal("100.0"))
                .readingTimestamp(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllEnergyReadings_Success_ReturnsPage() throws Exception {
        // Given
        Page<EnergyReadingDto> page = new PageImpl<>(Arrays.asList(testReadingDto));
        when(energyService.getAllEnergyReadings(any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/energy/readings")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].voltage").value(220.0));

        verify(energyService).getAllEnergyReadings(any(Pageable.class));
    }

    @Test
    void saveEnergyReading_Success_ReturnsCreated() throws Exception {
        // Given
        when(energyService.saveEnergyReading(any(EnergyReadingDto.class))).thenReturn(testReadingDto);

        // When & Then
        mockMvc.perform(post("/api/v1/energy/readings")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testReadingDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.voltage").value(220.0));

        verify(energyService).saveEnergyReading(any(EnergyReadingDto.class));
    }

    @Test
    void saveEnergyReadingWithAuth_Success_ReturnsCreated() throws Exception {
        // Given
        when(energyService.saveEnergyReadingWithAuth(any(EnergyReadingDto.class), anyString())).thenReturn(testReadingDto);

        // When & Then
        mockMvc.perform(post("/api/v1/energy/readings/secure")
                .with(csrf())
                .header("X-Device-Auth-Secret", "secret-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testReadingDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.voltage").value(220.0));

        verify(energyService).saveEnergyReadingWithAuth(any(EnergyReadingDto.class), anyString());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getEnergyReadingById_Success_ReturnsReading() throws Exception {
        // Given
        when(energyService.getEnergyReadingById(testReadingId)).thenReturn(testReadingDto);

        // When & Then
        mockMvc.perform(get("/api/v1/energy/readings/{readingId}", testReadingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testReadingId.toString()))
                .andExpect(jsonPath("$.voltage").value(220.0));

        verify(energyService).getEnergyReadingById(testReadingId);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getEnergyReadingsByUserId_Success_ReturnsPage() throws Exception {
        // Given
        Page<EnergyReadingDto> page = new PageImpl<>(Arrays.asList(testReadingDto));
        when(energyService.getEnergyReadingsByUserId(eq(testUserId), any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/energy/readings/user/{userId}", testUserId)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(energyService).getEnergyReadingsByUserId(eq(testUserId), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getEnergyReadingsByTimeRange_Success_ReturnsList() throws Exception {
        // Given
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();
        List<EnergyReadingDto> readings = Arrays.asList(testReadingDto);
        when(energyService.getEnergyReadingsByUserIdAndTimeRange(eq(testUserId), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(readings);

        // When & Then
        mockMvc.perform(get("/api/v1/energy/readings/user/{userId}/time-range", testUserId)
                .param("startTime", startTime.toString())
                .param("endTime", endTime.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(energyService).getEnergyReadingsByUserIdAndTimeRange(eq(testUserId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void aggregateEnergyConsumption_Success_ReturnsConsumption() throws Exception {
        // Given
        EnergyConsumptionDto consumptionDto = EnergyConsumptionDto.builder()
                .userId(testUserId)
                .deviceId("DEVICE-001")
                .totalEnergy(new BigDecimal("1000.0"))
                .build();
        when(energyService.aggregateEnergyConsumption(any(UUID.class), anyString(), any(EnergyConsumption.PeriodType.class), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(consumptionDto);

        // When & Then
        mockMvc.perform(post("/api/v1/energy/consumption/aggregate")
                .with(csrf())
                .param("userId", testUserId.toString())
                .param("deviceId", "DEVICE-001")
                .param("periodType", "DAILY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEnergy").value(1000.0));

        verify(energyService).aggregateEnergyConsumption(any(UUID.class), anyString(), any(EnergyConsumption.PeriodType.class), any(LocalDateTime.class), any(LocalDateTime.class));
    }
}

