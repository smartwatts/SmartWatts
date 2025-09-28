package com.smartwatts.deviceverificationservice.model;

public enum DeviceStatus {
    ACTIVE,      // Device is active and verified
    INACTIVE,    // Device is inactive (not yet activated)
    EXPIRED,     // Device activation has expired
    SUSPENDED,   // Device is temporarily suspended
    TAMPERED     // Device tampering detected
}
