package com.smartwatts.deviceservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartwatts.deviceservice.dto.DeviceDto;
import com.smartwatts.deviceservice.dto.DeviceConfigurationDto;
import com.smartwatts.deviceservice.dto.DeviceEventDto;
import com.smartwatts.deviceservice.model.Device;
import com.smartwatts.deviceservice.model.DeviceEvent;
import com.smartwatts.deviceservice.service.DeviceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeviceController.class)
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeviceService deviceService;

    @Autowired
    private ObjectMapper objectMapper;

    private DeviceDto testDeviceDto;
    private UUID testDeviceId;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testDeviceId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        
        testDeviceDto = DeviceDto.builder()
                .id(testDeviceId)
                .userId(testUserId)
                .deviceId("DEVICE-001")
                .name("Test Device")
                .description("Test Description")
                .deviceType(Device.DeviceType.SMART_METER)
                .protocol(Device.Protocol.MQTT)
                .status(Device.DeviceStatus.ACTIVE)
                .connectionStatus(Device.ConnectionStatus.ONLINE)
                .locationLat(new BigDecimal("6.5244"))
                .locationLng(new BigDecimal("3.3792"))
                .installationDate(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllDevices_Success_ReturnsPage() throws Exception {
        // Given
        Page<DeviceDto> page = new PageImpl<>(Arrays.asList(testDeviceDto));
        when(deviceService.getAllDevices(any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/devices")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Test Device"));

        verify(deviceService).getAllDevices(any(Pageable.class));
    }

    @Test
    void registerDevice_Success_ReturnsCreated() throws Exception {
        // Given
        when(deviceService.registerDevice(any(DeviceDto.class))).thenReturn(testDeviceDto);

        // When & Then
        mockMvc.perform(post("/api/v1/devices/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testDeviceDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Device"));

        verify(deviceService).registerDevice(any(DeviceDto.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getDeviceById_Success_ReturnsDeviceDto() throws Exception {
        // Given
        when(deviceService.getDeviceById(testDeviceId)).thenReturn(testDeviceDto);

        // When & Then
        mockMvc.perform(get("/api/v1/devices/{deviceId}", testDeviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testDeviceId.toString()))
                .andExpect(jsonPath("$.name").value("Test Device"));

        verify(deviceService).getDeviceById(testDeviceId);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getDeviceByDeviceId_Success_ReturnsDeviceDto() throws Exception {
        // Given
        when(deviceService.getDeviceByDeviceId("DEVICE-001")).thenReturn(testDeviceDto);

        // When & Then
        mockMvc.perform(get("/api/v1/devices/device-id/{deviceId}", "DEVICE-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId").value("DEVICE-001"));

        verify(deviceService).getDeviceByDeviceId("DEVICE-001");
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getDevicesByUserId_Success_ReturnsPage() throws Exception {
        // Given
        Page<DeviceDto> page = new PageImpl<>(Arrays.asList(testDeviceDto));
        when(deviceService.getDevicesByUserId(eq(testUserId), any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/devices/user/{userId}", testUserId)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(deviceService).getDevicesByUserId(eq(testUserId), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getActiveDevicesByUserId_Success_ReturnsList() throws Exception {
        // Given
        List<DeviceDto> devices = Arrays.asList(testDeviceDto);
        when(deviceService.getActiveDevicesByUserId(testUserId)).thenReturn(devices);

        // When & Then
        mockMvc.perform(get("/api/v1/devices/user/{userId}/active", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(deviceService).getActiveDevicesByUserId(testUserId);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateDevice_Success_ReturnsUpdatedDevice() throws Exception {
        // Given
        testDeviceDto.setName("Updated Device");
        when(deviceService.updateDevice(eq(testDeviceId), any(DeviceDto.class))).thenReturn(testDeviceDto);

        // When & Then
        mockMvc.perform(put("/api/v1/devices/{deviceId}", testDeviceId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testDeviceDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Device"));

        verify(deviceService).updateDevice(eq(testDeviceId), any(DeviceDto.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateDeviceStatus_Success_ReturnsUpdatedDevice() throws Exception {
        // Given
        testDeviceDto.setStatus(Device.DeviceStatus.INACTIVE);
        when(deviceService.updateDeviceStatus(eq(testDeviceId), any(Device.DeviceStatus.class))).thenReturn(testDeviceDto);

        // When & Then
        mockMvc.perform(put("/api/v1/devices/{deviceId}/status", testDeviceId)
                .with(csrf())
                .param("status", "INACTIVE"))
                .andExpect(status().isOk());

        verify(deviceService).updateDeviceStatus(eq(testDeviceId), any(Device.DeviceStatus.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateConnectionStatus_Success_ReturnsUpdatedDevice() throws Exception {
        // Given
        testDeviceDto.setConnectionStatus(Device.ConnectionStatus.OFFLINE);
        when(deviceService.updateConnectionStatus(eq(testDeviceId), any(Device.ConnectionStatus.class))).thenReturn(testDeviceDto);

        // When & Then
        mockMvc.perform(put("/api/v1/devices/{deviceId}/connection-status", testDeviceId)
                .with(csrf())
                .param("status", "OFFLINE"))
                .andExpect(status().isOk());

        verify(deviceService).updateConnectionStatus(eq(testDeviceId), any(Device.ConnectionStatus.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteDevice_Success_ReturnsNoContent() throws Exception {
        // Given
        doNothing().when(deviceService).deleteDevice(testDeviceId);

        // When & Then
        mockMvc.perform(delete("/api/v1/devices/{deviceId}", testDeviceId)
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(deviceService).deleteDevice(testDeviceId);
    }

    @Test
    void getDevicesNeedingMaintenance_Success_ReturnsList() throws Exception {
        // Given
        List<DeviceDto> devices = Arrays.asList(testDeviceDto);
        when(deviceService.getDevicesNeedingMaintenance()).thenReturn(devices);

        // When & Then
        mockMvc.perform(get("/api/v1/devices/maintenance/needed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(deviceService).getDevicesNeedingMaintenance();
    }

    @Test
    void getDevicesNeedingCalibration_Success_ReturnsList() throws Exception {
        // Given
        List<DeviceDto> devices = Arrays.asList(testDeviceDto);
        when(deviceService.getDevicesNeedingCalibration()).thenReturn(devices);

        // When & Then
        mockMvc.perform(get("/api/v1/devices/calibration/needed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(deviceService).getDevicesNeedingCalibration();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getDeviceEvents_Success_ReturnsPage() throws Exception {
        // Given
        DeviceEventDto eventDto = new DeviceEventDto();
        eventDto.setId(UUID.randomUUID());
        eventDto.setDeviceId(testDeviceId);
        eventDto.setEventType(DeviceEvent.EventType.ALERT_TRIGGERED);
        Page<DeviceEventDto> page = new PageImpl<>(Arrays.asList(eventDto));
        when(deviceService.getDeviceEvents(eq(testDeviceId), any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/devices/{deviceId}/events", testDeviceId)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(deviceService).getDeviceEvents(eq(testDeviceId), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getRecentDeviceEvents_Success_ReturnsList() throws Exception {
        // Given
        DeviceEventDto eventDto = new DeviceEventDto();
        eventDto.setId(UUID.randomUUID());
        eventDto.setDeviceId(testDeviceId);
        List<DeviceEventDto> events = Arrays.asList(eventDto);
        when(deviceService.getRecentDeviceEvents(eq(testDeviceId), any(LocalDateTime.class))).thenReturn(events);

        // When & Then
        mockMvc.perform(get("/api/v1/devices/{deviceId}/events/recent", testDeviceId)
                .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(deviceService).getRecentDeviceEvents(eq(testDeviceId), any(LocalDateTime.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createDeviceConfiguration_Success_ReturnsConfiguration() throws Exception {
        // Given
        DeviceConfigurationDto configDto = new DeviceConfigurationDto();
        configDto.setDeviceId(testDeviceId);
        configDto.setConfigKey("test-key");
        configDto.setConfigValue("test-value");
        when(deviceService.saveDeviceConfiguration(any(DeviceConfigurationDto.class))).thenReturn(configDto);

        // When & Then
        mockMvc.perform(post("/api/v1/devices/{deviceId}/configurations", testDeviceId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(configDto)))
                .andExpect(status().isCreated());

        verify(deviceService).saveDeviceConfiguration(any(DeviceConfigurationDto.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getDeviceConfigurations_Success_ReturnsList() throws Exception {
        // Given
        DeviceConfigurationDto configDto = new DeviceConfigurationDto();
        configDto.setDeviceId(testDeviceId);
        List<DeviceConfigurationDto> configs = Arrays.asList(configDto);
        when(deviceService.getDeviceConfigurations(testDeviceId)).thenReturn(configs);

        // When & Then
        mockMvc.perform(get("/api/v1/devices/{deviceId}/configurations", testDeviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(deviceService).getDeviceConfigurations(testDeviceId);
    }
}

