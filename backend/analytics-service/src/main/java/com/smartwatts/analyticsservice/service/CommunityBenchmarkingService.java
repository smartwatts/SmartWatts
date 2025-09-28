package com.smartwatts.analyticsservice.service;

import com.smartwatts.analyticsservice.model.CommunityBenchmark;
import com.smartwatts.analyticsservice.model.EnergyReading;
import com.smartwatts.analyticsservice.repository.CommunityBenchmarkRepository;
import com.smartwatts.analyticsservice.repository.EnergyReadingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityBenchmarkingService {

    private final CommunityBenchmarkRepository benchmarkRepository;
    private final EnergyReadingRepository energyReadingRepository;

    /**
     * Calculate user's efficiency ranking in their region
     */
    @Transactional
    public Map<String, Object> calculateUserRanking(UUID userId, String region) {
        log.info("Calculating user ranking for user: {} in region: {}", userId, region);
        
        // Get user's energy data
        Map<String, Object> userMetrics = calculateUserMetrics(userId);
        
        // Get regional benchmarks
        CommunityBenchmark regionalBenchmark = benchmarkRepository.findByRegionAndMetricType(region, "ENERGY_EFFICIENCY")
            .orElse(createDefaultBenchmark(region, "ENERGY_EFFICIENCY"));
        
        // Calculate ranking
        Map<String, Object> ranking = new HashMap<>();
        ranking.put("userId", userId);
        ranking.put("region", region);
        ranking.put("userMetrics", userMetrics);
        ranking.put("regionalBenchmark", regionalBenchmark);
        ranking.put("ranking", calculateRanking(userMetrics, regionalBenchmark));
        ranking.put("percentile", calculatePercentile(userMetrics, regionalBenchmark));
        ranking.put("recommendations", generateRecommendations(userMetrics, regionalBenchmark));
        ranking.put("calculatedAt", LocalDateTime.now());
        
        return ranking;
    }

    /**
     * Get community leaderboard for a region
     */
    public Map<String, Object> getCommunityLeaderboard(String region, String metricType, int limit) {
        log.info("Getting community leaderboard for region: {}, metric: {}, limit: {}", region, metricType, limit);
        
        // Get top performers in the region
        List<Map<String, Object>> topPerformers = getTopPerformers(region, metricType, limit);
        
        // Get regional statistics
        Map<String, Object> regionalStats = getRegionalStatistics(region, metricType);
        
        Map<String, Object> leaderboard = new HashMap<>();
        leaderboard.put("region", region);
        leaderboard.put("metricType", metricType);
        leaderboard.put("topPerformers", topPerformers);
        leaderboard.put("regionalStats", regionalStats);
        leaderboard.put("generatedAt", LocalDateTime.now());
        
        return leaderboard;
    }

    /**
     * Update community benchmarks with anonymized data
     */
    @Transactional
    public void updateCommunityBenchmarks(String region, String metricType, Map<String, Object> anonymizedData) {
        log.info("Updating community benchmarks for region: {}, metric: {}", region, metricType);
        
        CommunityBenchmark benchmark = benchmarkRepository.findByRegionAndMetricType(region, metricType)
            .orElse(CommunityBenchmark.builder()
                .region(region)
                .metricType(metricType)
                .build());
        
        // Update benchmark data
        benchmark.setAverageValue(new BigDecimal(anonymizedData.get("average").toString()));
        benchmark.setMedianValue(new BigDecimal(anonymizedData.get("median").toString()));
        benchmark.setPercentile25(new BigDecimal(anonymizedData.get("percentile25").toString()));
        benchmark.setPercentile75(new BigDecimal(anonymizedData.get("percentile75").toString()));
        benchmark.setPercentile90(new BigDecimal(anonymizedData.get("percentile90").toString()));
        benchmark.setSampleSize((Integer) anonymizedData.get("sampleSize"));
        benchmark.setLastUpdated(LocalDateTime.now());
        benchmark.setIsActive(true);
        
        benchmarkRepository.save(benchmark);
        
        log.info("Updated community benchmark for region: {}, metric: {}", region, metricType);
    }

    /**
     * Get solar utilization comparison
     */
    public Map<String, Object> getSolarUtilizationComparison(UUID userId, String region) {
        log.info("Getting solar utilization comparison for user: {} in region: {}", userId, region);
        
        // Get user's solar data
        Map<String, Object> userSolarData = calculateUserSolarMetrics(userId);
        
        // Get regional solar benchmarks
        CommunityBenchmark solarBenchmark = benchmarkRepository.findByRegionAndMetricType(region, "SOLAR_UTILIZATION")
            .orElse(createDefaultSolarBenchmark(region));
        
        // Calculate comparison
        Map<String, Object> comparison = new HashMap<>();
        comparison.put("userId", userId);
        comparison.put("region", region);
        comparison.put("userSolarData", userSolarData);
        comparison.put("regionalBenchmark", solarBenchmark);
        comparison.put("utilizationRanking", calculateSolarRanking(userSolarData, solarBenchmark));
        comparison.put("efficiencyMessage", generateSolarEfficiencyMessage(userSolarData, solarBenchmark));
        comparison.put("calculatedAt", LocalDateTime.now());
        
        return comparison;
    }

    /**
     * Get energy savings potential comparison
     */
    public Map<String, Object> getEnergySavingsComparison(UUID userId, String region) {
        log.info("Getting energy savings comparison for user: {} in region: {}", userId, region);
        
        // Get user's current consumption
        Map<String, Object> userConsumption = calculateUserConsumptionMetrics(userId);
        
        // Get regional consumption benchmarks
        CommunityBenchmark consumptionBenchmark = benchmarkRepository.findByRegionAndMetricType(region, "ENERGY_CONSUMPTION")
            .orElse(createDefaultConsumptionBenchmark(region));
        
        // Calculate savings potential
        Map<String, Object> savingsComparison = new HashMap<>();
        savingsComparison.put("userId", userId);
        savingsComparison.put("region", region);
        savingsComparison.put("userConsumption", userConsumption);
        savingsComparison.put("regionalBenchmark", consumptionBenchmark);
        savingsComparison.put("savingsPotential", calculateSavingsPotential(userConsumption, consumptionBenchmark));
        savingsComparison.put("savingsRanking", calculateSavingsRanking(userConsumption, consumptionBenchmark));
        savingsComparison.put("recommendations", generateSavingsRecommendations(userConsumption, consumptionBenchmark));
        savingsComparison.put("calculatedAt", LocalDateTime.now());
        
        return savingsComparison;
    }

    /**
     * Calculate user metrics
     */
    private Map<String, Object> calculateUserMetrics(UUID userId) {
        // Get user's energy readings for the last 30 days
        LocalDateTime startTime = LocalDateTime.now().minusDays(30);
        LocalDateTime endTime = LocalDateTime.now();
        
        List<EnergyReading> readings = energyReadingRepository.findByUserIdAndReadingTimestampBetween(userId, startTime, endTime);
        
        Map<String, Object> metrics = new HashMap<>();
        
        if (!readings.isEmpty()) {
            // Calculate average daily consumption
            BigDecimal totalConsumption = readings.stream()
                .map(EnergyReading::getEnergyConsumed)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal averageDailyConsumption = totalConsumption.divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);
            metrics.put("averageDailyConsumption", averageDailyConsumption);
            
            // Calculate efficiency score (simplified)
            BigDecimal efficiencyScore = calculateEfficiencyScore(readings);
            metrics.put("efficiencyScore", efficiencyScore);
            
            // Calculate peak usage
            BigDecimal peakUsage = readings.stream()
                .map(EnergyReading::getEnergyConsumed)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
            metrics.put("peakUsage", peakUsage);
        } else {
            metrics.put("averageDailyConsumption", BigDecimal.ZERO);
            metrics.put("efficiencyScore", BigDecimal.ZERO);
            metrics.put("peakUsage", BigDecimal.ZERO);
        }
        
        return metrics;
    }

    /**
     * Calculate user solar metrics
     */
    private Map<String, Object> calculateUserSolarMetrics(UUID userId) {
        // This would integrate with solar monitoring data
        Map<String, Object> solarMetrics = new HashMap<>();
        solarMetrics.put("solarGeneration", new BigDecimal("500")); // kWh/month
        solarMetrics.put("solarUtilization", new BigDecimal("85")); // percentage
        solarMetrics.put("solarEfficiency", new BigDecimal("92")); // percentage
        return solarMetrics;
    }

    /**
     * Calculate user consumption metrics
     */
    private Map<String, Object> calculateUserConsumptionMetrics(UUID userId) {
        Map<String, Object> consumptionMetrics = new HashMap<>();
        consumptionMetrics.put("monthlyConsumption", new BigDecimal("800")); // kWh
        consumptionMetrics.put("dailyAverage", new BigDecimal("26.7")); // kWh
        consumptionMetrics.put("peakDemand", new BigDecimal("5.2")); // kW
        return consumptionMetrics;
    }

    /**
     * Calculate efficiency score
     */
    private BigDecimal calculateEfficiencyScore(List<EnergyReading> readings) {
        // Simplified efficiency calculation
        if (readings.isEmpty()) return BigDecimal.ZERO;
        
        BigDecimal totalConsumption = readings.stream()
            .map(EnergyReading::getEnergyConsumed)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Efficiency score based on consumption patterns
        BigDecimal averageConsumption = totalConsumption.divide(BigDecimal.valueOf(readings.size()), 2, RoundingMode.HALF_UP);
        
        // Normalize to 0-100 scale
        return averageConsumption.multiply(new BigDecimal("10")).min(new BigDecimal("100"));
    }

    /**
     * Calculate ranking
     */
    private String calculateRanking(Map<String, Object> userMetrics, CommunityBenchmark benchmark) {
        BigDecimal userEfficiency = (BigDecimal) userMetrics.get("efficiencyScore");
        BigDecimal regionalAverage = benchmark.getAverageValue();
        
        if (userEfficiency.compareTo(regionalAverage.multiply(new BigDecimal("1.2"))) > 0) {
            return "TOP_10_PERCENT";
        } else if (userEfficiency.compareTo(regionalAverage.multiply(new BigDecimal("1.1"))) > 0) {
            return "TOP_20_PERCENT";
        } else if (userEfficiency.compareTo(regionalAverage) > 0) {
            return "ABOVE_AVERAGE";
        } else if (userEfficiency.compareTo(regionalAverage.multiply(new BigDecimal("0.9"))) > 0) {
            return "AVERAGE";
        } else {
            return "BELOW_AVERAGE";
        }
    }

    /**
     * Calculate percentile
     */
    private BigDecimal calculatePercentile(Map<String, Object> userMetrics, CommunityBenchmark benchmark) {
        BigDecimal userEfficiency = (BigDecimal) userMetrics.get("efficiencyScore");
        BigDecimal regionalAverage = benchmark.getAverageValue();
        
        // Simplified percentile calculation
        if (userEfficiency.compareTo(benchmark.getPercentile90()) > 0) {
            return new BigDecimal("95");
        } else if (userEfficiency.compareTo(benchmark.getPercentile75()) > 0) {
            return new BigDecimal("85");
        } else if (userEfficiency.compareTo(regionalAverage) > 0) {
            return new BigDecimal("65");
        } else if (userEfficiency.compareTo(benchmark.getPercentile25()) > 0) {
            return new BigDecimal("35");
        } else {
            return new BigDecimal("15");
        }
    }

    /**
     * Generate recommendations
     */
    private List<String> generateRecommendations(Map<String, Object> userMetrics, CommunityBenchmark benchmark) {
        List<String> recommendations = new ArrayList<>();
        
        BigDecimal userEfficiency = (BigDecimal) userMetrics.get("efficiencyScore");
        BigDecimal regionalAverage = benchmark.getAverageValue();
        
        if (userEfficiency.compareTo(regionalAverage) < 0) {
            recommendations.add("Consider upgrading to energy-efficient appliances");
            recommendations.add("Implement smart home automation for better energy management");
            recommendations.add("Schedule energy-intensive tasks during off-peak hours");
        } else {
            recommendations.add("Great job! You're above the regional average");
            recommendations.add("Consider sharing your energy-saving tips with the community");
        }
        
        return recommendations;
    }

    /**
     * Get top performers
     */
    private List<Map<String, Object>> getTopPerformers(String region, String metricType, int limit) {
        // This would query anonymized user data
        List<Map<String, Object>> performers = new ArrayList<>();
        
        for (int i = 1; i <= limit; i++) {
            Map<String, Object> performer = new HashMap<>();
            performer.put("rank", i);
            performer.put("userId", "user_" + i); // Anonymized
            performer.put("value", new BigDecimal("100").subtract(new BigDecimal(i * 5)));
            performer.put("region", region);
            performers.add(performer);
        }
        
        return performers;
    }

    /**
     * Get regional statistics
     */
    private Map<String, Object> getRegionalStatistics(String region, String metricType) {
        CommunityBenchmark benchmark = benchmarkRepository.findByRegionAndMetricType(region, metricType)
            .orElse(createDefaultBenchmark(region, metricType));
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("average", benchmark.getAverageValue());
        stats.put("median", benchmark.getMedianValue());
        stats.put("percentile25", benchmark.getPercentile25());
        stats.put("percentile75", benchmark.getPercentile75());
        stats.put("percentile90", benchmark.getPercentile90());
        stats.put("sampleSize", benchmark.getSampleSize());
        
        return stats;
    }

    /**
     * Calculate solar ranking
     */
    private String calculateSolarRanking(Map<String, Object> userSolarData, CommunityBenchmark benchmark) {
        BigDecimal userUtilization = (BigDecimal) userSolarData.get("solarUtilization");
        BigDecimal regionalAverage = benchmark.getAverageValue();
        
        if (userUtilization.compareTo(regionalAverage.multiply(new BigDecimal("1.2"))) > 0) {
            return "TOP_10_PERCENT";
        } else if (userUtilization.compareTo(regionalAverage.multiply(new BigDecimal("1.1"))) > 0) {
            return "TOP_20_PERCENT";
        } else if (userUtilization.compareTo(regionalAverage) > 0) {
            return "ABOVE_AVERAGE";
        } else {
            return "BELOW_AVERAGE";
        }
    }

    /**
     * Generate solar efficiency message
     */
    private String generateSolarEfficiencyMessage(Map<String, Object> userSolarData, CommunityBenchmark benchmark) {
        String ranking = calculateSolarRanking(userSolarData, benchmark);
        
        switch (ranking) {
            case "TOP_10_PERCENT":
                return "You're in the top 10% of households in " + benchmark.getRegion() + " for solar utilization!";
            case "TOP_20_PERCENT":
                return "You're in the top 20% of households in " + benchmark.getRegion() + " for solar utilization!";
            case "ABOVE_AVERAGE":
                return "You're above average for solar utilization in " + benchmark.getRegion() + "!";
            default:
                return "Consider optimizing your solar system to improve utilization in " + benchmark.getRegion() + ".";
        }
    }

    /**
     * Calculate savings potential
     */
    private Map<String, Object> calculateSavingsPotential(Map<String, Object> userConsumption, CommunityBenchmark benchmark) {
        Map<String, Object> savings = new HashMap<>();
        
        BigDecimal userConsumptionValue = (BigDecimal) userConsumption.get("monthlyConsumption");
        BigDecimal regionalAverage = benchmark.getAverageValue();
        
        if (userConsumptionValue.compareTo(regionalAverage) > 0) {
            BigDecimal potentialSavings = userConsumptionValue.subtract(regionalAverage);
            savings.put("monthlySavings", potentialSavings);
            savings.put("annualSavings", potentialSavings.multiply(new BigDecimal("12")));
            savings.put("savingsPercentage", potentialSavings.divide(userConsumptionValue, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")));
        } else {
            savings.put("monthlySavings", BigDecimal.ZERO);
            savings.put("annualSavings", BigDecimal.ZERO);
            savings.put("savingsPercentage", BigDecimal.ZERO);
        }
        
        return savings;
    }

    /**
     * Calculate savings ranking
     */
    private String calculateSavingsRanking(Map<String, Object> userConsumption, CommunityBenchmark benchmark) {
        BigDecimal userConsumptionValue = (BigDecimal) userConsumption.get("monthlyConsumption");
        BigDecimal regionalAverage = benchmark.getAverageValue();
        
        if (userConsumptionValue.compareTo(regionalAverage.multiply(new BigDecimal("0.8"))) < 0) {
            return "EXCELLENT";
        } else if (userConsumptionValue.compareTo(regionalAverage) < 0) {
            return "GOOD";
        } else if (userConsumptionValue.compareTo(regionalAverage.multiply(new BigDecimal("1.2"))) < 0) {
            return "AVERAGE";
        } else {
            return "NEEDS_IMPROVEMENT";
        }
    }

    /**
     * Generate savings recommendations
     */
    private List<String> generateSavingsRecommendations(Map<String, Object> userConsumption, CommunityBenchmark benchmark) {
        List<String> recommendations = new ArrayList<>();
        
        String ranking = calculateSavingsRanking(userConsumption, benchmark);
        
        switch (ranking) {
            case "NEEDS_IMPROVEMENT":
                recommendations.add("Consider energy-efficient appliances");
                recommendations.add("Implement smart home automation");
                recommendations.add("Schedule energy-intensive tasks during off-peak hours");
                break;
            case "AVERAGE":
                recommendations.add("You're doing well! Consider minor optimizations");
                recommendations.add("Monitor peak usage patterns");
                break;
            default:
                recommendations.add("Excellent energy efficiency! Keep up the great work!");
                break;
        }
        
        return recommendations;
    }

    /**
     * Create default benchmark - only when no real data exists
     */
    private CommunityBenchmark createDefaultBenchmark(String region, String metricType) {
        // Return null to indicate no data available
        return null;
    }

    /**
     * Create default solar benchmark - only when no real data exists
     */
    private CommunityBenchmark createDefaultSolarBenchmark(String region) {
        // Return null to indicate no data available
        return null;
    }

    /**
     * Create default consumption benchmark - only when no real data exists
     */
    private CommunityBenchmark createDefaultConsumptionBenchmark(String region) {
        // Return null to indicate no data available
        return null;
    }
}
