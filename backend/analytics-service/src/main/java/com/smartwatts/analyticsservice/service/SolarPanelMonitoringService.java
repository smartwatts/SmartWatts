package com.smartwatts.analyticsservice.service;

import com.smartwatts.analyticsservice.model.SolarPanel;
import com.smartwatts.analyticsservice.model.SolarString;
import com.smartwatts.analyticsservice.model.SolarInverter;
import com.smartwatts.analyticsservice.repository.SolarPanelRepository;
import com.smartwatts.analyticsservice.repository.SolarStringRepository;
import com.smartwatts.analyticsservice.repository.SolarInverterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SolarPanelMonitoringService {

    private final SolarPanelRepository solarPanelRepository;
    private final SolarStringRepository solarStringRepository;
    private final SolarInverterRepository solarInverterRepository;
    private final RestTemplate restTemplate;

    /**
     * Fetch per-panel data from inverter API
     */
    @Transactional
    public void updateSolarPanelData(UUID inverterId) {
        log.info("Updating solar panel data for inverter: {}", inverterId);
        
        SolarInverter inverter = solarInverterRepository.findById(inverterId)
            .orElseThrow(() -> new RuntimeException("Solar inverter not found: " + inverterId));
        
        try {
            // Fetch data from inverter API based on type
            Map<String, Object> inverterData = fetchInverterData(inverter);
            
            // Update strings and panels
            updateSolarStrings(inverterId, inverterData);
            updateSolarPanels(inverterId, inverterData);
            
            // Check for faults
            checkForFaults(inverterId);
            
            log.info("Successfully updated solar panel data for inverter: {}", inverterId);
        } catch (Exception e) {
            log.error("Error updating solar panel data for inverter: {}", inverterId, e);
        }
    }

    /**
     * Get solar array heatmap data for dashboard
     */
    public Map<String, Object> getSolarArrayHeatmap(UUID inverterId) {
        log.info("Getting solar array heatmap for inverter: {}", inverterId);
        
        List<SolarString> strings = solarStringRepository.findByInverterIdAndIsActive(inverterId, true);
        List<SolarPanel> panels = solarPanelRepository.findByInverterIdAndIsActive(inverterId, true);
        
        Map<String, Object> heatmapData = new HashMap<>();
        
        // Build string-level data
        List<Map<String, Object>> stringData = strings.stream()
            .map(this::buildStringHeatmapData)
            .collect(Collectors.toList());
        
        // Build panel-level data
        List<Map<String, Object>> panelData = panels.stream()
            .map(this::buildPanelHeatmapData)
            .collect(Collectors.toList());
        
        heatmapData.put("strings", stringData);
        heatmapData.put("panels", panelData);
        heatmapData.put("totalGeneration", calculateTotalGeneration(panels));
        heatmapData.put("averageEfficiency", calculateAverageEfficiency(panels));
        heatmapData.put("faultCount", countFaults(panels));
        
        return heatmapData;
    }

    /**
     * Get real-time vs historical solar production comparison
     */
    public Map<String, Object> getSolarProductionComparison(UUID inverterId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Getting solar production comparison for inverter: {}", inverterId);
        
        List<SolarPanel> panels = solarPanelRepository.findByInverterIdAndIsActive(inverterId, true);
        
        Map<String, Object> comparisonData = new HashMap<>();
        
        // Current production
        BigDecimal currentProduction = panels.stream()
            .map(SolarPanel::getCurrentPower)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Historical average for same time period
        BigDecimal historicalAverage = calculateHistoricalAverage(inverterId, startTime, endTime);
        
        // Performance metrics
        BigDecimal performanceRatio = historicalAverage.compareTo(BigDecimal.ZERO) > 0 
            ? currentProduction.divide(historicalAverage, 4, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
        comparisonData.put("currentProduction", currentProduction);
        comparisonData.put("historicalAverage", historicalAverage);
        comparisonData.put("performanceRatio", performanceRatio);
        comparisonData.put("performanceStatus", getPerformanceStatus(performanceRatio));
        comparisonData.put("timestamp", LocalDateTime.now());
        
        return comparisonData;
    }

    /**
     * Detect solar panel faults
     */
    public List<Map<String, Object>> detectSolarFaults(UUID inverterId) {
        log.info("Detecting solar faults for inverter: {}", inverterId);
        
        List<SolarPanel> panels = solarPanelRepository.findByInverterIdAndIsActive(inverterId, true);
        List<Map<String, Object>> faults = new ArrayList<>();
        
        for (SolarPanel panel : panels) {
            Map<String, Object> fault = checkPanelFault(panel);
            if (fault != null) {
                faults.add(fault);
            }
        }
        
        return faults;
    }

    /**
     * Get solar panel performance analytics
     */
    public Map<String, Object> getSolarPerformanceAnalytics(UUID inverterId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Getting solar performance analytics for inverter: {}", inverterId);
        
        List<SolarPanel> panels = solarPanelRepository.findByInverterIdAndIsActive(inverterId, true);
        
        Map<String, Object> analytics = new HashMap<>();
        
        // Performance metrics
        analytics.put("totalPanels", panels.size());
        analytics.put("activePanels", panels.stream().mapToInt(p -> p.getIsActive() ? 1 : 0).sum());
        analytics.put("faultyPanels", panels.stream().mapToInt(p -> p.getHasFault() ? 1 : 0).sum());
        
        // Generation statistics
        BigDecimal totalGeneration = panels.stream()
            .map(SolarPanel::getCurrentPower)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        analytics.put("totalGeneration", totalGeneration);
        
        // Efficiency analysis
        BigDecimal averageEfficiency = calculateAverageEfficiency(panels);
        analytics.put("averageEfficiency", averageEfficiency);
        
        // Top and bottom performers
        List<SolarPanel> sortedPanels = panels.stream()
            .sorted((p1, p2) -> p2.getCurrentPower().compareTo(p1.getCurrentPower()))
            .collect(Collectors.toList());
        
        if (!sortedPanels.isEmpty()) {
            analytics.put("topPerformer", buildPanelSummary(sortedPanels.get(0)));
            analytics.put("bottomPerformer", buildPanelSummary(sortedPanels.get(sortedPanels.size() - 1)));
        }
        
        // String-level analysis
        List<SolarString> strings = solarStringRepository.findByInverterIdAndIsActive(inverterId, true);
        analytics.put("stringAnalysis", strings.stream()
            .map(this::buildStringAnalysis)
            .collect(Collectors.toList()));
        
        return analytics;
    }

    /**
     * Fetch data from inverter API
     */
    private Map<String, Object> fetchInverterData(SolarInverter inverter) {
        String apiUrl = buildInverterApiUrl(inverter);
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(apiUrl, Map.class);
            return response != null ? response : new HashMap<>();
        } catch (Exception e) {
            log.error("Error fetching data from inverter API: {}", apiUrl, e);
            return new HashMap<>();
        }
    }

    /**
     * Build inverter API URL based on type
     */
    private String buildInverterApiUrl(SolarInverter inverter) {
        String baseUrl = inverter.getApiBaseUrl();
        String apiKey = inverter.getApiKey();
        
        switch (inverter.getInverterType()) {
            case DEYE:
                return baseUrl + "/api/v1/realTimeData?token=" + apiKey;
            case SOLIS:
                return baseUrl + "/api/v1/station/realTime?stationId=" + inverter.getStationId() + "&key=" + apiKey;
            case GROWATT:
                return baseUrl + "/api/v1/device/realTimeData?deviceId=" + inverter.getDeviceId() + "&token=" + apiKey;
            default:
                return baseUrl + "/api/data";
        }
    }

    /**
     * Update solar strings with inverter data
     */
    private void updateSolarStrings(UUID inverterId, Map<String, Object> inverterData) {
        List<SolarString> strings = solarStringRepository.findByInverterIdAndIsActive(inverterId, true);
        
        for (SolarString string : strings) {
            // Extract string data from inverter response
            Map<String, Object> stringData = extractStringData(inverterData, string.getStringNumber());
            
            if (stringData != null) {
                string.setVoltage(new BigDecimal(stringData.get("voltage").toString()));
                string.setCurrent(new BigDecimal(stringData.get("current").toString()));
                string.setPower(new BigDecimal(stringData.get("power").toString()));
                string.setLastReadingTime(LocalDateTime.now());
                string.setUpdatedAt(LocalDateTime.now());
                
                solarStringRepository.save(string);
            }
        }
    }

    /**
     * Update solar panels with inverter data
     */
    private void updateSolarPanels(UUID inverterId, Map<String, Object> inverterData) {
        List<SolarPanel> panels = solarPanelRepository.findByInverterIdAndIsActive(inverterId, true);
        
        for (SolarPanel panel : panels) {
            // Extract panel data from inverter response
            Map<String, Object> panelData = extractPanelData(inverterData, panel.getPanelNumber());
            
            if (panelData != null) {
                panel.setVoltage(new BigDecimal(panelData.get("voltage").toString()));
                panel.setCurrent(new BigDecimal(panelData.get("current").toString()));
                panel.setCurrentPower(new BigDecimal(panelData.get("power").toString()));
                panel.setTemperature(new BigDecimal(panelData.get("temperature").toString()));
                panel.setLastReadingTime(LocalDateTime.now());
                panel.setUpdatedAt(LocalDateTime.now());
                
                solarPanelRepository.save(panel);
            }
        }
    }

    /**
     * Check for faults in solar system
     */
    private void checkForFaults(UUID inverterId) {
        List<SolarPanel> panels = solarPanelRepository.findByInverterIdAndIsActive(inverterId, true);
        
        for (SolarPanel panel : panels) {
            boolean hasFault = checkPanelFault(panel) != null;
            if (panel.getHasFault() != hasFault) {
                panel.setHasFault(hasFault);
                panel.setUpdatedAt(LocalDateTime.now());
                solarPanelRepository.save(panel);
            }
        }
    }

    /**
     * Check individual panel for faults
     */
    private Map<String, Object> checkPanelFault(SolarPanel panel) {
        Map<String, Object> fault = null;
        
        // Check for low power output
        if (panel.getCurrentPower().compareTo(panel.getRatedPower().multiply(new BigDecimal("0.5"))) < 0) {
            fault = new HashMap<>();
            fault.put("panelId", panel.getId());
            fault.put("panelNumber", panel.getPanelNumber());
            fault.put("faultType", "LOW_POWER_OUTPUT");
            fault.put("severity", "WARNING");
            fault.put("description", "Panel power output is below 50% of rated capacity");
        }
        
        // Check for high temperature
        if (panel.getTemperature().compareTo(new BigDecimal("80")) > 0) {
            fault = new HashMap<>();
            fault.put("panelId", panel.getId());
            fault.put("panelNumber", panel.getPanelNumber());
            fault.put("faultType", "HIGH_TEMPERATURE");
            fault.put("severity", "CRITICAL");
            fault.put("description", "Panel temperature exceeds safe operating limits");
        }
        
        return fault;
    }

    /**
     * Build string heatmap data
     */
    private Map<String, Object> buildStringHeatmapData(SolarString string) {
        Map<String, Object> data = new HashMap<>();
        data.put("stringId", string.getId());
        data.put("stringNumber", string.getStringNumber());
        data.put("voltage", string.getVoltage());
        data.put("current", string.getCurrent());
        data.put("power", string.getPower());
        data.put("status", getStringStatus(string));
        data.put("efficiency", calculateStringEfficiency(string));
        return data;
    }

    /**
     * Build panel heatmap data
     */
    private Map<String, Object> buildPanelHeatmapData(SolarPanel panel) {
        Map<String, Object> data = new HashMap<>();
        data.put("panelId", panel.getId());
        data.put("panelNumber", panel.getPanelNumber());
        data.put("voltage", panel.getVoltage());
        data.put("current", panel.getCurrent());
        data.put("power", panel.getCurrentPower());
        data.put("temperature", panel.getTemperature());
        data.put("status", getPanelStatus(panel));
        data.put("efficiency", calculatePanelEfficiency(panel));
        data.put("hasFault", panel.getHasFault());
        return data;
    }

    /**
     * Get string status based on performance
     */
    private String getStringStatus(SolarString string) {
        BigDecimal efficiency = calculateStringEfficiency(string);
        
        if (efficiency.compareTo(new BigDecimal("0.9")) > 0) {
            return "OPTIMAL";
        } else if (efficiency.compareTo(new BigDecimal("0.7")) > 0) {
            return "GOOD";
        } else if (efficiency.compareTo(new BigDecimal("0.5")) > 0) {
            return "UNDERPERFORMING";
        } else {
            return "FAULT";
        }
    }

    /**
     * Get panel status based on performance
     */
    private String getPanelStatus(SolarPanel panel) {
        if (panel.getHasFault()) {
            return "FAULT";
        }
        
        BigDecimal efficiency = calculatePanelEfficiency(panel);
        
        if (efficiency.compareTo(new BigDecimal("0.9")) > 0) {
            return "OPTIMAL";
        } else if (efficiency.compareTo(new BigDecimal("0.7")) > 0) {
            return "GOOD";
        } else if (efficiency.compareTo(new BigDecimal("0.5")) > 0) {
            return "UNDERPERFORMING";
        } else {
            return "FAULT";
        }
    }

    /**
     * Calculate string efficiency
     */
    private BigDecimal calculateStringEfficiency(SolarString string) {
        if (string.getRatedPower().compareTo(BigDecimal.ZERO) > 0) {
            return string.getPower().divide(string.getRatedPower(), 4, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Calculate panel efficiency
     */
    private BigDecimal calculatePanelEfficiency(SolarPanel panel) {
        if (panel.getRatedPower().compareTo(BigDecimal.ZERO) > 0) {
            return panel.getCurrentPower().divide(panel.getRatedPower(), 4, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Calculate total generation
     */
    private BigDecimal calculateTotalGeneration(List<SolarPanel> panels) {
        return panels.stream()
            .map(SolarPanel::getCurrentPower)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate average efficiency
     */
    private BigDecimal calculateAverageEfficiency(List<SolarPanel> panels) {
        if (panels.isEmpty()) return BigDecimal.ZERO;
        
        BigDecimal totalEfficiency = panels.stream()
            .map(this::calculatePanelEfficiency)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return totalEfficiency.divide(BigDecimal.valueOf(panels.size()), 4, RoundingMode.HALF_UP);
    }

    /**
     * Count faults
     */
    private long countFaults(List<SolarPanel> panels) {
        return panels.stream().mapToLong(p -> p.getHasFault() ? 1 : 0).sum();
    }

    /**
     * Calculate historical average
     */
    private BigDecimal calculateHistoricalAverage(UUID inverterId, LocalDateTime startTime, LocalDateTime endTime) {
        // This would query historical data - simplified for now
        return new BigDecimal("1000"); // Placeholder
    }

    /**
     * Get performance status
     */
    private String getPerformanceStatus(BigDecimal performanceRatio) {
        if (performanceRatio.compareTo(new BigDecimal("1.1")) > 0) {
            return "EXCELLENT";
        } else if (performanceRatio.compareTo(new BigDecimal("0.9")) > 0) {
            return "GOOD";
        } else if (performanceRatio.compareTo(new BigDecimal("0.7")) > 0) {
            return "FAIR";
        } else {
            return "POOR";
        }
    }

    /**
     * Build panel summary
     */
    private Map<String, Object> buildPanelSummary(SolarPanel panel) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("panelId", panel.getId());
        summary.put("panelNumber", panel.getPanelNumber());
        summary.put("power", panel.getCurrentPower());
        summary.put("efficiency", calculatePanelEfficiency(panel));
        summary.put("status", getPanelStatus(panel));
        return summary;
    }

    /**
     * Build string analysis
     */
    private Map<String, Object> buildStringAnalysis(SolarString string) {
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("stringId", string.getId());
        analysis.put("stringNumber", string.getStringNumber());
        analysis.put("power", string.getPower());
        analysis.put("efficiency", calculateStringEfficiency(string));
        analysis.put("status", getStringStatus(string));
        return analysis;
    }

    /**
     * Extract string data from inverter response
     */
    private Map<String, Object> extractStringData(Map<String, Object> inverterData, Integer stringNumber) {
        // This would parse the specific inverter response format
        // Simplified for now
        Map<String, Object> stringData = new HashMap<>();
        stringData.put("voltage", 400.0);
        stringData.put("current", 10.0);
        stringData.put("power", 4000.0);
        return stringData;
    }

    /**
     * Extract panel data from inverter response
     */
    private Map<String, Object> extractPanelData(Map<String, Object> inverterData, Integer panelNumber) {
        // This would parse the specific inverter response format
        // Simplified for now
        Map<String, Object> panelData = new HashMap<>();
        panelData.put("voltage", 40.0);
        panelData.put("current", 10.0);
        panelData.put("power", 400.0);
        panelData.put("temperature", 45.0);
        return panelData;
    }
}
