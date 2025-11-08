package com.smartwatts.apigateway.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProxyController.class)
class ProxyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DiscoveryClient discoveryClient;

    @MockBean
    private RestTemplate restTemplate;

    private ServiceInstance mockServiceInstance;

    @BeforeEach
    void setUp() {
        mockServiceInstance = mock(ServiceInstance.class);
        when(mockServiceInstance.getHost()).thenReturn("localhost");
        when(mockServiceInstance.getPort()).thenReturn(8081);
    }

    @Test
    void proxyGet_Success_ReturnsResponse() throws Exception {
        // Given
        List<ServiceInstance> instances = Arrays.asList(mockServiceInstance);
        when(discoveryClient.getInstances("user-service")).thenReturn(instances);
        
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", "123");
        responseBody.put("name", "Test User");
        
        ResponseEntity<Object> mockResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(Object.class))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/proxy")
                .param("service", "user-service")
                .param("path", "/api/v1/users/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.name").value("Test User"));

        verify(discoveryClient).getInstances("user-service");
        verify(restTemplate).exchange(anyString(), any(), any(), eq(Object.class));
    }

    @Test
    void proxyGet_ServiceNotFound_ReturnsNotFound() throws Exception {
        // Given
        when(discoveryClient.getInstances("non-existent-service")).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/proxy")
                .param("service", "non-existent-service")
                .param("path", "/api/v1/test"))
                .andExpect(status().isNotFound());

        verify(discoveryClient).getInstances("non-existent-service");
        verify(restTemplate, never()).exchange(anyString(), any(), any(), eq(Object.class));
    }

    @Test
    void proxyGet_Error_ReturnsInternalServerError() throws Exception {
        // Given
        List<ServiceInstance> instances = Arrays.asList(mockServiceInstance);
        when(discoveryClient.getInstances("user-service")).thenReturn(instances);
        when(restTemplate.exchange(anyString(), any(), any(), eq(Object.class)))
                .thenThrow(new RuntimeException("Connection error"));

        // When & Then
        mockMvc.perform(get("/api/proxy")
                .param("service", "user-service")
                .param("path", "/api/v1/users/123"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Proxy request failed"));

        verify(discoveryClient).getInstances("user-service");
        verify(restTemplate).exchange(anyString(), any(), any(), eq(Object.class));
    }

    @Test
    void proxyPost_Success_ReturnsResponse() throws Exception {
        // Given
        List<ServiceInstance> instances = Arrays.asList(mockServiceInstance);
        when(discoveryClient.getInstances("user-service")).thenReturn(instances);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", "test@example.com");
        requestBody.put("password", "password123");
        
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("token", "access-token");
        responseBody.put("userId", "123");
        
        ResponseEntity<Object> mockResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(Object.class))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/proxy")
                .param("service", "user-service")
                .param("path", "/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token"));

        verify(discoveryClient).getInstances("user-service");
        verify(restTemplate).exchange(anyString(), any(), any(), eq(Object.class));
    }

    @Test
    void proxyPost_ServiceNotFound_ReturnsNotFound() throws Exception {
        // Given
        when(discoveryClient.getInstances("non-existent-service")).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(post("/api/proxy")
                .param("service", "non-existent-service")
                .param("path", "/api/v1/test")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isNotFound());

        verify(discoveryClient).getInstances("non-existent-service");
        verify(restTemplate, never()).exchange(anyString(), any(), any(), eq(Object.class));
    }

    @Test
    void proxyPut_Success_ReturnsResponse() throws Exception {
        // Given
        List<ServiceInstance> instances = Arrays.asList(mockServiceInstance);
        when(discoveryClient.getInstances("user-service")).thenReturn(instances);
        
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", "123");
        responseBody.put("name", "Updated User");
        
        ResponseEntity<Object> mockResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(Object.class))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(put("/api/proxy")
                .param("service", "user-service")
                .param("path", "/api/v1/users/123")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Updated User\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated User"));

        verify(discoveryClient).getInstances("user-service");
        verify(restTemplate).exchange(anyString(), any(), any(), eq(Object.class));
    }

    @Test
    void proxyDelete_Success_ReturnsResponse() throws Exception {
        // Given
        List<ServiceInstance> instances = Arrays.asList(mockServiceInstance);
        when(discoveryClient.getInstances("user-service")).thenReturn(instances);
        
        ResponseEntity<Object> mockResponse = new ResponseEntity<>(HttpStatus.NO_CONTENT);
        when(restTemplate.exchange(anyString(), any(), any(), eq(Object.class))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(delete("/api/proxy")
                .param("service", "user-service")
                .param("path", "/api/v1/users/123"))
                .andExpect(status().isNoContent());

        verify(discoveryClient).getInstances("user-service");
        verify(restTemplate).exchange(anyString(), any(), any(), eq(Object.class));
    }
}

