package com.smartwatts.billingservice.service;

import com.smartwatts.billingservice.dto.TariffDto;
import com.smartwatts.billingservice.model.Tariff;
import com.smartwatts.billingservice.repository.TariffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TariffService {
    
    private final TariffRepository tariffRepository;
    
    @Transactional
    public TariffDto createTariff(TariffDto tariffDto) {
        log.info("Creating tariff: {}", tariffDto.getTariffCode());
        
        Tariff tariff = new Tariff();
        BeanUtils.copyProperties(tariffDto, tariff);
        
        // Set default values
        if (tariff.getStatus() == null) {
            tariff.setStatus(Tariff.TariffStatus.DRAFT);
        }
        if (tariff.getCreatedAt() == null) {
            tariff.setCreatedAt(LocalDateTime.now());
        }
        tariff.setUpdatedAt(LocalDateTime.now());
        
        Tariff savedTariff = tariffRepository.save(tariff);
        log.info("Tariff created with ID: {}", savedTariff.getId());
        
        return convertToDto(savedTariff);
    }
    
    @Transactional(readOnly = true)
    public TariffDto getTariffById(UUID tariffId) {
        log.info("Fetching tariff with ID: {}", tariffId);
        Tariff tariff = tariffRepository.findById(tariffId)
                .orElseThrow(() -> new RuntimeException("Tariff not found with ID: " + tariffId));
        return convertToDto(tariff);
    }
    
    @Transactional(readOnly = true)
    public Optional<TariffDto> getTariffByCode(String tariffCode) {
        log.info("Fetching tariff with code: {}", tariffCode);
        Optional<Tariff> tariff = tariffRepository.findByTariffCode(tariffCode);
        return tariff.map(this::convertToDto);
    }
    
    @Transactional(readOnly = true)
    public Page<TariffDto> getTariffsByStatus(Tariff.TariffStatus status, Pageable pageable) {
        log.info("Fetching tariffs with status: {}", status);
        Page<Tariff> tariffs = tariffRepository.findByStatus(status, pageable);
        return tariffs.map(this::convertToDto);
    }
    
    @Transactional(readOnly = true)
    public List<TariffDto> getActiveTariffs() {
        log.info("Fetching active tariffs");
        List<Tariff> tariffs = tariffRepository.findActiveTariffs(Tariff.TariffStatus.ACTIVE, LocalDateTime.now());
        return tariffs.stream().map(this::convertToDto).toList();
    }
    
    @Transactional(readOnly = true)
    public List<TariffDto> getActiveTariffsByTypeAndSource(Tariff.CustomerType customerType, Tariff.EnergySource energySource) {
        log.info("Fetching active tariffs for customer type: {} and energy source: {}", customerType, energySource);
        List<Tariff> tariffs = tariffRepository.findActiveTariffsByTypeAndSource(customerType, energySource, Tariff.TariffStatus.ACTIVE, LocalDateTime.now());
        return tariffs.stream().map(this::convertToDto).toList();
    }
    
    @Transactional
    public TariffDto activateTariff(UUID tariffId, String approvedBy) {
        log.info("Activating tariff with ID: {}", tariffId);
        Tariff tariff = tariffRepository.findById(tariffId)
                .orElseThrow(() -> new RuntimeException("Tariff not found with ID: " + tariffId));
        
        if (tariff.getStatus() != Tariff.TariffStatus.DRAFT) {
            throw new RuntimeException("Tariff is not in draft status");
        }
        
        // Convert String to UUID
        tariff.setApprovedBy(UUID.fromString(approvedBy));
        tariff.setApprovedDate(LocalDateTime.now());
        tariff.setUpdatedAt(LocalDateTime.now());
        
        Tariff savedTariff = tariffRepository.save(tariff);
        return convertToDto(savedTariff);
    }
    
    @Transactional
    public TariffDto updateTariff(UUID tariffId, TariffDto tariffDto) {
        log.info("Updating tariff with ID: {}", tariffId);
        Tariff tariff = tariffRepository.findById(tariffId)
                .orElseThrow(() -> new RuntimeException("Tariff not found with ID: " + tariffId));
        
        // Update fields
        tariff.setTariffName(tariffDto.getTariffName());
        tariff.setBaseRate(tariffDto.getBaseRate());
        tariff.setServiceCharge(tariffDto.getServiceCharge());
        tariff.setVatRate(tariffDto.getVatRate());
        tariff.setMinimumCharge(tariffDto.getMinimumCharge());
        tariff.setMaximumCharge(tariffDto.getMaximumCharge());
        tariff.setEffectiveDate(tariffDto.getEffectiveDate());
        tariff.setExpiryDate(tariffDto.getExpiryDate());
        tariff.setNotes(tariffDto.getNotes());
        tariff.setUpdatedAt(LocalDateTime.now());
        
        Tariff savedTariff = tariffRepository.save(tariff);
        return convertToDto(savedTariff);
    }
    
    @Transactional
    public TariffDto expireTariff(UUID tariffId) {
        log.info("Expiring tariff with ID: {}", tariffId);
        Tariff tariff = tariffRepository.findById(tariffId)
                .orElseThrow(() -> new RuntimeException("Tariff not found with ID: " + tariffId));
        
        tariff.setStatus(Tariff.TariffStatus.EXPIRED);
        tariff.setExpiryDate(LocalDateTime.now());
        tariff.setUpdatedAt(LocalDateTime.now());
        
        Tariff savedTariff = tariffRepository.save(tariff);
        return convertToDto(savedTariff);
    }
    
    @Transactional(readOnly = true)
    public List<TariffDto> getExpiredTariffs() {
        log.info("Fetching expired tariffs");
        List<Tariff> expiredTariffs = tariffRepository.findExpiredTariffs(Tariff.TariffStatus.ACTIVE, LocalDateTime.now());
        return expiredTariffs.stream().map(this::convertToDto).toList();
    }
    
    @Transactional(readOnly = true)
    public long getTariffCountByStatus(Tariff.TariffStatus status) {
        log.info("Counting tariffs with status: {}", status);
        return tariffRepository.countByStatus(status);
    }
    
    private TariffDto convertToDto(Tariff tariff) {
        TariffDto dto = new TariffDto();
        BeanUtils.copyProperties(tariff, dto);
        return dto;
    }
} 