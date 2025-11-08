package com.smartwatts.energyservice.service;

import com.smartwatts.energyservice.dto.EnergyReadingDto;
import com.smartwatts.energyservice.dto.EnergyConsumptionDto;
import com.smartwatts.energyservice.dto.EnergyAlertDto;
import com.smartwatts.energyservice.model.EnergyReading;
import com.smartwatts.energyservice.model.EnergyConsumption;
import com.smartwatts.energyservice.model.EnergyAlert;
import com.smartwatts.energyservice.repository.EnergyReadingRepository;
import com.smartwatts.energyservice.repository.EnergyConsumptionRepository;
import com.smartwatts.energyservice.repository.EnergyAlertRepository;
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
public class EnergyService {
    
    private final EnergyReadingRepository energyReadingRepository;
    private final EnergyConsumptionRepository energyConsumptionRepository;
    private final EnergyAlertRepository energyAlertRepository;
    private final AlertService alertService;
    private final DataIngestionSecurityService dataIngestionSecurityService;
    private final DiscoMonitoringService discoMonitoringService;
    
    @Transactional
    public EnergyReadingDto saveEnergyReading(EnergyReadingDto readingDto) {
        log.info("Saving energy reading for user: {}, device: {}", readingDto.getUserId(), readingDto.getDeviceId());
        
        // Security validation: Check if device is verified and can send data
        dataIngestionSecurityService.validateDeviceDataIngestion(readingDto, null);
        
        EnergyReading reading = new EnergyReading();
        BeanUtils.copyProperties(readingDto, reading);
        
        // Set default values
        if (reading.getReadingTimestamp() == null) {
            reading.setReadingTimestamp(LocalDateTime.now());
        }
        if (reading.getSourceType() == null) {
            reading.setSourceType(EnergyReading.EnergySource.GRID);
        }
        if (reading.getReadingType() == null) {
            reading.setReadingType(EnergyReading.ReadingType.REAL_TIME);
        }
        
        // Calculate power if not provided
        if (reading.getPower() == null && reading.getVoltage() != null && reading.getCurrent() != null) {
            reading.setPower(reading.getVoltage().multiply(reading.getCurrent()));
        }
        
        // Calculate quality score
        reading.setQualityScore(calculateQualityScore(reading));
        
        EnergyReading savedReading = energyReadingRepository.save(reading);
        log.info("Energy reading saved with ID: {}", savedReading.getId());
        
        // Check for alerts
        alertService.checkForAlerts(savedReading);
        
        return convertToDto(savedReading);
    }
    
    /**
     * Save energy reading with device authentication validation
     * This method is used when devices provide authentication secrets
     */
    @Transactional
    public EnergyReadingDto saveEnergyReadingWithAuth(EnergyReadingDto readingDto, String deviceAuthSecret) {
        log.info("Saving energy reading with auth for user: {}, device: {}", readingDto.getUserId(), readingDto.getDeviceId());
        
        // Security validation: Check if device is verified and validate auth secret
        dataIngestionSecurityService.validateDeviceDataIngestion(readingDto, deviceAuthSecret);
        
        // Log successful authentication
        dataIngestionSecurityService.logSecurityEvent(
            readingDto.getDeviceId(), 
            "DATA_INGESTION_AUTH_SUCCESS", 
            "Device authenticated successfully for data ingestion", 
            true
        );
        
        // Proceed with normal data processing
        return saveEnergyReading(readingDto);
    }
    
    @Transactional(readOnly = true)
    public Page<EnergyReadingDto> getAllEnergyReadings(Pageable pageable) {
        log.info("Fetching all energy readings");
        Page<EnergyReading> readings = energyReadingRepository.findAll(pageable);
        return readings.map(this::convertToDto);
    }
    
    @Transactional(readOnly = true)
    public EnergyReadingDto getEnergyReadingById(UUID readingId) {
        log.info("Fetching energy reading with ID: {}", readingId);
        EnergyReading reading = energyReadingRepository.findById(readingId)
                .orElseThrow(() -> new RuntimeException("Energy reading not found with ID: " + readingId));
        return convertToDto(reading);
    }
    
