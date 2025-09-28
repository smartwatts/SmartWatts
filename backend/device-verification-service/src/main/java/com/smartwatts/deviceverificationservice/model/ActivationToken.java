package com.smartwatts.deviceverificationservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "activation_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "token_hash", nullable = false)
    private String tokenHash;

    @Column(name = "token_type", nullable = false)
    private String tokenType; // ONLINE or OFFLINE

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "activated_at", nullable = false)
    private LocalDateTime activatedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "customer_type", nullable = false)
    private String customerType; // RESIDENTIAL or COMMERCIAL

    @Column(name = "validity_days", nullable = false)
    private Integer validityDays; // 365 for residential, 90 for commercial (initial), 365 for all renewals

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "revoked_reason")
    private String revokedReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Helper methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isResidential() {
        return "RESIDENTIAL".equalsIgnoreCase(customerType);
    }

    public boolean isCommercial() {
        return "COMMERCIAL".equalsIgnoreCase(customerType);
    }

    public boolean isRenewal() {
        // If validity days is 365 and customer is commercial, it's a renewal
        return validityDays == 365 && isCommercial();
    }

    public void revoke(String reason) {
        this.isActive = false;
        this.revokedAt = LocalDateTime.now();
        this.revokedReason = reason;
    }
}
