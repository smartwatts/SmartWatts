package com.smartwatts.appliancemonitoringservice.service;

import com.smartwatts.appliancemonitoringservice.model.ApplianceReading;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnomalyDetectionService {
    
    private static final BigDecimal HIGH_CONSUMPTION_THRESHOLD = BigDecimal.valueOf(150.0); // 150% of normal - used for anomaly detection
    
    /**
     * Get high consumption threshold for external use
     */
    public static BigDecimal getHighConsumptionThreshold() {
        return HIGH_CONSUMPTION_THRESHOLD;
    }
    private static final BigDecimal LOW_EFFICIENCY_THRESHOLD = BigDecimal.valueOf(60.0); // Below 60% efficiency
    private static final BigDecimal TEMPERATURE_ANOMALY_THRESHOLD = BigDecimal.valueOf(80.0); // Above 80°C
    
    /**
     * Detect anomalies in appliance readings
     */
    public boolean detectAnomaly(ApplianceReading reading) {
        if (reading == null) {
            return false;
        }
        
        // Check for high power consumption
        if (reading.getRealTimePowerWatts() != null && 
            reading.getRealTimePowerWatts().compareTo(BigDecimal.valueOf(5000)) > 0) {
            log.debug("High power consumption anomaly detected: {}W", reading.getRealTimePowerWatts());
            return true;
        }
        
        // Check for low efficiency
        if (reading.getEfficiencyPercentage() != null && 
            reading.getEfficiencyPercentage().compareTo(LOW_EFFICIENCY_THRESHOLD) < 0) {
            log.debug("Low efficiency anomaly detected: {}%", reading.getEfficiencyPercentage());
            return true;
        }
        
        // Check for temperature anomalies
        if (reading.getTemperatureCelsius() != null && 
            reading.getTemperatureCelsius().compareTo(TEMPERATURE_ANOMALY_THRESHOLD) > 0) {
            log.debug("Temperature anomaly detected: {}°C", reading.getTemperatureCelsius());
            return true;
        }
        
        // Check for power factor anomalies
        if (reading.getPowerFactor() != null && 
            reading.getPowerFactor().compareTo(BigDecimal.valueOf(0.8)) < 0) {
            log.debug("Low power factor anomaly detected: {}", reading.getPowerFactor());
            return true;
        }
        
        return false;
    }
    
    /**
     * Determine the type of anomaly detected
     */
    public String determineAnomalyType(ApplianceReading reading) {
        if (reading == null) {
            return "UNKNOWN";
        }
        
        // High power consumption
        if (reading.getRealTimePowerWatts() != null && 
            reading.getRealTimePowerWatts().compareTo(BigDecimal.valueOf(5000)) > 0) {
            return "HIGH_CONSUMPTION";
        }
        
        // Low efficiency
        if (reading.getEfficiencyPercentage() != null && 
            reading.getEfficiencyPercentage().compareTo(LOW_EFFICIENCY_THRESHOLD) < 0) {
            return "LOW_EFFICIENCY";
        }
        
        // Temperature anomaly
        if (reading.getTemperatureCelsius() != null && 
            reading.getTemperatureCelsius().compareTo(TEMPERATURE_ANOMALY_THRESHOLD) > 0) {
            return "TEMPERATURE_ANOMALY";
        }
        
        // Power factor anomaly
        if (reading.getPowerFactor() != null && 
            reading.getPowerFactor().compareTo(BigDecimal.valueOf(0.8)) < 0) {
            return "POWER_FACTOR_ANOMALY";
        }
        
        return "UNKNOWN";
    }
    
    /**
     * Calculate anomaly severity score (0-100, higher = more severe)
     */
    public int calculateAnomalySeverity(ApplianceReading reading) {
        if (reading == null || !reading.getAnomalyDetected()) {
            return 0;
        }
        
        int severity = 0;
        
        // Power consumption severity
        if (reading.getRealTimePowerWatts() != null) {
            if (reading.getRealTimePowerWatts().compareTo(BigDecimal.valueOf(10000)) > 0) {
                severity += 40; // Critical
            } else if (reading.getRealTimePowerWatts().compareTo(BigDecimal.valueOf(5000)) > 0) {
                severity += 25; // High
            }
        }
        
        // Efficiency severity
        if (reading.getEfficiencyPercentage() != null) {
            if (reading.getEfficiencyPercentage().compareTo(BigDecimal.valueOf(30)) < 0) {
                severity += 35; // Critical
            } else if (reading.getEfficiencyPercentage().compareTo(BigDecimal.valueOf(60)) < 0) {
                severity += 20; // High
            }
        }
        
        // Temperature severity
        if (reading.getTemperatureCelsius() != null) {
            if (reading.getTemperatureCelsius().compareTo(BigDecimal.valueOf(100)) > 0) {
                severity += 30; // Critical
            } else if (reading.getTemperatureCelsius().compareTo(BigDecimal.valueOf(80)) > 0) {
                severity += 15; // High
            }
        }
        
        return Math.min(severity, 100);
    }
}
