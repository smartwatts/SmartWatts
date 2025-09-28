package com.smartwatts.energyservice.exception;

/**
 * Exception thrown when a device that is not verified attempts to send data
 */
public class DeviceNotVerifiedException extends RuntimeException {
    
    public DeviceNotVerifiedException(String message) {
        super(message);
    }
    
    public DeviceNotVerifiedException(String message, Throwable cause) {
        super(message, cause);
    }
}
