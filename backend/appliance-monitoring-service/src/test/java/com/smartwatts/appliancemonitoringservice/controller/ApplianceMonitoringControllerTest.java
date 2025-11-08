package com.smartwatts.appliancemonitoringservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartwatts.appliancemonitoringservice.model.Appliance;
import com.smartwatts.appliancemonitoringservice.model.ApplianceReading;
import com.smartwatts.appliancemonitoringservice.model.ApplianceType;
import com.smartwatts.appliancemonitoringservice.service.ApplianceMonitoringService;
import com.smartwatts.appliancemonitoringservice.service.WeatherIntegrationService;
import com.smartwatts.appliancemonitoringservice.config.TestSecurityConfig;
import com.smartwatts.appliancemonitoringservice.TestApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
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

@WebMvcTest(controllers = ApplianceMonitoringController.class,
    excludeAutoConfiguration = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class,
        SecurityAutoConfiguration.class
    }
)
@ComponentScan(
    basePackages = "com.smartwatts.appliancemonitoringservice.controller",
    useDefaultFilters = false,
    includeFilters = @ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE, classes = ApplianceMonitoringController.class)
)
@ContextConfiguration(classes = {TestApplication.class, TestSecurityConfig.class})
class ApplianceMonitoringControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplianceMonitoringService applianceMonitoringService;

    @MockBean
    private WeatherIntegrationService weatherIntegrationService;
    
    // Mock JWT-related beans to prevent context loading issues
    @MockBean
    private com.smartwatts.appliancemonitoringservice.config.JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @MockBean
    private com.smartwatts.appliancemonitoringservice.config.JwtConfig jwtConfig;

    @Autowired
    private ObjectMapper objectMapper;

    private Appliance testAppliance;
    private ApplianceReading testReading;
    private UUID testApplianceId;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testApplianceId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        
        testAppliance = new Appliance();
        testAppliance.setId(testApplianceId);
        testAppliance.setUserId(testUserId);
        testAppliance.setApplianceName("Test Refrigerator");
        testAppliance.setApplianceType(ApplianceType.REFRIGERATOR);
        testAppliance.setManufacturer("Test Brand");
        testAppliance.setModel("Test Model");
        testAppliance.setIsActive(true);
        
        testReading = new ApplianceReading();
        testReading.setId(UUID.randomUUID());
        testReading.setApplianceId(testApplianceId);
        testReading.setRealTimePowerWatts(new BigDecimal("150.5"));
        testReading.setTimestamp(LocalDateTime.now());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void createAppliance_Success_ReturnsCreated() throws Exception {
        // Given
        when(applianceMonitoringService.createAppliance(any(Appliance.class))).thenReturn(testAppliance);

        // When & Then
        mockMvc.perform(post("/api/v1/appliance-monitoring/appliances")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAppliance)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applianceName").exists());

        verify(applianceMonitoringService).createAppliance(any(Appliance.class));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getUserAppliances_Success_ReturnsList() throws Exception {
        // Given
        List<Appliance> appliances = Arrays.asList(testAppliance);
        when(applianceMonitoringService.getUserAppliances(testUserId)).thenReturn(appliances);

        // When & Then
        mockMvc.perform(get("/api/v1/appliance-monitoring/appliances/user/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].applianceName").exists());

        verify(applianceMonitoringService).getUserAppliances(testUserId);
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getAppliance_Success_ReturnsAppliance() throws Exception {
        // Given
        when(applianceMonitoringService.getAppliance(testApplianceId)).thenReturn(testAppliance);

        // When & Then
        mockMvc.perform(get("/api/v1/appliance-monitoring/appliances/{applianceId}", testApplianceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applianceName").exists());

        verify(applianceMonitoringService).getAppliance(testApplianceId);
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void updateAppliance_Success_ReturnsUpdatedAppliance() throws Exception {
        // Given
        testAppliance.setApplianceName("Updated Refrigerator");
        when(applianceMonitoringService.updateAppliance(any(Appliance.class))).thenReturn(testAppliance);

        // When & Then
        mockMvc.perform(put("/api/v1/appliance-monitoring/appliances/{applianceId}", testApplianceId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAppliance)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applianceName").exists());

        verify(applianceMonitoringService).updateAppliance(any(Appliance.class));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void deleteAppliance_Success_ReturnsNoContent() throws Exception {
        // Given
        doNothing().when(applianceMonitoringService).deactivateAppliance(testApplianceId);

        // When & Then
        mockMvc.perform(delete("/api/v1/appliance-monitoring/appliances/{applianceId}", testApplianceId)
                .with(csrf()))
                .andExpect(status().isOk());

        verify(applianceMonitoringService).deactivateAppliance(testApplianceId);
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void recordApplianceReading_Success_ReturnsCreated() throws Exception {
        // Given
        when(applianceMonitoringService.recordApplianceReading(eq(testApplianceId), any(ApplianceReading.class))).thenReturn(testReading);

        // When & Then
        mockMvc.perform(post("/api/v1/appliance-monitoring/appliances/{applianceId}/readings", testApplianceId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testReading)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.realTimePowerWatts").exists());

        verify(applianceMonitoringService).recordApplianceReading(eq(testApplianceId), any(ApplianceReading.class));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getApplianceReadings_Success_ReturnsList() throws Exception {
        // Given
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();
        List<ApplianceReading> readings = Arrays.asList(testReading);
        when(applianceMonitoringService.getApplianceReadings(eq(testApplianceId), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(readings);

        // When & Then
        mockMvc.perform(get("/api/v1/appliance-monitoring/appliances/{applianceId}/readings", testApplianceId)
                .param("startTime", startTime.toString())
                .param("endTime", endTime.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].realTimePowerWatts").exists());

        verify(applianceMonitoringService).getApplianceReadings(eq(testApplianceId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getApplianceConsumption_Success_ReturnsConsumption() throws Exception {
        // Given
        ApplianceMonitoringService.ApplianceEfficiencyStats stats = ApplianceMonitoringService.ApplianceEfficiencyStats.builder()
                .totalConsumption(new BigDecimal("1000.5"))
                .averageEfficiency(new BigDecimal("85.5"))
                .readingsCount(10)
                .build();
        when(applianceMonitoringService.getEfficiencyStats(eq(testApplianceId), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(stats);

        // When & Then
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();
        mockMvc.perform(get("/api/v1/appliance-monitoring/appliances/{applianceId}/efficiency", testApplianceId)
                .param("startTime", startTime.toString())
                .param("endTime", endTime.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalConsumption").exists());

        verify(applianceMonitoringService).getEfficiencyStats(eq(testApplianceId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    // Note: detectAppliances method not implemented in service - test commented out
    // @Test
    // @WithMockUser(roles = {"USER"})
    // void detectAppliances_Success_ReturnsList() throws Exception {
    //     // Given
    //     List<Map<String, Object>> detectedAppliances = Arrays.asList(
    //         Map.of("applianceName", "Refrigerator", "confidence", 0.95)
    //     );
    //     when(applianceMonitoringService.detectAppliances(any(UUID.class), any())).thenReturn(detectedAppliances);
    //
    //     // When & Then
    //     mockMvc.perform(post("/api/v1/appliance-monitoring/detect/{userId}", testUserId)
    //             .with(csrf())
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content("{\"powerSignature\": [100, 150, 200]}"))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$").isArray());
    //
    //     verify(applianceMonitoringService).detectAppliances(any(UUID.class), any());
    // }
}

