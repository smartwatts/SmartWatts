package com.smartwatts.apidocs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "service-discovery")
public class ServiceDiscoveryConfig {
    
    private List<Map<String, Object>> services;
    
    public List<Map<String, Object>> getServices() {
        return services;
    }
    
    public void setServices(List<Map<String, Object>> services) {
        this.services = services;
    }
} 