package com.smartwatts.userservice.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class EnvironmentValidation {

    private final Environment environment;
    private final boolean isProduction;

    public EnvironmentValidation(Environment environment) {
        this.environment = environment;
        this.isProduction = "production".equals(environment.getProperty("spring.profiles.active"));
    }

    @PostConstruct
    public void validateEnvironment() {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Required environment variables
        validateRequired("POSTGRES_PASSWORD", "Database password", errors);
        validateRequired("JWT_SECRET", "JWT secret key", errors);
        validateRequired("REDIS_PASSWORD", "Redis password", errors);

        // Production-specific validations
        if (isProduction) {
            validateProductionEnvironment(errors, warnings);
        }

        // Log warnings
        if (!warnings.isEmpty()) {
            log.warn("Environment validation warnings:");
            warnings.forEach(warning -> log.warn("  - {}", warning));
        }

        // Log errors and throw exception if critical
        if (!errors.isEmpty()) {
            log.error("Environment validation errors:");
            errors.forEach(error -> log.error("  - {}", error));
            
            if (isProduction) {
                throw new IllegalStateException(
                    "Environment validation failed. Missing required environment variables: " + 
                    String.join(", ", errors)
                );
            } else {
                log.warn("Environment validation failed but not in production mode. Continuing with warnings.");
            }
        } else {
            log.info("Environment validation passed successfully");
        }
    }

    private void validateRequired(String envVar, String description, List<String> errors) {
        String value = environment.getProperty(envVar);
        if (value == null || value.trim().isEmpty()) {
            errors.add(description + " (" + envVar + ") is required but not set");
        } else if (isDefaultValue(envVar, value)) {
            errors.add(description + " (" + envVar + ") is set to default value. Must be changed in production.");
        }
    }

    private boolean isDefaultValue(String envVar, String value) {
        // Check for common default values
        return value.contains("CHANGE_ME") || 
               value.contains("default") || 
               value.equals("postgres") ||
               value.equals("admin") ||
               value.length() < 8; // Too short to be secure
    }

    private void validateProductionEnvironment(List<String> errors, List<String> warnings) {
        // Validate CORS configuration
        String corsOrigins = environment.getProperty("CORS_ALLOWED_ORIGINS");
        if (corsOrigins == null || corsOrigins.trim().isEmpty()) {
            warnings.add("CORS_ALLOWED_ORIGINS is not set. CORS will be disabled (may break frontend).");
        } else if (corsOrigins.contains("*")) {
            errors.add("CORS_ALLOWED_ORIGINS contains wildcard (*) which is not allowed in production");
        }

        // Validate SSL configuration
        String sslEnabled = environment.getProperty("SSL_ENABLED");
        if (!"true".equalsIgnoreCase(sslEnabled)) {
            warnings.add("SSL_ENABLED is not set to true. HTTPS may not be enforced.");
        }

        // Validate password strength
        validatePasswordStrength("POSTGRES_PASSWORD", "Database password", errors);
        validatePasswordStrength("REDIS_PASSWORD", "Redis password", errors);
        validatePasswordStrength("JWT_SECRET", "JWT secret", errors);
    }

    private void validatePasswordStrength(String envVar, String description, List<String> errors) {
        String value = environment.getProperty(envVar);
        if (value != null && value.length() < 16) {
            errors.add(description + " (" + envVar + ") is too short. Minimum 16 characters required in production.");
        }
    }
}

