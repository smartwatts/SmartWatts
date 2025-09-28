package com.smartwatts.featureflagservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeatureFlagDto {

    private UUID id;
    private String featureKey;
    private String featureName;
    private String description;
    private Boolean isGloballyEnabled;
    private Boolean isPaidFeature;
    private String featureCategory;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
