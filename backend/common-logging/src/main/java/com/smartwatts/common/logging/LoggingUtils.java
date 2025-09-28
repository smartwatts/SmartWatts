package com.smartwatts.common.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Centralized logging utilities for SmartWatts
 * Provides structured logging with consistent formatting and context
 */
@Component
public class LoggingUtils {
    
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("AUDIT");
    private static final Logger SECURITY_LOGGER = LoggerFactory.getLogger("SECURITY");
    private static final Logger PERFORMANCE_LOGGER = LoggerFactory.getLogger("PERFORMANCE");
    
    /**
     * Log user authentication events
     */
    public static void logAuthEvent(String event, String userId, String email, String ipAddress, boolean success) {
        MDC.put("eventType", "AUTH");
        MDC.put("userId", userId);
        MDC.put("email", email);
        MDC.put("ipAddress", ipAddress);
        MDC.put("success", String.valueOf(success));
        
        AUDIT_LOGGER.info("Authentication event: {} for user: {} from IP: {}", event, email, ipAddress);
        
        clearAuthContext();
    }
    
    /**
     * Log API access events
     */
    public static void logApiAccess(String method, String endpoint, String userId, int responseCode, long duration) {
        MDC.put("eventType", "API_ACCESS");
        MDC.put("method", method);
        MDC.put("endpoint", endpoint);
        MDC.put("userId", userId);
        MDC.put("responseCode", String.valueOf(responseCode));
        MDC.put("duration", String.valueOf(duration));
        
        AUDIT_LOGGER.info("API access: {} {} by user: {} - {}ms", method, endpoint, userId, duration);
        
        clearApiContext();
    }
    
    /**
     * Log security events
     */
    public static void logSecurityEvent(String event, String userId, String details, String severity) {
        MDC.put("eventType", "SECURITY");
        MDC.put("userId", userId);
        MDC.put("severity", severity);
        MDC.put("details", details);
        
        SECURITY_LOGGER.warn("Security event: {} - User: {} - Details: {}", event, userId, details);
        
        clearSecurityContext();
    }
    
    /**
     * Log performance metrics
     */
    public static void logPerformance(String operation, String service, long duration, Map<String, Object> metrics) {
        MDC.put("eventType", "PERFORMANCE");
        MDC.put("operation", operation);
        MDC.put("service", service);
        MDC.put("duration", String.valueOf(duration));
        
        StringBuilder metricsStr = new StringBuilder();
        metrics.forEach((key, value) -> metricsStr.append(key).append("=").append(value).append(","));
        
        PERFORMANCE_LOGGER.info("Performance: {} in {} - {}ms - Metrics: {}", 
            operation, service, duration, metricsStr.toString());
        
        clearPerformanceContext();
    }
    
    /**
     * Log database operations
     */
    public static void logDatabaseOperation(String operation, String table, String userId, int recordCount) {
        MDC.put("eventType", "DATABASE");
        MDC.put("operation", operation);
        MDC.put("table", table);
        MDC.put("userId", userId);
        MDC.put("recordCount", String.valueOf(recordCount));
        
        AUDIT_LOGGER.info("Database operation: {} on table: {} by user: {} - {} records", 
            operation, table, userId, recordCount);
        
        clearDatabaseContext();
    }
    
    /**
     * Log business events
     */
    public static void logBusinessEvent(String event, String userId, String entityType, String entityId, Map<String, Object> data) {
        MDC.put("eventType", "BUSINESS");
        MDC.put("userId", userId);
        MDC.put("entityType", entityType);
        MDC.put("entityId", entityId);
        
        StringBuilder dataStr = new StringBuilder();
        data.forEach((key, value) -> dataStr.append(key).append("=").append(value).append(","));
        
        AUDIT_LOGGER.info("Business event: {} - User: {} - Entity: {}:{} - Data: {}", 
            event, userId, entityType, entityId, dataStr.toString());
        
        clearBusinessContext();
    }
    
    /**
     * Generate correlation ID for request tracing
     */
    public static String generateCorrelationId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Set correlation ID in MDC
     */
    public static void setCorrelationId(String correlationId) {
        MDC.put("correlationId", correlationId);
    }
    
    /**
     * Clear all MDC context
     */
    public static void clearAllContext() {
        MDC.clear();
    }
    
    // Private helper methods to clear specific contexts
    private static void clearAuthContext() {
        MDC.remove("eventType");
        MDC.remove("userId");
        MDC.remove("email");
        MDC.remove("ipAddress");
        MDC.remove("success");
    }
    
    private static void clearApiContext() {
        MDC.remove("eventType");
        MDC.remove("method");
        MDC.remove("endpoint");
        MDC.remove("userId");
        MDC.remove("responseCode");
        MDC.remove("duration");
    }
    
    private static void clearSecurityContext() {
        MDC.remove("eventType");
        MDC.remove("userId");
        MDC.remove("severity");
        MDC.remove("details");
    }
    
    private static void clearPerformanceContext() {
        MDC.remove("eventType");
        MDC.remove("operation");
        MDC.remove("service");
        MDC.remove("duration");
    }
    
    private static void clearDatabaseContext() {
        MDC.remove("eventType");
        MDC.remove("operation");
        MDC.remove("table");
        MDC.remove("userId");
        MDC.remove("recordCount");
    }
    
    private static void clearBusinessContext() {
        MDC.remove("eventType");
        MDC.remove("userId");
        MDC.remove("entityType");
        MDC.remove("entityId");
    }
}