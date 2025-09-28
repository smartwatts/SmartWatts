package com.smartwatts.featureflagservice.controller;

import com.smartwatts.featureflagservice.dto.FeatureFlagDto;
import com.smartwatts.featureflagservice.dto.UserFeatureAccessDto;
import com.smartwatts.featureflagservice.service.FeatureFlagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/feature-flags")
@RequiredArgsConstructor
@Slf4j
public class FeatureFlagController {

    private final FeatureFlagService featureFlagService;

    @GetMapping("/features")
    public ResponseEntity<List<FeatureFlagDto>> getAllFeatureFlags() {
        log.info("GET /api/feature-flags/features - Fetching all feature flags");
        List<FeatureFlagDto> features = featureFlagService.getAllFeatureFlags();
        return ResponseEntity.ok(features);
    }

    @GetMapping("/features/{featureKey}")
    public ResponseEntity<FeatureFlagDto> getFeatureFlagByKey(@PathVariable String featureKey) {
        log.info("GET /api/feature-flags/features/{} - Fetching feature flag", featureKey);
        return featureFlagService.getFeatureFlagByKey(featureKey)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/features/globally-enabled")
    public ResponseEntity<List<FeatureFlagDto>> getGloballyEnabledFeatures() {
        log.info("GET /api/feature-flags/features/globally-enabled - Fetching globally enabled features");
        List<FeatureFlagDto> features = featureFlagService.getGloballyEnabledFeatures();
        return ResponseEntity.ok(features);
    }

    @PutMapping("/features/{featureKey}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FeatureFlagDto> updateFeatureFlag(
            @PathVariable String featureKey,
            @RequestBody FeatureFlagDto updateDto) {
        log.info("PUT /api/feature-flags/features/{} - Updating feature flag", featureKey);
        try {
            FeatureFlagDto updated = featureFlagService.updateFeatureFlag(featureKey, updateDto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            log.error("Error updating feature flag: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user-access/{userId}")
    public ResponseEntity<UserFeatureAccessDto> getUserFeatureAccess(@PathVariable UUID userId) {
        log.info("GET /api/feature-flags/user-access/{} - Getting user feature access", userId);
        UserFeatureAccessDto access = featureFlagService.getUserFeatureAccess(userId);
        return ResponseEntity.ok(access);
    }

    @GetMapping("/check/{featureKey}/user/{userId}")
    public ResponseEntity<Boolean> isFeatureEnabledForUser(
            @PathVariable String featureKey,
            @PathVariable UUID userId) {
        log.info("GET /api/feature-flags/check/{}/user/{} - Checking feature access", featureKey, userId);
        boolean isEnabled = featureFlagService.isFeatureEnabledForUser(featureKey, userId);
        return ResponseEntity.ok(isEnabled);
    }

    @PostMapping("/toggle/{featureKey}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FeatureFlagDto> toggleFeatureFlag(@PathVariable String featureKey) {
        log.info("POST /api/feature-flags/toggle/{} - Toggling feature flag", featureKey);
        try {
            FeatureFlagDto current = featureFlagService.getFeatureFlagByKey(featureKey)
                    .orElseThrow(() -> new RuntimeException("Feature flag not found: " + featureKey));
            
            current.setIsGloballyEnabled(!current.getIsGloballyEnabled());
            FeatureFlagDto updated = featureFlagService.updateFeatureFlag(featureKey, current);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            log.error("Error toggling feature flag: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
