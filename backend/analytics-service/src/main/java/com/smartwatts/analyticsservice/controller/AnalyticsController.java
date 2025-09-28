package com.smartwatts.analyticsservice.controller;

import com.smartwatts.analyticsservice.dto.EnergyAnalyticsDto;
import com.smartwatts.analyticsservice.dto.UsagePatternDto;
import com.smartwatts.analyticsservice.dto.ReportDto;
import com.smartwatts.analyticsservice.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Energy Analytics", description = "APIs for energy data analytics and reporting")
public class AnalyticsController {
    
    private final AnalyticsService analyticsService;
    
    @GetMapping
    @Operation(summary = "Get all analytics", description = "Retrieves all analytics (for testing)")
    public ResponseEntity<Page<EnergyAnalyticsDto>> getAllAnalytics(Pageable pageable) {
        log.info("Fetching all analytics");
        Page<EnergyAnalyticsDto> analytics = analyticsService.getAllAnalytics(pageable);
        return ResponseEntity.ok(analytics);
    }
    
    // Energy Analytics Endpoints
    @PostMapping("/energy-analytics")
    @Operation(summary = "Create energy analytics", description = "Creates energy analytics data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnergyAnalyticsDto> createEnergyAnalytics(@Valid @RequestBody EnergyAnalyticsDto analyticsDto) {
        log.info("Creating energy analytics for user: {}", analyticsDto.getUserId());
        EnergyAnalyticsDto createdAnalytics = analyticsService.createEnergyAnalytics(analyticsDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAnalytics);
    }
    
