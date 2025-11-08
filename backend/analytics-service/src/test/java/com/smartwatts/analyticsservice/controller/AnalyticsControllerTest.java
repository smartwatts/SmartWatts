package com.smartwatts.analyticsservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartwatts.analyticsservice.dto.EnergyAnalyticsDto;
import com.smartwatts.analyticsservice.dto.UsagePatternDto;
import com.smartwatts.analyticsservice.dto.ReportDto;
import com.smartwatts.analyticsservice.model.Report;
import com.smartwatts.analyticsservice.model.UsagePattern;
import com.smartwatts.analyticsservice.service.AnalyticsService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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

import com.smartwatts.analyticsservice.config.TestSecurityConfig;
import com.smartwatts.analyticsservice.TestApplication;
import com.smartwatts.analyticsservice.service.PatternAnalysisService;
import com.smartwatts.analyticsservice.service.ReportGenerationService;
import com.smartwatts.analyticsservice.service.ApplianceRecognitionService;

@WebMvcTest(controllers = AnalyticsController.class, 
    excludeAutoConfiguration = {
        DataSourceAutoConfiguration.class, 
        HibernateJpaAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class,
        SecurityAutoConfiguration.class
    }
)
@ComponentScan(
    basePackages = "com.smartwatts.analyticsservice.controller",
    useDefaultFilters = false,
    includeFilters = @ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE, classes = AnalyticsController.class)
)
@ContextConfiguration(classes = {TestApplication.class, TestSecurityConfig.class})
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;
    
    @MockBean
    private PatternAnalysisService patternAnalysisService;
    
    @MockBean
    private ReportGenerationService reportGenerationService;
    
    @MockBean
    private ApplianceRecognitionService applianceRecognitionService;

    @Autowired
    private ObjectMapper objectMapper;

    private EnergyAnalyticsDto testAnalyticsDto;
    private UUID testAnalyticsId;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testAnalyticsId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        
        LocalDateTime now = LocalDateTime.now();
        testAnalyticsDto = EnergyAnalyticsDto.builder()
                .id(testAnalyticsId)
                .userId(testUserId)
                .deviceId(UUID.randomUUID())
                .efficiencyScore(new BigDecimal("85.5"))
                .analyticsDate(now)
                .periodType(com.smartwatts.analyticsservice.model.EnergyAnalytics.PeriodType.DAILY)
                .startTime(now.minusHours(24))
                .endTime(now)
                .build();
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getAllAnalytics_Success_ReturnsPage() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<EnergyAnalyticsDto> page = new PageImpl<>(Arrays.asList(testAnalyticsDto), pageable, 1);
        when(analyticsService.getAllAnalytics(any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/analytics")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].efficiencyScore").value(85.5));

        verify(analyticsService).getAllAnalytics(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createEnergyAnalytics_Success_ReturnsCreated() throws Exception {
        // Given
        when(analyticsService.createEnergyAnalytics(any(EnergyAnalyticsDto.class))).thenReturn(testAnalyticsDto);

        // When & Then
        mockMvc.perform(post("/api/v1/analytics/energy-analytics")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAnalyticsDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.efficiencyScore").value(85.5));

        verify(analyticsService).createEnergyAnalytics(any(EnergyAnalyticsDto.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getEnergyAnalyticsById_Success_ReturnsAnalytics() throws Exception {
        // Given
        when(analyticsService.getEnergyAnalyticsById(testAnalyticsId)).thenReturn(testAnalyticsDto);

        // When & Then
        mockMvc.perform(get("/api/v1/analytics/energy-analytics/{analyticsId}", testAnalyticsId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testAnalyticsId.toString()))
                .andExpect(jsonPath("$.efficiencyScore").value(85.5));

        verify(analyticsService).getEnergyAnalyticsById(testAnalyticsId);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getEnergyAnalyticsByUserId_Success_ReturnsPage() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<EnergyAnalyticsDto> page = new PageImpl<>(Arrays.asList(testAnalyticsDto), pageable, 1);
        when(analyticsService.getEnergyAnalyticsByUserId(eq(testUserId), any(Pageable.class))).thenReturn(page);

        // When & Then - Use ADMIN role to bypass userId check
        mockMvc.perform(get("/api/v1/analytics/energy-analytics/user/{userId}", testUserId)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(analyticsService).getEnergyAnalyticsByUserId(eq(testUserId), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getEnergyAnalyticsByDateRange_Success_ReturnsList() throws Exception {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        List<EnergyAnalyticsDto> analytics = Arrays.asList(testAnalyticsDto);
        when(analyticsService.getEnergyAnalyticsByDateRange(eq(testUserId), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(analytics);

        // When & Then
        mockMvc.perform(get("/api/v1/analytics/energy-analytics/user/{userId}/date-range", testUserId)
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(analyticsService).getEnergyAnalyticsByDateRange(eq(testUserId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getEfficientAnalytics_Success_ReturnsList() throws Exception {
        // Given
        List<EnergyAnalyticsDto> analytics = Arrays.asList(testAnalyticsDto);
        when(analyticsService.getEfficientAnalytics(eq(testUserId), any(BigDecimal.class))).thenReturn(analytics);

        // When & Then
        mockMvc.perform(get("/api/v1/analytics/energy-analytics/user/{userId}/efficient", testUserId)
                .param("minScore", "70.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(analyticsService).getEfficientAnalytics(eq(testUserId), any(BigDecimal.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getAnalyticsWithAnomalies_Success_ReturnsList() throws Exception {
        // Given
        List<EnergyAnalyticsDto> analytics = Arrays.asList(testAnalyticsDto);
        when(analyticsService.getAnalyticsWithAnomalies(testUserId)).thenReturn(analytics);

        // When & Then
        mockMvc.perform(get("/api/v1/analytics/energy-analytics/user/{userId}/anomalies", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(analyticsService).getAnalyticsWithAnomalies(testUserId);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getUsagePatterns_Success_ReturnsList() throws Exception {
        // Given
        UsagePatternDto patternDto = UsagePatternDto.builder()
                .userId(testUserId)
                .patternType(UsagePattern.PatternType.DAILY_RHYTHM)
                .build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<UsagePatternDto> patterns = new PageImpl<>(Arrays.asList(patternDto), pageable, 1);
        when(analyticsService.getUsagePatternsByUserId(eq(testUserId), any(Pageable.class))).thenReturn(patterns);

        // When & Then
        mockMvc.perform(get("/api/v1/analytics/usage-patterns/user/{userId}", testUserId)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(analyticsService).getUsagePatternsByUserId(eq(testUserId), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void generateReport_Success_ReturnsReport() throws Exception {
        // Given
        LocalDateTime now = LocalDateTime.now();
        ReportDto reportDto = ReportDto.builder()
                .userId(testUserId)
                .reportName("Test Report")
                .reportType(Report.ReportType.CONSUMPTION_SUMMARY)
                .startDate(now.minusDays(30))
                .endDate(now)
                .build();
        when(analyticsService.generateReport(any(ReportDto.class))).thenReturn(reportDto);

        // When & Then
        mockMvc.perform(post("/api/v1/analytics/reports")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reportDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reportType").value("CONSUMPTION_SUMMARY"));

        verify(analyticsService).generateReport(any(ReportDto.class));
    }
}
