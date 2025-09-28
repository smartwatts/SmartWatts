package com.smartwatts.energyservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartwatts.energyservice.model.Energy;
import com.smartwatts.energyservice.repository.EnergyRepository;
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
class EnergyControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private EnergyRepository energyRepository;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        energyRepository.deleteAll();
    }

    @Test
    void testCreateEnergy() throws Exception {
        Energy energy = new Energy();
        energy.setDeviceId("test-device-1");
        energy.setTimestamp(Instant.now());
        energy.setSource("grid");
        energy.setVoltage(220.0);
        energy.setCurrent(5.0);
        energy.setPower(1100.0);
        energy.setEnergy(1.1);
        energy.setStatus("active");

        mockMvc.perform(post("/api/v1/energy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(energy)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId").value("test-device-1"))
                .andExpect(jsonPath("$.source").value("grid"));
    }

    @Test
    void testGetAllEnergy() throws Exception {
        // Create test data
        Energy energy1 = new Energy();
        energy1.setDeviceId("device-1");
        energy1.setTimestamp(Instant.now());
        energy1.setSource("solar");
        energyRepository.save(energy1);

        Energy energy2 = new Energy();
        energy2.setDeviceId("device-2");
        energy2.setTimestamp(Instant.now());
        energy2.setSource("grid");
        energyRepository.save(energy2);

        mockMvc.perform(get("/api/v1/energy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].deviceId").value("device-1"))
                .andExpect(jsonPath("$[1].deviceId").value("device-2"));
    }

    @Test
    void testGetEnergyById() throws Exception {
        Energy energy = new Energy();
        energy.setDeviceId("test-device");
        energy.setTimestamp(Instant.now());
        energy.setSource("inverter");
        Energy saved = energyRepository.save(energy);

        mockMvc.perform(get("/api/v1/energy/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId").value("test-device"))
                .andExpect(jsonPath("$.source").value("inverter"));
    }

    @Test
    void testUpdateEnergy() throws Exception {
        Energy energy = new Energy();
        energy.setDeviceId("original-device");
        energy.setTimestamp(Instant.now());
        energy.setSource("grid");
        Energy saved = energyRepository.save(energy);

        Energy updated = new Energy();
        updated.setDeviceId("updated-device");
        updated.setTimestamp(Instant.now());
        updated.setSource("solar");

        mockMvc.perform(put("/api/v1/energy/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId").value("updated-device"))
                .andExpect(jsonPath("$.source").value("solar"));
    }

    @Test
    void testDeleteEnergy() throws Exception {
        Energy energy = new Energy();
        energy.setDeviceId("delete-device");
        energy.setTimestamp(Instant.now());
        energy.setSource("generator");
        Energy saved = energyRepository.save(energy);

        mockMvc.perform(delete("/api/v1/energy/" + saved.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/energy/" + saved.getId()))
                .andExpect(status().isNotFound());
    }
} 