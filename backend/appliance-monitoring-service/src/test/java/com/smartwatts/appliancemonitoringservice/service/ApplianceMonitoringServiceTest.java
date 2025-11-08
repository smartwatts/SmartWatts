package com.smartwatts.appliancemonitoringservice.service;

import com.smartwatts.appliancemonitoringservice.model.Appliance;
import com.smartwatts.appliancemonitoringservice.model.ApplianceReading;
import com.smartwatts.appliancemonitoringservice.model.ApplianceType;
import com.smartwatts.appliancemonitoringservice.repository.ApplianceRepository;
import com.smartwatts.appliancemonitoringservice.repository.ApplianceReadingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplianceMonitoringServiceTest {

    @Mock
    private ApplianceRepository applianceRepository;

    @Mock
    private ApplianceReadingRepository readingRepository;

    @Mock
    private AnomalyDetectionService anomalyDetectionService;

    @Mock
    private MaintenanceSchedulingService maintenanceService;

    @InjectMocks
    private ApplianceMonitoringService applianceMonitoringService;

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
        testAppliance.setIsActive(true);

        testReading = new ApplianceReading();
        testReading.setId(UUID.randomUUID());
        testReading.setApplianceId(testApplianceId);
        testReading.setRealTimePowerWatts(BigDecimal.valueOf(1000.0));
        testReading.setEfficiencyPercentage(BigDecimal.valueOf(85.0));
        testReading.setTimestamp(LocalDateTime.now());
    }

    @Test
    void recordApplianceReading_Success_RecordsReading() {
        // Given
        when(anomalyDetectionService.detectAnomaly(any(ApplianceReading.class))).thenReturn(false);
        when(maintenanceService.checkMaintenanceNeeded(eq(testApplianceId), any(ApplianceReading.class))).thenReturn(false);
        when(readingRepository.save(any(ApplianceReading.class))).thenReturn(testReading);

        // When
        ApplianceReading result = applianceMonitoringService.recordApplianceReading(testApplianceId, testReading);

        // Then
        assertNotNull(result);
        assertEquals(testApplianceId, result.getApplianceId());
        assertNotNull(result.getTimestamp());
        verify(anomalyDetectionService).detectAnomaly(any(ApplianceReading.class));
        verify(maintenanceService).checkMaintenanceNeeded(eq(testApplianceId), any(ApplianceReading.class));
        verify(readingRepository).save(any(ApplianceReading.class));
    }

    @Test
    void recordApplianceReading_WithAnomaly_SetsAnomalyFields() {
        // Given
        when(anomalyDetectionService.detectAnomaly(any(ApplianceReading.class))).thenReturn(true);
        when(anomalyDetectionService.determineAnomalyType(any(ApplianceReading.class))).thenReturn("HIGH_CONSUMPTION");
        when(maintenanceService.checkMaintenanceNeeded(eq(testApplianceId), any(ApplianceReading.class))).thenReturn(false);
        when(readingRepository.save(any(ApplianceReading.class))).thenReturn(testReading);

        // When
        ApplianceReading result = applianceMonitoringService.recordApplianceReading(testApplianceId, testReading);

        // Then
        assertNotNull(result);
        verify(anomalyDetectionService).detectAnomaly(any(ApplianceReading.class));
        verify(anomalyDetectionService).determineAnomalyType(any(ApplianceReading.class));
    }

    @Test
    void recordApplianceReading_WithMaintenanceAlert_SetsMaintenanceFields() {
        // Given
        when(anomalyDetectionService.detectAnomaly(any(ApplianceReading.class))).thenReturn(false);
        when(maintenanceService.checkMaintenanceNeeded(eq(testApplianceId), any(ApplianceReading.class))).thenReturn(true);
        when(maintenanceService.generateMaintenanceMessage(eq(testApplianceId), any(ApplianceReading.class))).thenReturn("Maintenance needed");
        when(readingRepository.save(any(ApplianceReading.class))).thenReturn(testReading);

        // When
        ApplianceReading result = applianceMonitoringService.recordApplianceReading(testApplianceId, testReading);

        // Then
        assertNotNull(result);
        verify(maintenanceService).checkMaintenanceNeeded(eq(testApplianceId), any(ApplianceReading.class));
        verify(maintenanceService).generateMaintenanceMessage(eq(testApplianceId), any(ApplianceReading.class));
    }

    @Test
    void recordApplianceReading_CalculatesEfficiency_WhenNotProvided() {
        // Given
        testReading.setEfficiencyPercentage(null);
        when(anomalyDetectionService.detectAnomaly(any(ApplianceReading.class))).thenReturn(false);
        when(maintenanceService.checkMaintenanceNeeded(eq(testApplianceId), any(ApplianceReading.class))).thenReturn(false);
        when(readingRepository.save(any(ApplianceReading.class))).thenReturn(testReading);

        // When
        ApplianceReading result = applianceMonitoringService.recordApplianceReading(testApplianceId, testReading);

        // Then
        assertNotNull(result);
        assertNotNull(result.getEfficiencyPercentage());
    }

    @Test
    void getApplianceReadings_Success_ReturnsReadings() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();
        List<ApplianceReading> readings = Arrays.asList(testReading);
        when(readingRepository.findByApplianceIdAndTimestampBetween(testApplianceId, startTime, endTime)).thenReturn(readings);

        // When
        List<ApplianceReading> result = applianceMonitoringService.getApplianceReadings(testApplianceId, startTime, endTime);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(readingRepository).findByApplianceIdAndTimestampBetween(testApplianceId, startTime, endTime);
    }

    @Test
    void getEfficiencyStats_WithReadings_ReturnsStats() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();
        testReading.setEfficiencyPercentage(BigDecimal.valueOf(85.0));
        testReading.setEnergyConsumptionKwh(BigDecimal.valueOf(10.0));
        List<ApplianceReading> readings = Arrays.asList(testReading);
        when(readingRepository.findByApplianceIdAndTimestampBetween(testApplianceId, startTime, endTime)).thenReturn(readings);

        // When
        ApplianceMonitoringService.ApplianceEfficiencyStats result = 
            applianceMonitoringService.getEfficiencyStats(testApplianceId, startTime, endTime);

        // Then
        assertNotNull(result);
        verify(readingRepository).findByApplianceIdAndTimestampBetween(testApplianceId, startTime, endTime);
    }

    @Test
    void getEfficiencyStats_NoReadings_ReturnsEmptyStats() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();
        when(readingRepository.findByApplianceIdAndTimestampBetween(testApplianceId, startTime, endTime)).thenReturn(Arrays.asList());

        // When
        ApplianceMonitoringService.ApplianceEfficiencyStats result = 
            applianceMonitoringService.getEfficiencyStats(testApplianceId, startTime, endTime);

        // Then
        assertNotNull(result);
    }

    @Test
    void getUserAppliancesWithStatus_Success_ReturnsStatusList() {
        // Given
        List<Appliance> appliances = Arrays.asList(testAppliance);
        when(applianceRepository.findByUserIdAndIsActive(testUserId, true)).thenReturn(appliances);
        when(readingRepository.findTopByApplianceIdOrderByTimestampDesc(testApplianceId)).thenReturn(null);

        // When
        List<ApplianceMonitoringService.ApplianceStatus> result = 
            applianceMonitoringService.getUserAppliancesWithStatus(testUserId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(applianceRepository).findByUserIdAndIsActive(testUserId, true);
    }

    @Test
    void createAppliance_Success_CreatesAppliance() {
        // Given
        when(applianceRepository.save(any(Appliance.class))).thenReturn(testAppliance);

        // When
        Appliance result = applianceMonitoringService.createAppliance(testAppliance);

        // Then
        assertNotNull(result);
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        verify(applianceRepository).save(any(Appliance.class));
    }

    @Test
    void getUserAppliances_Success_ReturnsAppliances() {
        // Given
        List<Appliance> appliances = Arrays.asList(testAppliance);
        when(applianceRepository.findByUserId(testUserId)).thenReturn(appliances);

        // When
        List<Appliance> result = applianceMonitoringService.getUserAppliances(testUserId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(applianceRepository).findByUserId(testUserId);
    }

    @Test
    void getAppliance_Success_ReturnsAppliance() {
        // Given
        when(applianceRepository.findById(testApplianceId)).thenReturn(Optional.of(testAppliance));

        // When
        Appliance result = applianceMonitoringService.getAppliance(testApplianceId);

        // Then
        assertNotNull(result);
        assertEquals(testApplianceId, result.getId());
        verify(applianceRepository).findById(testApplianceId);
    }

    @Test
    void getAppliance_NotFound_ThrowsException() {
        // Given
        when(applianceRepository.findById(testApplianceId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            applianceMonitoringService.getAppliance(testApplianceId);
        });
    }

    @Test
    void updateAppliance_Success_UpdatesAppliance() {
        // Given
        when(applianceRepository.save(any(Appliance.class))).thenReturn(testAppliance);

        // When
        Appliance result = applianceMonitoringService.updateAppliance(testAppliance);

        // Then
        assertNotNull(result);
        assertNotNull(result.getUpdatedAt());
        verify(applianceRepository).save(any(Appliance.class));
    }

    @Test
    void deactivateAppliance_Success_DeactivatesAppliance() {
        // Given
        when(applianceRepository.findById(testApplianceId)).thenReturn(Optional.of(testAppliance));
        when(applianceRepository.save(any(Appliance.class))).thenReturn(testAppliance);

        // When
        applianceMonitoringService.deactivateAppliance(testApplianceId);

        // Then
        assertFalse(testAppliance.getIsActive());
        verify(applianceRepository).findById(testApplianceId);
        verify(applianceRepository).save(any(Appliance.class));
    }

    @Test
    void getApplianceAnomalies_Success_ReturnsAnomalies() {
        // Given
        testReading.setAnomalyDetected(true);
        List<ApplianceReading> anomalies = Arrays.asList(testReading);
        when(readingRepository.findAnomaliesByApplianceId(testApplianceId)).thenReturn(anomalies);

        // When
        List<ApplianceReading> result = applianceMonitoringService.getApplianceAnomalies(testApplianceId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(readingRepository).findAnomaliesByApplianceId(testApplianceId);
    }

    @Test
    void getMaintenanceAlerts_Success_ReturnsAlerts() {
        // Given
        testReading.setMaintenanceAlert(true);
        List<ApplianceReading> alerts = Arrays.asList(testReading);
        when(readingRepository.findMaintenanceAlertsByApplianceId(testApplianceId)).thenReturn(alerts);

        // When
        List<ApplianceReading> result = applianceMonitoringService.getMaintenanceAlerts(testApplianceId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(readingRepository).findMaintenanceAlertsByApplianceId(testApplianceId);
    }

    @Test
    void getUserAnalyticsSummary_Success_ReturnsSummary() {
        // Given
        List<Appliance> appliances = Arrays.asList(testAppliance);
        when(applianceRepository.findByUserId(testUserId)).thenReturn(appliances);

        // When
        Object result = applianceMonitoringService.getUserAnalyticsSummary(testUserId);

        // Then
        assertNotNull(result);
        verify(applianceRepository).findByUserId(testUserId);
    }
}

