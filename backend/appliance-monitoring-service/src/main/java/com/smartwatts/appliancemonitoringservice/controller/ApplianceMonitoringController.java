package com.smartwatts.appliancemonitoringservice.controller;

import com.smartwatts.appliancemonitoringservice.model.Appliance;
import com.smartwatts.appliancemonitoringservice.model.ApplianceReading;
import com.smartwatts.appliancemonitoringservice.model.ApplianceType;
import com.smartwatts.appliancemonitoringservice.service.ApplianceMonitoringService;
import com.smartwatts.appliancemonitoringservice.service.WeatherIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/appliance-monitoring")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Appliance Monitoring", description = "APIs for monitoring individual appliances and weather integration")
public class ApplianceMonitoringController {
    
    private final ApplianceMonitoringService applianceMonitoringService;
    private final WeatherIntegrationService weatherIntegrationService;
    
    // Appliance Management Endpoints
    
    @PostMapping("/appliances")
    @Operation(summary = "Create new appliance", description = "Registers a new appliance for monitoring")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Appliance> createAppliance(@Valid @RequestBody Appliance appliance) {
        log.info("Creating new appliance: {}", appliance.getApplianceName());
        Appliance savedAppliance = applianceMonitoringService.createAppliance(appliance);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAppliance);
    }
    
    @GetMapping("/appliances/user/{userId}")
    @Operation(summary = "Get user appliances", description = "Retrieves all appliances for a specific user")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<Appliance>> getUserAppliances(@PathVariable UUID userId) {
        log.info("Fetching appliances for user: {}", userId);
        List<Appliance> appliances = applianceMonitoringService.getUserAppliances(userId);
        return ResponseEntity.ok(appliances);
    }
    
    @GetMapping("/appliances/{applianceId}")
    @Operation(summary = "Get appliance by ID", description = "Retrieves a specific appliance by ID")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Appliance> getAppliance(@PathVariable UUID applianceId) {
        log.info("Fetching appliance: {}", applianceId);
        Appliance appliance = applianceMonitoringService.getAppliance(applianceId);
        return ResponseEntity.ok(appliance);
    }
    
    @PutMapping("/appliances/{applianceId}")
    @Operation(summary = "Update appliance", description = "Updates an existing appliance")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Appliance> updateAppliance(@PathVariable UUID applianceId, @Valid @RequestBody Appliance appliance) {
        log.info("Updating appliance: {}", applianceId);
        appliance.setId(applianceId);
        Appliance updatedAppliance = applianceMonitoringService.updateAppliance(appliance);
        return ResponseEntity.ok(updatedAppliance);
    }
    
    @DeleteMapping("/appliances/{applianceId}")
    @Operation(summary = "Delete appliance", description = "Deactivates an appliance")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAppliance(@PathVariable UUID applianceId) {
        log.info("Deactivating appliance: {}", applianceId);
        applianceMonitoringService.deactivateAppliance(applianceId);
        return ResponseEntity.noContent().build();
    }
    
    // Appliance Monitoring Endpoints
    
    @PostMapping("/appliances/{applianceId}/readings")
    @Operation(summary = "Record appliance reading", description = "Records a new power consumption reading for an appliance")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApplianceReading> recordApplianceReading(
            @PathVariable UUID applianceId,
            @Valid @RequestBody ApplianceReading reading) {
        log.info("Recording reading for appliance: {}", applianceId);
        ApplianceReading savedReading = applianceMonitoringService.recordApplianceReading(applianceId, reading);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedReading);
    }
    
    @GetMapping("/appliances/{applianceId}/readings")
    @Operation(summary = "Get appliance readings", description = "Retrieves power consumption readings for an appliance")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<ApplianceReading>> getApplianceReadings(
            @PathVariable UUID applianceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        log.info("Fetching readings for appliance: {} from {} to {}", applianceId, startTime, endTime);
        List<ApplianceReading> readings = applianceMonitoringService.getApplianceReadings(applianceId, startTime, endTime);
        return ResponseEntity.ok(readings);
    }
    
    @GetMapping("/appliances/{applianceId}/efficiency")
    @Operation(summary = "Get efficiency stats", description = "Retrieves efficiency statistics for an appliance")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApplianceMonitoringService.ApplianceEfficiencyStats> getEfficiencyStats(
            @PathVariable UUID applianceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        log.info("Fetching efficiency stats for appliance: {} from {} to {}", applianceId, startTime, endTime);
        ApplianceMonitoringService.ApplianceEfficiencyStats stats = applianceMonitoringService.getEfficiencyStats(applianceId, startTime, endTime);
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/appliances/user/{userId}/status")
    @Operation(summary = "Get user appliances status", description = "Retrieves current status of all user appliances")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<ApplianceMonitoringService.ApplianceStatus>> getUserAppliancesStatus(@PathVariable UUID userId) {
        log.info("Fetching appliance status for user: {}", userId);
        List<ApplianceMonitoringService.ApplianceStatus> statuses = applianceMonitoringService.getUserAppliancesWithStatus(userId);
        return ResponseEntity.ok(statuses);
    }
    
    // Weather Integration Endpoints
    
    @GetMapping("/weather/{locationId}")
    @Operation(summary = "Get weather data", description = "Fetches current weather data for a location")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Object> getWeatherData(
            @PathVariable String locationId,
            @RequestParam double latitude,
            @RequestParam double longitude) {
        log.info("Fetching weather data for location: {} ({}, {})", locationId, latitude, longitude);
        Object weatherData = weatherIntegrationService.fetchCurrentWeather(locationId, latitude, longitude);
        return ResponseEntity.ok(weatherData);
    }
    
    // Analytics Endpoints
    
    @GetMapping("/analytics/user/{userId}/summary")
    @Operation(summary = "Get user analytics summary", description = "Retrieves summary analytics for all user appliances")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Object> getUserAnalyticsSummary(@PathVariable UUID userId) {
        log.info("Fetching analytics summary for user: {}", userId);
        Object summary = applianceMonitoringService.getUserAnalyticsSummary(userId);
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/analytics/appliances/{applianceId}/anomalies")
    @Operation(summary = "Get appliance anomalies", description = "Retrieves anomaly detection results for an appliance")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<ApplianceReading>> getApplianceAnomalies(@PathVariable UUID applianceId) {
        log.info("Fetching anomalies for appliance: {}", applianceId);
        List<ApplianceReading> anomalies = applianceMonitoringService.getApplianceAnomalies(applianceId);
        return ResponseEntity.ok(anomalies);
    }
    
    @GetMapping("/analytics/appliances/{applianceId}/maintenance")
    @Operation(summary = "Get maintenance alerts", description = "Retrieves maintenance alerts for an appliance")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<ApplianceReading>> getMaintenanceAlerts(@PathVariable UUID applianceId) {
        log.info("Fetching maintenance alerts for appliance: {}", applianceId);
        List<ApplianceReading> alerts = applianceMonitoringService.getMaintenanceAlerts(applianceId);
        return ResponseEntity.ok(alerts);
    }
    
    // Utility Endpoints
    
    @GetMapping("/appliance-types")
    @Operation(summary = "Get appliance types", description = "Retrieves all available appliance types")
    public ResponseEntity<ApplianceType[]> getApplianceTypes() {
        log.info("Fetching available appliance types");
        return ResponseEntity.ok(ApplianceType.values());
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Service health check endpoint")
    public ResponseEntity<Object> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "Appliance Monitoring Service",
            "timestamp", LocalDateTime.now()
        ));
    }
}
