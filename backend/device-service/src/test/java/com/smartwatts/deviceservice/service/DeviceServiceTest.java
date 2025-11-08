package com.smartwatts.deviceservice.service;

import com.smartwatts.deviceservice.dto.DeviceDto;
import com.smartwatts.deviceservice.model.Device;
import com.smartwatts.deviceservice.repository.DeviceRepository;
import com.smartwatts.deviceservice.repository.DeviceConfigurationRepository;
import com.smartwatts.deviceservice.repository.DeviceEventRepository;
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
class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceConfigurationRepository deviceConfigurationRepository;

    @Mock
    private DeviceEventRepository deviceEventRepository;

    @Mock
    private EventService eventService;

    @InjectMocks
    private DeviceService deviceService;

    private Device testDevice;
    private DeviceDto testDeviceDto;
    private UUID testDeviceId;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testDeviceId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        
        testDevice = new Device();
        testDevice.setId(testDeviceId);
        testDevice.setUserId(testUserId);
        testDevice.setDeviceId("DEVICE-001");
        testDevice.setName("Test Device");
        testDevice.setDeviceType(Device.DeviceType.SMART_METER);
        testDevice.setProtocol(Device.Protocol.MQTT);
        testDevice.setStatus(Device.DeviceStatus.ACTIVE);
        testDevice.setConnectionStatus(Device.ConnectionStatus.ONLINE);
        testDevice.setLocationLat(new BigDecimal("6.5244"));
        testDevice.setLocationLng(new BigDecimal("3.3792"));
        testDevice.setInstallationDate(LocalDateTime.now());
        
        testDeviceDto = DeviceDto.builder()
                .id(testDeviceId)
                .userId(testUserId)
                .deviceId("DEVICE-001")
                .name("Test Device")
                .deviceType(Device.DeviceType.SMART_METER)
                .protocol(Device.Protocol.MQTT)
                .build();
    }

    @Test
    void registerDevice_Success_ReturnsDeviceDto() {
        // Given
        when(deviceRepository.existsByDeviceId("DEVICE-001")).thenReturn(false);
        when(deviceRepository.existsByUserIdAndDeviceId(testUserId, "DEVICE-001")).thenReturn(false);
        when(deviceRepository.save(any(Device.class))).thenReturn(testDevice);

        // When
        DeviceDto result = deviceService.registerDevice(testDeviceDto);

        // Then
        assertNotNull(result);
        assertEquals("DEVICE-001", result.getDeviceId());
        verify(deviceRepository).save(any(Device.class));
        verify(eventService).createDeviceEvent(any(), any(), any(), anyString(), anyString());
    }

    @Test
    void registerDevice_DeviceExists_ThrowsException() {
        // Given
        when(deviceRepository.existsByDeviceId("DEVICE-001")).thenReturn(true);

        // When & Then
        assertThrows(RuntimeException.class, () -> deviceService.registerDevice(testDeviceDto));
        verify(deviceRepository, never()).save(any(Device.class));
    }

    @Test
    void getDeviceById_Success_ReturnsDeviceDto() {
        // Given
        when(deviceRepository.findById(testDeviceId)).thenReturn(Optional.of(testDevice));

        // When
        DeviceDto result = deviceService.getDeviceById(testDeviceId);

        // Then
        assertNotNull(result);
        assertEquals(testDeviceId, result.getId());
        assertEquals("Test Device", result.getName());
    }

    @Test
    void getDeviceById_NotFound_ThrowsException() {
        // Given
        when(deviceRepository.findById(testDeviceId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> deviceService.getDeviceById(testDeviceId));
    }

    @Test
    void getAllDevices_Success_ReturnsPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Device> page = new PageImpl<>(Arrays.asList(testDevice));
        when(deviceRepository.findAll(pageable)).thenReturn(page);

        // When
        Page<DeviceDto> result = deviceService.getAllDevices(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(deviceRepository).findAll(pageable);
    }

    @Test
    void getDevicesByUserId_Success_ReturnsPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Device> page = new PageImpl<>(Arrays.asList(testDevice));
        when(deviceRepository.findByUserId(testUserId, pageable)).thenReturn(page);

        // When
        Page<DeviceDto> result = deviceService.getDevicesByUserId(testUserId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(deviceRepository).findByUserId(testUserId, pageable);
    }

    @Test
    void updateDevice_Success_ReturnsUpdatedDeviceDto() {
        // Given
        testDeviceDto.setName("Updated Device");
        when(deviceRepository.findById(testDeviceId)).thenReturn(Optional.of(testDevice));
        when(deviceRepository.save(any(Device.class))).thenReturn(testDevice);

        // When
        DeviceDto result = deviceService.updateDevice(testDeviceId, testDeviceDto);

        // Then
        assertNotNull(result);
        verify(deviceRepository).findById(testDeviceId);
        verify(deviceRepository).save(any(Device.class));
    }

    @Test
    void updateDevice_NotFound_ThrowsException() {
        // Given
        when(deviceRepository.findById(testDeviceId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> deviceService.updateDevice(testDeviceId, testDeviceDto));
        verify(deviceRepository, never()).save(any(Device.class));
    }

    @Test
    void deleteDevice_Success_DeletesDevice() {
        // Given
        when(deviceRepository.findById(testDeviceId)).thenReturn(Optional.of(testDevice));
        doNothing().when(deviceRepository).delete(testDevice);

        // When
        deviceService.deleteDevice(testDeviceId);

        // Then
        verify(deviceRepository).findById(testDeviceId);
        verify(deviceRepository).delete(testDevice);
    }

    @Test
    void getActiveDevicesByUserId_Success_ReturnsList() {
        // Given
        LocalDateTime since = LocalDateTime.now().minusMinutes(5);
        List<Device> devices = Arrays.asList(testDevice);
        when(deviceRepository.findActiveDevicesByUserId(testUserId, since)).thenReturn(devices);

        // When
        List<DeviceDto> result = deviceService.getActiveDevicesByUserId(testUserId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(deviceRepository).findActiveDevicesByUserId(eq(testUserId), any(LocalDateTime.class));
    }

    @Test
    void updateDeviceStatus_Success_ReturnsUpdatedDeviceDto() {
        // Given
        when(deviceRepository.findById(testDeviceId)).thenReturn(Optional.of(testDevice));
        testDevice.setStatus(Device.DeviceStatus.INACTIVE);
        when(deviceRepository.save(any(Device.class))).thenReturn(testDevice);

        // When
        DeviceDto result = deviceService.updateDeviceStatus(testDeviceId, Device.DeviceStatus.INACTIVE);

        // Then
        assertNotNull(result);
        verify(deviceRepository).findById(testDeviceId);
        verify(deviceRepository).save(any(Device.class));
    }

    @Test
    void updateConnectionStatus_Success_ReturnsUpdatedDeviceDto() {
        // Given
        when(deviceRepository.findById(testDeviceId)).thenReturn(Optional.of(testDevice));
        testDevice.setConnectionStatus(Device.ConnectionStatus.OFFLINE);
        when(deviceRepository.save(any(Device.class))).thenReturn(testDevice);

        // When
        DeviceDto result = deviceService.updateConnectionStatus(testDeviceId, Device.ConnectionStatus.OFFLINE);

        // Then
        assertNotNull(result);
        verify(deviceRepository).findById(testDeviceId);
        verify(deviceRepository).save(any(Device.class));
    }
}

