package com.smartwatts.featureflagservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "feature_flags")
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class FeatureFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "feature_key", nullable = false, unique = true)
    private String featureKey;

    @Column(name = "feature_name", nullable = false)
    private String featureName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_globally_enabled", nullable = false)
    private Boolean isGloballyEnabled = false;

    @Column(name = "is_paid_feature", nullable = false)
    private Boolean isPaidFeature = false;

    @Column(name = "feature_category", nullable = false)
    private String featureCategory;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
