package com.smartwatts.apidocs.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ApiDocsController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class ApiDocsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DiscoveryClient discoveryClient;

    @BeforeEach
    void setUp() {
        // Setup common mocks
    }

    @Test
    void getServices_Success_ReturnsServices() throws Exception {
        // Given
        List<String> services = Arrays.asList("user-service", "device-service", "energy-service");
        when(discoveryClient.getServices()).thenReturn(services);

        // When & Then
        mockMvc.perform(get("/api-docs/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("SmartWatts API Docs Aggregator"))
                .andExpect(jsonPath("$.version").value("1.0.0"))
                .andExpect(jsonPath("$.status").value("running"))
                .andExpect(jsonPath("$.discovered-services").isArray())
                .andExpect(jsonPath("$.total-services").value(3));

        verify(discoveryClient).getServices();
    }

    @Test
    void getHealth_Success_ReturnsHealth() throws Exception {
        // When & Then
        mockMvc.perform(get("/api-docs/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("api-docs-service"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void getInfo_Success_ReturnsInfo() throws Exception {
        // When & Then
        mockMvc.perform(get("/api-docs/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("SmartWatts API Documentation Service"))
                .andExpect(jsonPath("$.description").value("Aggregated API documentation for all SmartWatts microservices"))
                .andExpect(jsonPath("$.swagger-ui").value("/swagger-ui.html"))
                .andExpect(jsonPath("$.openapi").value("/v3/api-docs"))
                .andExpect(jsonPath("$.services-endpoint").value("/api-docs/services"));
    }
}