    @GetMapping("/energy-analytics/{analyticsId}")
    @Operation(summary = "Get energy analytics by ID", description = "Retrieves specific energy analytics by ID")
    @PreAuthorize("hasRole('ADMIN') or #analyticsId == authentication.principal.username")
    public ResponseEntity<EnergyAnalyticsDto> getEnergyAnalyticsById(
            @Parameter(description = "Analytics ID") @PathVariable UUID analyticsId) {
        log.info("Fetching energy analytics with ID: {}", analyticsId);
        EnergyAnalyticsDto analytics = analyticsService.getEnergyAnalyticsById(analyticsId);
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/energy-analytics/user/{userId}")
    @Operation(summary = "Get energy analytics by user", description = "Retrieves all energy analytics for a specific user")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public ResponseEntity<Page<EnergyAnalyticsDto>> getEnergyAnalyticsByUserId(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            Pageable pageable) {
        log.info("Fetching energy analytics for user: {}", userId);
        Page<EnergyAnalyticsDto> analytics = analyticsService.getEnergyAnalyticsByUserId(userId, pageable);
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/energy-analytics/user/{userId}/date-range")
    @Operation(summary = "Get energy analytics by date range", description = "Retrieves energy analytics for a specific date range")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public ResponseEntity<List<EnergyAnalyticsDto>> getEnergyAnalyticsByDateRange(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Start date") @RequestParam LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam LocalDateTime endDate) {
        log.info("Fetching energy analytics for user: {} between {} and {}", userId, startDate, endDate);
        List<EnergyAnalyticsDto> analytics = analyticsService.getEnergyAnalyticsByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/energy-analytics/user/{userId}/efficient")
    @Operation(summary = "Get efficient analytics", description = "Retrieves energy analytics with high efficiency scores")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public ResponseEntity<List<EnergyAnalyticsDto>> getEfficientAnalytics(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Minimum efficiency score") @RequestParam(defaultValue = "70.0") BigDecimal minScore) {
        log.info("Fetching efficient analytics for user: {} with min score: {}", userId, minScore);
        List<EnergyAnalyticsDto> analytics = analyticsService.getEfficientAnalytics(userId, minScore);
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/energy-analytics/user/{userId}/anomalies")
    @Operation(summary = "Get analytics with anomalies", description = "Retrieves energy analytics that contain anomalies")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public ResponseEntity<List<EnergyAnalyticsDto>> getAnalyticsWithAnomalies(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        log.info("Fetching analytics with anomalies for user: {}", userId);
        List<EnergyAnalyticsDto> analytics = analyticsService.getAnalyticsWithAnomalies(userId);
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/energy-analytics/user/{userId}/average-consumption")
    @Operation(summary = "Get average consumption", description = "Calculates average consumption for a date range")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public ResponseEntity<BigDecimal> getAverageConsumption(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Start date") @RequestParam LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam LocalDateTime endDate) {
        log.info("Calculating average consumption for user: {} between {} and {}", userId, startDate, endDate);
        BigDecimal averageConsumption = analyticsService.getAverageConsumption(userId, startDate, endDate);
        return ResponseEntity.ok(averageConsumption);
    }
    
    @GetMapping("/energy-analytics/user/{userId}/total-cost")
    @Operation(summary = "Get total cost", description = "Calculates total cost for a date range")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public ResponseEntity<BigDecimal> getTotalCost(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Start date") @RequestParam LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam LocalDateTime endDate) {
        log.info("Calculating total cost for user: {} between {} and {}", userId, startDate, endDate);
        BigDecimal totalCost = analyticsService.getTotalCost(userId, startDate, endDate);
        return ResponseEntity.ok(totalCost);
    }
    
    @GetMapping("/energy-analytics/user/{userId}/average-efficiency")
    @Operation(summary = "Get average efficiency", description = "Calculates average efficiency for a date range")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public ResponseEntity<BigDecimal> getAverageEfficiency(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Start date") @RequestParam LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam LocalDateTime endDate) {
        log.info("Calculating average efficiency for user: {} between {} and {}", userId, startDate, endDate);
        BigDecimal averageEfficiency = analyticsService.getAverageEfficiency(userId, startDate, endDate);
        return ResponseEntity.ok(averageEfficiency);
    }
    
    // Usage Pattern Endpoints
    @PostMapping("/usage-patterns")
    @Operation(summary = "Create usage pattern", description = "Creates a usage pattern analysis")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsagePatternDto> createUsagePattern(@Valid @RequestBody UsagePatternDto patternDto) {
        log.info("Creating usage pattern for user: {}", patternDto.getUserId());
        UsagePatternDto createdPattern = analyticsService.createUsagePattern(patternDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPattern);
    }
    
    @GetMapping("/usage-patterns/{patternId}")
    @Operation(summary = "Get usage pattern by ID", description = "Retrieves a specific usage pattern by ID")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsagePatternDto> getUsagePatternById(
            @Parameter(description = "Pattern ID") @PathVariable UUID patternId) {
        log.info("Fetching usage pattern with ID: {}", patternId);
        UsagePatternDto pattern = analyticsService.getUsagePatternById(patternId);
        return ResponseEntity.ok(pattern);
    }
    
    @GetMapping("/usage-patterns/user/{userId}")
    @Operation(summary = "Get usage patterns by user", description = "Retrieves all usage patterns for a specific user")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public ResponseEntity<Page<UsagePatternDto>> getUsagePatternsByUserId(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            Pageable pageable) {
        log.info("Fetching usage patterns for user: {}", userId);
        Page<UsagePatternDto> patterns = analyticsService.getUsagePatternsByUserId(userId, pageable);
        return ResponseEntity.ok(patterns);
    }
    
    @GetMapping("/usage-patterns/user/{userId}/anomalous")
    @Operation(summary = "Get anomalous patterns", description = "Retrieves usage patterns that are anomalous")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public ResponseEntity<List<UsagePatternDto>> getAnomalousPatterns(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        log.info("Fetching anomalous patterns for user: {}", userId);
        List<UsagePatternDto> patterns = analyticsService.getAnomalousPatterns(userId);
        return ResponseEntity.ok(patterns);
    }
    
    @GetMapping("/usage-patterns/user/{userId}/efficient")
    @Operation(summary = "Get efficient patterns", description = "Retrieves usage patterns with high efficiency ratings")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public ResponseEntity<List<UsagePatternDto>> getEfficientPatterns(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Minimum efficiency rating") @RequestParam(defaultValue = "70.0") BigDecimal minRating) {
        log.info("Fetching efficient patterns for user: {} with min rating: {}", userId, minRating);
        List<UsagePatternDto> patterns = analyticsService.getEfficientPatterns(userId, minRating);
        return ResponseEntity.ok(patterns);
    }
    
    @GetMapping("/usage-patterns/user/{userId}/category/{category}")
    @Operation(summary = "Get patterns by category", description = "Retrieves usage patterns by category")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public ResponseEntity<List<UsagePatternDto>> getPatternsByCategory(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Category") @PathVariable String category) {
        log.info("Fetching patterns by category: {} for user: {}", category, userId);
        List<UsagePatternDto> patterns = analyticsService.getPatternsByCategory(userId, category);
        return ResponseEntity.ok(patterns);
    }
    
    // Report Endpoints
    @PostMapping("/reports")
    @Operation(summary = "Generate report", description = "Generates a new energy report")
    @PreAuthorize("hasRole('ADMIN') or #reportDto.userId == authentication.principal.username")
    public ResponseEntity<ReportDto> generateReport(@Valid @RequestBody ReportDto reportDto) {
        log.info("Generating report for user: {}, type: {}", reportDto.getUserId(), reportDto.getReportType());
        ReportDto generatedReport = analyticsService.generateReport(reportDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(generatedReport);
    }
    
    @GetMapping("/reports/{reportId}")
    @Operation(summary = "Get report by ID", description = "Retrieves a specific report by ID")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportDto> getReportById(
            @Parameter(description = "Report ID") @PathVariable UUID reportId) {
        log.info("Fetching report with ID: {}", reportId);
        ReportDto report = analyticsService.getReportById(reportId);
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/reports/user/{userId}")
    @Operation(summary = "Get reports by user", description = "Retrieves all reports for a specific user")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public ResponseEntity<Page<ReportDto>> getReportsByUserId(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            Pageable pageable) {
        log.info("Fetching reports for user: {}", userId);
        Page<ReportDto> reports = analyticsService.getReportsByUserId(userId, pageable);
        return ResponseEntity.ok(reports);
    }
    
    @GetMapping("/reports/user/{userId}/active")
    @Operation(summary = "Get active reports by user", description = "Retrieves all active reports for a specific user")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public ResponseEntity<Page<ReportDto>> getActiveReportsByUserId(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            Pageable pageable) {
        log.info("Fetching active reports for user: {}", userId);
        Page<ReportDto> reports = analyticsService.getActiveReportsByUserId(userId, pageable);
        return ResponseEntity.ok(reports);
    }
    
    // Dashboard-specific endpoints (temporarily without authentication for testing)
    @GetMapping("/dashboard-stats")
    @Operation(summary = "Get dashboard statistics", description = "Retrieves dashboard statistics for the current user")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        log.info("Fetching dashboard statistics");
        Map<String, Object> stats = analyticsService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/cost-optimizations")
    @Operation(summary = "Get cost optimizations", description = "Retrieves cost optimization recommendations")
    public ResponseEntity<List<Map<String, Object>>> getCostOptimizations() {
        log.info("Fetching cost optimizations");
        List<Map<String, Object>> optimizations = analyticsService.getCostOptimizations();
        return ResponseEntity.ok(optimizations);
    }
    
    @GetMapping("/efficiency-metrics")
    @Operation(summary = "Get efficiency metrics", description = "Retrieves energy efficiency metrics")
    public ResponseEntity<List<Map<String, Object>>> getEfficiencyMetrics() {
        log.info("Fetching efficiency metrics");
        List<Map<String, Object>> metrics = analyticsService.getEfficiencyMetrics();
        return ResponseEntity.ok(metrics);
    }
    
    @GetMapping("/alerts")
    @Operation(summary = "Get energy alerts", description = "Retrieves energy-related alerts")
    public ResponseEntity<List<Map<String, Object>>> getEnergyAlerts() {
        log.info("Fetching energy alerts");
        List<Map<String, Object>> alerts = analyticsService.getEnergyAlerts();
        return ResponseEntity.ok(alerts);
    }
    
    @GetMapping("/forecasts")
    @Operation(summary = "Get energy forecasts", description = "Retrieves energy consumption forecasts")
    public ResponseEntity<List<Map<String, Object>>> getEnergyForecasts() {
        log.info("Fetching energy forecasts");
        List<Map<String, Object>> forecasts = analyticsService.getEnergyForecasts();
        return ResponseEntity.ok(forecasts);
    }
    
    @GetMapping("/recommendations")
    @Operation(summary = "Get smart recommendations", description = "Retrieves smart energy recommendations")
    public ResponseEntity<List<Map<String, Object>>> getSmartRecommendations() {
        log.info("Fetching smart recommendations");
        List<Map<String, Object>> recommendations = analyticsService.getSmartRecommendations();
        return ResponseEntity.ok(recommendations);
    }
    
    @GetMapping("/load-profile")
    @Operation(summary = "Get load profile", description = "Retrieves energy load profile data")
    public ResponseEntity<Map<String, Object>> getLoadProfile() {
        log.info("Fetching load profile");
        Map<String, Object> loadProfile = analyticsService.getLoadProfile();
        return ResponseEntity.ok(loadProfile);
    }
    
    @GetMapping("/carbon-footprint")
    @Operation(summary = "Get carbon footprint", description = "Retrieves carbon footprint data")
    public ResponseEntity<Map<String, Object>> getCarbonFootprint() {
        log.info("Fetching carbon footprint");
        Map<String, Object> carbonFootprint = analyticsService.getCarbonFootprint();
        return ResponseEntity.ok(carbonFootprint);
    }
    
    @GetMapping("/device-consumption")
    @Operation(summary = "Get device consumption", description = "Retrieves device consumption data")
    public ResponseEntity<List<Map<String, Object>>> getDeviceConsumption() {
        log.info("Fetching device consumption");
        List<Map<String, Object>> deviceConsumption = analyticsService.getDeviceConsumption();
        return ResponseEntity.ok(deviceConsumption);
    }
    
    @GetMapping("/time-of-use")
    @Operation(summary = "Get time of use analysis", description = "Retrieves time of use analysis data")
    public ResponseEntity<Map<String, Object>> getTimeOfUseAnalysis() {
        log.info("Fetching time of use analysis");
        Map<String, Object> timeOfUse = analyticsService.getTimeOfUseAnalysis();
        return ResponseEntity.ok(timeOfUse);
    }
    
    @GetMapping("/weather-impact")
    @Operation(summary = "Get weather impact", description = "Retrieves weather impact on energy consumption")
    public ResponseEntity<Map<String, Object>> getWeatherImpact() {
        log.info("Fetching weather impact");
        Map<String, Object> weatherImpact = analyticsService.getWeatherImpact();
        return ResponseEntity.ok(weatherImpact);
    }
    
    @GetMapping("/efficiency-benchmarks")
    @Operation(summary = "Get efficiency benchmarks", description = "Retrieves efficiency benchmark data")
    public ResponseEntity<List<Map<String, Object>>> getEfficiencyBenchmarks() {
        log.info("Fetching efficiency benchmarks");
        List<Map<String, Object>> benchmarks = analyticsService.getEfficiencyBenchmarks();
        return ResponseEntity.ok(benchmarks);
    }
    
    @GetMapping("/reports/user/{userId}/scheduled")
    @Operation(summary = "Get scheduled reports", description = "Retrieves all scheduled reports for a user")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public ResponseEntity<List<ReportDto>> getScheduledReports(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        log.info("Fetching scheduled reports for user: {}", userId);
        List<ReportDto> reports = analyticsService.getScheduledReports(userId);
        return ResponseEntity.ok(reports);
    }
    
    @PutMapping("/reports/{reportId}/archive")
    @Operation(summary = "Archive report", description = "Archives a specific report")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> archiveReport(
            @Parameter(description = "Report ID") @PathVariable UUID reportId,
            @Parameter(description = "User ID who archived") @RequestParam UUID archivedBy) {
        log.info("Archiving report with ID: {}", reportId);
        analyticsService.archiveReport(reportId, archivedBy);
        return ResponseEntity.noContent().build();
    }
} 