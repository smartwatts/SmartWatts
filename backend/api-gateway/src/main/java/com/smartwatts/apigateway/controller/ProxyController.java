package com.smartwatts.apigateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/proxy")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "http://localhost:3003", "http://localhost:3004", "http://localhost:3005", "http://localhost:3006"})
public class ProxyController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping
    public ResponseEntity<Object> proxyGet(
            @RequestParam String service,
            @RequestParam String path,
            @RequestHeader Map<String, String> headers) {
        
        try {
            // Find service instance
            List<ServiceInstance> instances = discoveryClient.getInstances(service);
            if (instances.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            ServiceInstance instance = instances.get(0);
            String baseUrl = "http://" + instance.getHost() + ":" + instance.getPort();
            String fullUrl = baseUrl + path;
            
            // Prepare headers
            HttpHeaders httpHeaders = new HttpHeaders();
            headers.forEach(httpHeaders::set);
            
            HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
            
            // Make the request
            ResponseEntity<Object> response = restTemplate.exchange(
                fullUrl, 
                HttpMethod.GET, 
                entity, 
                Object.class
            );
            
            return ResponseEntity.ok(response.getBody());
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Proxy request failed", "message", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<Object> proxyPost(
            @RequestParam String service,
            @RequestParam String path,
            @RequestHeader Map<String, String> headers,
            @RequestBody(required = false) Object body) {
        
        try {
            // Find service instance
            List<ServiceInstance> instances = discoveryClient.getInstances(service);
            if (instances.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            ServiceInstance instance = instances.get(0);
            String baseUrl = "http://" + instance.getHost() + ":" + instance.getPort();
            String fullUrl = baseUrl + path;
            
            // Prepare headers
            HttpHeaders httpHeaders = new HttpHeaders();
            headers.forEach(httpHeaders::set);
            
            HttpEntity<Object> entity = new HttpEntity<>(body, httpHeaders);
            
            // Make the request
            ResponseEntity<Object> response = restTemplate.exchange(
                fullUrl, 
                HttpMethod.POST, 
                entity, 
                Object.class
            );
            
            return ResponseEntity.ok(response.getBody());
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Proxy request failed", "message", e.getMessage()));
        }
    }

    @PutMapping
    public ResponseEntity<Object> proxyPut(
            @RequestParam String service,
            @RequestParam String path,
            @RequestHeader Map<String, String> headers,
            @RequestBody(required = false) Object body) {
        
        try {
            // Find service instance
            List<ServiceInstance> instances = discoveryClient.getInstances(service);
            if (instances.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            ServiceInstance instance = instances.get(0);
            String baseUrl = "http://" + instance.getHost() + ":" + instance.getPort();
            String fullUrl = baseUrl + path;
            
            // Prepare headers
            HttpHeaders httpHeaders = new HttpHeaders();
            headers.forEach(httpHeaders::set);
            
            HttpEntity<Object> entity = new HttpEntity<>(body, httpHeaders);
            
            // Make the request
            ResponseEntity<Object> response = restTemplate.exchange(
                fullUrl, 
                HttpMethod.PUT, 
                entity, 
                Object.class
            );
            
            return ResponseEntity.ok(response.getBody());
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Proxy request failed", "message", e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<Object> proxyDelete(
            @RequestParam String service,
            @RequestParam String path,
            @RequestHeader Map<String, String> headers) {
        
        try {
            // Find service instance
            List<ServiceInstance> instances = discoveryClient.getInstances(service);
            if (instances.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            ServiceInstance instance = instances.get(0);
            String baseUrl = "http://" + instance.getHost() + ":" + instance.getPort();
            String fullUrl = baseUrl + path;
            
            // Prepare headers
            HttpHeaders httpHeaders = new HttpHeaders();
            headers.forEach(httpHeaders::set);
            
            HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
            
            // Make the request
            ResponseEntity<Object> response = restTemplate.exchange(
                fullUrl, 
                HttpMethod.DELETE, 
                entity, 
                Object.class
            );
            
            return ResponseEntity.ok(response.getBody());
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Proxy request failed", "message", e.getMessage()));
        }
    }
}


