package com.smartwatts.userservice;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Test-specific application class that doesn't enable JPA auditing
 * to prevent services from being loaded during @WebMvcTest
 */
@SpringBootApplication
public class TestApplication {
    // Empty - just used to override the main application class
}

