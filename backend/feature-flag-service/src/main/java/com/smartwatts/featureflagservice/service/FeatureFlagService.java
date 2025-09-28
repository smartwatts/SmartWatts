package com.smartwatts.featureflagservice.service;

import com.smartwatts.featureflagservice.dto.FeatureFlagDto;
import com.smartwatts.featureflagservice.dto.UserFeatureAccessDto;
import com.smartwatts.featureflagservice.model.FeatureFlag;
import com.smartwatts.featureflagservice.model.UserSubscription;
import com.smartwatts.featureflagservice.repository.FeatureFlagRepository;
import com.smartwatts.featureflagservice.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FeatureFlagService {

    private final FeatureFlagRepository featureFlagRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;

    public List<FeatureFlagDto> getAllFeatureFlags() {
        log.info("Fetching all feature flags");
        return featureFlagRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<FeatureFlagDto> getFeatureFlagByKey(String featureKey) {
        log.info("Fetching feature flag with key: {}", featureKey);
        return featureFlagRepository.findByFeatureKey(featureKey)
                .map(this::convertToDto);
    }

    public List<FeatureFlagDto> getGloballyEnabledFeatures() {
        log.info("Fetching globally enabled feature flags");
        return featureFlagRepository.findAllGloballyEnabled().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public FeatureFlagDto updateFeatureFlag(String featureKey, FeatureFlagDto updateDto) {
        log.info("Updating feature flag with key: {}", featureKey);
        FeatureFlag featureFlag = featureFlagRepository.findByFeatureKey(featureKey)
                .orElseThrow(() -> new RuntimeException("Feature flag not found: " + featureKey));

        featureFlag.setIsGloballyEnabled(updateDto.getIsGloballyEnabled());
        featureFlag.setIsPaidFeature(updateDto.getIsPaidFeature());
        featureFlag.setFeatureCategory(updateDto.getFeatureCategory());

        FeatureFlag saved = featureFlagRepository.save(featureFlag);
        return convertToDto(saved);
    }

    public boolean isFeatureEnabledForUser(String featureKey, UUID userId) {
        log.info("Checking if feature {} is enabled for user: {}", featureKey, userId);
        
        FeatureFlag featureFlag = featureFlagRepository.findByFeatureKey(featureKey)
                .orElse(null);
        
        if (featureFlag == null) {
            log.warn("Feature flag not found: {}", featureKey);
            return false;
        }

        // If feature is globally disabled, return false
        if (!featureFlag.getIsGloballyEnabled()) {
            log.debug("Feature {} is globally disabled", featureKey);
            return false;
        }

        // If feature is not paid, return true
        if (!featureFlag.getIsPaidFeature()) {
            log.debug("Feature {} is free, allowing access", featureKey);
            return true;
        }

        // Check user subscription for paid features
        Optional<UserSubscription> activeSubscription = userSubscriptionRepository
                .findActiveSubscriptionByUserId(userId, LocalDateTime.now());

        if (activeSubscription.isEmpty()) {
            log.debug("User {} has no active subscription for paid feature {}", userId, featureKey);
            return false;
        }

        // Check if user's plan includes this feature
        boolean hasAccess = checkSubscriptionFeatureAccess(featureKey, activeSubscription.get().getSubscriptionPlan().getPlanKey());
        log.debug("User {} access to feature {}: {}", userId, featureKey, hasAccess);
        
        return hasAccess;
    }

    public UserFeatureAccessDto getUserFeatureAccess(UUID userId) {
        log.info("Getting feature access for user: {}", userId);
        
        Optional<UserSubscription> activeSubscription = userSubscriptionRepository
                .findActiveSubscriptionByUserId(userId, LocalDateTime.now());

        String currentPlan = activeSubscription.map(sub -> sub.getSubscriptionPlan().getPlanKey()).orElse("FREEMIUM");
        Boolean hasActiveSubscription = activeSubscription.isPresent();

        List<FeatureFlag> allFeatures = featureFlagRepository.findAll();
        List<String> enabledFeatures = new ArrayList<>();
        List<String> disabledFeatures = new ArrayList<>();

        for (FeatureFlag feature : allFeatures) {
            if (isFeatureEnabledForUser(feature.getFeatureKey(), userId)) {
                enabledFeatures.add(feature.getFeatureKey());
            } else {
                disabledFeatures.add(feature.getFeatureKey());
            }
        }

        return new UserFeatureAccessDto(
                userId.toString(),
                currentPlan,
                enabledFeatures,
                disabledFeatures,
                hasActiveSubscription
        );
    }

    private boolean checkSubscriptionFeatureAccess(String featureKey, String planKey) {
        // Define feature access by plan
        switch (planKey) {
            case "FREEMIUM":
                return featureKey.equals("BASIC_MONITORING") || featureKey.equals("BASIC_ANALYTICS");
            case "PREMIUM":
                return featureKey.equals("BASIC_MONITORING") || 
                       featureKey.equals("BASIC_ANALYTICS") || 
                       featureKey.equals("FACILITY360") || 
                       featureKey.equals("BILLING_DASHBOARD");
            case "ENTERPRISE":
                return true; // All features
            default:
                return false;
        }
    }

    private FeatureFlagDto convertToDto(FeatureFlag featureFlag) {
        return new FeatureFlagDto(
                featureFlag.getId(),
                featureFlag.getFeatureKey(),
                featureFlag.getFeatureName(),
                featureFlag.getDescription(),
                featureFlag.getIsGloballyEnabled(),
                featureFlag.getIsPaidFeature(),
                featureFlag.getFeatureCategory(),
                featureFlag.getCreatedAt(),
                featureFlag.getUpdatedAt()
        );
    }
}
