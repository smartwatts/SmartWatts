package com.smartwatts.energyservice.service;

import com.smartwatts.energyservice.dto.EnergyReadingDto;
import com.smartwatts.energyservice.model.EnergyReading;
import com.smartwatts.energyservice.exception.DeviceNotVerifiedException;
import com.smartwatts.energyservice.exception.InvalidDeviceAuthException;
import com.smartwatts.energyservice.repository.EnergyReadingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private DataIngestionSecurityService dataIngestionSecurityService;

    @InjectMocks
    private EnergyService energyService;

    private EnergyReadingDto testReadingDto;
    private EnergyReading testReading;
    private String deviceId;
    private String deviceAuthSecret;

    @BeforeEach
    void setUp() {
        deviceId = UUID.randomUUID().toString();
        deviceAuthSecret = "test-auth-secret-123";
        
        testReadingDto = EnergyReadingDto.builder()
                .deviceId(deviceId)
                .readingTimestamp(LocalDateTime.now())
                .voltage(new BigDecimal("220.5"))
                .current(new BigDecimal("5.2"))
                .power(new BigDecimal("1146.6"))
                .energyConsumed(new BigDecimal("2.3"))
                .frequency(new BigDecimal("50.0"))
                .powerFactor(new BigDecimal("0.95"))
                .build();

        testReading = new EnergyReading();
        testReading.setId(UUID.randomUUID());
        testReading.setDeviceId(deviceId);
        testReading.setReadingTimestamp(LocalDateTime.now());
        testReading.setVoltage(new BigDecimal("220.5"));
        testReading.setCurrent(new BigDecimal("5.2"));
        testReading.setPower(new BigDecimal("1146.6"));
        testReading.setEnergyConsumed(new BigDecimal("2.3"));
        testReading.setFrequency(new BigDecimal("50.0"));
        testReading.setPowerFactor(new BigDecimal("0.95"));
    }

    @Test
    void saveEnergyReading_Success() {
        // Mock security validation passes
        doNothing().when(dataIngestionSecurityService)
                .validateDeviceDataIngestion(testReadingDto, null);

        // Mock repository save
        when(energyReadingRepository.save(any(EnergyReading.class)))
                .thenReturn(testReading);

        EnergyReadingDto result = energyService.saveEnergyReading(testReadingDto);

        assertNotNull(result);
        assertEquals(deviceId, result.getDeviceId());
        assertEquals(testReadingDto.getVoltage(), result.getVoltage());
        assertEquals(testReadingDto.getCurrent(), result.getCurrent());
        assertEquals(testReadingDto.getPower(), result.getPower());

        verify(dataIngestionSecurityService, times(1))
                .validateDeviceDataIngestion(testReadingDto, null);
        verify(energyReadingRepository, times(1)).save(any(EnergyReading.class));
    }

    @Test
    void saveEnergyReading_DeviceNotVerified() {
        // Mock security validation fails
        doThrow(new DeviceNotVerifiedException(deviceId))
                .when(dataIngestionSecurityService)
                .validateDeviceDataIngestion(testReadingDto, null);

        // Should throw DeviceNotVerifiedException
        DeviceNotVerifiedException exception = assertThrows(
            DeviceNotVerifiedException.class,
            () -> energyService.saveEnergyReading(testReadingDto)
        );

        assertTrue(exception.getMessage().contains(deviceId));
        verify(dataIngestionSecurityService, times(1))
                .validateDeviceDataIngestion(testReadingDto, null);
        verify(energyReadingRepository, never()).save(any(EnergyReading.class));
    }

    @Test
    void saveEnergyReading_InvalidDeviceAuth() {
        // Mock security validation fails with invalid auth
        doThrow(new InvalidDeviceAuthException(deviceId))
                .when(dataIngestionSecurityService)
                .validateDeviceDataIngestion(testReadingDto, null);

        // Should throw InvalidDeviceAuthException
        InvalidDeviceAuthException exception = assertThrows(
            InvalidDeviceAuthException.class,
            () -> energyService.saveEnergyReading(testReadingDto)
        );

        assertTrue(exception.getMessage().contains(deviceId));
        verify(dataIngestionSecurityService, times(1))
                .validateDeviceDataIngestion(testReadingDto, null);
        verify(energyReadingRepository, never()).save(any(EnergyReading.class));
    }

    @Test
    void saveEnergyReadingWithAuth_Success() {
        // Mock security validation passes
        doNothing().when(dataIngestionSecurityService)
                .validateDeviceDataIngestion(testReadingDto, deviceAuthSecret);

        // Mock repository save
        when(energyReadingRepository.save(any(EnergyReading.class)))
                .thenReturn(testReading);

        EnergyReadingDto result = energyService.saveEnergyReadingWithAuth(testReadingDto, deviceAuthSecret);

        assertNotNull(result);
        assertEquals(deviceId, result.getDeviceId());
        assertEquals(testReadingDto.getVoltage(), result.getVoltage());
        assertEquals(testReadingDto.getCurrent(), result.getCurrent());
        assertEquals(testReadingDto.getPower(), result.getPower());

        verify(dataIngestionSecurityService, times(1))
                .validateDeviceDataIngestion(testReadingDto, deviceAuthSecret);
        verify(energyReadingRepository, times(1)).save(any(EnergyReading.class));
    }

    @Test
    void saveEnergyReadingWithAuth_DeviceNotVerified() {
        // Mock security validation fails
        doThrow(new DeviceNotVerifiedException(deviceId))
                .when(dataIngestionSecurityService)
                .validateDeviceDataIngestion(testReadingDto, deviceAuthSecret);

        // Should throw DeviceNotVerifiedException
        DeviceNotVerifiedException exception = assertThrows(
            DeviceNotVerifiedException.class,
            () -> energyService.saveEnergyReadingWithAuth(testReadingDto, deviceAuthSecret)
        );

        assertTrue(exception.getMessage().contains(deviceId));
        verify(dataIngestionSecurityService, times(1))
                .validateDeviceDataIngestion(testReadingDto, deviceAuthSecret);
        verify(energyReadingRepository, never()).save(any(EnergyReading.class));
    }

    @Test
    void saveEnergyReadingWithAuth_InvalidDeviceAuth() {
        // Mock security validation fails with invalid auth
        doThrow(new InvalidDeviceAuthException(deviceId))
                .when(dataIngestionSecurityService)
                .validateDeviceDataIngestion(testReadingDto, deviceAuthSecret);

        // Should throw InvalidDeviceAuthException
        InvalidDeviceAuthException exception = assertThrows(
            InvalidDeviceAuthException.class,
            () -> energyService.saveEnergyReadingWithAuth(testReadingDto, deviceAuthSecret)
        );

        assertTrue(exception.getMessage().contains(deviceId));
        verify(dataIngestionSecurityService, times(1))
                .validateDeviceDataIngestion(testReadingDto, deviceAuthSecret);
        verify(energyReadingRepository, never()).save(any(EnergyReading.class));
    }

    @Test
    void saveEnergyReadingWithAuth_NullAuthSecret() {
        // Mock security validation passes (null auth secret is handled by security service)
        doNothing().when(dataIngestionSecurityService)
                .validateDeviceDataIngestion(testReadingDto, null);

        // Mock repository save
        when(energyReadingRepository.save(any(EnergyReading.class)))
                .thenReturn(testReading);

        EnergyReadingDto result = energyService.saveEnergyReadingWithAuth(testReadingDto, null);

        assertNotNull(result);
        assertEquals(deviceId, result.getDeviceId());

        verify(dataIngestionSecurityService, times(1))
                .validateDeviceDataIngestion(testReadingDto, null);
        verify(energyReadingRepository, times(1)).save(any(EnergyReading.class));
    }

    @Test
    void saveEnergyReadingWithAuth_EmptyAuthSecret() {
        // Mock security validation passes (empty auth secret is handled by security service)
        doNothing().when(dataIngestionSecurityService)
                .validateDeviceDataIngestion(testReadingDto, "");

        // Mock repository save
        when(energyReadingRepository.save(any(EnergyReading.class)))
                .thenReturn(testReading);

        EnergyReadingDto result = energyService.saveEnergyReadingWithAuth(testReadingDto, "");

        assertNotNull(result);
        assertEquals(deviceId, result.getDeviceId());

        verify(dataIngestionSecurityService, times(1))
                .validateDeviceDataIngestion(testReadingDto, "");
        verify(energyReadingRepository, times(1)).save(any(EnergyReading.class));
    }



    @Test
    void getEnergyReadingById_Success() {
        when(energyReadingRepository.findById(testReading.getId()))
                .thenReturn(Optional.of(testReading));

        EnergyReadingDto result = energyService.getEnergyReadingById(testReading.getId());

        assertNotNull(result);
        assertEquals(testReading.getId(), result.getId());
        assertEquals(deviceId, result.getDeviceId());
        verify(energyReadingRepository, times(1)).findById(testReading.getId());
    }

    @Test
    void getEnergyReadingById_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(energyReadingRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            energyService.getEnergyReadingById(nonExistentId)
        );
        verify(energyReadingRepository, times(1)).findById(nonExistentId);
    }






}
