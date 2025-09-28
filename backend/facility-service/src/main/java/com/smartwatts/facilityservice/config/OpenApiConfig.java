package com.smartwatts.facilityservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Facility360 API")
                        .description("Comprehensive Facility Management System API for SmartWatts Platform")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("SmartWatts Development Team")
                                .email("dev@smartwatts.com")
                                .url("https://smartwatts.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://smartwatts.com/license")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8089")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.smartwatts.com/facility")
                                .description("Production Server")
                ));
    }
}
