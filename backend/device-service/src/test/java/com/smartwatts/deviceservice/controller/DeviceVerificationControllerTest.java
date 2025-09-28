package com.smartwatts.deviceservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartwatts.deviceservice.dto.DeviceVerificationRequestDto;
import com.smartwatts.deviceservice.dto.DeviceVerificationReviewDto;
import com.smartwatts.deviceservice.model.Device;
import com.smartwatts.deviceservice.service.DeviceVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DeviceVerificationControllerTest {

    @Mock
    private DeviceVerificationService deviceVerificationService;

    @InjectMocks
    private DeviceVerificationController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Device testDevice;
    private DeviceVerificationRequestDto verificationRequest;
    private DeviceVerificationReviewDto reviewRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();

        testDevice = new Device();
        testDevice.setId(UUID.randomUUID());
        testDevice.setDeviceId("TEST_DEVICE_001");
        testDevice.setName("Test Smart Meter");
        testDevice.setDeviceType(Device.DeviceType.SMART_METER);
        testDevice.setManufacturer("Test Manufacturer");
        testDevice.setModel("Test Model 2024");
        testDevice.setSerialNumber("TSM001");
        testDevice.setUserId(UUID.randomUUID());
        testDevice.setLocationLat(new java.math.BigDecimal("6.5244"));
        testDevice.setLocationLng(new java.math.BigDecimal("3.3792"));
        testDevice.setProtocol(Device.Protocol.MQTT);
        testDevice.setConnectionStatus(Device.ConnectionStatus.ONLINE);
        testDevice.setIsVerified(false);
        testDevice.setVerificationStatus(Device.VerificationStatus.PENDING);
        testDevice.setTrustLevel(Device.TrustLevel.UNVERIFIED);

        verificationRequest = new DeviceVerificationRequestDto();
        verificationRequest.setDeviceId(UUID.randomUUID());
        verificationRequest.setSamplePayload("{\"voltage\": 220.5, \"current\": 5.2}");
        verificationRequest.setNotes("Test device for verification");
        verificationRequest.setBrand("Test Brand");
        verificationRequest.setModel("Test Model");
        verificationRequest.setPreferredProtocol("MQTT");

        reviewRequest = new DeviceVerificationReviewDto();
        reviewRequest.setDeviceId(UUID.randomUUID());
        reviewRequest.setVerificationStatus("APPROVED");
        reviewRequest.setNotes("Device approved after review");
        reviewRequest.setReviewerId(UUID.randomUUID());
    }

    @Test
    void submitForVerification_Success() throws Exception {
        when(deviceVerificationService.submitForVerification(any(UUID.class), anyString(), anyString()))
                .thenReturn(testDevice);

        mockMvc.perform(post("/api/v1/devices/verification/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verificationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId").value("TEST_DEVICE_001"))
                .andExpect(jsonPath("$.verificationStatus").value("PENDING"))
                .andExpect(jsonPath("$.trustLevel").value("UNVERIFIED"));

        verify(deviceVerificationService, times(1))
                .submitForVerification(any(UUID.class), anyString(), anyString());
    }

    @Test
    void submitForVerification_InvalidRequest() throws Exception {
        DeviceVerificationRequestDto invalidRequest = new DeviceVerificationRequestDto();
        // Missing required fields

        mockMvc.perform(post("/api/v1/devices/verification/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDevicesPendingVerification_Success() throws Exception {
        List<Device> pendingDevices = Arrays.asList(testDevice);
        when(deviceVerificationService.getDevicesPendingVerification())
                .thenReturn(pendingDevices);

        mockMvc.perform(get("/api/v1/devices/verification/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].deviceId").value("TEST_DEVICE_001"))
                .andExpect(jsonPath("$[0].verificationStatus").value("PENDING"))
                .andExpect(jsonPath("$[0].trustLevel").value("UNVERIFIED"));

        verify(deviceVerificationService, times(1)).getDevicesPendingVerification();
    }

    @Test
    void getDevicesPendingVerification_EmptyList() throws Exception {
        when(deviceVerificationService.getDevicesPendingVerification())
                .thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/v1/devices/verification/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(deviceVerificationService, times(1)).getDevicesPendingVerification();
    }

    @Test
    void getDevicesUnderReview_Success() throws Exception {
        Device underReviewDevice = new Device();
        underReviewDevice.setId(testDevice.getId());
        underReviewDevice.setDeviceId(testDevice.getDeviceId());
        underReviewDevice.setName(testDevice.getName());
        underReviewDevice.setDeviceType(testDevice.getDeviceType());
        underReviewDevice.setManufacturer(testDevice.getManufacturer());
        underReviewDevice.setModel(testDevice.getModel());
        underReviewDevice.setSerialNumber(testDevice.getSerialNumber());
        underReviewDevice.setUserId(testDevice.getUserId());
        underReviewDevice.setLocationLat(testDevice.getLocationLat());
        underReviewDevice.setLocationLng(testDevice.getLocationLng());
        underReviewDevice.setProtocol(testDevice.getProtocol());
        underReviewDevice.setConnectionStatus(testDevice.getConnectionStatus());
        underReviewDevice.setIsVerified(testDevice.getIsVerified());
        underReviewDevice.setVerificationStatus(Device.VerificationStatus.UNDER_REVIEW);
        underReviewDevice.setTrustLevel(testDevice.getTrustLevel());
        List<Device> underReviewDevices = Arrays.asList(underReviewDevice);
        
        when(deviceVerificationService.getDevicesUnderReview())
                .thenReturn(underReviewDevices);

        mockMvc.perform(get("/api/v1/devices/verification/under-review"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].deviceId").value("TEST_DEVICE_001"))
                .andExpect(jsonPath("$[0].verificationStatus").value("UNDER_REVIEW"));

        verify(deviceVerificationService, times(1)).getDevicesUnderReview();
    }

    @Test
    void reviewVerification_Success() throws Exception {
        Device approvedDevice = new Device();
        approvedDevice.setId(testDevice.getId());
        approvedDevice.setDeviceId(testDevice.getDeviceId());
        approvedDevice.setName(testDevice.getName());
        approvedDevice.setDeviceType(testDevice.getDeviceType());
        approvedDevice.setManufacturer(testDevice.getManufacturer());
        approvedDevice.setModel(testDevice.getModel());
        approvedDevice.setSerialNumber(testDevice.getSerialNumber());
        approvedDevice.setUserId(testDevice.getUserId());
        approvedDevice.setLocationLat(testDevice.getLocationLat());
        approvedDevice.setLocationLng(testDevice.getLocationLng());
        approvedDevice.setProtocol(testDevice.getProtocol());
        approvedDevice.setConnectionStatus(testDevice.getConnectionStatus());
        approvedDevice.setIsVerified(true);
        approvedDevice.setVerificationStatus(Device.VerificationStatus.APPROVED);
        approvedDevice.setVerificationDate(LocalDateTime.now());
        approvedDevice.setTrustLevel(testDevice.getTrustLevel());
        
        when(deviceVerificationService.reviewVerification(any(UUID.class), any(Device.VerificationStatus.class), anyString(), any(UUID.class)))
                .thenReturn(approvedDevice);

        mockMvc.perform(post("/api/v1/devices/verification/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("APPROVED"))
                .andExpect(jsonPath("$.isVerified").value(true));

        verify(deviceVerificationService, times(1))
                .reviewVerification(any(UUID.class), any(Device.VerificationStatus.class), anyString(), any(UUID.class));
    }

    @Test
    void reviewVerification_InvalidRequest() throws Exception {
        DeviceVerificationReviewDto invalidRequest = new DeviceVerificationReviewDto();
        // Missing required fields

        mockMvc.perform(post("/api/v1/devices/verification/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void markDeviceUnderReview_Success() throws Exception {
        Device underReviewDevice = new Device();
        underReviewDevice.setId(testDevice.getId());
        underReviewDevice.setDeviceId(testDevice.getDeviceId());
        underReviewDevice.setName(testDevice.getName());
        underReviewDevice.setDeviceType(testDevice.getDeviceType());
        underReviewDevice.setManufacturer(testDevice.getManufacturer());
        underReviewDevice.setModel(testDevice.getModel());
        underReviewDevice.setSerialNumber(testDevice.getSerialNumber());
        underReviewDevice.setUserId(testDevice.getUserId());
        underReviewDevice.setLocationLat(testDevice.getLocationLat());
        underReviewDevice.setLocationLng(testDevice.getLocationLng());
        underReviewDevice.setProtocol(testDevice.getProtocol());
        underReviewDevice.setConnectionStatus(testDevice.getConnectionStatus());
        underReviewDevice.setIsVerified(testDevice.getIsVerified());
        underReviewDevice.setVerificationStatus(Device.VerificationStatus.UNDER_REVIEW);
        underReviewDevice.setTrustLevel(testDevice.getTrustLevel());
        
        when(deviceVerificationService.markUnderReview(any(UUID.class), any(UUID.class)))
                .thenReturn(underReviewDevice);

        mockMvc.perform(post("/api/v1/devices/verification/TEST_DEVICE_001/mark-under-review"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("UNDER_REVIEW"));

        verify(deviceVerificationService, times(1)).markUnderReview(any(UUID.class), any(UUID.class));
    }

    @Test
    void canDeviceSendData_Success() throws Exception {
        when(deviceVerificationService.canDeviceSendData(any(UUID.class)))
                .thenReturn(true);

        mockMvc.perform(get("/api/v1/devices/verification/TEST_DEVICE_001/can-send-data"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(deviceVerificationService, times(1)).canDeviceSendData(any(UUID.class));
    }

    @Test
    void canDeviceSendData_False() throws Exception {
        when(deviceVerificationService.canDeviceSendData(any(UUID.class)))
                .thenReturn(false);

        mockMvc.perform(get("/api/v1/devices/verification/TEST_DEVICE_001/can-send-data"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(deviceVerificationService, times(1)).canDeviceSendData(any(UUID.class));
    }

    @Test
    void validateDeviceAuthSecret_Success() throws Exception {
        when(deviceVerificationService.validateDeviceAuthSecret(any(UUID.class), anyString()))
                .thenReturn(true);

        mockMvc.perform(post("/api/v1/devices/verification/validate-auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"deviceId\": \"TEST_DEVICE_001\", \"authSecret\": \"valid-secret\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(deviceVerificationService, times(1))
                .validateDeviceAuthSecret(any(UUID.class), anyString());
    }

    @Test
    void validateDeviceAuthSecret_InvalidSecret() throws Exception {
        when(deviceVerificationService.validateDeviceAuthSecret(any(UUID.class), anyString()))
                .thenReturn(false);

        mockMvc.perform(post("/api/v1/devices/verification/validate-auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"deviceId\": \"TEST_DEVICE_001\", \"authSecret\": \"invalid-secret\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(deviceVerificationService, times(1))
                .validateDeviceAuthSecret(any(UUID.class), anyString());
    }

    @Test
    void validateDeviceAuthSecret_InvalidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/devices/verification/validate-auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"deviceId\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDeviceVerificationStatus_Success() throws Exception {
        when(deviceVerificationService.getDeviceVerificationStatus("TEST_DEVICE_001"))
                .thenReturn("APPROVED");

        mockMvc.perform(get("/api/v1/devices/verification/TEST_DEVICE_001/status"))
                .andExpect(status().isOk())
                .andExpect(content().string("APPROVED"));

        verify(deviceVerificationService, times(1)).getDeviceVerificationStatus("TEST_DEVICE_001");
    }

    @Test
    void getDeviceVerificationStatus_DeviceNotFound() throws Exception {
        when(deviceVerificationService.getDeviceVerificationStatus("NONEXISTENT_DEVICE"))
                .thenReturn(null);

        mockMvc.perform(get("/api/v1/devices/verification/NONEXISTENT_DEVICE/status"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(deviceVerificationService, times(1)).getDeviceVerificationStatus("NONEXISTENT_DEVICE");
    }

    @Test
    void getVerificationStats_Success() throws Exception {
        // Mock verification statistics
        when(deviceVerificationService.getDevicesPendingVerification())
                .thenReturn(Arrays.asList(testDevice));
        when(deviceVerificationService.getDevicesUnderReview())
                .thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/v1/devices/verification/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingCount").value(1))
                .andExpect(jsonPath("$.underReviewCount").value(0));

        verify(deviceVerificationService, times(1)).getDevicesPendingVerification();
        verify(deviceVerificationService, times(1)).getDevicesUnderReview();
    }

    @Test
    void getVerificationStats_EmptyStats() throws Exception {
        when(deviceVerificationService.getDevicesPendingVerification())
                .thenReturn(Arrays.asList());
        when(deviceVerificationService.getDevicesUnderReview())
                .thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/v1/devices/verification/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingCount").value(0))
                .andExpect(jsonPath("$.underReviewCount").value(0));

        verify(deviceVerificationService, times(1)).getDevicesPendingVerification();
        verify(deviceVerificationService, times(1)).getDevicesUnderReview();
    }

    @Test
    void generateDeviceAuthSecret_Success() throws Exception {
        String generatedSecret = "generated-secret-123";
        when(deviceVerificationService.generateDeviceAuthSecret(any(UUID.class)))
                .thenReturn(generatedSecret);

        mockMvc.perform(post("/api/v1/devices/verification/TEST_DEVICE_001/generate-auth-secret"))
                .andExpect(status().isOk())
                .andExpect(content().string(generatedSecret));

        verify(deviceVerificationService, times(1)).generateDeviceAuthSecret(any(UUID.class));
    }

    @Test
    void getDeviceVerificationDetails_Success() throws Exception {
        when(deviceVerificationService.getDeviceVerificationDetails("TEST_DEVICE_001"))
                .thenReturn(testDevice);

        mockMvc.perform(get("/api/v1/devices/verification/TEST_DEVICE_001/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId").value("TEST_DEVICE_001"))
                .andExpect(jsonPath("$.verificationStatus").value("PENDING"))
                .andExpect(jsonPath("$.trustLevel").value("UNVERIFIED"));

        verify(deviceVerificationService, times(1)).getDeviceVerificationDetails("TEST_DEVICE_001");
    }

    @Test
    void getDeviceVerificationDetails_DeviceNotFound() throws Exception {
        when(deviceVerificationService.getDeviceVerificationDetails("NONEXISTENT_DEVICE"))
                .thenReturn(null);

        mockMvc.perform(get("/api/v1/devices/verification/NONEXISTENT_DEVICE/details"))
                .andExpect(status().isNotFound());

        verify(deviceVerificationService, times(1)).getDeviceVerificationDetails("NONEXISTENT_DEVICE");
    }
}
