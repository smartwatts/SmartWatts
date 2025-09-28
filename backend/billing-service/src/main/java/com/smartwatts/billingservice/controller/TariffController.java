package com.smartwatts.billingservice.controller;

import com.smartwatts.billingservice.dto.TariffDto;
import com.smartwatts.billingservice.model.Tariff;
import com.smartwatts.billingservice.service.TariffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tariffs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tariff Management", description = "APIs for managing MYTO tariffs")
public class TariffController {
    
    private final TariffService tariffService;
    
    @PostMapping
    @Operation(summary = "Create a new tariff", description = "Creates a new MYTO tariff")
    public ResponseEntity<TariffDto> createTariff(@Valid @RequestBody TariffDto tariffDto) {
        log.info("Creating tariff: {}", tariffDto.getTariffCode());
        TariffDto createdTariff = tariffService.createTariff(tariffDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTariff);
    }
    
    @GetMapping("/{tariffId}")
    @Operation(summary = "Get tariff by ID", description = "Retrieves a specific tariff by its ID")
    public ResponseEntity<TariffDto> getTariffById(
            @Parameter(description = "Tariff ID") @PathVariable UUID tariffId) {
        log.info("Fetching tariff with ID: {}", tariffId);
        TariffDto tariff = tariffService.getTariffById(tariffId);
        return ResponseEntity.ok(tariff);
    }
    
    @GetMapping("/code/{tariffCode}")
    @Operation(summary = "Get tariff by code", description = "Retrieves a specific tariff by its code")
    public ResponseEntity<TariffDto> getTariffByCode(
            @Parameter(description = "Tariff code") @PathVariable String tariffCode) {
        log.info("Fetching tariff with code: {}", tariffCode);
        Optional<TariffDto> tariff = tariffService.getTariffByCode(tariffCode);
        return tariff.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Get tariffs by status", description = "Retrieves all tariffs with a specific status")
    public ResponseEntity<Page<TariffDto>> getTariffsByStatus(
            @Parameter(description = "Tariff status") @PathVariable Tariff.TariffStatus status,
            Pageable pageable) {
        log.info("Fetching tariffs with status: {}", status);
        Page<TariffDto> tariffs = tariffService.getTariffsByStatus(status, pageable);
        return ResponseEntity.ok(tariffs);
    }
    
    @GetMapping("/active")
    @Operation(summary = "Get active tariffs", description = "Retrieves all currently active tariffs")
    public ResponseEntity<List<TariffDto>> getActiveTariffs() {
        log.info("Fetching active tariffs");
        List<TariffDto> tariffs = tariffService.getActiveTariffs();
        return ResponseEntity.ok(tariffs);
    }
    
    @GetMapping("/active/type/{customerType}/source/{energySource}")
    @Operation(summary = "Get active tariffs by type and source", description = "Retrieves active tariffs for specific customer type and energy source")
    public ResponseEntity<List<TariffDto>> getActiveTariffsByTypeAndSource(
            @Parameter(description = "Customer type") @PathVariable Tariff.CustomerType customerType,
            @Parameter(description = "Energy source") @PathVariable Tariff.EnergySource energySource) {
        log.info("Fetching active tariffs for customer type: {} and energy source: {}", customerType, energySource);
        List<TariffDto> tariffs = tariffService.getActiveTariffsByTypeAndSource(customerType, energySource);
        return ResponseEntity.ok(tariffs);
    }
    
    @PutMapping("/{tariffId}/activate")
    @Operation(summary = "Activate tariff", description = "Activates a draft tariff")
    public ResponseEntity<TariffDto> activateTariff(
            @Parameter(description = "Tariff ID") @PathVariable UUID tariffId,
            @Parameter(description = "Approved by") @RequestParam String approvedBy) {
        log.info("Activating tariff with ID: {}", tariffId);
        TariffDto activatedTariff = tariffService.activateTariff(tariffId, approvedBy);
        return ResponseEntity.ok(activatedTariff);
    }
    
    @PutMapping("/{tariffId}")
    @Operation(summary = "Update tariff", description = "Updates an existing tariff")
    public ResponseEntity<TariffDto> updateTariff(
            @Parameter(description = "Tariff ID") @PathVariable UUID tariffId,
            @Valid @RequestBody TariffDto tariffDto) {
        log.info("Updating tariff with ID: {}", tariffId);
        TariffDto updatedTariff = tariffService.updateTariff(tariffId, tariffDto);
        return ResponseEntity.ok(updatedTariff);
    }
    
    @PutMapping("/{tariffId}/expire")
    @Operation(summary = "Expire tariff", description = "Expires an active tariff")
    public ResponseEntity<TariffDto> expireTariff(
            @Parameter(description = "Tariff ID") @PathVariable UUID tariffId) {
        log.info("Expiring tariff with ID: {}", tariffId);
        TariffDto expiredTariff = tariffService.expireTariff(tariffId);
        return ResponseEntity.ok(expiredTariff);
    }
    
    @GetMapping("/expired")
    @Operation(summary = "Get expired tariffs", description = "Retrieves all expired tariffs")
    public ResponseEntity<List<TariffDto>> getExpiredTariffs() {
        log.info("Fetching expired tariffs");
        List<TariffDto> expiredTariffs = tariffService.getExpiredTariffs();
        return ResponseEntity.ok(expiredTariffs);
    }
    
    @GetMapping("/count/status/{status}")
    @Operation(summary = "Get tariff count by status", description = "Returns the count of tariffs with a specific status")
    public ResponseEntity<Long> getTariffCountByStatus(
            @Parameter(description = "Tariff status") @PathVariable Tariff.TariffStatus status) {
        log.info("Counting tariffs with status: {}", status);
        long count = tariffService.getTariffCountByStatus(status);
        return ResponseEntity.ok(count);
    }
} 