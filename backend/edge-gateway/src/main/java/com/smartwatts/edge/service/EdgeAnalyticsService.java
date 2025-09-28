package com.smartwatts.edge.service;

import com.smartwatts.edge.model.EnergyPrediction;
import com.smartwatts.edge.model.AnomalyDetection;
import com.smartwatts.edge.model.OptimizationRecommendation;

import com.smartwatts.edge.storage.EdgeStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Edge Analytics Service for Real-time Data Analysis and Insights
 * Provides comprehensive analytics using ML models and edge processing
 */
@Service
public class EdgeAnalyticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(EdgeAnalyticsService.class);
    
    private final TensorFlowLiteService tensorFlowLiteService;
    private final EdgeMLService edgeMLService;
    private final EdgeStorageService edgeStorageService;
    
    // Analytics cache and real-time metrics
    private final Map<String, Object> analyticsCache = new ConcurrentHashMap<>();
    private final Map<String, Double> realTimeMetrics = new ConcurrentHashMap<>();
    private final List<AnomalyDetection> recentAnomalies = new ArrayList<>();
    private final List<EnergyPrediction> recentPredictions = new ArrayList<>();
    
    // Configuration
    private static final int CACHE_TTL_MINUTES = 15; // Cache TTL in minutes
    private static final int MAX_RECENT_ITEMS = 100;
    private static final int ANALYTICS_WINDOW_HOURS = 24;
    
    @Autowired
    public EdgeAnalyticsService(TensorFlowLiteService tensorFlowLiteService,
                              EdgeMLService edgeMLService,
                              EdgeStorageService edgeStorageService) {
        this.tensorFlowLiteService = tensorFlowLiteService;
        this.edgeMLService = edgeMLService;
        this.edgeStorageService = edgeStorageService;
    }
    
    /**
     * Generate comprehensive facility analytics
     */
    public Map<String, Object> generateFacilityAnalytics(String facilityId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            String cacheKey = String.format("facility_%s_%s_%s", facilityId, startTime, endTime);
            
            // Check cache first
            if (isCacheValid(cacheKey)) {
                logger.debug("Returning cached analytics for facility: {}", facilityId);
                @SuppressWarnings("unchecked")
                Map<String, Object> cachedResult = (Map<String, Object>) analyticsCache.get(cacheKey);
                return cachedResult;
            }
            
            logger.info("Generating analytics for facility: {} from {} to {}", facilityId, startTime, endTime);
            
            Map<String, Object> analytics = new HashMap<>();
            
            // Basic metrics
            Map<String, Object> basicMetrics = calculateBasicMetrics(facilityId, startTime, endTime);
            analytics.put("basic_metrics", basicMetrics);
            
            // Energy consumption analysis
            Map<String, Object> energyAnalysis = analyzeEnergyConsumption(facilityId, startTime, endTime);
            analytics.put("energy_analysis", energyAnalysis);
            
            // Anomaly detection
            Map<String, Object> anomalyAnalysis = analyzeAnomalies(facilityId, startTime, endTime);
            analytics.put("anomaly_analysis", anomalyAnalysis);
            
            // Predictive insights
            Map<String, Object> predictiveInsights = generatePredictiveInsights(facilityId, startTime, endTime);
            analytics.put("predictive_insights", predictiveInsights);
            
            // Optimization recommendations
            List<OptimizationRecommendation> recommendations = generateOptimizationRecommendations(facilityId);
            analytics.put("optimization_recommendations", recommendations);
            
            // Performance trends
            Map<String, Object> performanceTrends = analyzePerformanceTrends(facilityId, startTime, endTime);
            analytics.put("performance_trends", performanceTrends);
            
            // Metadata
            analytics.put("facility_id", facilityId);
            analytics.put("analysis_period", Map.of(
                "start_time", startTime,
                "end_time", endTime,
                "duration_hours", ChronoUnit.HOURS.between(startTime, endTime)
            ));
            analytics.put("generated_at", LocalDateTime.now());
            analytics.put("cache_key", cacheKey);
            
            // Cache the results
            cacheAnalytics(cacheKey, analytics);
            
            logger.info("Analytics generated successfully for facility: {}", facilityId);
            return analytics;
            
        } catch (Exception e) {
            logger.error("Failed to generate facility analytics for: {}", facilityId, e);
            return createErrorAnalytics(facilityId, e.getMessage());
        }
    }
    
    /**
     * Generate real-time analytics for immediate insights
     */
    public Map<String, Object> generateRealTimeAnalytics(String facilityId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime windowStart = now.minusHours(1); // Last hour
            
            logger.debug("Generating real-time analytics for facility: {} (window: {} to {})", facilityId, windowStart, now);
            
            Map<String, Object> realTimeAnalytics = new HashMap<>();
            
            // Current energy consumption
            double currentConsumption = getCurrentEnergyConsumption(facilityId);
            realTimeAnalytics.put("current_energy_consumption", currentConsumption);
            
            // Real-time metrics
            realTimeAnalytics.put("real_time_metrics", new HashMap<>(realTimeMetrics));
            
            // Recent anomalies
            List<AnomalyDetection> recentAnomalies = getRecentAnomalies(facilityId, 10);
            realTimeAnalytics.put("recent_anomalies", recentAnomalies);
            
            // Recent predictions
            List<EnergyPrediction> recentPredictions = getRecentPredictions(facilityId, 5);
            realTimeAnalytics.put("recent_predictions", recentPredictions);
            
            // Performance indicators
            Map<String, Object> performanceIndicators = calculatePerformanceIndicators(facilityId);
            realTimeAnalytics.put("performance_indicators", performanceIndicators);
            
            // Alerts and notifications
            List<Map<String, Object>> alerts = generateAlerts(facilityId);
            realTimeAnalytics.put("alerts", alerts);
            
            // Metadata
            realTimeAnalytics.put("facility_id", facilityId);
            realTimeAnalytics.put("generated_at", now);
            realTimeAnalytics.put("analysis_window", "1_hour");
            
            return realTimeAnalytics;
            
        } catch (Exception e) {
            logger.error("Failed to generate real-time analytics for facility: {}", facilityId, e);
            return Map.of("error", e.getMessage(), "facility_id", facilityId);
        }
    }
    
    /**
     * Generate comparative analytics between facilities
     */
    public Map<String, Object> generateComparativeAnalytics(List<String> facilityIds, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            logger.info("Generating comparative analytics for {} facilities", facilityIds.size());
            
            Map<String, Object> comparativeAnalytics = new HashMap<>();
            
            // Individual facility analytics
            Map<String, Object> individualAnalytics = new HashMap<>();
            for (String facilityId : facilityIds) {
                Map<String, Object> facilityAnalytics = generateFacilityAnalytics(facilityId, startTime, endTime);
                individualAnalytics.put(facilityId, facilityAnalytics);
            }
            comparativeAnalytics.put("individual_analytics", individualAnalytics);
            
            // Comparative metrics
            Map<String, Object> comparativeMetrics = calculateComparativeMetrics(facilityIds, startTime, endTime);
            comparativeAnalytics.put("comparative_metrics", comparativeMetrics);
            
            // Ranking and benchmarking
            Map<String, Object> rankings = generateFacilityRankings(facilityIds, startTime, endTime);
            comparativeAnalytics.put("rankings", rankings);
            
            // Best practices identification
            List<Map<String, Object>> bestPractices = identifyBestPractices(facilityIds, startTime, endTime);
            comparativeAnalytics.put("best_practices", bestPractices);
            
            // Metadata
            comparativeAnalytics.put("facility_ids", facilityIds);
            comparativeAnalytics.put("analysis_period", Map.of(
                "start_time", startTime,
                "end_time", endTime
            ));
            comparativeAnalytics.put("generated_at", LocalDateTime.now());
            
            logger.info("Comparative analytics generated successfully for {} facilities", facilityIds.size());
            return comparativeAnalytics;
            
        } catch (Exception e) {
            logger.error("Failed to generate comparative analytics", e);
            return Map.of("error", e.getMessage());
        }
    }
    
    /**
     * Generate predictive analytics for future planning
     */
    public Map<String, Object> generatePredictiveAnalytics(String facilityId, int forecastHours) {
        try {
            logger.info("Generating predictive analytics for facility: {} ({} hours)", facilityId, forecastHours);
            
            Map<String, Object> predictiveAnalytics = new HashMap<>();
            
            // Historical data for prediction
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(ANALYTICS_WINDOW_HOURS);
            
            logger.debug("Using historical data from {} to {} for prediction", startTime, endTime);
            
            // Energy consumption forecast
            List<EnergyPrediction> energyForecast = generateEnergyForecast(facilityId, forecastHours);
            predictiveAnalytics.put("energy_forecast", energyForecast);
            
            // Demand prediction
            Map<String, Object> demandPrediction = predictDemandPatterns(facilityId, forecastHours);
            predictiveAnalytics.put("demand_prediction", demandPrediction);
            
            // Cost projections
            Map<String, Object> costProjections = projectEnergyCosts(facilityId, forecastHours);
            predictiveAnalytics.put("cost_projections", costProjections);
            
            // Risk assessment
            Map<String, Object> riskAssessment = assessOperationalRisks(facilityId, forecastHours);
            predictiveAnalytics.put("risk_assessment", riskAssessment);
            
            // Optimization opportunities
            List<Map<String, Object>> optimizationOpportunities = identifyOptimizationOpportunities(facilityId, forecastHours);
            predictiveAnalytics.put("optimization_opportunities", optimizationOpportunities);
            
            // Metadata
            predictiveAnalytics.put("facility_id", facilityId);
            predictiveAnalytics.put("forecast_hours", forecastHours);
            predictiveAnalytics.put("generated_at", LocalDateTime.now());
            predictiveAnalytics.put("confidence_level", calculatePredictionConfidence(facilityId));
            
            logger.info("Predictive analytics generated successfully for facility: {}", facilityId);
            return predictiveAnalytics;
            
        } catch (Exception e) {
            logger.error("Failed to generate predictive analytics for facility: {}", facilityId, e);
            return Map.of("error", e.getMessage(), "facility_id", facilityId);
        }
    }
    
    /**
     * Update real-time metrics
     */
    public void updateRealTimeMetric(String metricName, double value) {
        realTimeMetrics.put(metricName, value);
        logger.debug("Updated real-time metric: {} = {}", metricName, value);
    }
    
    /**
     * Add recent anomaly detection
     */
    public void addRecentAnomaly(AnomalyDetection anomaly) {
        synchronized (recentAnomalies) {
            recentAnomalies.add(anomaly);
            if (recentAnomalies.size() > MAX_RECENT_ITEMS) {
                recentAnomalies.remove(0);
            }
        }
        logger.debug("Added recent anomaly: {}", anomaly.getSeverity());
    }
    
    /**
     * Add recent energy prediction
     */
    public void addRecentPrediction(EnergyPrediction prediction) {
        synchronized (recentPredictions) {
            recentPredictions.add(prediction);
            if (recentPredictions.size() > MAX_RECENT_ITEMS) {
                recentPredictions.remove(0);
            }
        }
        logger.debug("Added recent prediction with confidence: {}%", prediction.getConfidence());
    }
    
    // Private helper methods
    
    private Map<String, Object> calculateBasicMetrics(String facilityId, LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // Get device readings for the facility
            List<Map<String, Object>> readings = edgeStorageService.getDeviceReadings(facilityId, 1000);
            
            if (readings.isEmpty()) {
                metrics.put("total_readings", 0);
                metrics.put("average_consumption", 0.0);
                metrics.put("peak_consumption", 0.0);
                return metrics;
            }
            
            // Filter by time range (placeholder implementation)
            List<Map<String, Object>> filteredReadings = readings.stream()
                    .filter(r -> {
                        // Placeholder filtering logic
                        return true;
                    })
                    .collect(Collectors.toList());
            
            // Calculate metrics (placeholder implementation)
            double totalConsumption = filteredReadings.size() * 10.0; // Placeholder calculation
            double avgConsumption = 10.0; // Placeholder calculation
            double peakConsumption = 15.0; // Placeholder calculation
            
            metrics.put("total_readings", filteredReadings.size());
            metrics.put("total_consumption", totalConsumption);
            metrics.put("average_consumption", avgConsumption);
            metrics.put("peak_consumption", peakConsumption);
            metrics.put("data_completeness", (double) filteredReadings.size() / readings.size());
            
        } catch (Exception e) {
            logger.error("Failed to calculate basic metrics", e);
            metrics.put("error", e.getMessage());
        }
        
        return metrics;
    }
    
    private Map<String, Object> analyzeEnergyConsumption(String facilityId, LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> analysis = new HashMap<>();
        
        try {
            // Get consumption patterns
            Map<String, Object> consumptionPatterns = analyzeConsumptionPatterns(facilityId, startTime, endTime);
            analysis.put("consumption_patterns", consumptionPatterns);
            
            // Efficiency metrics
            Map<String, Object> efficiencyMetrics = calculateEfficiencyMetrics(facilityId, startTime, endTime);
            analysis.put("efficiency_metrics", efficiencyMetrics);
            
            // Cost analysis
            Map<String, Object> costAnalysis = analyzeEnergyCosts(facilityId, startTime, endTime);
            analysis.put("cost_analysis", costAnalysis);
            
        } catch (Exception e) {
            logger.error("Failed to analyze energy consumption", e);
            analysis.put("error", e.getMessage());
        }
        
        return analysis;
    }
    
    private Map<String, Object> analyzeAnomalies(String facilityId, LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> analysis = new HashMap<>();
        
        try {
            // Get anomalies from storage
            List<AnomalyDetection> anomalies = getAnomaliesInTimeRange(facilityId, startTime, endTime);
            
            // Categorize anomalies
            Map<String, Long> severityCounts = anomalies.stream()
                    .collect(Collectors.groupingBy(anomaly -> anomaly.getSeverity().toString(), Collectors.counting()));
            
            // Calculate anomaly statistics
            double anomalyRate = calculateAnomalyRate(facilityId, startTime, endTime);
            
            analysis.put("total_anomalies", anomalies.size());
            analysis.put("anomaly_rate", anomalyRate);
            analysis.put("severity_distribution", severityCounts);
            analysis.put("recent_anomalies", anomalies.stream()
                    .sorted((a1, a2) -> a2.getTimestamp().compareTo(a1.getTimestamp()))
                    .limit(10)
                    .collect(Collectors.toList()));
            
        } catch (Exception e) {
            logger.error("Failed to analyze anomalies", e);
            analysis.put("error", e.getMessage());
        }
        
        return analysis;
    }
    
    private Map<String, Object> generatePredictiveInsights(String facilityId, LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> insights = new HashMap<>();
        
        try {
            // Generate short-term forecast
            EnergyPrediction shortTermForecast = generateShortTermForecast(facilityId);
            insights.put("short_term_forecast", shortTermForecast);
            
            // Identify trends
            Map<String, Object> trends = identifyEnergyTrends(facilityId, startTime, endTime);
            insights.put("trends", trends);
            
            // Seasonal patterns
            Map<String, Object> seasonalPatterns = analyzeSeasonalPatterns(facilityId, startTime, endTime);
            insights.put("seasonal_patterns", seasonalPatterns);
            
        } catch (Exception e) {
            logger.error("Failed to generate predictive insights", e);
            insights.put("error", e.getMessage());
        }
        
        return insights;
    }
    
    private List<OptimizationRecommendation> generateOptimizationRecommendations(String facilityId) {
        try {
            // Get current facility metrics
            Map<String, Double> facilityMetrics = getCurrentFacilityMetrics(facilityId);
            
            // Get historical data
            Map<String, Double> historicalData = getHistoricalData(facilityId);
            
            // Generate recommendations using TensorFlow Lite
            return tensorFlowLiteService.generateOptimizationRecommendations(facilityMetrics, historicalData);
            
        } catch (Exception e) {
            logger.error("Failed to generate optimization recommendations", e);
            return List.of();
        }
    }
    
    private Map<String, Object> analyzePerformanceTrends(String facilityId, LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> trends = new HashMap<>();
        
        try {
            // Calculate trend indicators
            Map<String, Object> trendIndicators = calculateTrendIndicators(facilityId, startTime, endTime);
            trends.put("trend_indicators", trendIndicators);
            
            // Performance comparison
            Map<String, Object> performanceComparison = comparePerformancePeriods(facilityId, startTime, endTime);
            trends.put("performance_comparison", performanceComparison);
            
            // Improvement opportunities
            List<String> improvementOpportunities = identifyImprovementOpportunities(facilityId, startTime, endTime);
            trends.put("improvement_opportunities", improvementOpportunities);
            
        } catch (Exception e) {
            logger.error("Failed to analyze performance trends", e);
            trends.put("error", e.getMessage());
        }
        
        return trends;
    }
    
    // Additional helper methods (implemented as placeholders)
    
    private Map<String, Object> analyzeConsumptionPatterns(String facilityId, LocalDateTime startTime, LocalDateTime endTime) {
        // Placeholder implementation
        return Map.of("pattern_type", "daily", "confidence", 0.85);
    }
    
    private Map<String, Object> calculateEfficiencyMetrics(String facilityId, LocalDateTime startTime, LocalDateTime endTime) {
        // Placeholder implementation
        return Map.of("overall_efficiency", 0.78, "power_factor", 0.92);
    }
    
    private Map<String, Object> analyzeEnergyCosts(String facilityId, LocalDateTime startTime, LocalDateTime endTime) {
        // Placeholder implementation
        return Map.of("total_cost", 1250.50, "cost_per_kwh", 0.15);
    }
    
    private List<AnomalyDetection> getAnomaliesInTimeRange(String facilityId, LocalDateTime startTime, LocalDateTime endTime) {
        // Placeholder implementation - return recent anomalies
        return new ArrayList<>(recentAnomalies);
    }
    
    private double calculateAnomalyRate(String facilityId, LocalDateTime startTime, LocalDateTime endTime) {
        // Placeholder implementation
        return 0.05; // 5% anomaly rate
    }
    
    private EnergyPrediction generateShortTermForecast(String facilityId) {
        // Placeholder implementation
        return edgeMLService.forecastEnergyConsumption("device123", "facility456", 24, Map.of("temperature", 25.0));
    }
    
    private Map<String, Object> identifyEnergyTrends(String facilityId, LocalDateTime startTime, LocalDateTime endTime) {
        // Placeholder implementation
        return Map.of("trend_direction", "decreasing", "trend_strength", 0.7);
    }
    
    private Map<String, Object> analyzeSeasonalPatterns(String facilityId, LocalDateTime startTime, LocalDateTime endTime) {
        // Placeholder implementation
        return Map.of("seasonal_variation", 0.15, "peak_season", "summer");
    }
    
    private Map<String, Double> getCurrentFacilityMetrics(String facilityId) {
        // Placeholder implementation
        return Map.of("efficiency", 78.0, "powerFactor", 0.92, "demand", 450.0);
    }
    
    private Map<String, Double> getHistoricalData(String facilityId) {
        // Placeholder implementation
        return Map.of("avgConsumption", 45.0, "peakDemand", 500.0, "costPerKwh", 0.15);
    }
    
    private Map<String, Object> calculateTrendIndicators(String facilityId, LocalDateTime startTime, LocalDateTime endTime) {
        // Placeholder implementation
        return Map.of("trend_direction", "stable", "change_rate", 0.02);
    }
    
    private Map<String, Object> comparePerformancePeriods(String facilityId, LocalDateTime startTime, LocalDateTime endTime) {
        // Placeholder implementation
        return Map.of("current_period", "Q4_2024", "previous_period", "Q3_2024", "improvement", 0.08);
    }
    
    private List<String> identifyImprovementOpportunities(String facilityId, LocalDateTime startTime, LocalDateTime endTime) {
        // Placeholder implementation
        return List.of("Optimize HVAC scheduling", "Implement demand response", "Upgrade lighting systems");
    }
    
    private double getCurrentEnergyConsumption(String facilityId) {
        // Placeholder implementation
        return realTimeMetrics.getOrDefault("current_consumption", 45.0);
    }
    
    private List<AnomalyDetection> getRecentAnomalies(String facilityId, int limit) {
        synchronized (recentAnomalies) {
            return recentAnomalies.stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        }
    }
    
    private List<EnergyPrediction> getRecentPredictions(String facilityId, int limit) {
        synchronized (recentPredictions) {
            return recentPredictions.stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        }
    }
    
    private Map<String, Object> calculatePerformanceIndicators(String facilityId) {
        // Placeholder implementation
        return Map.of("efficiency_score", 0.78, "reliability_score", 0.92, "cost_score", 0.85);
    }
    
    private List<Map<String, Object>> generateAlerts(String facilityId) {
        // Placeholder implementation
        List<Map<String, Object>> alerts = new ArrayList<>();
        
        // Check for high consumption
        if (getCurrentEnergyConsumption(facilityId) > 80.0) {
            alerts.add(Map.of(
                "type", "HIGH_CONSUMPTION",
                "severity", "MEDIUM",
                "message", "Energy consumption is above normal levels",
                "timestamp", LocalDateTime.now()
            ));
        }
        
        return alerts;
    }
    
    private Map<String, Object> calculateComparativeMetrics(List<String> facilityIds, LocalDateTime startTime, LocalDateTime endTime) {
        // Placeholder implementation
        return Map.of("total_facilities", facilityIds.size(), "comparison_basis", "energy_efficiency");
    }
    
    private Map<String, Object> generateFacilityRankings(List<String> facilityIds, LocalDateTime startTime, LocalDateTime endTime) {
        // Placeholder implementation
        return Map.of("ranking_criteria", "overall_efficiency", "top_performer", facilityIds.get(0));
    }
    
    private List<Map<String, Object>> identifyBestPractices(List<String> facilityIds, LocalDateTime startTime, LocalDateTime endTime) {
        // Placeholder implementation
        return List.of(Map.of("practice", "Smart scheduling", "impact", "15% reduction"));
    }
    
    private List<EnergyPrediction> generateEnergyForecast(String facilityId, int forecastHours) {
        // Placeholder implementation
        List<EnergyPrediction> forecasts = new ArrayList<>();
        for (int i = 1; i <= forecastHours; i++) {
            EnergyPrediction forecast = edgeMLService.forecastEnergyConsumption("device123", "facility456", i, Map.of("hour", (double) i));
            forecasts.add(forecast);
        }
        return forecasts;
    }
    
    private Map<String, Object> predictDemandPatterns(String facilityId, int forecastHours) {
        // Placeholder implementation
        return Map.of("peak_hours", List.of(9, 14, 18), "demand_variation", 0.25);
    }
    
    private Map<String, Object> projectEnergyCosts(String facilityId, int forecastHours) {
        // Placeholder implementation
        return Map.of("projected_cost", 1250.0, "cost_trend", "stable", "savings_potential", 150.0);
    }
    
    private Map<String, Object> assessOperationalRisks(String facilityId, int forecastHours) {
        // Placeholder implementation
        return Map.of("risk_level", "LOW", "main_risk", "equipment_aging", "mitigation", "preventive_maintenance");
    }
    
    private List<Map<String, Object>> identifyOptimizationOpportunities(String facilityId, int forecastHours) {
        // Placeholder implementation
        return List.of(Map.of("opportunity", "Load shifting", "potential_savings", 200.0));
    }
    
    private double calculatePredictionConfidence(String facilityId) {
        // Placeholder implementation
        return 0.85;
    }
    
    // Cache management methods
    
    private boolean isCacheValid(String cacheKey) {
        if (!analyticsCache.containsKey(cacheKey)) {
            return false;
        }
        
        // Check TTL - cache expires after CACHE_TTL_MINUTES
        // For now, always return true as we don't store timestamps
        // In a real implementation, you would store cache timestamps and check them
        // using CACHE_TTL_MINUTES to determine if cache entry is still valid
        return true; // Placeholder - implement proper TTL checking using CACHE_TTL_MINUTES
    }
    
    /**
     * Get cache TTL in minutes for external reference
     */
    public int getCacheTtlMinutes() {
        return CACHE_TTL_MINUTES;
    }
    
    private void cacheAnalytics(String cacheKey, Map<String, Object> analytics) {
        analyticsCache.put(cacheKey, analytics);
        logger.debug("Cached analytics for key: {}", cacheKey);
    }
    
    private Map<String, Object> createErrorAnalytics(String facilityId, String errorMessage) {
        Map<String, Object> errorAnalytics = new HashMap<>();
        errorAnalytics.put("facility_id", facilityId);
        errorAnalytics.put("error", errorMessage);
        errorAnalytics.put("generated_at", LocalDateTime.now());
        errorAnalytics.put("status", "error");
        return errorAnalytics;
    }
    
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void cleanupAnalyticsCache() {
        try {
            // Clean up old cache entries
            int initialSize = analyticsCache.size();
            analyticsCache.clear();
            logger.info("Cleaned up analytics cache: {} entries removed", initialSize);
        } catch (Exception e) {
            logger.error("Failed to cleanup analytics cache", e);
        }
    }
}
