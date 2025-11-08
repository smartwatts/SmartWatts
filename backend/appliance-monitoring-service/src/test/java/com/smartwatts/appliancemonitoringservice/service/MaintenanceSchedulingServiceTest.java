package com.smartwatts.appliancemonitoringservice.service;

import com.smartwatts.appliancemonitoringservice.model.ApplianceReading;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceSchedulingServiceTest {

    @InjectMocks
    private MaintenanceSchedulingService maintenanceSchedulingService;

    private ApplianceReading testReading;
    private UUID testApplianceId;

    @BeforeEach
    void setUp() {
        testApplianceId = UUID.randomUUID();
        testReading = new ApplianceReading();
        testReading.setId(UUID.randomUUID());
        testReading.setApplianceId(testApplianceId);
        testReading.setRealTimePowerWatts(BigDecimal.valueOf(1000.0));
        testReading.setEfficiencyPercentage(BigDecimal.valueOf(85.0));
        testReading.setAnomalyDetected(false);
        testReading.setMaintenanceAlert(false);
    }

    @Test
    void checkMaintenanceNeeded_NullReading_ReturnsFalse() {
        // When
        boolean result = maintenanceSchedulingService.checkMaintenanceNeeded(testApplianceId, null);

        // Then
        assertFalse(result);
    }

    @Test
    void checkMaintenanceNeeded_NormalReading_ReturnsFalse() {
        // Given - normal reading
        testReading.setAnomalyDetected(false);
        testReading.setMaintenanceAlert(false);

        // When
        boolean result = maintenanceSchedulingService.checkMaintenanceNeeded(testApplianceId, testReading);

        // Then
        assertFalse(result);
    }

    @Test
    void checkMaintenanceNeeded_TemperatureAnomaly_ReturnsTrue() {
        // Given - temperature anomaly
        testReading.setAnomalyDetected(true);
        testReading.setAnomalyType("TEMPERATURE_ANOMALY");

        // When
        boolean result = maintenanceSchedulingService.checkMaintenanceNeeded(testApplianceId, testReading);

        // Then
        assertTrue(result);
    }

    @Test
    void checkMaintenanceNeeded_HighConsumptionCritical_ReturnsTrue() {
        // Given - critical high consumption
        testReading.setAnomalyDetected(true);
        testReading.setAnomalyType("HIGH_CONSUMPTION");
        testReading.setRealTimePowerWatts(BigDecimal.valueOf(12000.0));

        // When
        boolean result = maintenanceSchedulingService.checkMaintenanceNeeded(testApplianceId, testReading);

        // Then
        assertTrue(result);
    }

    @Test
    void checkMaintenanceNeeded_HighConsumptionNonCritical_ReturnsFalse() {
        // Given - non-critical high consumption
        testReading.setAnomalyDetected(true);
        testReading.setAnomalyType("HIGH_CONSUMPTION");
        testReading.setRealTimePowerWatts(BigDecimal.valueOf(5000.0));

        // When
        boolean result = maintenanceSchedulingService.checkMaintenanceNeeded(testApplianceId, testReading);

        // Then
        assertFalse(result);
    }

    @Test
    void checkMaintenanceNeeded_LowEfficiencyCritical_ReturnsTrue() {
        // Given - critical low efficiency
        testReading.setAnomalyDetected(true);
        testReading.setAnomalyType("LOW_EFFICIENCY");
        testReading.setEfficiencyPercentage(BigDecimal.valueOf(25.0));

        // When
        boolean result = maintenanceSchedulingService.checkMaintenanceNeeded(testApplianceId, testReading);

        // Then
        assertTrue(result);
    }

    @Test
    void checkMaintenanceNeeded_LowEfficiencyNonCritical_ReturnsFalse() {
        // Given - non-critical low efficiency
        testReading.setAnomalyDetected(true);
        testReading.setAnomalyType("LOW_EFFICIENCY");
        testReading.setEfficiencyPercentage(BigDecimal.valueOf(50.0));

        // When
        boolean result = maintenanceSchedulingService.checkMaintenanceNeeded(testApplianceId, testReading);

        // Then
        assertFalse(result);
    }

    @Test
    void checkMaintenanceNeeded_MaintenanceAlert_ReturnsTrue() {
        // Given - maintenance alert set
        testReading.setMaintenanceAlert(true);

        // When
        boolean result = maintenanceSchedulingService.checkMaintenanceNeeded(testApplianceId, testReading);

        // Then
        assertTrue(result);
    }

    @Test
    void generateMaintenanceMessage_NullReading_ReturnsDefaultMessage() {
        // When
        String result = maintenanceSchedulingService.generateMaintenanceMessage(testApplianceId, null);

        // Then
        assertNotNull(result);
        assertEquals("Maintenance check required", result);
    }

    @Test
    void generateMaintenanceMessage_TemperatureAnomaly_ReturnsTemperatureMessage() {
        // Given
        testReading.setAnomalyDetected(true);
        testReading.setAnomalyType("TEMPERATURE_ANOMALY");
        testReading.setTemperatureCelsius(BigDecimal.valueOf(85.0));

        // When
        String result = maintenanceSchedulingService.generateMaintenanceMessage(testApplianceId, testReading);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("CRITICAL"));
        assertTrue(result.contains("High temperature"));
        assertTrue(result.contains("85.0"));
    }

    @Test
    void generateMaintenanceMessage_HighConsumption_ReturnsConsumptionMessage() {
        // Given
        testReading.setAnomalyDetected(true);
        testReading.setAnomalyType("HIGH_CONSUMPTION");
        testReading.setRealTimePowerWatts(BigDecimal.valueOf(6000.0));

        // When
        String result = maintenanceSchedulingService.generateMaintenanceMessage(testApplianceId, testReading);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("High power consumption"));
        assertTrue(result.contains("6000.0"));
    }

    @Test
    void generateMaintenanceMessage_LowEfficiency_ReturnsEfficiencyMessage() {
        // Given
        testReading.setAnomalyDetected(true);
        testReading.setAnomalyType("LOW_EFFICIENCY");
        testReading.setEfficiencyPercentage(BigDecimal.valueOf(50.0));

        // When
        String result = maintenanceSchedulingService.generateMaintenanceMessage(testApplianceId, testReading);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Low efficiency"));
        assertTrue(result.contains("50.0"));
    }

    @Test
    void generateMaintenanceMessage_PowerFactorAnomaly_ReturnsPowerFactorMessage() {
        // Given
        testReading.setAnomalyDetected(true);
        testReading.setAnomalyType("POWER_FACTOR_ANOMALY");
        testReading.setPowerFactor(BigDecimal.valueOf(0.7));

        // When
        String result = maintenanceSchedulingService.generateMaintenanceMessage(testApplianceId, testReading);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Low power factor"));
        assertTrue(result.contains("0.7"));
    }

    @Test
    void generateMaintenanceMessage_MaintenanceAlert_ReturnsAlertMessage() {
        // Given
        testReading.setMaintenanceAlert(true);

        // When
        String result = maintenanceSchedulingService.generateMaintenanceMessage(testApplianceId, testReading);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Maintenance alert"));
    }

    @Test
    void calculateNextMaintenanceDate_Refrigerator_Returns12MonthsLater() {
        // When
        LocalDateTime result = maintenanceSchedulingService.calculateNextMaintenanceDate(testApplianceId, "REFRIGERATOR");

        // Then
        assertNotNull(result);
        assertTrue(result.isAfter(LocalDateTime.now().plusMonths(11)));
        assertTrue(result.isBefore(LocalDateTime.now().plusMonths(13)));
    }

    @Test
    void calculateNextMaintenanceDate_ACUnit_Returns6MonthsLater() {
        // When
        LocalDateTime result = maintenanceSchedulingService.calculateNextMaintenanceDate(testApplianceId, "AC_UNIT");

        // Then
        assertNotNull(result);
        assertTrue(result.isAfter(LocalDateTime.now().plusMonths(5)));
        assertTrue(result.isBefore(LocalDateTime.now().plusMonths(7)));
    }

    @Test
    void calculateNextMaintenanceDate_DefaultType_Returns12MonthsLater() {
        // When
        LocalDateTime result = maintenanceSchedulingService.calculateNextMaintenanceDate(testApplianceId, "UNKNOWN_TYPE");

        // Then
        assertNotNull(result);
        assertTrue(result.isAfter(LocalDateTime.now().plusMonths(11)));
        assertTrue(result.isBefore(LocalDateTime.now().plusMonths(13)));
    }

    @Test
    void isMaintenanceOverdue_Overdue_ReturnsTrue() {
        // Given
        LocalDateTime lastMaintenance = LocalDateTime.now().minusMonths(13);
        LocalDateTime nextMaintenance = LocalDateTime.now().minusMonths(1);

        // When
        boolean result = maintenanceSchedulingService.isMaintenanceOverdue(lastMaintenance, nextMaintenance);

        // Then
        assertTrue(result);
    }

    @Test
    void isMaintenanceOverdue_NotOverdue_ReturnsFalse() {
        // Given
        LocalDateTime lastMaintenance = LocalDateTime.now().minusMonths(1);
        LocalDateTime nextMaintenance = LocalDateTime.now().plusMonths(11);

        // When
        boolean result = maintenanceSchedulingService.isMaintenanceOverdue(lastMaintenance, nextMaintenance);

        // Then
        assertFalse(result);
    }

    @Test
    void isMaintenanceOverdue_NullDates_ReturnsFalse() {
        // When
        boolean result = maintenanceSchedulingService.isMaintenanceOverdue(null, null);

        // Then
        assertFalse(result);
    }
}

