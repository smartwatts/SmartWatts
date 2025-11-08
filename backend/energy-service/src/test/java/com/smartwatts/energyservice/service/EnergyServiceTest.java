package com.smartwatts.energyservice.service;

import com.smartwatts.energyservice.dto.EnergyReadingDto;
import com.smartwatts.energyservice.dto.EnergyConsumptionDto;
import com.smartwatts.energyservice.model.EnergyReading;
import com.smartwatts.energyservice.model.EnergyConsumption;
import com.smartwatts.energyservice.repository.EnergyReadingRepository;
import com.smartwatts.energyservice.repository.EnergyConsumptionRepository;
import com.smartwatts.energyservice.repository.EnergyAlertRepository;
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
class EnergyServiceTest {

    @Mock
    private EnergyReadingRepository energyReadingRepository;

    @Mock
    private EnergyConsumptionRepository energyConsumptionRepository;

    @Mock
    private EnergyAlertRepository energyAlertRepository;

    @Mock
    private AlertService alertService;

    @Mock
    private DataIngestionSecurityService dataIngestionSecurityService;

    @Mock
    private DiscoMonitoringService discoMonitoringService;

    @InjectMocks
    private EnergyService energyService;

    private EnergyReading testReading;
    private EnergyReadingDto testReadingDto;
    private UUID testReadingId;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testReadingId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        
        testReading = new EnergyReading();
        testReading.setId(testReadingId);
        testReading.setUserId(testUserId);
        testReading.setDeviceId("DEVICE-001");
        testReading.setVoltage(new BigDecimal("220.0"));
        testReading.setCurrent(new BigDecimal("10.5"));
        testReading.setPower(new BigDecimal("2310.0"));
        testReading.setEnergyConsumed(new BigDecimal("100.0"));
        testReading.setReadingTimestamp(LocalDateTime.now());
        testReading.setSourceType(EnergyReading.EnergySource.GRID);
        testReading.setReadingType(EnergyReading.ReadingType.REAL_TIME);
        
        testReadingDto = EnergyReadingDto.builder()
                .id(testReadingId)
                .userId(testUserId)
                .deviceId("DEVICE-001")
                .voltage(new BigDecimal("220.0"))
                .current(new BigDecimal("10.5"))
                .power(new BigDecimal("2310.0"))
                .energyConsumed(new BigDecimal("100.0"))
                .readingTimestamp(LocalDateTime.now())
                .build();
    }

    @Test
    void saveEnergyReading_Success_ReturnsReadingDto() {
        // Given
        doNothing().when(dataIngestionSecurityService).validateDeviceDataIngestion(any(), any());
        when(energyReadingRepository.save(any(EnergyReading.class))).thenReturn(testReading);
        doNothing().when(alertService).checkForAlerts(any(EnergyReading.class));

        // When
        EnergyReadingDto result = energyService.saveEnergyReading(testReadingDto);

        // Then
        assertNotNull(result);
        assertEquals(testReadingId, result.getId());
        verify(energyReadingRepository).save(any(EnergyReading.class));
        verify(alertService).checkForAlerts(any(EnergyReading.class));
    }

    @Test
    void saveEnergyReading_CalculatesPower_WhenNotProvided() {
        // Given
        testReadingDto.setPower(null);
        doNothing().when(dataIngestionSecurityService).validateDeviceDataIngestion(any(), any());
        when(energyReadingRepository.save(any(EnergyReading.class))).thenReturn(testReading);
        doNothing().when(alertService).checkForAlerts(any(EnergyReading.class));

        // When
        EnergyReadingDto result = energyService.saveEnergyReading(testReadingDto);

        // Then
        assertNotNull(result);
        verify(energyReadingRepository).save(any(EnergyReading.class));
    }

    @Test
    void saveEnergyReadingWithAuth_Success_ReturnsReadingDto() {
        // Given
        String deviceAuthSecret = "secret-token";
        doNothing().when(dataIngestionSecurityService).validateDeviceDataIngestion(any(), eq(deviceAuthSecret));
        when(energyReadingRepository.save(any(EnergyReading.class))).thenReturn(testReading);
        doNothing().when(alertService).checkForAlerts(any(EnergyReading.class));

        // When
        EnergyReadingDto result = energyService.saveEnergyReadingWithAuth(testReadingDto, deviceAuthSecret);

        // Then
        assertNotNull(result);
        verify(dataIngestionSecurityService).validateDeviceDataIngestion(any(), eq(deviceAuthSecret));
        verify(energyReadingRepository).save(any(EnergyReading.class));
    }

    @Test
    void getEnergyReadingById_Success_ReturnsReadingDto() {
        // Given
        when(energyReadingRepository.findById(testReadingId)).thenReturn(Optional.of(testReading));

        // When
        EnergyReadingDto result = energyService.getEnergyReadingById(testReadingId);

        // Then
        assertNotNull(result);
        assertEquals(testReadingId, result.getId());
        verify(energyReadingRepository).findById(testReadingId);
    }

    @Test
    void getEnergyReadingById_NotFound_ThrowsException() {
        // Given
        when(energyReadingRepository.findById(testReadingId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> energyService.getEnergyReadingById(testReadingId));
    }

    @Test
    void getAllEnergyReadings_Success_ReturnsPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<EnergyReading> page = new PageImpl<>(Arrays.asList(testReading));
        when(energyReadingRepository.findAll(pageable)).thenReturn(page);

        // When
        Page<EnergyReadingDto> result = energyService.getAllEnergyReadings(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(energyReadingRepository).findAll(pageable);
    }

    @Test
    void getEnergyReadingsByUserId_Success_ReturnsPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<EnergyReading> page = new PageImpl<>(Arrays.asList(testReading));
        when(energyReadingRepository.findByUserId(testUserId, pageable)).thenReturn(page);

        // When
        Page<EnergyReadingDto> result = energyService.getEnergyReadingsByUserId(testUserId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(energyReadingRepository).findByUserId(testUserId, pageable);
    }

    @Test
    void getEnergyReadingsByUserIdAndTimeRange_Success_ReturnsList() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();
        List<EnergyReading> readings = Arrays.asList(testReading);
        when(energyReadingRepository.findByUserIdAndReadingTimestampBetween(testUserId, startTime, endTime))
                .thenReturn(readings);

        // When
        List<EnergyReadingDto> result = energyService.getEnergyReadingsByUserIdAndTimeRange(testUserId, startTime, endTime);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(energyReadingRepository).findByUserIdAndReadingTimestampBetween(testUserId, startTime, endTime);
    }

    @Test
    void aggregateEnergyConsumption_Success_ReturnsConsumptionDto() {
        // Given
        EnergyConsumption consumption = new EnergyConsumption();
        consumption.setUserId(testUserId);
        consumption.setDeviceId("DEVICE-001");
        consumption.setTotalEnergy(new BigDecimal("1000.0"));
        consumption.setPeriodType(EnergyConsumption.PeriodType.DAY);
        
        when(energyConsumptionRepository.save(any(EnergyConsumption.class))).thenReturn(consumption);

        // When
        EnergyConsumptionDto result = energyService.aggregateEnergyConsumption(
                testUserId, "DEVICE-001", EnergyConsumption.PeriodType.DAY, 
                LocalDateTime.now().minusDays(1), LocalDateTime.now());

        // Then
        assertNotNull(result);
        verify(energyConsumptionRepository).save(any(EnergyConsumption.class));
    }

}
