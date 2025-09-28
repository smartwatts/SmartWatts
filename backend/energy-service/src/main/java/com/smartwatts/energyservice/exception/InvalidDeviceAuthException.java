package com.smartwatts.energyservice.exception;

/**
 * Exception thrown when a device provides invalid authentication credentials
 */
public class InvalidDeviceAuthException extends RuntimeException {
    
    public InvalidDeviceAuthException(String message) {
        super(message);
    }
    
    public InvalidDeviceAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
