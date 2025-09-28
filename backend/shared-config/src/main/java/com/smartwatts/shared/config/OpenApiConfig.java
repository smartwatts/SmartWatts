package com.smartwatts.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:smartwatts-service}")
    private String applicationName;

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        String serviceName = getServiceDisplayName(applicationName);
        String serviceDescription = getServiceDescription(applicationName);
        
        return new OpenAPI()
                .info(new Info()
                        .title(serviceName)
                        .description(serviceDescription)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("SmartWatts Team")
                                .email("support@smartwatts.com")
                                .url("https://smartwatts.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Development Server"),
                        new Server()
                                .url("https://api.smartwatts.com")
                                .description("Production Server")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token authentication")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    private String getServiceDisplayName(String serviceName) {
        switch (serviceName) {
            case "api-gateway":
                return "SmartWatts API Gateway";
            case "user-service":
                return "SmartWatts User Service API";
            case "energy-service":
                return "SmartWatts Energy Service API";
            case "device-service":
                return "SmartWatts Device Service API";
            case "analytics-service":
                return "SmartWatts Analytics Service API";
            case "billing-service":
                return "SmartWatts Billing Service API";
            case "notification-service":
                return "SmartWatts Notification Service API";
            case "edge-gateway-service":
                return "SmartWatts Edge Gateway Service API";
            default:
                return "SmartWatts " + serviceName.replace("-", " ").toUpperCase();
        }
    }

    private String getServiceDescription(String serviceName) {
        switch (serviceName) {
            case "api-gateway":
                return "API Gateway for SmartWatts Energy Monitoring Platform with routing, rate limiting, and circuit breaker functionality";
            case "user-service":
                return "User management and authentication service for SmartWatts platform including registration, login, and profile management";
            case "energy-service":
                return "Energy monitoring and consumption tracking service with real-time data collection and analysis";
            case "device-service":
                return "IoT device management service for smart meters, inverters, and energy monitoring devices";
            case "analytics-service":
                return "Advanced analytics and reporting service with energy insights, forecasting, and optimization recommendations";
            case "billing-service":
                return "Billing and cost management service with MYTO tariff calculations and prepaid token tracking";
            case "notification-service":
                return "Notification and alert management service for energy monitoring alerts and system notifications";
            case "edge-gateway-service":
                return "Edge gateway service for offline-first energy monitoring with MQTT and Modbus protocol support";
            default:
                return "SmartWatts " + serviceName.replace("-", " ") + " service for energy monitoring platform";
        }
    }
}
