package com.smartwatts.featureflagservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFeatureAccessDto {

    private String userId;
    private String currentPlan;
    private List<String> enabledFeatures;
    private List<String> disabledFeatures;
    private Boolean hasActiveSubscription;
}
