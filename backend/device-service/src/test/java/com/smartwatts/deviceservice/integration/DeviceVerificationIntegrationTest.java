package com.smartwatts.deviceservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartwatts.deviceservice.dto.DeviceVerificationRequestDto;
import com.smartwatts.deviceservice.dto.DeviceVerificationReviewDto;
import com.smartwatts.deviceservice.model.Device;
import com.smartwatts.deviceservice.repository.DeviceRepository;
import com.smartwatts.deviceservice.service.DeviceVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;


import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DeviceVerificationIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private DeviceVerificationService deviceVerificationService; // Used for test setup
    
    /**
     * Get device verification service for test setup
     */
    public DeviceVerificationService getDeviceVerificationService() {
        return deviceVerificationService;
    }

    @Autowired
    private DeviceRepository deviceRepository;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Device testDevice;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        
        testUserId = UUID.randomUUID();
        
        // Create a test device
        testDevice = new Device();
        testDevice.setDeviceId("INTEGRATION_TEST_001");
        testDevice.setName("Integration Test Smart Meter");
        testDevice.setDeviceType(Device.DeviceType.SMART_METER);
        testDevice.setManufacturer("Test Manufacturer");
        testDevice.setModel("Integration Test Model");
        testDevice.setSerialNumber("ITSM001");
        testDevice.setUserId(testUserId);
        testDevice.setLocationLat(new java.math.BigDecimal("6.5244"));
        testDevice.setLocationLng(new java.math.BigDecimal("3.3792"));
        testDevice.setProtocol(Device.Protocol.MQTT);
        testDevice.setConnectionStatus(Device.ConnectionStatus.ONLINE);
        testDevice.setIsVerified(false);
        testDevice.setVerificationStatus(Device.VerificationStatus.PENDING);
        testDevice.setTrustLevel(Device.TrustLevel.UNVERIFIED);
        
        testDevice = deviceRepository.save(testDevice);
    }

    @Test
    void testCompleteDeviceVerificationWorkflow() throws Exception {
        // Step 1: Submit device for verification
        DeviceVerificationRequestDto verificationRequest = new DeviceVerificationRequestDto();
        verificationRequest.setDeviceId(testDevice.getId());
        verificationRequest.setSamplePayload("{\"voltage\": 220.5, \"current\": 5.2, \"power\": 1146.6}");
        verificationRequest.setNotes("Integration test device for verification");
        verificationRequest.setBrand("Test Brand");
        verificationRequest.setModel("Integration Test Model");
        verificationRequest.setPreferredProtocol("MQTT");

        mockMvc.perform(post("/api/v1/devices/verification/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verificationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("PENDING"))
                .andExpect(jsonPath("$.trustLevel").value("UNVERIFIED"));

        // Step 2: Check if device can send data (should be false initially)
        mockMvc.perform(get("/api/v1/devices/verification/INTEGRATION_TEST_001/can-send-data"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        // Step 3: Mark device under review
        mockMvc.perform(post("/api/v1/devices/verification/INTEGRATION_TEST_001/mark-under-review"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("UNDER_REVIEW"));

        // Step 4: Review and approve device
        DeviceVerificationReviewDto reviewRequest = new DeviceVerificationReviewDto();
        reviewRequest.setDeviceId(testDevice.getId());
        reviewRequest.setVerificationStatus("APPROVED");
        reviewRequest.setNotes("Device approved after integration testing");
        reviewRequest.setReviewerId(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/devices/verification/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("APPROVED"))
                .andExpect(jsonPath("$.isVerified").value(true))
                .andExpect(jsonPath("$.trustLevel").value("VERIFIED"));

        // Step 5: Verify device can now send data
        mockMvc.perform(get("/api/v1/devices/verification/INTEGRATION_TEST_001/can-send-data"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // Step 6: Generate authentication secret
        String authSecret = mockMvc.perform(post("/api/v1/devices/verification/INTEGRATION_TEST_001/generate-auth-secret"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertNotNull(authSecret);
        assertFalse(authSecret.isEmpty());

        // Step 7: Validate authentication secret
        mockMvc.perform(post("/api/v1/devices/verification/validate-auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"deviceId\": \"INTEGRATION_TEST_001\", \"authSecret\": \"" + authSecret + "\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // Step 8: Check verification status
        mockMvc.perform(get("/api/v1/devices/verification/INTEGRATION_TEST_001/status"))
                .andExpect(status().isOk())
                .andExpect(content().string("APPROVED"));

        // Step 9: Get verification details
        mockMvc.perform(get("/api/v1/devices/verification/INTEGRATION_TEST_001/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("APPROVED"))
                .andExpect(jsonPath("$.isVerified").value(true))
                .andExpect(jsonPath("$.trustLevel").value("VERIFIED"));
    }

    @Test
    void testDeviceVerificationRejection() throws Exception {
        // Step 1: Submit device for verification
        DeviceVerificationRequestDto verificationRequest = new DeviceVerificationRequestDto();
        verificationRequest.setDeviceId(testDevice.getId());
        verificationRequest.setSamplePayload("{\"voltage\": 220.5, \"current\": 5.2}");
        verificationRequest.setNotes("Integration test device for rejection");
        verificationRequest.setBrand("Test Brand");
        verificationRequest.setModel("Integration Test Model");
        verificationRequest.setPreferredProtocol("MQTT");

        mockMvc.perform(post("/api/v1/devices/verification/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verificationRequest)))
                .andExpect(status().isOk());

        // Step 2: Reject device
        DeviceVerificationReviewDto rejectionRequest = new DeviceVerificationReviewDto();
        rejectionRequest.setDeviceId(testDevice.getId());
        rejectionRequest.setVerificationStatus("REJECTED");
        rejectionRequest.setNotes("Device rejected due to insufficient documentation");
        rejectionRequest.setReviewerId(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/devices/verification/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rejectionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("REJECTED"))
                .andExpect(jsonPath("$.isVerified").value(false));

        // Step 3: Verify device cannot send data
        mockMvc.perform(get("/api/v1/devices/verification/INTEGRATION_TEST_001/can-send-data"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        // Step 4: Check verification status
        mockMvc.perform(get("/api/v1/devices/verification/INTEGRATION_TEST_001/status"))
                .andExpect(status().isOk())
                .andExpect(content().string("REJECTED"));
    }

    @Test
    void testDeviceVerificationSuspension() throws Exception {
        // Step 1: Submit and approve device first
        DeviceVerificationRequestDto verificationRequest = new DeviceVerificationRequestDto();
        verificationRequest.setDeviceId(testDevice.getId());
        verificationRequest.setSamplePayload("{\"voltage\": 220.5, \"current\": 5.2}");
        verificationRequest.setNotes("Integration test device for suspension");
        verificationRequest.setBrand("Test Brand");
        verificationRequest.setModel("Integration Test Model");
        verificationRequest.setPreferredProtocol("MQTT");

        mockMvc.perform(post("/api/v1/devices/verification/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verificationRequest)))
                .andExpect(status().isOk());

        // Approve device
        DeviceVerificationReviewDto approvalRequest = new DeviceVerificationReviewDto();
        approvalRequest.setDeviceId(testDevice.getId());
        approvalRequest.setVerificationStatus("APPROVED");
        approvalRequest.setNotes("Device approved");
        approvalRequest.setReviewerId(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/devices/verification/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isOk());

        // Step 2: Suspend device
        DeviceVerificationReviewDto suspensionRequest = new DeviceVerificationReviewDto();
        suspensionRequest.setDeviceId(testDevice.getId());
        suspensionRequest.setVerificationStatus("SUSPENDED");
        suspensionRequest.setNotes("Device suspended due to suspicious activity");
        suspensionRequest.setReviewerId(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/devices/verification/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(suspensionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("SUSPENDED"));

        // Step 3: Verify device cannot send data when suspended
        mockMvc.perform(get("/api/v1/devices/verification/INTEGRATION_TEST_001/can-send-data"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void testVerificationStatistics() throws Exception {
        // Create additional test devices
        Device device2 = new Device();
        device2.setDeviceId("INTEGRATION_TEST_002");
        device2.setName("Integration Test Device 2");
        device2.setDeviceType(Device.DeviceType.SOLAR_INVERTER);
        device2.setManufacturer("Test Manufacturer");
        device2.setModel("Integration Test Model 2");
        device2.setSerialNumber("ITSM002");
        device2.setUserId(testUserId);
        device2.setLocationLat(new java.math.BigDecimal("6.5244"));
        device2.setLocationLng(new java.math.BigDecimal("3.3792"));
        device2.setProtocol(Device.Protocol.MQTT);
        device2.setConnectionStatus(Device.ConnectionStatus.ONLINE);
        device2.setIsVerified(false);
        device2.setVerificationStatus(Device.VerificationStatus.PENDING);
        device2.setTrustLevel(Device.TrustLevel.UNVERIFIED);
        
        deviceRepository.save(device2);

        // Check verification statistics
        mockMvc.perform(get("/api/v1/devices/verification/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingCount").value(2))
                .andExpect(jsonPath("$.underReviewCount").value(0));

        // Mark one device under review
        mockMvc.perform(post("/api/v1/devices/verification/INTEGRATION_TEST_001/mark-under-review"))
                .andExpect(status().isOk());

        // Check updated statistics
        mockMvc.perform(get("/api/v1/devices/verification/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingCount").value(1))
                .andExpect(jsonPath("$.underReviewCount").value(1));
    }

    @Test
    void testInvalidVerificationRequests() throws Exception {
        // Test with missing device ID
        DeviceVerificationRequestDto invalidRequest = new DeviceVerificationRequestDto();
        invalidRequest.setSamplePayload("{\"voltage\": 220.5}");
        invalidRequest.setNotes("Invalid request");

        mockMvc.perform(post("/api/v1/devices/verification/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // Test with empty device ID
        DeviceVerificationRequestDto emptyDeviceIdRequest = new DeviceVerificationRequestDto();
        emptyDeviceIdRequest.setDeviceId(UUID.randomUUID()); // Set a valid UUID
        emptyDeviceIdRequest.setSamplePayload("{\"voltage\": 220.5}");
        emptyDeviceIdRequest.setNotes("Empty device ID");

        mockMvc.perform(post("/api/v1/devices/verification/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyDeviceIdRequest)))
                .andExpect(status().isBadRequest());

        // Test with non-existent device ID
        mockMvc.perform(get("/api/v1/devices/verification/NONEXISTENT_DEVICE/status"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        mockMvc.perform(get("/api/v1/devices/verification/NONEXISTENT_DEVICE/details"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAuthenticationSecretValidation() throws Exception {
        // First approve the device
        DeviceVerificationRequestDto verificationRequest = new DeviceVerificationRequestDto();
        verificationRequest.setDeviceId(testDevice.getId());
        verificationRequest.setSamplePayload("{\"voltage\": 220.5, \"current\": 5.2}");
        verificationRequest.setNotes("Integration test device for auth testing");
        verificationRequest.setBrand("Test Brand");
        verificationRequest.setModel("Integration Test Model");
        verificationRequest.setPreferredProtocol("MQTT");

        mockMvc.perform(post("/api/v1/devices/verification/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verificationRequest)))
                .andExpect(status().isOk());

        DeviceVerificationReviewDto approvalRequest = new DeviceVerificationReviewDto();
        approvalRequest.setDeviceId(testDevice.getId());
        approvalRequest.setVerificationStatus("APPROVED");
        approvalRequest.setNotes("Device approved for auth testing");
        approvalRequest.setReviewerId(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/devices/verification/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isOk());

        // Generate authentication secret
        String authSecret = mockMvc.perform(post("/api/v1/devices/verification/INTEGRATION_TEST_001/generate-auth-secret"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Test valid authentication
        mockMvc.perform(post("/api/v1/devices/verification/validate-auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"deviceId\": \"INTEGRATION_TEST_001\", \"authSecret\": \"" + authSecret + "\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // Test invalid authentication
        mockMvc.perform(post("/api/v1/devices/verification/validate-auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"deviceId\": \"INTEGRATION_TEST_001\", \"authSecret\": \"invalid-secret\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        // Test with null auth secret
        mockMvc.perform(post("/api/v1/devices/verification/validate-auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"deviceId\": \"INTEGRATION_TEST_001\", \"authSecret\": null}"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}
