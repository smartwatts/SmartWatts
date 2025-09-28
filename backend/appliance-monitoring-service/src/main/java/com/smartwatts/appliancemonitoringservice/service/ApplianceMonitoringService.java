package com.smartwatts.appliancemonitoringservice.service;

import com.smartwatts.appliancemonitoringservice.model.Appliance;
import com.smartwatts.appliancemonitoringservice.model.ApplianceReading;
import com.smartwatts.appliancemonitoringservice.model.ApplianceType;
import com.smartwatts.appliancemonitoringservice.repository.ApplianceRepository;
import com.smartwatts.appliancemonitoringservice.repository.ApplianceReadingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplianceMonitoringService {
    
    private final ApplianceRepository applianceRepository;
    private final ApplianceReadingRepository readingRepository;
    private final AnomalyDetectionService anomalyDetectionService;
    private final MaintenanceSchedulingService maintenanceService;
    
    /**
     * Record a new appliance reading and analyze for anomalies
     */
    @Transactional
    public ApplianceReading recordApplianceReading(UUID applianceId, ApplianceReading reading) {
        log.info("Recording reading for appliance: {}", applianceId);
        
        // Set timestamp and appliance ID
        reading.setApplianceId(applianceId);
        reading.setTimestamp(LocalDateTime.now());
        
        // Calculate efficiency if not provided
        if (reading.getEfficiencyPercentage() == null) {
            reading.setEfficiencyPercentage(calculateEfficiency(reading));
        }
        
        // Check for anomalies
        boolean hasAnomaly = anomalyDetectionService.detectAnomaly(reading);
        reading.setAnomalyDetected(hasAnomaly);
        
        if (hasAnomaly) {
            reading.setAnomalyType(anomalyDetectionService.determineAnomalyType(reading));
            log.warn("Anomaly detected for appliance {}: {}", applianceId, reading.getAnomalyType());
        }
        
        // Check for maintenance alerts
        boolean needsMaintenance = maintenanceService.checkMaintenanceNeeded(applianceId, reading);
        reading.setMaintenanceAlert(needsMaintenance);
        
        if (needsMaintenance) {
            reading.setMaintenanceMessage(maintenanceService.generateMaintenanceMessage(applianceId, reading));
        }
        
        // Calculate data quality score
        reading.setDataQualityScore(calculateDataQuality(reading));
        
        ApplianceReading savedReading = readingRepository.save(reading);
        log.info("Appliance reading recorded with ID: {}", savedReading.getId());
        
        return savedReading;
    }
    
    /**
     * Get appliance readings within a time range
     */
    public List<ApplianceReading> getApplianceReadings(UUID applianceId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Fetching readings for appliance: {} from {} to {}", applianceId, startTime, endTime);
        return readingRepository.findByApplianceIdAndTimestampBetween(applianceId, startTime, endTime);
    }
    
    /**
     * Get appliance efficiency statistics
     */
    public ApplianceEfficiencyStats getEfficiencyStats(UUID applianceId, LocalDateTime startTime, LocalDateTime endTime) {
        List<ApplianceReading> readings = readingRepository.findByApplianceIdAndTimestampBetween(
            applianceId, startTime, endTime);
        
        if (readings.isEmpty()) {
            return new ApplianceEfficiencyStats();
        }
        
        BigDecimal avgEfficiency = readings.stream()
            .map(ApplianceReading::getEfficiencyPercentage)
            .filter(eff -> eff != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(readings.size()), 2, java.math.RoundingMode.HALF_UP);
        
        BigDecimal totalConsumption = readings.stream()
            .map(ApplianceReading::getEnergyConsumptionKwh)
            .filter(cons -> cons != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return ApplianceEfficiencyStats.builder()
            .averageEfficiency(avgEfficiency)
            .totalConsumption(totalConsumption)
            .readingsCount(readings.size())
            .build();
    }
    
    /**
     * Get appliances by user with current status
     */
    public List<ApplianceStatus> getUserAppliancesWithStatus(UUID userId) {
        List<Appliance> appliances = applianceRepository.findByUserIdAndIsActive(userId, true);
        
        return appliances.stream()
            .map(this::getApplianceStatus)
            .toList();
    }
    
    /**
     * Calculate appliance efficiency based on power consumption and rated power
     */
    private BigDecimal calculateEfficiency(ApplianceReading reading) {
        if (reading.getRealTimePowerWatts() == null || reading.getRealTimePowerWatts().equals(BigDecimal.ZERO)) {
            return BigDecimal.valueOf(100.0); // Default to 100% if no power data
        }
        
        // This is a simplified efficiency calculation
        // In a real system, you'd compare against historical baselines and manufacturer specs
        BigDecimal efficiency = BigDecimal.valueOf(85.0); // Default efficiency
        
        // Adjust based on temperature if available
        if (reading.getTemperatureCelsius() != null) {
            if (reading.getTemperatureCelsius().compareTo(BigDecimal.valueOf(25)) > 0) {
                efficiency = efficiency.subtract(BigDecimal.valueOf(5.0)); // Reduce efficiency in high temps
            }
        }
        
        return efficiency.max(BigDecimal.valueOf(0.0)).min(BigDecimal.valueOf(100.0));
    }
    
    /**
     * Calculate data quality score based on completeness and validity
     */
    private BigDecimal calculateDataQuality(ApplianceReading reading) {
        int score = 100;
        
        // Deduct points for missing critical data
        if (reading.getRealTimePowerWatts() == null) score -= 30;
        if (reading.getVoltageVolts() == null) score -= 20;
        if (reading.getCurrentAmps() == null) score -= 20;
        if (reading.getTimestamp() == null) score -= 10;
        
        // Deduct points for invalid data
        if (reading.getRealTimePowerWatts() != null && reading.getRealTimePowerWatts().compareTo(BigDecimal.ZERO) < 0) {
            score -= 20;
        }
        
        return BigDecimal.valueOf(Math.max(0, score));
    }
    
    /**
     * Get current status of an appliance
     */
    private ApplianceStatus getApplianceStatus(Appliance appliance) {
        ApplianceReading latestReading = readingRepository.findTopByApplianceIdOrderByTimestampDesc(appliance.getId());
        
        return ApplianceStatus.builder()
            .applianceId(appliance.getId())
            .applianceName(appliance.getApplianceName())
            .applianceType(appliance.getApplianceType())
            .currentPower(latestReading != null ? latestReading.getRealTimePowerWatts() : BigDecimal.ZERO)
            .efficiency(latestReading != null ? latestReading.getEfficiencyPercentage() : BigDecimal.valueOf(100.0))
            .operatingStatus(latestReading != null ? latestReading.getOperatingStatus() : "UNKNOWN")
            .lastReadingTime(latestReading != null ? latestReading.getTimestamp() : null)
            .hasAnomaly(latestReading != null && latestReading.getAnomalyDetected())
            .needsMaintenance(latestReading != null && latestReading.getMaintenanceAlert())
            .build();
    }
    
    // Analytics Methods
    
    public Object getUserAnalyticsSummary(UUID userId) {
        log.info("Fetching analytics summary for user: {}", userId);
        List<Appliance> appliances = getUserAppliances(userId);
        
        // Calculate summary statistics
        int totalAppliances = appliances.size();
        int activeAppliances = (int) appliances.stream().filter(Appliance::getIsActive).count();
        double averageEfficiency = 85.0; // Placeholder
        
        return Map.of(
            "totalAppliances", totalAppliances,
            "activeAppliances", activeAppliances,
            "averageEfficiency", averageEfficiency,
            "lastUpdated", LocalDateTime.now()
        );
    }
    
    public List<ApplianceReading> getApplianceAnomalies(UUID applianceId) {
        log.info("Fetching anomalies for appliance: {}", applianceId);
        return readingRepository.findAnomaliesByApplianceId(applianceId);
    }
    
    public List<ApplianceReading> getMaintenanceAlerts(UUID applianceId) {
        log.info("Fetching maintenance alerts for appliance: {}", applianceId);
        return readingRepository.findMaintenanceAlertsByApplianceId(applianceId);
    }
    
    // Appliance Management Methods
    
    @Transactional
    public Appliance createAppliance(Appliance appliance) {
        log.info("Creating new appliance: {}", appliance.getApplianceName());
        appliance.setCreatedAt(LocalDateTime.now());
        appliance.setUpdatedAt(LocalDateTime.now());
        return applianceRepository.save(appliance);
    }
    
    public List<Appliance> getUserAppliances(UUID userId) {
        log.info("Fetching appliances for user: {}", userId);
        return applianceRepository.findByUserId(userId);
    }
    
    public Appliance getAppliance(UUID applianceId) {
        log.info("Fetching appliance: {}", applianceId);
        return applianceRepository.findById(applianceId)
            .orElseThrow(() -> new RuntimeException("Appliance not found: " + applianceId));
    }
    
    @Transactional
    public Appliance updateAppliance(Appliance appliance) {
        log.info("Updating appliance: {}", appliance.getId());
        appliance.setUpdatedAt(LocalDateTime.now());
        return applianceRepository.save(appliance);
    }
    
    @Transactional
    public void deactivateAppliance(UUID applianceId) {
        log.info("Deactivating appliance: {}", applianceId);
        Appliance appliance = getAppliance(applianceId);
        appliance.setIsActive(false);
        appliance.setUpdatedAt(LocalDateTime.now());
        applianceRepository.save(appliance);
    }
    
    // Inner classes for data transfer
    public static class ApplianceEfficiencyStats {
        @SuppressWarnings("unused")
        private BigDecimal averageEfficiency;
        @SuppressWarnings("unused")
        private BigDecimal totalConsumption;
        @SuppressWarnings("unused")
        private int readingsCount;
        
        // Builder pattern
        public static ApplianceEfficiencyStatsBuilder builder() {
            return new ApplianceEfficiencyStatsBuilder();
        }
        
        public static class ApplianceEfficiencyStatsBuilder {
            private ApplianceEfficiencyStats stats = new ApplianceEfficiencyStats();
            
            public ApplianceEfficiencyStatsBuilder averageEfficiency(BigDecimal avg) {
                stats.averageEfficiency = avg;
                return this;
            }
            
            public ApplianceEfficiencyStatsBuilder totalConsumption(BigDecimal total) {
                stats.totalConsumption = total;
                return this;
            }
            
            public ApplianceEfficiencyStatsBuilder readingsCount(int count) {
                stats.readingsCount = count;
                return this;
            }
            
            public ApplianceEfficiencyStats build() {
                return stats;
            }
        }
    }
    
    public static class ApplianceStatus {
        @SuppressWarnings("unused")
        private UUID applianceId;
        @SuppressWarnings("unused")
        private String applianceName;
        @SuppressWarnings("unused")
        private ApplianceType applianceType;
        @SuppressWarnings("unused")
        private BigDecimal currentPower;
        @SuppressWarnings("unused")
        private BigDecimal efficiency;
        @SuppressWarnings("unused")
        private String operatingStatus;
        @SuppressWarnings("unused")
        private LocalDateTime lastReadingTime;
        @SuppressWarnings("unused")
        private boolean hasAnomaly;
        @SuppressWarnings("unused")
        private boolean needsMaintenance;
        
        // Builder pattern
        public static ApplianceStatusBuilder builder() {
            return new ApplianceStatusBuilder();
        }
        
        public static class ApplianceStatusBuilder {
            private ApplianceStatus status = new ApplianceStatus();
            
            public ApplianceStatusBuilder applianceId(UUID id) {
                status.applianceId = id;
                return this;
            }
            
            public ApplianceStatusBuilder applianceName(String name) {
                status.applianceName = name;
                return this;
            }
            
            public ApplianceStatusBuilder applianceType(ApplianceType type) {
                status.applianceType = type;
                return this;
            }
            
            public ApplianceStatusBuilder currentPower(BigDecimal power) {
                status.currentPower = power;
                return this;
            }
            
            public ApplianceStatusBuilder efficiency(BigDecimal eff) {
                status.efficiency = eff;
                return this;
            }
            
            public ApplianceStatusBuilder operatingStatus(String status) {
                this.status.operatingStatus = status;
                return this;
            }
            
            public ApplianceStatusBuilder lastReadingTime(LocalDateTime time) {
                status.lastReadingTime = time;
                return this;
            }
            
            public ApplianceStatusBuilder hasAnomaly(boolean anomaly) {
                status.hasAnomaly = anomaly;
                return this;
            }
            
            public ApplianceStatusBuilder needsMaintenance(boolean maintenance) {
                status.needsMaintenance = maintenance;
                return this;
            }
            
            public ApplianceStatus build() {
                return status;
            }
        }
    }
}
