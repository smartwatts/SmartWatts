package com.smartwatts.analyticsservice.service;

import com.smartwatts.analyticsservice.dto.EnergyAnalyticsDto;
import com.smartwatts.analyticsservice.dto.UsagePatternDto;
import com.smartwatts.analyticsservice.dto.ReportDto;
import com.smartwatts.analyticsservice.model.EnergyAnalytics;
import com.smartwatts.analyticsservice.model.UsagePattern;
import com.smartwatts.analyticsservice.model.Report;
import com.smartwatts.analyticsservice.repository.EnergyAnalyticsRepository;
import com.smartwatts.analyticsservice.repository.UsagePatternRepository;
import com.smartwatts.analyticsservice.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {
    
    private final EnergyAnalyticsRepository energyAnalyticsRepository;
    private final UsagePatternRepository usagePatternRepository;
    private final ReportRepository reportRepository;
    private final PatternAnalysisService patternAnalysisService;
    private final ReportGenerationService reportGenerationService;
    
    // Energy Analytics Methods
    @Transactional
    public EnergyAnalyticsDto createEnergyAnalytics(EnergyAnalyticsDto analyticsDto) {
        log.info("Creating energy analytics for user: {}, period: {}", analyticsDto.getUserId(), analyticsDto.getPeriodType());
        
        EnergyAnalytics analytics = new EnergyAnalytics();
        BeanUtils.copyProperties(analyticsDto, analytics);
        
        // Calculate derived fields if not provided
        if (analytics.getEfficiencyScore() == null) {
            analytics.setEfficiencyScore(calculateEfficiencyScore(analytics));
        }
        
        if (analytics.getQualityScore() == null) {
            analytics.setQualityScore(calculateQualityScore(analytics));
        }
        
        if (analytics.getCarbonFootprintKg() == null) {
            analytics.setCarbonFootprintKg(calculateCarbonFootprint(analytics));
        }
        
        EnergyAnalytics savedAnalytics = energyAnalyticsRepository.save(analytics);
        log.info("Energy analytics created with ID: {}", savedAnalytics.getId());
        
        return convertToDto(savedAnalytics);
    }
    
    @Transactional(readOnly = true)
    public Page<EnergyAnalyticsDto> getAllAnalytics(Pageable pageable) {
        log.info("Fetching all analytics");
        Page<EnergyAnalytics> analytics = energyAnalyticsRepository.findAll(pageable);
        return analytics.map(this::convertToDto);
    }
    
    @Transactional(readOnly = true)
    public EnergyAnalyticsDto getEnergyAnalyticsById(UUID analyticsId) {
        log.info("Fetching energy analytics with ID: {}", analyticsId);
        EnergyAnalytics analytics = energyAnalyticsRepository.findById(analyticsId)
                .orElseThrow(() -> new RuntimeException("Energy analytics not found with ID: " + analyticsId));
        return convertToDto(analytics);
    }
    
    @Transactional(readOnly = true)
    public Page<EnergyAnalyticsDto> getEnergyAnalyticsByUserId(UUID userId, Pageable pageable) {
        log.info("Fetching energy analytics for user: {}", userId);
        Page<EnergyAnalytics> analytics = energyAnalyticsRepository.findByUserId(userId, pageable);
        return analytics.map(this::convertToDto);
    }
    
    @Transactional(readOnly = true)
    public List<EnergyAnalyticsDto> getEnergyAnalyticsByDateRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching energy analytics for user: {} between {} and {}", userId, startDate, endDate);
        List<EnergyAnalytics> analytics = energyAnalyticsRepository.findByUserIdAndAnalyticsDateBetween(userId, startDate, endDate);
        return analytics.stream().map(this::convertToDto).toList();
    }
    
    @Transactional(readOnly = true)
    public List<EnergyAnalyticsDto> getEfficientAnalytics(UUID userId, BigDecimal minScore) {
        log.info("Fetching efficient analytics for user: {} with min score: {}", userId, minScore);
        List<EnergyAnalytics> analytics = energyAnalyticsRepository.findEfficientAnalyticsByUserId(userId, minScore);
        return analytics.stream().map(this::convertToDto).toList();
    }
    
    @Transactional(readOnly = true)
    public List<EnergyAnalyticsDto> getAnalyticsWithAnomalies(UUID userId) {
        log.info("Fetching analytics with anomalies for user: {}", userId);
        List<EnergyAnalytics> analytics = energyAnalyticsRepository.findAnalyticsWithAnomaliesByUserId(userId);
        return analytics.stream().map(this::convertToDto).toList();
    }
    
    @Transactional(readOnly = true)
    public BigDecimal getAverageConsumption(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Calculating average consumption for user: {} between {} and {}", userId, startDate, endDate);
        return energyAnalyticsRepository.findAverageConsumptionByUserIdAndDateRange(userId, startDate, endDate);
    }
    
    @Transactional(readOnly = true)
    public BigDecimal getTotalCost(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Calculating total cost for user: {} between {} and {}", userId, startDate, endDate);
        return energyAnalyticsRepository.findTotalCostByUserIdAndDateRange(userId, startDate, endDate);
    }
    
    @Transactional(readOnly = true)
    public BigDecimal getAverageEfficiency(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Calculating average efficiency for user: {} between {} and {}", userId, startDate, endDate);
        return energyAnalyticsRepository.findAverageEfficiencyByUserIdAndDateRange(userId, startDate, endDate);
    }
    
    // Usage Pattern Methods
    @Transactional
    public UsagePatternDto createUsagePattern(UsagePatternDto patternDto) {
        log.info("Creating usage pattern for user: {}, type: {}", patternDto.getUserId(), patternDto.getPatternType());
        
        UsagePattern pattern = new UsagePattern();
        BeanUtils.copyProperties(patternDto, pattern);
        
        // Use PatternAnalysisService for pattern analysis
        if (pattern.getPatternType() != null) {
            switch (pattern.getPatternType()) {
                case DAILY_RHYTHM:
                    pattern = patternAnalysisService.analyzeDailyRhythm(
                        pattern.getUserId(), pattern.getDeviceId(), pattern.getPatternDate(),
                        pattern.getTotalConsumptionKwh(), pattern.getAveragePowerKw(), pattern.getPeakPowerKw());
                    break;
                case WEEKLY_PATTERN:
                    pattern = patternAnalysisService.analyzeWeeklyPattern(
                        pattern.getUserId(), pattern.getDeviceId(), pattern.getPatternDate(),
                        pattern.getTotalConsumptionKwh(), pattern.getAveragePowerKw(), pattern.getPeakPowerKw());
                    break;
                case PEAK_USAGE:
                    pattern = patternAnalysisService.analyzePeakUsage(
                        pattern.getUserId(), pattern.getDeviceId(), pattern.getStartTime(), pattern.getEndTime(),
                        pattern.getTotalConsumptionKwh(), pattern.getAveragePowerKw(), pattern.getPeakPowerKw());
                    break;
                case ANOMALOUS_USAGE:
                    pattern = patternAnalysisService.analyzeAnomalousUsage(
                        pattern.getUserId(), pattern.getDeviceId(), pattern.getStartTime(), pattern.getEndTime(),
                        pattern.getTotalConsumptionKwh(), pattern.getAveragePowerKw(), pattern.getPeakPowerKw(),
                        pattern.getAnomalyScore());
                    break;
                default:
                    // For other pattern types, use basic analysis
                    log.debug("Using basic pattern analysis for type: {}", pattern.getPatternType());
                    break;
            }
        }
        
        // Calculate derived fields if not provided
        if (pattern.getConfidenceScore() == null) {
            pattern.setConfidenceScore(calculateConfidenceScore(pattern));
        }
        
        if (pattern.getEfficiencyRating() == null) {
            pattern.setEfficiencyRating(calculateEfficiencyRating(pattern));
        }
        
        if (pattern.getOptimizationPotential() == null) {
            pattern.setOptimizationPotential(calculateOptimizationPotential(pattern));
        }
        
        UsagePattern savedPattern = usagePatternRepository.save(pattern);
        log.info("Usage pattern created with ID: {}", savedPattern.getId());
        
        return convertToDto(savedPattern);
    }
    
    @Transactional(readOnly = true)
    public UsagePatternDto getUsagePatternById(UUID patternId) {
        log.info("Fetching usage pattern with ID: {}", patternId);
        UsagePattern pattern = usagePatternRepository.findById(patternId)
                .orElseThrow(() -> new RuntimeException("Usage pattern not found with ID: " + patternId));
        return convertToDto(pattern);
    }
    
    @Transactional(readOnly = true)
    public Page<UsagePatternDto> getUsagePatternsByUserId(UUID userId, Pageable pageable) {
        log.info("Fetching usage patterns for user: {}", userId);
        Page<UsagePattern> patterns = usagePatternRepository.findByUserId(userId, pageable);
        return patterns.map(this::convertToDto);
    }
    
    @Transactional(readOnly = true)
    public List<UsagePatternDto> getAnomalousPatterns(UUID userId) {
        log.info("Fetching anomalous patterns for user: {}", userId);
        List<UsagePattern> patterns = usagePatternRepository.findAnomalousPatternsByUserId(userId);
        return patterns.stream().map(this::convertToDto).toList();
    }
    
    @Transactional(readOnly = true)
    public List<UsagePatternDto> getEfficientPatterns(UUID userId, BigDecimal minRating) {
        log.info("Fetching efficient patterns for user: {} with min rating: {}", userId, minRating);
        List<UsagePattern> patterns = usagePatternRepository.findEfficientPatternsByUserId(userId, minRating);
        return patterns.stream().map(this::convertToDto).toList();
    }
    
    @Transactional(readOnly = true)
    public List<UsagePatternDto> getPatternsByCategory(UUID userId, String category) {
        log.info("Fetching patterns by category: {} for user: {}", category, userId);
        List<UsagePattern> patterns = usagePatternRepository.findPatternsByCategory(userId, category);
        return patterns.stream().map(this::convertToDto).toList();
    }
    
    @Transactional
    public List<UsagePatternDto> detectPatternsFromEnergyData(UUID userId, UUID deviceId, List<Object> energyData) {
        log.info("Detecting patterns from energy data for user: {}, device: {}", userId, deviceId);
        
        // Use PatternAnalysisService to detect patterns
        List<UsagePattern> detectedPatterns = patternAnalysisService.detectPatternsFromData(userId, deviceId, energyData);
        
        // Save detected patterns
        List<UsagePattern> savedPatterns = usagePatternRepository.saveAll(detectedPatterns);
        log.info("Detected and saved {} patterns for user: {}", savedPatterns.size(), userId);
        
        return savedPatterns.stream().map(this::convertToDto).toList();
    }
    
    // Report Methods
    @Transactional
    public ReportDto generateReport(ReportDto reportDto) {
        log.info("Generating report for user: {}, type: {}", reportDto.getUserId(), reportDto.getReportType());
        
        Report report = new Report();
        BeanUtils.copyProperties(reportDto, report);
        
        report.setGeneratedAt(LocalDateTime.now());
        report.setGeneratedBy(reportDto.getGeneratedBy());
        
        // Generate report content
        String reportContent = reportGenerationService.generateReportContent(report);
        report.setSummary(reportContent);
        
        Report savedReport = reportRepository.save(report);
        log.info("Report generated with ID: {}", savedReport.getId());
        
        return convertToDto(savedReport);
    }
    
    @Transactional(readOnly = true)
    public ReportDto getReportById(UUID reportId) {
        log.info("Fetching report with ID: {}", reportId);
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found with ID: " + reportId));
        return convertToDto(report);
    }
    
    @Transactional(readOnly = true)
    public Page<ReportDto> getReportsByUserId(UUID userId, Pageable pageable) {
        log.info("Fetching reports for user: {}", userId);
        Page<Report> reports = reportRepository.findByUserId(userId, pageable);
        return reports.map(this::convertToDto);
    }
    
    @Transactional(readOnly = true)
    public List<ReportDto> getActiveReports(UUID userId) {
        log.info("Fetching active reports for user: {}", userId);
        List<Report> reports = reportRepository.findActiveReportsByUserId(userId);
        return reports.stream().map(this::convertToDto).toList();
    }
    
    @Transactional(readOnly = true)
    public List<ReportDto> getScheduledReports(UUID userId) {
        log.info("Fetching scheduled reports for user: {}", userId);
        List<Report> reports = reportRepository.findScheduledReportsByUserId(userId);
        return reports.stream().map(this::convertToDto).toList();
    }
    
    @Transactional
    public void archiveReport(UUID reportId, UUID archivedBy) {
        log.info("Archiving report with ID: {}", reportId);
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found with ID: " + reportId));
        
        report.setIsArchived(true);
        report.setArchivedAt(LocalDateTime.now());
        report.setArchivedBy(archivedBy);
        
        reportRepository.save(report);
        log.info("Report archived successfully");
    }
    
    // Analytics Calculation Methods
    private BigDecimal calculateEfficiencyScore(EnergyAnalytics analytics) {
        // Simple efficiency calculation based on consumption patterns
        if (analytics.getTotalConsumptionKwh() == null || analytics.getTotalConsumptionKwh().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(50.0); // Default score
        }
        
        // Calculate efficiency based on consumption vs expected
        BigDecimal efficiency = BigDecimal.valueOf(100.0);
        
        // Reduce score for high peak consumption
        if (analytics.getPeakConsumptionKw() != null && analytics.getPeakConsumptionKw().compareTo(BigDecimal.valueOf(10.0)) > 0) {
            efficiency = efficiency.subtract(BigDecimal.valueOf(20.0));
        }
        
        // Reduce score for high night consumption
        if (analytics.getNightConsumptionKwh() != null && analytics.getNightConsumptionKwh().compareTo(analytics.getTotalConsumptionKwh().multiply(BigDecimal.valueOf(0.3))) > 0) {
            efficiency = efficiency.subtract(BigDecimal.valueOf(15.0));
        }
        
        return efficiency.max(BigDecimal.ZERO).min(BigDecimal.valueOf(100.0));
    }
    
    private BigDecimal calculateQualityScore(EnergyAnalytics analytics) {
        // Calculate data quality score based on completeness and consistency
        BigDecimal quality = BigDecimal.valueOf(100.0);
        
        if (analytics.getCompletenessPercentage() != null) {
            quality = quality.multiply(analytics.getCompletenessPercentage()).divide(BigDecimal.valueOf(100.0), 2, RoundingMode.HALF_UP);
        }
        
        if (analytics.getDataPointsCount() != null && analytics.getDataPointsCount() < 100) {
            quality = quality.subtract(BigDecimal.valueOf(10.0));
        }
        
        return quality.max(BigDecimal.ZERO).min(BigDecimal.valueOf(100.0));
    }
    
    private BigDecimal calculateCarbonFootprint(EnergyAnalytics analytics) {
        // Calculate carbon footprint based on consumption (Nigerian grid average)
        if (analytics.getTotalConsumptionKwh() == null) {
            return BigDecimal.ZERO;
        }
        
        // Nigerian grid average: ~0.5 kg CO2 per kWh
        return analytics.getTotalConsumptionKwh().multiply(BigDecimal.valueOf(0.5));
    }
    
    private BigDecimal calculateConfidenceScore(UsagePattern pattern) {
        // Calculate confidence based on frequency and consistency
        BigDecimal confidence = BigDecimal.valueOf(50.0);
        
        if (pattern.getFrequencyPercentage() != null) {
            confidence = confidence.add(pattern.getFrequencyPercentage().multiply(BigDecimal.valueOf(0.5)));
        }
        
        if (pattern.getDurationHours() != null && pattern.getDurationHours().compareTo(BigDecimal.valueOf(1.0)) > 0) {
            confidence = confidence.add(BigDecimal.valueOf(10.0));
        }
        
        return confidence.max(BigDecimal.ZERO).min(BigDecimal.valueOf(100.0));
    }
    
    private BigDecimal calculateEfficiencyRating(UsagePattern pattern) {
        // Calculate efficiency rating based on consumption patterns
        BigDecimal efficiency = BigDecimal.valueOf(70.0);
        
        if (pattern.getAveragePowerKw() != null && pattern.getAveragePowerKw().compareTo(BigDecimal.valueOf(5.0)) < 0) {
            efficiency = efficiency.add(BigDecimal.valueOf(20.0));
        }
        
        if (pattern.getPeakPowerKw() != null && pattern.getPeakPowerKw().compareTo(BigDecimal.valueOf(10.0)) < 0) {
            efficiency = efficiency.add(BigDecimal.valueOf(10.0));
        }
        
        return efficiency.max(BigDecimal.ZERO).min(BigDecimal.valueOf(100.0));
    }
    
    private BigDecimal calculateOptimizationPotential(UsagePattern pattern) {
        // Calculate optimization potential based on efficiency gaps
        BigDecimal potential = BigDecimal.valueOf(30.0);
        
        if (pattern.getEfficiencyRating() != null && pattern.getEfficiencyRating().compareTo(BigDecimal.valueOf(80.0)) < 0) {
            potential = potential.add(BigDecimal.valueOf(40.0));
        }
        
        if (pattern.getPeakPowerKw() != null && pattern.getPeakPowerKw().compareTo(BigDecimal.valueOf(8.0)) > 0) {
            potential = potential.add(BigDecimal.valueOf(20.0));
        }
        
        return potential.max(BigDecimal.ZERO).min(BigDecimal.valueOf(100.0));
    }
    
    private EnergyAnalyticsDto convertToDto(EnergyAnalytics analytics) {
        EnergyAnalyticsDto dto = new EnergyAnalyticsDto();
        BeanUtils.copyProperties(analytics, dto);
        return dto;
    }
    
    private UsagePatternDto convertToDto(UsagePattern pattern) {
        UsagePatternDto dto = new UsagePatternDto();
        BeanUtils.copyProperties(pattern, dto);
        return dto;
    }
    
    private ReportDto convertToDto(Report report) {
        ReportDto dto = new ReportDto();
        BeanUtils.copyProperties(report, dto);
        return dto;
    }
    
    // Dashboard-specific methods
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEnergyConsumption", 0.0);
        stats.put("totalEnergyGeneration", 0.0);
        stats.put("totalCost", 0.0);
        stats.put("totalSavings", 0.0);
        stats.put("efficiency", 0.0);
        stats.put("carbonFootprint", 0.0);
        return stats;
    }
    
    public List<Map<String, Object>> getCostOptimizations() {
        return new ArrayList<>();
    }
    
    public List<Map<String, Object>> getEfficiencyMetrics() {
        return new ArrayList<>();
    }
    
    public List<Map<String, Object>> getEnergyAlerts() {
        return new ArrayList<>();
    }
    
    public List<Map<String, Object>> getEnergyForecasts() {
        return new ArrayList<>();
    }
    
    public List<Map<String, Object>> getSmartRecommendations() {
        return new ArrayList<>();
    }
    
    public Map<String, Object> getLoadProfile() {
        Map<String, Object> loadProfile = new HashMap<>();
        loadProfile.put("hourly", new ArrayList<>());
        loadProfile.put("daily", new ArrayList<>());
        loadProfile.put("weekly", new ArrayList<>());
        loadProfile.put("monthly", new ArrayList<>());
        return loadProfile;
    }
    
    public Map<String, Object> getCarbonFootprint() {
        Map<String, Object> carbonFootprint = new HashMap<>();
        carbonFootprint.put("total", 0.0);
        carbonFootprint.put("renewable", 0.0);
        carbonFootprint.put("nonRenewable", 0.0);
        carbonFootprint.put("savings", 0.0);
        return carbonFootprint;
    }
    
    public List<Map<String, Object>> getDeviceConsumption() {
        return new ArrayList<>();
    }
    
    public Map<String, Object> getTimeOfUseAnalysis() {
        Map<String, Object> timeOfUse = new HashMap<>();
        Map<String, Object> peak = new HashMap<>();
        peak.put("consumption", 0.0);
        peak.put("cost", 0.0);
        Map<String, Object> offPeak = new HashMap<>();
        offPeak.put("consumption", 0.0);
        offPeak.put("cost", 0.0);
        timeOfUse.put("peak", peak);
        timeOfUse.put("offPeak", offPeak);
        timeOfUse.put("savings", 0.0);
        return timeOfUse;
    }
    
    public Map<String, Object> getWeatherImpact() {
        Map<String, Object> weatherImpact = new HashMap<>();
        weatherImpact.put("temperature", 0.0);
        weatherImpact.put("humidity", 0.0);
        weatherImpact.put("impact", 0.0);
        return weatherImpact;
    }
    
    public List<Map<String, Object>> getEfficiencyBenchmarks() {
        return new ArrayList<>();
    }
    
    public Page<ReportDto> getActiveReportsByUserId(UUID userId, Pageable pageable) {
        List<Report> reports = reportRepository.findActiveReportsByUserId(userId);
        // Convert List to Page manually since the repository method returns List
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), reports.size());
        List<Report> pageContent = reports.subList(start, end);
        Page<Report> page = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, reports.size());
        return page.map(this::convertToDto);
    }
} 