package com.smartwatts.deviceservice.service;

import com.smartwatts.deviceservice.model.Device;
import com.smartwatts.deviceservice.repository.DeviceRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceVerificationServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceEventService eventService;

    @InjectMocks
    private DeviceVerificationService deviceVerificationService;

    private Device testDevice;
    private UUID deviceId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        deviceId = UUID.randomUUID();
        userId = UUID.randomUUID();
        
        testDevice = new Device();
        testDevice.setId(deviceId);
        testDevice.setUserId(userId);
        testDevice.setDeviceId("TEST_DEVICE_001");
        testDevice.setName("Test Device");
        testDevice.setManufacturer("Third Party Manufacturer");
        testDevice.setIsVerified(false);
        testDevice.setTrustLevel(Device.TrustLevel.UNVERIFIED);
        testDevice.setVerificationStatus(Device.VerificationStatus.PENDING);
    }

    @Test
    void submitForVerification_Success() {
        // Given
        String samplePayload = "{\"voltage\": 220, \"current\": 5.5}";
        String notes = "Device working well, needs verification";
        
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(testDevice));
        when(deviceRepository.save(any(Device.class))).thenReturn(testDevice);
        doNothing().when(eventService).logDeviceEvent(any(), any(), any());

        // When
        Device result = deviceVerificationService.submitForVerification(deviceId, samplePayload, notes);

        // Then
        assertNotNull(result);
        assertEquals(Device.VerificationStatus.PENDING, result.getVerificationStatus());
        assertEquals(samplePayload, result.getSamplePayload());
        assertEquals(notes, result.getVerificationNotes());
        assertNotNull(result.getVerificationRequestDate());
        
        verify(deviceRepository).save(testDevice);
        verify(eventService).logDeviceEvent(eq(deviceId), any(), any());
    }

    @Test
    void submitForVerification_DeviceNotFound() {
        // Given
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> 
            deviceVerificationService.submitForVerification(deviceId, "payload", "notes"));
        
        verify(deviceRepository, never()).save(any());
    }

    @Test
    void submitForVerification_AlreadyVerified() {
        // Given
        testDevice.setIsVerified(true);
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(testDevice));

        // When & Then
        assertThrows(RuntimeException.class, () -> 
            deviceVerificationService.submitForVerification(deviceId, "payload", "notes"));
        
        verify(deviceRepository, never()).save(any());
    }

    @Test
    void reviewVerification_Approve() {
        // Given
        UUID reviewerId = UUID.randomUUID();
        String notes = "Device approved after testing";
        
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(testDevice));
        when(deviceRepository.save(any(Device.class))).thenReturn(testDevice);
        doNothing().when(eventService).logDeviceEvent(any(), any(), any());

        // When
        Device result = deviceVerificationService.reviewVerification(
            deviceId, 
            Device.VerificationStatus.APPROVED, 
            notes, 
            reviewerId
        );

        // Then
        assertNotNull(result);
        assertTrue(result.getIsVerified());
        assertEquals(Device.VerificationStatus.APPROVED, result.getVerificationStatus());
        assertEquals(notes, result.getVerificationNotes());
        assertEquals(reviewerId, result.getVerificationReviewer());
        assertNotNull(result.getVerificationReviewDate());
        assertNotNull(result.getVerificationDate());
        assertNotNull(result.getDeviceAuthSecret());
        
        verify(deviceRepository).save(testDevice);
        verify(eventService).logDeviceEvent(eq(deviceId), any(), any());
    }

    @Test
    void reviewVerification_Reject() {
        // Given
        UUID reviewerId = UUID.randomUUID();
        String notes = "Device failed verification tests";
        
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(testDevice));
        when(deviceRepository.save(any(Device.class))).thenReturn(testDevice);
        doNothing().when(eventService).logDeviceEvent(any(), any(), any());

        // When
        Device result = deviceVerificationService.reviewVerification(
            deviceId, 
            Device.VerificationStatus.REJECTED, 
            notes, 
            reviewerId
        );

        // Then
        assertNotNull(result);
        assertFalse(result.getIsVerified());
        assertEquals(Device.VerificationStatus.REJECTED, result.getVerificationStatus());
        assertEquals(notes, result.getVerificationNotes());
        assertEquals(reviewerId, result.getVerificationReviewer());
        assertNotNull(result.getVerificationReviewDate());
        assertNull(result.getDeviceAuthSecret());
        
        verify(deviceRepository).save(testDevice);
        verify(eventService).logDeviceEvent(eq(deviceId), any(), any());
    }

    @Test
    void canDeviceSendData_VerifiedDevice() {
        // Given
        testDevice.setIsVerified(true);
        testDevice.setVerificationStatus(Device.VerificationStatus.APPROVED);
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(testDevice));

        // When
        boolean result = deviceVerificationService.canDeviceSendData(deviceId);

        // Then
        assertTrue(result);
    }

    @Test
    void canDeviceSendData_UnverifiedDevice() {
        // Given
        testDevice.setIsVerified(false);
        testDevice.setVerificationStatus(Device.VerificationStatus.PENDING);
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(testDevice));

        // When
        boolean result = deviceVerificationService.canDeviceSendData(deviceId);

        // Then
        assertFalse(result);
    }

    @Test
    void canDeviceSendData_DeviceNotFound() {
        // Given
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        // When
        boolean result = deviceVerificationService.canDeviceSendData(deviceId);

        // Then
        assertFalse(result);
    }

    @Test
    void validateDeviceAuthSecret_ValidSecret() {
        // Given
        String authSecret = "valid-secret-123";
        testDevice.setDeviceAuthSecret(authSecret);
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(testDevice));

        // When
        boolean result = deviceVerificationService.validateDeviceAuthSecret(deviceId, authSecret);

        // Then
        assertTrue(result);
    }

    @Test
    void validateDeviceAuthSecret_InvalidSecret() {
        // Given
        String authSecret = "valid-secret-123";
        testDevice.setDeviceAuthSecret("different-secret");
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(testDevice));

        // When
        boolean result = deviceVerificationService.validateDeviceAuthSecret(deviceId, authSecret);

        // Then
        assertFalse(result);
    }

    @Test
    void validateDeviceAuthSecret_DeviceNotFound() {
        // Given
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        // When
        boolean result = deviceVerificationService.validateDeviceAuthSecret(deviceId, "secret");

        // Then
        assertFalse(result);
    }

    @Test
    void generateDeviceAuthSecret_Unique() {
        // Given
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(testDevice));
        when(deviceRepository.save(any(Device.class))).thenReturn(testDevice);

        // When
        String secret1 = deviceVerificationService.generateDeviceAuthSecret(deviceId);
        String secret2 = deviceVerificationService.generateDeviceAuthSecret(deviceId);

        // Then
        assertNotNull(secret1);
        assertNotNull(secret2);
        assertNotEquals(secret1, secret2);
        assertEquals(32, secret1.length());
        assertEquals(32, secret2.length());
    }
}
