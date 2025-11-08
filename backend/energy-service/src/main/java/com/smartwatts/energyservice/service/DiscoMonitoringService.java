package com.smartwatts.energyservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscoMonitoringService {
    
    // Nigerian DisCos
    private static final Map<String, String> DISCOS = Map.ofEntries(
        Map.entry("EKEDC", "Eko Electricity Distribution Company"),
        Map.entry("IKEDC", "Ikeja Electric Distribution Company"),
        Map.entry("AEDC", "Abuja Electricity Distribution Company"),
        Map.entry("KEDCO", "Kano Electricity Distribution Company"),
        Map.entry("PHED", "Port Harcourt Electricity Distribution Company"),
        Map.entry("BEDC", "Benin Electricity Distribution Company"),
        Map.entry("EEDC", "Enugu Electricity Distribution Company"),
        Map.entry("IBEDC", "Ibadan Electricity Distribution Company"),
        Map.entry("JEDC", "Jos Electricity Distribution Company"),
        Map.entry("KAEDCO", "Kaduna Electricity Distribution Company"),
        Map.entry("YEDC", "Yola Electricity Distribution Company")
    );
    
    public Map<String, Object> getSourceBreakdown(UUID userId) {
        log.info("Getting energy source breakdown for user: {}", userId);
        
        // Simulate multi-source energy breakdown
        Map<String, Object> breakdown = new HashMap<>();
        breakdown.put("grid", Map.of(
            "percentage", 60.0,
            "kwh", 1200.0,
            "cost", 24000.0,
            "status", "active"
        ));
        breakdown.put("solar", Map.of(
            "percentage", 25.0,
            "kwh", 500.0,
            "cost", 0.0,
            "status", "active"
        ));
        breakdown.put("inverter", Map.of(
            "percentage", 10.0,
            "kwh", 200.0,
            "cost", 2000.0,
            "status", "standby"
        ));
        breakdown.put("generator", Map.of(
            "percentage", 5.0,
            "kwh", 100.0,
            "cost", 8000.0,
            "status", "standby"
        ));
        
        breakdown.put("totalConsumption", 2000.0);
        breakdown.put("totalCost", 34000.0);
        breakdown.put("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return breakdown;
    }
    
    public Map<String, Object> getDiscoStatus(UUID userId) {
        log.info("Getting DisCo status for user: {}", userId);
        
        // Simulate DisCo availability based on location
        String userDisco = getUserDisco(userId);
        boolean isAvailable = isDiscoAvailable(userDisco);
        
        Map<String, Object> status = new HashMap<>();
        status.put("disco", userDisco);
        status.put("discoName", DISCOS.get(userDisco));
        status.put("isAvailable", isAvailable);
        status.put("availabilityPercentage", isAvailable ? 95.0 : 45.0);
        status.put("lastOutage", isAvailable ? null : LocalDateTime.now().minusHours(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        status.put("estimatedRestoration", isAvailable ? null : LocalDateTime.now().plusHours(4).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        status.put("outageReason", isAvailable ? null : "Scheduled maintenance");
        status.put("customerCare", getCustomerCareNumber(userDisco));
        status.put("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return status;
    }
    
    public Map<String, Object> getGridStability() {
        log.info("Getting grid stability metrics");
        
        Map<String, Object> stability = new HashMap<>();
        stability.put("voltage", Map.of(
            "current", 220.5,
            "min", 180.0,
            "max", 250.0,
            "status", "normal",
            "quality", "good"
        ));
        stability.put("frequency", Map.of(
            "current", 50.1,
            "min", 49.0,
            "max", 51.0,
            "status", "normal"
        ));
        stability.put("powerFactor", Map.of(
            "current", 0.95,
            "status", "good",
            "recommendation", "Within acceptable range"
        ));
        stability.put("harmonics", Map.of(
            "thd", 3.2,
            "status", "acceptable",
            "limit", 5.0
        ));
        stability.put("phaseBalance", Map.of(
            "status", "balanced",
            "imbalance", 2.1,
            "limit", 5.0
        ));
        stability.put("overallStatus", "stable");
        stability.put("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return stability;
    }
    
    public Map<String, Object> getVoltageQuality(UUID userId) {
        log.info("Getting voltage quality for user: {}", userId);
        
        Map<String, Object> quality = new HashMap<>();
        quality.put("voltage", Map.of(
            "current", 220.5,
            "average", 218.2,
            "min", 180.0,
            "max", 250.0,
            "fluctuation", 2.3,
            "status", "normal"
        ));
        quality.put("powerFactor", Map.of(
            "current", 0.95,
            "average", 0.92,
            "status", "good",
            "improvement", "Consider power factor correction"
        ));
        quality.put("harmonics", Map.of(
            "thd", 3.2,
            "limit", 5.0,
            "status", "acceptable",
            "recommendation", "Monitor for increase"
        ));
        quality.put("voltageVariations", Map.of(
            "sags", 2,
            "swells", 1,
            "interruptions", 0,
            "last24Hours", true
        ));
        quality.put("qualityScore", 85.0);
        quality.put("recommendations", Arrays.asList(
            "Voltage quality is within acceptable limits",
            "Consider voltage stabilizer for sensitive equipment",
            "Monitor power factor for optimization"
        ));
        quality.put("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return quality;
    }
    
    private String getUserDisco(UUID userId) {
        // Simulate user DisCo based on user ID hash
        int hash = userId.hashCode() % DISCOS.size();
        return DISCOS.keySet().toArray(new String[0])[Math.abs(hash)];
    }
    
    private boolean isDiscoAvailable(String disco) {
        // Simulate DisCo availability (90% chance of being available)
        return Math.random() > 0.1;
    }
    
    private String getCustomerCareNumber(String disco) {
        Map<String, String> customerCare = Map.ofEntries(
            Map.entry("EKEDC", "01-700-0255"),
            Map.entry("IKEDC", "01-700-0255"),
            Map.entry("AEDC", "070-8063-5555"),
            Map.entry("KEDCO", "080-6000-5555"),
            Map.entry("PHED", "070-8063-5555"),
            Map.entry("BEDC", "070-8063-5555"),
            Map.entry("EEDC", "070-8063-5555"),
            Map.entry("IBEDC", "070-8063-5555"),
            Map.entry("JEDC", "070-8063-5555"),
            Map.entry("KAEDCO", "070-8063-5555"),
            Map.entry("YEDC", "070-8063-5555")
        );
        return customerCare.getOrDefault(disco, "070-8063-5555");
    }
}


