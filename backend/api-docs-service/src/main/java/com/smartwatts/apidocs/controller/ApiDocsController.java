package com.smartwatts.apidocs.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api-docs")
public class ApiDocsController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @GetMapping("/services")
    public Map<String, Object> getServices() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "SmartWatts API Docs Aggregator");
        response.put("version", "1.0.0");
        response.put("status", "running");
        
        List<String> services = discoveryClient.getServices();
        response.put("discovered-services", services);
        response.put("total-services", services.size());
        
        return response;
    }

    @GetMapping("/health")
    public Map<String, Object> getHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "api-docs-service");
        health.put("timestamp", System.currentTimeMillis());
        return health;
    }

    @GetMapping("/info")
    public Map<String, Object> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "SmartWatts API Documentation Service");
        info.put("description", "Aggregated API documentation for all SmartWatts microservices");
        info.put("swagger-ui", "/swagger-ui.html");
        info.put("openapi", "/v3/api-docs");
        info.put("services-endpoint", "/api-docs/services");
        return info;
    }
} 