package com.smartwatts.analyticsservice.service;

import com.smartwatts.analyticsservice.model.UsagePattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatternAnalysisService {
    
    public UsagePattern analyzeDailyRhythm(UUID userId, UUID deviceId, LocalDateTime date, 
                                        BigDecimal totalConsumption, BigDecimal averagePower, BigDecimal peakPower) {
        log.info("Analyzing daily rhythm for user: {}, device: {}, date: {}", userId, deviceId, date);
        
        UsagePattern pattern = new UsagePattern();
        pattern.setUserId(userId);
        pattern.setDeviceId(deviceId);
        pattern.setPatternDate(date);
        pattern.setPatternType(UsagePattern.PatternType.DAILY_RHYTHM);
        pattern.setStartTime(date.toLocalDate().atStartOfDay());
        pattern.setEndTime(date.toLocalDate().atTime(23, 59, 59));
        pattern.setTotalConsumptionKwh(totalConsumption);
        pattern.setAveragePowerKw(averagePower);
        pattern.setPeakPowerKw(peakPower);
        pattern.setDurationHours(BigDecimal.valueOf(24.0));
        
        // Analyze consumption patterns
        analyzeConsumptionPattern(pattern);
        
        // Detect anomalies
        detectAnomalies(pattern);
        
        // Generate recommendations
        generateRecommendations(pattern);
        
        return pattern;
    }
    
    public UsagePattern analyzeWeeklyPattern(UUID userId, UUID deviceId, LocalDateTime startDate, 
                                          BigDecimal totalConsumption, BigDecimal averagePower, BigDecimal peakPower) {
        log.info("Analyzing weekly pattern for user: {}, device: {}, start date: {}", userId, deviceId, startDate);
        
        UsagePattern pattern = new UsagePattern();
        pattern.setUserId(userId);
        pattern.setDeviceId(deviceId);
        pattern.setPatternDate(startDate);
        pattern.setPatternType(UsagePattern.PatternType.WEEKLY_PATTERN);
        pattern.setStartTime(startDate);
        pattern.setEndTime(startDate.plusDays(7));
        pattern.setTotalConsumptionKwh(totalConsumption);
        pattern.setAveragePowerKw(averagePower);
        pattern.setPeakPowerKw(peakPower);
        pattern.setDurationHours(BigDecimal.valueOf(168.0)); // 7 days * 24 hours
        
        // Analyze weekly patterns
        analyzeWeeklyPattern(pattern);
        
        // Detect anomalies
        detectAnomalies(pattern);
        
        // Generate recommendations
        generateRecommendations(pattern);
        
        return pattern;
    }
    
    public UsagePattern analyzePeakUsage(UUID userId, UUID deviceId, LocalDateTime startTime, LocalDateTime endTime,
                                       BigDecimal totalConsumption, BigDecimal averagePower, BigDecimal peakPower) {
        log.info("Analyzing peak usage for user: {}, device: {}, period: {} to {}", userId, deviceId, startTime, endTime);
        
        UsagePattern pattern = new UsagePattern();
        pattern.setUserId(userId);
        pattern.setDeviceId(deviceId);
        pattern.setPatternDate(startTime);
        pattern.setPatternType(UsagePattern.PatternType.PEAK_USAGE);
        pattern.setStartTime(startTime);
        pattern.setEndTime(endTime);
        pattern.setTotalConsumptionKwh(totalConsumption);
        pattern.setAveragePowerKw(averagePower);
        pattern.setPeakPowerKw(peakPower);
        
        // Calculate duration
        long hours = java.time.Duration.between(startTime, endTime).toHours();
        pattern.setDurationHours(BigDecimal.valueOf(hours));
        
        // Analyze peak usage patterns
        analyzePeakUsagePattern(pattern);
        
        // Detect anomalies
        detectAnomalies(pattern);
        
        // Generate recommendations
        generateRecommendations(pattern);
        
        return pattern;
    }
    
    public UsagePattern analyzeAnomalousUsage(UUID userId, UUID deviceId, LocalDateTime startTime, LocalDateTime endTime,
                                            BigDecimal totalConsumption, BigDecimal averagePower, BigDecimal peakPower,
                                            BigDecimal anomalyScore) {
        log.info("Analyzing anomalous usage for user: {}, device: {}, period: {} to {}", userId, deviceId, startTime, endTime);
        
        UsagePattern pattern = new UsagePattern();
        pattern.setUserId(userId);
        pattern.setDeviceId(deviceId);
        pattern.setPatternDate(startTime);
        pattern.setPatternType(UsagePattern.PatternType.ANOMALOUS_USAGE);
        pattern.setStartTime(startTime);
        pattern.setEndTime(endTime);
        pattern.setTotalConsumptionKwh(totalConsumption);
        pattern.setAveragePowerKw(averagePower);
        pattern.setPeakPowerKw(peakPower);
        pattern.setIsAnomaly(true);
        pattern.setAnomalyScore(anomalyScore);
        
        // Calculate duration
        long hours = java.time.Duration.between(startTime, endTime).toHours();
        pattern.setDurationHours(BigDecimal.valueOf(hours));
        
        // Analyze anomalous patterns
        analyzeAnomalousPattern(pattern);
        
        // Generate recommendations
        generateRecommendations(pattern);
        
        return pattern;
    }
    
    private void analyzeConsumptionPattern(UsagePattern pattern) {
        // Analyze consumption patterns and set category/subcategory
        if (pattern.getTotalConsumptionKwh() != null) {
            if (pattern.getTotalConsumptionKwh().compareTo(BigDecimal.valueOf(50.0)) > 0) {
                pattern.setCategory("HIGH_CONSUMPTION");
                pattern.setSubcategory("EXCESSIVE_USAGE");
            } else if (pattern.getTotalConsumptionKwh().compareTo(BigDecimal.valueOf(20.0)) > 0) {
                pattern.setCategory("MODERATE_CONSUMPTION");
                pattern.setSubcategory("NORMAL_USAGE");
            } else {
                pattern.setCategory("LOW_CONSUMPTION");
                pattern.setSubcategory("EFFICIENT_USAGE");
            }
        }
        
        // Set pattern description
        pattern.setPatternDescription("Daily energy consumption pattern analysis");
    }
    
    private void analyzeWeeklyPattern(UsagePattern pattern) {
        // Analyze weekly patterns
        pattern.setCategory("WEEKLY_PATTERN");
        pattern.setSubcategory("REGULAR_SCHEDULE");
        pattern.setPatternDescription("Weekly energy consumption pattern analysis");
        
        // Calculate seasonal factor (simplified)
        pattern.setSeasonalFactor(BigDecimal.valueOf(1.0));
    }
    
    private void analyzePeakUsagePattern(UsagePattern pattern) {
        // Analyze peak usage patterns
        pattern.setCategory("PEAK_USAGE");
        pattern.setSubcategory("HIGH_DEMAND");
        pattern.setPatternDescription("Peak energy usage pattern analysis");
        
        // Calculate weather correlation (placeholder)
        pattern.setWeatherCorrelation(BigDecimal.valueOf(0.3));
    }
    
    private void analyzeAnomalousPattern(UsagePattern pattern) {
        // Analyze anomalous patterns
        pattern.setCategory("ANOMALOUS_USAGE");
        pattern.setSubcategory("UNUSUAL_PATTERN");
        pattern.setPatternDescription("Anomalous energy usage pattern detected");
        
        // Set high anomaly score
        if (pattern.getAnomalyScore() == null) {
            pattern.setAnomalyScore(BigDecimal.valueOf(85.0));
        }
    }
    
    private void detectAnomalies(UsagePattern pattern) {
        // Simple anomaly detection logic
        boolean isAnomaly = false;
        BigDecimal anomalyScore = BigDecimal.ZERO;
        
        // Check for high peak power
        if (pattern.getPeakPowerKw() != null && pattern.getPeakPowerKw().compareTo(BigDecimal.valueOf(15.0)) > 0) {
            isAnomaly = true;
            anomalyScore = anomalyScore.add(BigDecimal.valueOf(30.0));
        }
        
        // Check for unusual consumption
        if (pattern.getTotalConsumptionKwh() != null && pattern.getTotalConsumptionKwh().compareTo(BigDecimal.valueOf(100.0)) > 0) {
            isAnomaly = true;
            anomalyScore = anomalyScore.add(BigDecimal.valueOf(40.0));
        }
        
        // Check for high average power
        if (pattern.getAveragePowerKw() != null && pattern.getAveragePowerKw().compareTo(BigDecimal.valueOf(8.0)) > 0) {
            isAnomaly = true;
            anomalyScore = anomalyScore.add(BigDecimal.valueOf(25.0));
        }
        
        pattern.setIsAnomaly(isAnomaly);
        if (isAnomaly && pattern.getAnomalyScore() == null) {
            pattern.setAnomalyScore(anomalyScore.min(BigDecimal.valueOf(100.0)));
        }
    }
    
    private void generateRecommendations(UsagePattern pattern) {
        StringBuilder recommendations = new StringBuilder();
        
        if (pattern.getIsAnomaly() != null && pattern.getIsAnomaly()) {
            recommendations.append("High energy consumption detected. ");
            recommendations.append("Consider reducing usage during peak hours. ");
        }
        
        if (pattern.getPeakPowerKw() != null && pattern.getPeakPowerKw().compareTo(BigDecimal.valueOf(10.0)) > 0) {
            recommendations.append("Peak power usage is high. ");
            recommendations.append("Consider spreading usage across different times. ");
        }
        
        if (pattern.getTotalConsumptionKwh() != null && pattern.getTotalConsumptionKwh().compareTo(BigDecimal.valueOf(50.0)) > 0) {
            recommendations.append("Daily consumption is above average. ");
            recommendations.append("Review energy-intensive activities. ");
        }
        
        if (recommendations.length() == 0) {
            recommendations.append("Energy usage patterns are within normal ranges. ");
            recommendations.append("Continue current practices for efficiency. ");
        }
        
        pattern.setRecommendations(recommendations.toString());
    }
    
    public List<UsagePattern> detectPatternsFromData(UUID userId, UUID deviceId, List<Object> energyData) {
        log.info("Detecting patterns from energy data for user: {}, device: {}", userId, deviceId);
        
        // This would implement more sophisticated pattern detection algorithms
        // For now, return empty list as placeholder
        return List.of();
    }
    
    public BigDecimal calculateAnomalyScore(UsagePattern pattern) {
        // Calculate anomaly score based on various factors
        BigDecimal score = BigDecimal.ZERO;
        
        if (pattern.getPeakPowerKw() != null) {
            if (pattern.getPeakPowerKw().compareTo(BigDecimal.valueOf(15.0)) > 0) {
                score = score.add(BigDecimal.valueOf(40.0));
            } else if (pattern.getPeakPowerKw().compareTo(BigDecimal.valueOf(10.0)) > 0) {
                score = score.add(BigDecimal.valueOf(20.0));
            }
        }
        
        if (pattern.getTotalConsumptionKwh() != null) {
            if (pattern.getTotalConsumptionKwh().compareTo(BigDecimal.valueOf(100.0)) > 0) {
                score = score.add(BigDecimal.valueOf(50.0));
            } else if (pattern.getTotalConsumptionKwh().compareTo(BigDecimal.valueOf(50.0)) > 0) {
                score = score.add(BigDecimal.valueOf(25.0));
            }
        }
        
        return score.min(BigDecimal.valueOf(100.0));
    }
} 