    @Transactional(readOnly = true)
    public Page<EnergyReadingDto> getEnergyReadingsByUserId(UUID userId, Pageable pageable) {
        log.info("Fetching energy readings for user: {}", userId);
        Page<EnergyReading> readings = energyReadingRepository.findByUserId(userId, pageable);
        return readings.map(this::convertToDto);
    }
    
    @Transactional(readOnly = true)
    public List<EnergyReadingDto> getEnergyReadingsByUserIdAndTimeRange(UUID userId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Fetching energy readings for user: {} between {} and {}", userId, startTime, endTime);
        List<EnergyReading> readings = energyReadingRepository.findByUserIdAndReadingTimestampBetween(userId, startTime, endTime);
        return readings.stream().map(this::convertToDto).toList();
    }
    
    @Transactional
    public EnergyConsumptionDto aggregateEnergyConsumption(UUID userId, String deviceId, 
                                                         EnergyConsumption.PeriodType periodType,
                                                         LocalDateTime periodStart, LocalDateTime periodEnd) {
        log.info("Aggregating energy consumption for user: {}, device: {}, period: {} to {}", 
                userId, deviceId, periodStart, periodEnd);
        
        List<EnergyReading> readings = energyReadingRepository.findByUserIdAndDeviceIdAndReadingTimestampBetween(
                userId, deviceId, periodStart, periodEnd);
        
        if (readings.isEmpty()) {
            throw new RuntimeException("No energy readings found for the specified period");
        }
        
        EnergyConsumption consumption = new EnergyConsumption();
        consumption.setUserId(userId);
        consumption.setDeviceId(deviceId);
        consumption.setPeriodType(periodType);
        consumption.setPeriodStart(periodStart);
        consumption.setPeriodEnd(periodEnd);
        consumption.setSourceType(EnergyConsumption.EnergySource.valueOf(readings.get(0).getSourceType().name()));
        
        // Calculate aggregated values
        BigDecimal totalEnergy = readings.stream()
                .map(EnergyReading::getEnergyConsumed)
                .filter(energy -> energy != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal peakPower = readings.stream()
                .map(EnergyReading::getPower)
                .filter(power -> power != null)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        
        BigDecimal averagePower = readings.stream()
                .map(EnergyReading::getPower)
                .filter(power -> power != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(readings.size()), 2, RoundingMode.HALF_UP);
        
        BigDecimal minimumPower = readings.stream()
                .map(EnergyReading::getPower)
                .filter(power -> power != null)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        
        consumption.setTotalEnergy(totalEnergy);
        consumption.setPeakPower(peakPower);
        consumption.setAveragePower(averagePower);
        consumption.setMinimumPower(minimumPower);
        consumption.setReadingCount(readings.size());
        
        // Calculate quality score
        BigDecimal avgQualityScore = readings.stream()
                .map(EnergyReading::getQualityScore)
                .filter(score -> score != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(readings.size()), 2, RoundingMode.HALF_UP);
        consumption.setQualityScore(avgQualityScore);
        
        EnergyConsumption savedConsumption = energyConsumptionRepository.save(consumption);
        log.info("Energy consumption aggregated and saved with ID: {}", savedConsumption.getId());
        
        return convertToDto(savedConsumption);
    }
    
    @Transactional(readOnly = true)
    public Page<EnergyConsumptionDto> getEnergyConsumptionByUserId(UUID userId, Pageable pageable) {
        log.info("Fetching energy consumption for user: {}", userId);
        Page<EnergyConsumption> consumption = energyConsumptionRepository.findByUserId(userId, pageable);
        return consumption.map(this::convertToDto);
    }
    
    @Transactional(readOnly = true)
    public List<EnergyConsumptionDto> getEnergyConsumptionByUserIdAndTimeRange(UUID userId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Fetching energy consumption for user: {} between {} and {}", userId, startTime, endTime);
        List<EnergyConsumption> consumption = energyConsumptionRepository.findByUserIdAndPeriodStartBetween(userId, startTime, endTime);
        return consumption.stream().map(this::convertToDto).toList();
    }
    
    @Transactional(readOnly = true)
    public EnergyAlertDto getEnergyAlertById(UUID alertId) {
        log.info("Fetching energy alert with ID: {}", alertId);
        EnergyAlert alert = energyAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Energy alert not found with ID: " + alertId));
        return convertToDto(alert);
    }
    
    @Transactional(readOnly = true)
    public Page<EnergyAlertDto> getEnergyAlertsByUserId(UUID userId, Pageable pageable) {
        log.info("Fetching energy alerts for user: {}", userId);
        Page<EnergyAlert> alerts = energyAlertRepository.findByUserId(userId, pageable);
        return alerts.map(this::convertToDto);
    }
    
    @Transactional
    public EnergyAlertDto acknowledgeAlert(UUID alertId, UUID acknowledgedBy) {
        log.info("Acknowledging alert with ID: {}", alertId);
        EnergyAlert alert = energyAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Energy alert not found with ID: " + alertId));
        
        alert.setIsAcknowledged(true);
        alert.setAcknowledgedAt(LocalDateTime.now());
        alert.setAcknowledgedBy(acknowledgedBy);
        
        EnergyAlert savedAlert = energyAlertRepository.save(alert);
        return convertToDto(savedAlert);
    }
    
    @Transactional
    public EnergyAlertDto resolveAlert(UUID alertId, UUID resolvedBy, String resolutionNotes) {
        log.info("Resolving alert with ID: {}", alertId);
        EnergyAlert alert = energyAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Energy alert not found with ID: " + alertId));
        
        alert.setIsResolved(true);
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolvedBy(resolvedBy);
        alert.setResolutionNotes(resolutionNotes);
        
        EnergyAlert savedAlert = energyAlertRepository.save(alert);
        return convertToDto(savedAlert);
    }
    
    @Transactional(readOnly = true)
    public long getUnacknowledgedAlertCount(UUID userId) {
        return energyAlertRepository.countUnacknowledgedAlertsByUserId(userId);
    }
    
    @Transactional(readOnly = true)
    public long getUnresolvedAlertCount(UUID userId) {
        return energyAlertRepository.countUnresolvedAlertsByUserId(userId);
    }
    
    private BigDecimal calculateQualityScore(EnergyReading reading) {
        // Simple quality score calculation based on data completeness
        int score = 0;
        int total = 0;
        
        if (reading.getVoltage() != null) {
            score += 20;
            total += 20;
        }
        if (reading.getCurrent() != null) {
            score += 20;
            total += 20;
        }
        if (reading.getPower() != null) {
            score += 20;
            total += 20;
        }
        if (reading.getEnergyConsumed() != null) {
            score += 20;
            total += 20;
        }
        if (reading.getFrequency() != null) {
            score += 10;
            total += 10;
        }
        if (reading.getPowerFactor() != null) {
            score += 10;
            total += 10;
        }
        
        return total > 0 ? BigDecimal.valueOf(score).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }
    
    private EnergyReadingDto convertToDto(EnergyReading reading) {
        EnergyReadingDto dto = new EnergyReadingDto();
        BeanUtils.copyProperties(reading, dto);
        return dto;
    }
    
    private EnergyConsumptionDto convertToDto(EnergyConsumption consumption) {
        EnergyConsumptionDto dto = new EnergyConsumptionDto();
        BeanUtils.copyProperties(consumption, dto);
        return dto;
    }
    
    private EnergyAlertDto convertToDto(EnergyAlert alert) {
        EnergyAlertDto dto = new EnergyAlertDto();
        BeanUtils.copyProperties(alert, dto);
        return dto;
    }
    
    public Map<String, Object> getPowerQuality() {
        Map<String, Object> powerQuality = new HashMap<>();
        powerQuality.put("voltage", 0.0);
        powerQuality.put("current", 0.0);
        powerQuality.put("frequency", 0.0);
        powerQuality.put("powerFactor", 0.0);
        powerQuality.put("harmonics", 0.0);
        powerQuality.put("quality", "Good");
        return powerQuality;
    }
    
    public Map<String, Object> getSourceBreakdown(UUID userId) {
        return discoMonitoringService.getSourceBreakdown(userId);
    }
    
    public Map<String, Object> getDiscoStatus(UUID userId) {
        return discoMonitoringService.getDiscoStatus(userId);
    }
    
    public Map<String, Object> getGridStability() {
        return discoMonitoringService.getGridStability();
    }
    
    public Map<String, Object> getVoltageQuality(UUID userId) {
        return discoMonitoringService.getVoltageQuality(userId);
    }
} 