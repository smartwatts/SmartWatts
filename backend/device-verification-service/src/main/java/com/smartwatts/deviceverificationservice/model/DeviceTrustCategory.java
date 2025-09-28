package com.smartwatts.deviceverificationservice.model;

public enum DeviceTrustCategory {
    OEM_LOCKED,      // Supplied by SmartWatts OEM or certified installer, fully trusted
    OFFLINE_LOCKED,  // Verified offline with signed activation token, trusted
    UNVERIFIED,      // Any other device, blocked
    EXPIRED          // Device activation expired
}
