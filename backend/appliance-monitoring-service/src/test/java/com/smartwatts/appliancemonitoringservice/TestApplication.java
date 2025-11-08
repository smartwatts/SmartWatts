package com.smartwatts.appliancemonitoringservice;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Test-specific application class that doesn't enable scheduling or JPA auditing
 * to prevent services from being loaded during @WebMvcTest
 */
@SpringBootApplication
@ComponentScan(
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = com.smartwatts.appliancemonitoringservice.config.SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = com.smartwatts.appliancemonitoringservice.config.JwtAuthenticationFilter.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = com.smartwatts.appliancemonitoringservice.config.JwtConfig.class)
    }
)
public class TestApplication {
    // Empty - just used to override the main application class
}

