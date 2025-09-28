package com.smartwatts.apigateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private RouteLocator routeLocator;

    @GetMapping("/info")
    public Map<String, Object> getGatewayInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "SmartWatts API Gateway");
        info.put("version", "1.0.0");
        info.put("status", "running");
        info.put("port", 8080);
        return info;
    }

    @GetMapping("/routes")
    public Flux<Map<String, Object>> getRoutes() {
        return routeLocator.getRoutes()
            .map(route -> {
                Map<String, Object> routeInfo = new HashMap<>();
                routeInfo.put("id", route.getId());
                routeInfo.put("uri", route.getUri());
                routeInfo.put("predicates", route.getPredicate().toString());
                routeInfo.put("filters", route.getFilters().toString());
                return routeInfo;
            });
    }

    @GetMapping("/health")
    public Map<String, Object> getHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("gateway", "running");
        health.put("timestamp", System.currentTimeMillis());
        return health;
    }
} 