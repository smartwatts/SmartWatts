package com.smartwatts.integration.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.time.Duration;

@TestConfiguration
public class TestContainersConfig {

    @Bean
    public DockerComposeContainer<?> smartwattsServices() {
        DockerComposeContainer<?> container = new DockerComposeContainer<>(
                new File("../../docker-compose.yml"))
                .withExposedService("postgres", 5432)
                .withExposedService("redis", 6379)
                .withExposedService("eureka", 8761)
                .withExposedService("api-gateway", 8080)
                .withExposedService("user-service", 8081)
                .withExposedService("energy-service", 8082)
                .withExposedService("billing-service", 8083)
                .withExposedService("device-service", 8084)
                .withExposedService("analytics-service", 8085)
                .withExposedService("facility-service", 8086)
                .withExposedService("appliance-monitoring-service", 8087)
                .withExposedService("device-verification-service", 8088)
                .withExposedService("edge-gateway", 8089)
                .withExposedService("feature-flag-service", 8090)
                .withExposedService("api-docs-service", 8091)
                .withExposedService("spring-boot-admin", 8092)
                .waitingFor("api-gateway", Wait.forHttp("/actuator/health")
                        .forStatusCode(200)
                        .withStartupTimeout(Duration.ofMinutes(5)))
                .withLocalCompose(true);

        container.start();
        return container;
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry, DockerComposeContainer<?> container) {
        // Database
        registry.add("spring.datasource.url", () -> 
            String.format("jdbc:postgresql://%s:%d/testdb",
                container.getServiceHost("postgres", 5432),
                container.getServicePort("postgres", 5432)));
        
        // Redis
        registry.add("spring.redis.host", () -> 
            container.getServiceHost("redis", 6379));
        registry.add("spring.redis.port", () -> 
            container.getServicePort("redis", 6379));
        
        // Eureka
        registry.add("eureka.client.service-url.defaultZone", () -> 
            String.format("http://%s:%d/eureka/",
                container.getServiceHost("eureka", 8761),
                container.getServicePort("eureka", 8761)));
        
        // API Gateway
        registry.add("api.gateway.url", () -> 
            String.format("http://%s:%d",
                container.getServiceHost("api-gateway", 8080),
                container.getServicePort("api-gateway", 8080)));
        
        // Service URLs
        registry.add("user.service.url", () -> 
            String.format("http://%s:%d",
                container.getServiceHost("user-service", 8081),
                container.getServicePort("user-service", 8081)));
        
        registry.add("energy.service.url", () -> 
            String.format("http://%s:%d",
                container.getServiceHost("energy-service", 8082),
                container.getServicePort("energy-service", 8082)));
        
        registry.add("billing.service.url", () -> 
            String.format("http://%s:%d",
                container.getServiceHost("billing-service", 8083),
                container.getServicePort("billing-service", 8083)));
        
        registry.add("device.service.url", () -> 
            String.format("http://%s:%d",
                container.getServiceHost("device-service", 8084),
                container.getServicePort("device-service", 8084)));
        
        registry.add("analytics.service.url", () -> 
            String.format("http://%s:%d",
                container.getServiceHost("analytics-service", 8085),
                container.getServicePort("analytics-service", 8085)));
    }
}








