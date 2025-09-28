package com.smartwatts.deviceverificationservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "device_verifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "device_id", unique = true, nullable = false)
    private String deviceId;

    @Column(name = "device_type", nullable = false)
    private String deviceType;

    @Column(name = "hardware_id", nullable = false)
    private String hardwareId;

    @Column(name = "firmware_hash")
    private String firmwareHash;

    @Column(name = "firmware_version")
    private String firmwareVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "trust_category", nullable = false)
    private DeviceTrustCategory trustCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeviceStatus status;

    @Column(name = "customer_type", nullable = false)
    private String customerType; // RESIDENTIAL or COMMERCIAL

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "installer_id")
    private UUID installerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "installer_tier")
    private InstallerTier installerTier;

    @Column(name = "location_lat", precision = 10, scale = 8)
    private BigDecimal locationLat;

    @Column(name = "location_lng", precision = 11, scale = 8)
    private BigDecimal locationLng;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "activation_token")
    private String activationToken;

    @Column(name = "activation_attempts")
    private Integer activationAttempts;

    @Column(name = "last_activation_attempt")
    private LocalDateTime lastActivationAttempt;

    @Column(name = "tamper_detected")
    private Boolean tamperDetected;

    @Column(name = "tamper_details")
    private String tamperDetails;

    @Column(name = "docker_startup_valid")
    private Boolean dockerStartupValid;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Helper methods
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isActive() {
        return DeviceStatus.ACTIVE.equals(status) && !isExpired();
    }

    public boolean isResidential() {
        return "RESIDENTIAL".equalsIgnoreCase(customerType);
    }

    public boolean isCommercial() {
        return "COMMERCIAL".equalsIgnoreCase(customerType);
    }

    public int getValidityDays() {
        return isResidential() ? 365 : 90;
    }

    public void incrementActivationAttempts() {
        this.activationAttempts = (this.activationAttempts == null ? 0 : this.activationAttempts) + 1;
        this.lastActivationAttempt = LocalDateTime.now();
    }

    public void resetActivationAttempts() {
        this.activationAttempts = 0;
        this.lastActivationAttempt = null;
    }
}
