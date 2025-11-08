package com.smartwatts.analyticsservice.service;

import com.smartwatts.analyticsservice.dto.EnergyAnalyticsDto;
import com.smartwatts.analyticsservice.model.EnergyAnalytics;
import com.smartwatts.analyticsservice.repository.EnergyAnalyticsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private EnergyAnalyticsRepository energyAnalyticsRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private EnergyAnalytics testAnalytics;
    private EnergyAnalyticsDto testAnalyticsDto;
    private UUID testAnalyticsId;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testAnalyticsId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        
        testAnalytics = new EnergyAnalytics();
        testAnalytics.setId(testAnalyticsId);
        testAnalytics.setUserId(testUserId);
        testAnalytics.setDeviceId(UUID.randomUUID());
        testAnalytics.setEfficiencyScore(new BigDecimal("85.5"));
        testAnalytics.setAnalyticsDate(LocalDateTime.now());
        
        testAnalyticsDto = EnergyAnalyticsDto.builder()
                .id(testAnalyticsId)
                .userId(testUserId)
                .deviceId(UUID.randomUUID())
                .efficiencyScore(new BigDecimal("85.5"))
                .analyticsDate(LocalDateTime.now())
                .build();
    }

    @Test
    void createEnergyAnalytics_Success_ReturnsAnalyticsDto() {
        // Given
        when(energyAnalyticsRepository.save(any(EnergyAnalytics.class))).thenReturn(testAnalytics);

        // When
        EnergyAnalyticsDto result = analyticsService.createEnergyAnalytics(testAnalyticsDto);

        // Then
        assertNotNull(result);
        assertEquals(testAnalyticsId, result.getId());
        verify(energyAnalyticsRepository).save(any(EnergyAnalytics.class));
    }

    @Test
    void getEnergyAnalyticsById_Success_ReturnsAnalyticsDto() {
        // Given
        when(energyAnalyticsRepository.findById(testAnalyticsId)).thenReturn(Optional.of(testAnalytics));

        // When
        EnergyAnalyticsDto result = analyticsService.getEnergyAnalyticsById(testAnalyticsId);

        // Then
        assertNotNull(result);
        assertEquals(testAnalyticsId, result.getId());
        verify(energyAnalyticsRepository).findById(testAnalyticsId);
    }

    @Test
    void getEnergyAnalyticsById_NotFound_ThrowsException() {
        // Given
        when(energyAnalyticsRepository.findById(testAnalyticsId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> analyticsService.getEnergyAnalyticsById(testAnalyticsId));
    }

    @Test
    void getAllAnalytics_Success_ReturnsPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<EnergyAnalytics> page = new PageImpl<>(Arrays.asList(testAnalytics));
        when(energyAnalyticsRepository.findAll(pageable)).thenReturn(page);

        // When
        Page<EnergyAnalyticsDto> result = analyticsService.getAllAnalytics(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(energyAnalyticsRepository).findAll(pageable);
    }

    @Test
    void getEnergyAnalyticsByUserId_Success_ReturnsPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<EnergyAnalytics> page = new PageImpl<>(Arrays.asList(testAnalytics));
        when(energyAnalyticsRepository.findByUserId(testUserId, pageable)).thenReturn(page);

        // When
        Page<EnergyAnalyticsDto> result = analyticsService.getEnergyAnalyticsByUserId(testUserId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(energyAnalyticsRepository).findByUserId(testUserId, pageable);
    }

    @Test
    void getEnergyAnalyticsByDateRange_Success_ReturnsList() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        List<EnergyAnalytics> analytics = Arrays.asList(testAnalytics);
        when(energyAnalyticsRepository.findByUserIdAndAnalyticsDateBetween(testUserId, startDate, endDate))
                .thenReturn(analytics);

        // When
        List<EnergyAnalyticsDto> result = analyticsService.getEnergyAnalyticsByDateRange(testUserId, startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(energyAnalyticsRepository).findByUserIdAndAnalyticsDateBetween(testUserId, startDate, endDate);
    }

    @Test
    void getEfficientAnalytics_Success_ReturnsList() {
        // Given
        BigDecimal minScore = new BigDecimal("70.0");
        List<EnergyAnalytics> analytics = Arrays.asList(testAnalytics);
        when(energyAnalyticsRepository.findEfficientAnalyticsByUserId(testUserId, minScore))
                .thenReturn(analytics);

        // When
        List<EnergyAnalyticsDto> result = analyticsService.getEfficientAnalytics(testUserId, minScore);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(energyAnalyticsRepository).findEfficientAnalyticsByUserId(testUserId, minScore);
    }

    @Test
    void getAnalyticsWithAnomalies_Success_ReturnsList() {
        // Given
        List<EnergyAnalytics> analytics = Arrays.asList(testAnalytics);
        when(energyAnalyticsRepository.findAnalyticsWithAnomaliesByUserId(testUserId))
                .thenReturn(analytics);

        // When
        List<EnergyAnalyticsDto> result = analyticsService.getAnalyticsWithAnomalies(testUserId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(energyAnalyticsRepository).findAnalyticsWithAnomaliesByUserId(testUserId);
    }
}

