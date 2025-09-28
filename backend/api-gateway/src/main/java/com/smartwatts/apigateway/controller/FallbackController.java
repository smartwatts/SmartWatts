package com.smartwatts.apigateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
@Slf4j
public class FallbackController {

    @GetMapping("/user-service")
    public Mono<ResponseEntity<Map<String, Object>>> userServiceFallback() {
        log.warn("User service is unavailable, returning fallback response");
        return Mono.just(createFallbackResponse("User Service", "Service temporarily unavailable"));
    }

    @GetMapping("/energy-service")
    public Mono<ResponseEntity<Map<String, Object>>> energyServiceFallback() {
        log.warn("Energy service is unavailable, returning fallback response");
        return Mono.just(createFallbackResponse("Energy Service", "Service temporarily unavailable"));
    }

    @GetMapping("/device-service")
    public Mono<ResponseEntity<Map<String, Object>>> deviceServiceFallback() {
        log.warn("Device service is unavailable, returning fallback response");
        return Mono.just(createFallbackResponse("Device Service", "Service temporarily unavailable"));
    }

    @GetMapping("/analytics-service")
    public Mono<ResponseEntity<Map<String, Object>>> analyticsServiceFallback() {
        log.warn("Analytics service is unavailable, returning fallback response");
        return Mono.just(createFallbackResponse("Analytics Service", "Service temporarily unavailable"));
    }

    @GetMapping("/billing-service")
    public Mono<ResponseEntity<Map<String, Object>>> billingServiceFallback() {
        log.warn("Billing service is unavailable, returning fallback response");
        return Mono.just(createFallbackResponse("Billing Service", "Service temporarily unavailable"));
    }

    private ResponseEntity<Map<String, Object>> createFallbackResponse(String service, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("service", service);
        response.put("message", message);
        response.put("status", "SERVICE_UNAVAILABLE");
        response.put("code", "FALLBACK_RESPONSE");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
} 