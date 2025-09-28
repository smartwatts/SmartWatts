package com.smartwatts.appliancemonitoringservice.service;

import com.smartwatts.appliancemonitoringservice.model.ApplianceReading;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaintenanceSchedulingService {
    
    /**
     * Check if an appliance needs maintenance based on current readings
     */
    public boolean checkMaintenanceNeeded(UUID applianceId, ApplianceReading reading) {
        if (reading == null) {
            return false;
        }
        
        // Check for critical anomalies that require immediate attention
        if (reading.getAnomalyDetected() && reading.getAnomalyType() != null) {
            switch (reading.getAnomalyType()) {
                case "TEMPERATURE_ANOMALY":
                    return true; // High temperature requires immediate attention
                case "HIGH_CONSUMPTION":
                    return reading.getRealTimePowerWatts() != null && 
                           reading.getRealTimePowerWatts().compareTo(java.math.BigDecimal.valueOf(10000)) > 0;
                case "LOW_EFFICIENCY":
                    return reading.getEfficiencyPercentage() != null && 
                           reading.getEfficiencyPercentage().compareTo(java.math.BigDecimal.valueOf(30)) < 0;
            }
        }
        
        // Check for maintenance alerts from the reading itself
        if (reading.getMaintenanceAlert() != null && reading.getMaintenanceAlert()) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Generate maintenance message based on the issue detected
     */
    public String generateMaintenanceMessage(UUID applianceId, ApplianceReading reading) {
        if (reading == null) {
            return "Maintenance check required";
        }
        
        StringBuilder message = new StringBuilder();
        
        if (reading.getAnomalyDetected() && reading.getAnomalyType() != null) {
            switch (reading.getAnomalyType()) {
                case "TEMPERATURE_ANOMALY":
                    message.append("CRITICAL: High temperature detected. ");
                    if (reading.getTemperatureCelsius() != null) {
                        message.append("Current temperature: ").append(reading.getTemperatureCelsius()).append("°C. ");
                    }
                    message.append("Immediate maintenance required to prevent damage.");
                    break;
                    
                case "HIGH_CONSUMPTION":
                    message.append("High power consumption detected. ");
                    if (reading.getRealTimePowerWatts() != null) {
                        message.append("Current power: ").append(reading.getRealTimePowerWatts()).append("W. ");
                    }
                    message.append("Check for mechanical issues or overload conditions.");
                    break;
                    
                case "LOW_EFFICIENCY":
                    message.append("Low efficiency detected. ");
                    if (reading.getEfficiencyPercentage() != null) {
                        message.append("Current efficiency: ").append(reading.getEfficiencyPercentage()).append("%. ");
                    }
                    message.append("Maintenance may be needed to restore optimal performance.");
                    break;
                    
                case "POWER_FACTOR_ANOMALY":
                    message.append("Low power factor detected. ");
                    if (reading.getPowerFactor() != null) {
                        message.append("Current power factor: ").append(reading.getPowerFactor()).append(". ");
                    }
                    message.append("Check electrical connections and component health.");
                    break;
                    
                default:
                    message.append("Anomaly detected requiring investigation. ");
                    message.append("Schedule maintenance check.");
                    break;
            }
        } else if (reading.getMaintenanceAlert() != null && reading.getMaintenanceAlert()) {
            message.append("Maintenance alert: Regular maintenance due. ");
            message.append("Check manufacturer recommendations and schedule service.");
        } else {
            message.append("Maintenance check recommended based on performance metrics.");
        }
        
        return message.toString();
    }
    
    /**
     * Calculate next maintenance date based on appliance type and usage
     */
    public LocalDateTime calculateNextMaintenanceDate(UUID applianceId, String applianceType) {
        LocalDateTime now = LocalDateTime.now();
        
        // Base maintenance intervals (in months) for different appliance types
        int baseIntervalMonths = switch (applianceType) {
            case "REFRIGERATOR" -> 12;      // Annual maintenance
            case "AC_UNIT" -> 6;            // Semi-annual for AC
            case "WASHING_MACHINE" -> 18;   // Every 1.5 years
            case "DISHWASHER" -> 24;        // Every 2 years
            case "OVEN_STOVE" -> 12;        // Annual
            case "WATER_HEATER" -> 12;      // Annual
            case "HEATING_SYSTEM" -> 6;     // Semi-annual
            default -> 12;                  // Default to annual
        };
        
        return now.plusMonths(baseIntervalMonths);
    }
    
    /**
     * Check if maintenance is overdue
     */
    public boolean isMaintenanceOverdue(LocalDateTime lastMaintenanceDate, LocalDateTime nextMaintenanceDate) {
        if (lastMaintenanceDate == null || nextMaintenanceDate == null) {
            return false;
        }
        
        return LocalDateTime.now().isAfter(nextMaintenanceDate);
    }
}
