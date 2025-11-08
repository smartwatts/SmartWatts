package com.smartwatts.energyservice.controller;

import com.smartwatts.energyservice.dto.EnergyReadingDto;
import com.smartwatts.energyservice.dto.EnergyConsumptionDto;
import com.smartwatts.energyservice.dto.EnergyAlertDto;
import com.smartwatts.energyservice.model.EnergyConsumption;
import com.smartwatts.energyservice.service.EnergyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/energy")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Energy Management", description = "APIs for energy data management and monitoring")
public class EnergyController {
    
    private final EnergyService energyService;
    
    @GetMapping("/readings")
    @Operation(summary = "Get all energy readings", description = "Retrieves all energy readings (for testing)")
    public ResponseEntity<Page<EnergyReadingDto>> getAllEnergyReadings(Pageable pageable) {
        log.info("Fetching all energy readings");
        Page<EnergyReadingDto> readings = energyService.getAllEnergyReadings(pageable);
        return ResponseEntity.ok(readings);
    }
    
    @PostMapping("/readings")
    @Operation(summary = "Save energy reading", description = "Saves a new energy reading from a device")
    public ResponseEntity<EnergyReadingDto> saveEnergyReading(@Valid @RequestBody EnergyReadingDto readingDto) {
        log.info("Saving energy reading for user: {}, device: {}", readingDto.getUserId(), readingDto.getDeviceId());
        EnergyReadingDto savedReading = energyService.saveEnergyReading(readingDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedReading);
    }
    
