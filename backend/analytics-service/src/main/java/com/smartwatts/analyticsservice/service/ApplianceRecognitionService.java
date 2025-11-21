package com.smartwatts.analyticsservice.service;

import com.smartwatts.analyticsservice.model.ApplianceSignature;
import com.smartwatts.analyticsservice.model.ApplianceDetection;
import com.smartwatts.analyticsservice.model.EnergyReading;
import com.smartwatts.analyticsservice.repository.ApplianceSignatureRepository;
import com.smartwatts.analyticsservice.repository.ApplianceDetectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplianceRecognitionService {

    private final ApplianceSignatureRepository signatureRepository;
    private final ApplianceDetectionRepository detectionRepository;

    /**
     * Analyze energy readings to detect appliances using NILM techniques
     */
    @Transactional
    public List<ApplianceDetection> detectAppliances(UUID deviceId, List<EnergyReading> readings) {
        log.info("Starting appliance detection for device: {} with {} readings", deviceId, 
                readings != null ? readings.size() : 0);
        
        List<ApplianceDetection> detections = new ArrayList<>();
        
        // Handle null or empty readings - return empty list instead of throwing error
        if (readings == null || readings.isEmpty()) {
            log.warn("No energy readings provided for device: {}. Returning empty detection list.", deviceId);
            return detections;
        }
        
        // Get known appliance signatures
        List<ApplianceSignature> signatures = signatureRepository.findByDeviceId(deviceId);
        
        // Analyze power patterns
        Map<String, List<EnergyReading>> powerPatterns = analyzePowerPatterns(readings);
        
        // Detect appliances based on signatures
        for (ApplianceSignature signature : signatures) {
            ApplianceDetection detection = matchSignature(powerPatterns, signature, readings);
            if (detection != null) {
                detections.add(detection);
            }
        }
        
        // Detect unknown appliances using pattern analysis
        List<ApplianceDetection> unknownDetections = detectUnknownAppliances(powerPatterns, readings);
        detections.addAll(unknownDetections);
        
        // Save detections
        if (!detections.isEmpty()) {
            detectionRepository.saveAll(detections);
        }
        
        log.info("Detected {} appliances for device: {}", detections.size(), deviceId);
        return detections;
    }

    /**
     * Analyze power patterns from energy readings
     */
    private Map<String, List<EnergyReading>> analyzePowerPatterns(List<EnergyReading> readings) {
        Map<String, List<EnergyReading>> patterns = new HashMap<>();
        
        // Group readings by power level ranges
        for (EnergyReading reading : readings) {
            BigDecimal power = reading.getPowerConsumption();
            String powerRange = getPowerRange(power);
            patterns.computeIfAbsent(powerRange, k -> new ArrayList<>()).add(reading);
        }
        
        return patterns;
    }

    /**
     * Get power range category for pattern analysis
     */
    private String getPowerRange(BigDecimal power) {
        if (power.compareTo(new BigDecimal("50")) < 0) {
            return "LOW"; // 0-50W (LED lights, small electronics)
        } else if (power.compareTo(new BigDecimal("200")) < 0) {
            return "MEDIUM"; // 50-200W (TV, computer, small appliances)
        } else if (power.compareTo(new BigDecimal("1000")) < 0) {
            return "HIGH"; // 200-1000W (refrigerator, washing machine)
        } else if (power.compareTo(new BigDecimal("3000")) < 0) {
            return "VERY_HIGH"; // 1000-3000W (AC, dryer, water heater)
        } else {
            return "EXTREME"; // 3000W+ (EV charger, large AC units)
        }
    }

    /**
     * Match power patterns against known appliance signatures
     */
    private ApplianceDetection matchSignature(Map<String, List<EnergyReading>> patterns, 
                                            ApplianceSignature signature, 
                                            List<EnergyReading> readings) {
        // Calculate signature match score
        double matchScore = calculateMatchScore(patterns, signature);
        
        if (matchScore > 0.7) { // 70% confidence threshold
            ApplianceDetection detection = new ApplianceDetection();
            detection.setDeviceId(signature.getDeviceId());
            detection.setApplianceName(signature.getApplianceName());
            detection.setApplianceType(ApplianceDetection.ApplianceType.valueOf(signature.getApplianceType().name()));
            detection.setConfidenceScore(BigDecimal.valueOf(matchScore));
            detection.setDetectionTime(LocalDateTime.now());
            detection.setPowerConsumption(calculateAveragePower(readings));
            detection.setStatus(ApplianceDetection.DetectionStatus.DETECTED);
            
            return detection;
        }
        
        return null;
    }

    /**
     * Calculate match score between patterns and signature
     */
    private double calculateMatchScore(Map<String, List<EnergyReading>> patterns, ApplianceSignature signature) {
        // Simplified matching algorithm - in production, this would use more sophisticated ML
        double score = 0.0;
        
        // Check power level match
        String expectedRange = getPowerRange(signature.getTypicalPowerConsumption());
        if (patterns.containsKey(expectedRange)) {
            score += 0.4;
        }
        
        // Check usage pattern match (time-based)
        if (signature.getTypicalUsagePattern() != null) {
            score += 0.3;
        }
        
        // Check frequency characteristics
        if (signature.getFrequencyCharacteristics() != null) {
            score += 0.3;
        }
        
        return Math.min(1.0, score);
    }

    /**
     * Detect unknown appliances using pattern analysis
     */
    private List<ApplianceDetection> detectUnknownAppliances(Map<String, List<EnergyReading>> patterns, 
                                                           List<EnergyReading> readings) {
        List<ApplianceDetection> detections = new ArrayList<>();
        
        // Handle null or empty readings
        if (readings == null || readings.isEmpty()) {
            return detections;
        }
        
        // Look for significant power changes that might indicate new appliances
        for (Map.Entry<String, List<EnergyReading>> entry : patterns.entrySet()) {
            String powerRange = entry.getKey();
            List<EnergyReading> rangeReadings = entry.getValue();
            
            if (rangeReadings != null && rangeReadings.size() > 10) { // Minimum readings for detection
                ApplianceDetection detection = new ApplianceDetection();
                detection.setDeviceId(readings.get(0).getDeviceId());
                detection.setApplianceName("Unknown " + powerRange + " Appliance");
                detection.setApplianceType(ApplianceDetection.ApplianceType.UNKNOWN);
                detection.setConfidenceScore(BigDecimal.valueOf(0.5)); // Lower confidence for unknown
                detection.setDetectionTime(LocalDateTime.now());
                detection.setPowerConsumption(calculateAveragePower(rangeReadings));
                detection.setStatus(ApplianceDetection.DetectionStatus.UNKNOWN);
                
                detections.add(detection);
            }
        }
        
        return detections;
    }

    /**
     * Train appliance signature with user feedback
     */
    @Transactional
    public ApplianceSignature trainApplianceSignature(UUID deviceId, String applianceName, 
                                                     String applianceType, List<EnergyReading> trainingData) {
        log.info("Training appliance signature for: {} on device: {}", applianceName, deviceId);
        
        ApplianceSignature signature = new ApplianceSignature();
        signature.setDeviceId(deviceId);
        signature.setApplianceName(applianceName);
        signature.setApplianceType(ApplianceSignature.ApplianceType.valueOf(applianceType));
        signature.setTypicalPowerConsumption(calculateAveragePower(trainingData));
        signature.setTypicalUsagePattern(analyzeUsagePattern(trainingData));
        signature.setFrequencyCharacteristics(analyzeFrequencyCharacteristics(trainingData));
        signature.setTrainingDataSize(trainingData.size());
        signature.setAccuracyScore(BigDecimal.valueOf(0.8)); // Initial accuracy
        signature.setLastUpdated(LocalDateTime.now());
        signature.setIsActive(true);
        
        signatureRepository.save(signature);
        
        log.info("Trained signature for {} with {} training samples", applianceName, trainingData.size());
        return signature;
    }

    /**
     * Get appliance usage data for dashboard
     */
    public Map<String, Object> getApplianceUsage(UUID deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        List<ApplianceDetection> detections = detectionRepository.findByDeviceIdAndDetectionTimeBetween(
            deviceId, startTime, endTime);
        
        Map<String, Object> usageData = new HashMap<>();
        
        // Group by appliance type
        Map<ApplianceDetection.ApplianceType, List<ApplianceDetection>> byType = 
            detections.stream().collect(Collectors.groupingBy(ApplianceDetection::getApplianceType));
        
        // Calculate usage statistics
        Map<String, Object> applianceStats = new HashMap<>();
        for (Map.Entry<ApplianceDetection.ApplianceType, List<ApplianceDetection>> entry : byType.entrySet()) {
            ApplianceDetection.ApplianceType type = entry.getKey();
            List<ApplianceDetection> typeDetections = entry.getValue();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("count", typeDetections.size());
            stats.put("averagePower", calculateAveragePowerFromValues(typeDetections.stream()
                .map(ApplianceDetection::getPowerConsumption)
                .collect(Collectors.toList())));
            stats.put("totalConsumption", calculateTotalConsumption(typeDetections));
            
            applianceStats.put(type.name(), stats);
        }
        
        usageData.put("applianceStats", applianceStats);
        usageData.put("totalDetections", detections.size());
        usageData.put("detectionTime", LocalDateTime.now());
        
        return usageData;
    }

    /**
     * Analyze usage pattern from training data
     */
    private String analyzeUsagePattern(List<EnergyReading> readings) {
        // Simplified pattern analysis - in production, this would use time series analysis
        Map<Integer, Integer> hourUsage = new HashMap<>();
        
        for (EnergyReading reading : readings) {
            int hour = reading.getReadingTimestamp().getHour();
            hourUsage.put(hour, hourUsage.getOrDefault(hour, 0) + 1);
        }
        
        // Determine peak usage hours
        int peakHour = hourUsage.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(12);
        
        if (peakHour >= 6 && peakHour <= 18) {
            return "DAYTIME";
        } else {
            return "NIGHTTIME";
        }
    }

    /**
     * Analyze frequency characteristics from training data
     */
    private String analyzeFrequencyCharacteristics(List<EnergyReading> readings) {
        // Simplified frequency analysis - in production, this would use FFT
        BigDecimal avgPower = calculateAveragePower(readings);
        
        if (avgPower.compareTo(new BigDecimal("100")) < 0) {
            return "LOW_FREQUENCY";
        } else if (avgPower.compareTo(new BigDecimal("1000")) < 0) {
            return "MEDIUM_FREQUENCY";
        } else {
            return "HIGH_FREQUENCY";
        }
    }

    /**
     * Calculate average power from readings
     */
    private BigDecimal calculateAveragePower(List<EnergyReading> readings) {
        if (readings.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal total = readings.stream()
            .map(EnergyReading::getPowerConsumption)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return total.divide(BigDecimal.valueOf(readings.size()), 2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate average power from BigDecimal list
     */
    private BigDecimal calculateAveragePowerFromValues(List<BigDecimal> powerValues) {
        if (powerValues.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal total = powerValues.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return total.divide(BigDecimal.valueOf(powerValues.size()), 2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate total consumption from detections
     */
    private BigDecimal calculateTotalConsumption(List<ApplianceDetection> detections) {
        return detections.stream()
            .map(ApplianceDetection::getPowerConsumption)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Get detection history for a device within a time range
     */
    @Transactional(readOnly = true)
    public List<ApplianceDetection> getDetectionHistory(UUID deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Getting detection history for device: {} from {} to {}", deviceId, startTime, endTime);
        
        try {
            List<ApplianceDetection> detections = detectionRepository.findByDeviceIdAndDetectionTimeBetween(deviceId, startTime, endTime);
            log.debug("Found {} detections for device: {} in time range", detections.size(), deviceId);
            return detections;
        } catch (Exception e) {
            log.error("Error getting detection history for device: {}", deviceId, e);
            return new ArrayList<>();
        }
    }
}
