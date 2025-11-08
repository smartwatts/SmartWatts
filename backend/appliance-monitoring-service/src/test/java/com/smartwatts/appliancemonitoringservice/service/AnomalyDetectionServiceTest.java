package com.smartwatts.appliancemonitoringservice.service;

import com.smartwatts.appliancemonitoringservice.model.ApplianceReading;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AnomalyDetectionServiceTest {

    @InjectMocks
    private AnomalyDetectionService anomalyDetectionService;

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
        testReading.setTemperatureCelsius(BigDecimal.valueOf(25.0));
        testReading.setPowerFactor(BigDecimal.valueOf(0.95));
    }

    @Test
    void detectAnomaly_NullReading_ReturnsFalse() {
        // When
        boolean result = anomalyDetectionService.detectAnomaly(null);

        // Then
        assertFalse(result);
    }

    @Test
    void detectAnomaly_NormalReading_ReturnsFalse() {
        // Given - normal reading values
        testReading.setRealTimePowerWatts(BigDecimal.valueOf(1000.0));
        testReading.setEfficiencyPercentage(BigDecimal.valueOf(85.0));
        testReading.setTemperatureCelsius(BigDecimal.valueOf(25.0));
        testReading.setPowerFactor(BigDecimal.valueOf(0.95));

        // When
        boolean result = anomalyDetectionService.detectAnomaly(testReading);

        // Then
        assertFalse(result);
    }

    @Test
    void detectAnomaly_HighPowerConsumption_ReturnsTrue() {
        // Given - high power consumption
        testReading.setRealTimePowerWatts(BigDecimal.valueOf(6000.0));

        // When
        boolean result = anomalyDetectionService.detectAnomaly(testReading);

        // Then
        assertTrue(result);
    }

    @Test
    void detectAnomaly_LowEfficiency_ReturnsTrue() {
        // Given - low efficiency
        testReading.setEfficiencyPercentage(BigDecimal.valueOf(50.0));

        // When
        boolean result = anomalyDetectionService.detectAnomaly(testReading);

        // Then
        assertTrue(result);
    }

    @Test
    void detectAnomaly_HighTemperature_ReturnsTrue() {
        // Given - high temperature
        testReading.setTemperatureCelsius(BigDecimal.valueOf(85.0));

        // When
        boolean result = anomalyDetectionService.detectAnomaly(testReading);

        // Then
        assertTrue(result);
    }

    @Test
    void detectAnomaly_LowPowerFactor_ReturnsTrue() {
        // Given - low power factor
        testReading.setPowerFactor(BigDecimal.valueOf(0.7));

        // When
        boolean result = anomalyDetectionService.detectAnomaly(testReading);

        // Then
        assertTrue(result);
    }

    @Test
    void determineAnomalyType_NullReading_ReturnsUnknown() {
        // When
        String result = anomalyDetectionService.determineAnomalyType(null);

        // Then
        assertEquals("UNKNOWN", result);
    }

    @Test
    void determineAnomalyType_HighConsumption_ReturnsHighConsumption() {
        // Given
        testReading.setRealTimePowerWatts(BigDecimal.valueOf(6000.0));

        // When
        String result = anomalyDetectionService.determineAnomalyType(testReading);

        // Then
        assertEquals("HIGH_CONSUMPTION", result);
    }

    @Test
    void determineAnomalyType_LowEfficiency_ReturnsLowEfficiency() {
        // Given
        testReading.setEfficiencyPercentage(BigDecimal.valueOf(50.0));

        // When
        String result = anomalyDetectionService.determineAnomalyType(testReading);

        // Then
        assertEquals("LOW_EFFICIENCY", result);
    }

    @Test
    void determineAnomalyType_TemperatureAnomaly_ReturnsTemperatureAnomaly() {
        // Given
        testReading.setTemperatureCelsius(BigDecimal.valueOf(85.0));

        // When
        String result = anomalyDetectionService.determineAnomalyType(testReading);

        // Then
        assertEquals("TEMPERATURE_ANOMALY", result);
    }

    @Test
    void determineAnomalyType_PowerFactorAnomaly_ReturnsPowerFactorAnomaly() {
        // Given
        testReading.setPowerFactor(BigDecimal.valueOf(0.7));

        // When
        String result = anomalyDetectionService.determineAnomalyType(testReading);

        // Then
        assertEquals("POWER_FACTOR_ANOMALY", result);
    }

    @Test
    void determineAnomalyType_NormalReading_ReturnsUnknown() {
        // Given - normal reading
        testReading.setRealTimePowerWatts(BigDecimal.valueOf(1000.0));
        testReading.setEfficiencyPercentage(BigDecimal.valueOf(85.0));
        testReading.setTemperatureCelsius(BigDecimal.valueOf(25.0));
        testReading.setPowerFactor(BigDecimal.valueOf(0.95));

        // When
        String result = anomalyDetectionService.determineAnomalyType(testReading);

        // Then
        assertEquals("UNKNOWN", result);
    }

    @Test
    void calculateAnomalySeverity_NullReading_ReturnsZero() {
        // When
        int result = anomalyDetectionService.calculateAnomalySeverity(null);

        // Then
        assertEquals(0, result);
    }

    @Test
    void calculateAnomalySeverity_NoAnomaly_ReturnsZero() {
        // Given
        testReading.setAnomalyDetected(false);

        // When
        int result = anomalyDetectionService.calculateAnomalySeverity(testReading);

        // Then
        assertEquals(0, result);
    }

    @Test
    void calculateAnomalySeverity_CriticalPowerConsumption_ReturnsHighSeverity() {
        // Given - critical power consumption
        testReading.setAnomalyDetected(true);
        testReading.setRealTimePowerWatts(BigDecimal.valueOf(12000.0));

        // When
        int result = anomalyDetectionService.calculateAnomalySeverity(testReading);

        // Then
        assertTrue(result >= 40);
    }

    @Test
    void calculateAnomalySeverity_CriticalEfficiency_ReturnsHighSeverity() {
        // Given - critical efficiency
        testReading.setAnomalyDetected(true);
        testReading.setEfficiencyPercentage(BigDecimal.valueOf(25.0));

        // When
        int result = anomalyDetectionService.calculateAnomalySeverity(testReading);

        // Then
        assertTrue(result >= 35);
    }

    @Test
    void calculateAnomalySeverity_CriticalTemperature_ReturnsHighSeverity() {
        // Given - critical temperature
        testReading.setAnomalyDetected(true);
        testReading.setTemperatureCelsius(BigDecimal.valueOf(105.0));

        // When
        int result = anomalyDetectionService.calculateAnomalySeverity(testReading);

        // Then
        assertTrue(result >= 30);
    }

    @Test
    void calculateAnomalySeverity_MultipleAnomalies_ReturnsCappedAt100() {
        // Given - multiple critical anomalies
        testReading.setAnomalyDetected(true);
        testReading.setRealTimePowerWatts(BigDecimal.valueOf(12000.0));
        testReading.setEfficiencyPercentage(BigDecimal.valueOf(25.0));
        testReading.setTemperatureCelsius(BigDecimal.valueOf(105.0));

        // When
        int result = anomalyDetectionService.calculateAnomalySeverity(testReading);

        // Then
        assertTrue(result <= 100);
    }

    @Test
    void getHighConsumptionThreshold_ReturnsCorrectValue() {
        // When
        BigDecimal threshold = AnomalyDetectionService.getHighConsumptionThreshold();

        // Then
        assertEquals(BigDecimal.valueOf(150.0), threshold);
    }
}