    @PostMapping("/readings/secure")
    @Operation(summary = "Save energy reading with device authentication", description = "Saves a new energy reading from a device with authentication validation")
    public ResponseEntity<EnergyReadingDto> saveEnergyReadingWithAuth(
            @Valid @RequestBody EnergyReadingDto readingDto,
            @RequestHeader(value = "X-Device-Auth-Secret", required = false) String deviceAuthSecret) {
        log.info("Saving energy reading with auth for user: {}, device: {}", readingDto.getUserId(), readingDto.getDeviceId());
        EnergyReadingDto savedReading = energyService.saveEnergyReadingWithAuth(readingDto, deviceAuthSecret);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedReading);
    }
    
    @GetMapping("/readings/{readingId}")
    @Operation(summary = "Get energy reading by ID", description = "Retrieves a specific energy reading by its ID")
    @PreAuthorize("hasRole('ADMIN') or #readingId == authentication.principal.username")
    public ResponseEntity<EnergyReadingDto> getEnergyReadingById(
            @Parameter(description = "Energy reading ID") @PathVariable UUID readingId) {
        log.info("Fetching energy reading with ID: {}", readingId);
        EnergyReadingDto reading = energyService.getEnergyReadingById(readingId);
        return ResponseEntity.ok(reading);
    }
    
    @GetMapping("/readings/user/{userId}")
    @Operation(summary = "Get energy readings by user", description = "Retrieves all energy readings for a specific user")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public ResponseEntity<Page<EnergyReadingDto>> getEnergyReadingsByUserId(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            Pageable pageable) {
        log.info("Fetching energy readings for user: {}", userId);
        Page<EnergyReadingDto> readings = energyService.getEnergyReadingsByUserId(userId, pageable);
        return ResponseEntity.ok(readings);
    }
    
    @GetMapping("/readings/user/{userId}/time-range")
    @Operation(summary = "Get energy readings by time range", description = "Retrieves energy readings for a user within a specific time range")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public ResponseEntity<List<EnergyReadingDto>> getEnergyReadingsByTimeRange(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Start time") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "End time") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        log.info("Fetching energy readings for user: {} between {} and {}", userId, startTime, endTime);
        List<EnergyReadingDto> readings = energyService.getEnergyReadingsByUserIdAndTimeRange(userId, startTime, endTime);
        return ResponseEntity.ok(readings);
    }
    
    @PostMapping("/consumption/aggregate")
    @Operation(summary = "Aggregate energy consumption", description = "Aggregates energy readings into consumption data for a specific period")
    public ResponseEntity<EnergyConsumptionDto> aggregateEnergyConsumption(
            @Parameter(description = "User ID") @RequestParam UUID userId,
            @Parameter(description = "Device ID") @RequestParam String deviceId,
            @Parameter(description = "Period type") @RequestParam EnergyConsumption.PeriodType periodType,
            @Parameter(description = "Period start") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime periodStart,
            @Parameter(description = "Period end") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime periodEnd) {
        log.info("Aggregating energy consumption for user: {}, device: {}, period: {} to {}", 
                userId, deviceId, periodStart, periodEnd);
        EnergyConsumptionDto consumption = energyService.aggregateEnergyConsumption(userId, deviceId, periodType, periodStart, periodEnd);
        return ResponseEntity.status(HttpStatus.CREATED).body(consumption);
    }
    
    @GetMapping("/consumption/user/{userId}")
    @Operation(summary = "Get energy consumption by user", description = "Retrieves aggregated energy consumption data for a specific user")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public ResponseEntity<Page<EnergyConsumptionDto>> getEnergyConsumptionByUserId(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            Pageable pageable) {
        log.info("Fetching energy consumption for user: {}", userId);
        Page<EnergyConsumptionDto> consumption = energyService.getEnergyConsumptionByUserId(userId, pageable);
        return ResponseEntity.ok(consumption);
    }
    
    @GetMapping("/consumption/user/{userId}/time-range")
    @Operation(summary = "Get energy consumption by time range", description = "Retrieves energy consumption data for a user within a specific time range")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public ResponseEntity<List<EnergyConsumptionDto>> getEnergyConsumptionByTimeRange(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Start time") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "End time") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        log.info("Fetching energy consumption for user: {} between {} and {}", userId, startTime, endTime);
        List<EnergyConsumptionDto> consumption = energyService.getEnergyConsumptionByUserIdAndTimeRange(userId, startTime, endTime);
        return ResponseEntity.ok(consumption);
    }
    
    @GetMapping("/alerts/{alertId}")
    @Operation(summary = "Get energy alert by ID", description = "Retrieves a specific energy alert by its ID")
    @PreAuthorize("hasRole('ADMIN') or #alertId == authentication.principal.username")
    public ResponseEntity<EnergyAlertDto> getEnergyAlertById(
            @Parameter(description = "Energy alert ID") @PathVariable UUID alertId) {
        log.info("Fetching energy alert with ID: {}", alertId);
        EnergyAlertDto alert = energyService.getEnergyAlertById(alertId);
        return ResponseEntity.ok(alert);
    }
    
    @GetMapping("/alerts/user/{userId}")
    @Operation(summary = "Get energy alerts by user", description = "Retrieves all energy alerts for a specific user")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public ResponseEntity<Page<EnergyAlertDto>> getEnergyAlertsByUserId(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            Pageable pageable) {
        log.info("Fetching energy alerts for user: {}", userId);
        Page<EnergyAlertDto> alerts = energyService.getEnergyAlertsByUserId(userId, pageable);
        return ResponseEntity.ok(alerts);
    }
    
    @PutMapping("/alerts/{alertId}/acknowledge")
    @Operation(summary = "Acknowledge energy alert", description = "Marks an energy alert as acknowledged")
    @PreAuthorize("hasRole('ADMIN') or #alertId == authentication.principal.username")
    public ResponseEntity<EnergyAlertDto> acknowledgeAlert(
            @Parameter(description = "Energy alert ID") @PathVariable UUID alertId,
            @Parameter(description = "User ID of person acknowledging") @RequestParam UUID acknowledgedBy) {
        log.info("Acknowledging alert with ID: {}", alertId);
        EnergyAlertDto alert = energyService.acknowledgeAlert(alertId, acknowledgedBy);
        return ResponseEntity.ok(alert);
    }
    
    @PutMapping("/alerts/{alertId}/resolve")
    @Operation(summary = "Resolve energy alert", description = "Marks an energy alert as resolved")
    @PreAuthorize("hasRole('ADMIN') or #alertId == authentication.principal.username")
    public ResponseEntity<EnergyAlertDto> resolveAlert(
            @Parameter(description = "Energy alert ID") @PathVariable UUID alertId,
            @Parameter(description = "User ID of person resolving") @RequestParam UUID resolvedBy,
            @Parameter(description = "Resolution notes") @RequestParam String resolutionNotes) {
        log.info("Resolving alert with ID: {}", alertId);
        EnergyAlertDto alert = energyService.resolveAlert(alertId, resolvedBy, resolutionNotes);
        return ResponseEntity.ok(alert);
    }
    
    @GetMapping("/alerts/user/{userId}/unacknowledged-count")
    @Operation(summary = "Get unacknowledged alert count", description = "Returns the count of unacknowledged alerts for a user")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public ResponseEntity<Long> getUnacknowledgedAlertCount(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        log.info("Counting unacknowledged alerts for user: {}", userId);
        long count = energyService.getUnacknowledgedAlertCount(userId);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/alerts/user/{userId}/unresolved-count")
    @Operation(summary = "Get unresolved alert count", description = "Returns the count of unresolved alerts for a user")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public ResponseEntity<Long> getUnresolvedAlertCount(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        log.info("Counting unresolved alerts for user: {}", userId);
        long count = energyService.getUnresolvedAlertCount(userId);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/power-quality")
    @Operation(summary = "Get power quality data", description = "Retrieves power quality metrics")
    public ResponseEntity<Map<String, Object>> getPowerQuality() {
        log.info("Fetching power quality data");
        Map<String, Object> powerQuality = energyService.getPowerQuality();
        return ResponseEntity.ok(powerQuality);
    }
    
    @GetMapping("/source-breakdown/{userId}")
    @Operation(summary = "Get energy source breakdown", description = "Retrieves breakdown of energy sources (Grid, Solar, Inverter, Generator)")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public ResponseEntity<Map<String, Object>> getSourceBreakdown(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        log.info("Fetching energy source breakdown for user: {}", userId);
        Map<String, Object> breakdown = energyService.getSourceBreakdown(userId);
        return ResponseEntity.ok(breakdown);
    }
    
    @GetMapping("/disco-status/{userId}")
    @Operation(summary = "Get DisCo availability status", description = "Retrieves real-time DisCo availability and outage history")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public ResponseEntity<Map<String, Object>> getDiscoStatus(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        log.info("Fetching DisCo status for user: {}", userId);
        Map<String, Object> discoStatus = energyService.getDiscoStatus(userId);
        return ResponseEntity.ok(discoStatus);
    }
    
    @GetMapping("/grid-stability")
    @Operation(summary = "Get grid stability metrics", description = "Retrieves grid stability metrics including voltage, frequency, and phase balance")
    public ResponseEntity<Map<String, Object>> getGridStability() {
        log.info("Fetching grid stability metrics");
        Map<String, Object> stability = energyService.getGridStability();
        return ResponseEntity.ok(stability);
    }
    
    @GetMapping("/voltage-quality/{userId}")
    @Operation(summary = "Get voltage quality data", description = "Retrieves voltage quality metrics including fluctuations, power factor, and harmonics")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public ResponseEntity<Map<String, Object>> getVoltageQuality(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        log.info("Fetching voltage quality for user: {}", userId);
        Map<String, Object> voltageQuality = energyService.getVoltageQuality(userId);
        return ResponseEntity.ok(voltageQuality);
    }
} 