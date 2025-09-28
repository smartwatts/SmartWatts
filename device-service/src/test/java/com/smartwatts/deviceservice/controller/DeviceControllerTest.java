package com.smartwatts.deviceservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartwatts.deviceservice.model.Device;
import com.smartwatts.deviceservice.repository.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DeviceControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private DeviceRepository deviceRepository;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        deviceRepository.deleteAll();
    }

    @Test
    void testCreateDevice() throws Exception {
        Device device = new Device();
        device.setName("Test Smart Plug");
        device.setType("smart_plug");
        device.setStatus("online");
        device.setLocation("Living Room");
        device.setLastSeen(Instant.now());

        mockMvc.perform(post("/api/v1/devices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(device)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Smart Plug"))
                .andExpect(jsonPath("$.type").value("smart_plug"))
                .andExpect(jsonPath("$.status").value("online"));
    }

    @Test
    void testGetAllDevices() throws Exception {
        // Create test data
        Device device1 = new Device();
        device1.setName("Smart Plug 1");
        device1.setType("smart_plug");
        device1.setStatus("online");
        deviceRepository.save(device1);

        Device device2 = new Device();
        device2.setName("Energy Meter 1");
        device2.setType("energy_meter");
        device2.setStatus("offline");
        deviceRepository.save(device2);

        mockMvc.perform(get("/api/v1/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Smart Plug 1"))
                .andExpect(jsonPath("$[1].name").value("Energy Meter 1"));
    }

    @Test
    void testGetDeviceById() throws Exception {
        Device device = new Device();
        device.setName("Test Device");
        device.setType("smart_plug");
        device.setStatus("online");
        Device saved = deviceRepository.save(device);

        mockMvc.perform(get("/api/v1/devices/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Device"))
                .andExpect(jsonPath("$.type").value("smart_plug"));
    }

    @Test
    void testUpdateDevice() throws Exception {
        Device device = new Device();
        device.setName("Original Device");
        device.setType("smart_plug");
        device.setStatus("online");
        Device saved = deviceRepository.save(device);

        Device updated = new Device();
        updated.setName("Updated Device");
        updated.setType("energy_meter");
        updated.setStatus("offline");

        mockMvc.perform(put("/api/v1/devices/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Device"))
                .andExpect(jsonPath("$.type").value("energy_meter"));
    }

    @Test
    void testDeleteDevice() throws Exception {
        Device device = new Device();
        device.setName("Delete Device");
        device.setType("smart_plug");
        device.setStatus("online");
        Device saved = deviceRepository.save(device);

        mockMvc.perform(delete("/api/v1/devices/" + saved.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/devices/" + saved.getId()))
                .andExpect(status().isNotFound());
    }
} 