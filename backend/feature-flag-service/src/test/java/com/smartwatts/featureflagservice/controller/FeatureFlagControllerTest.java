package com.smartwatts.featureflagservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartwatts.featureflagservice.dto.FeatureFlagDto;
import com.smartwatts.featureflagservice.dto.UserFeatureAccessDto;
import com.smartwatts.featureflagservice.service.FeatureFlagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FeatureFlagController.class)
class FeatureFlagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FeatureFlagService featureFlagService;

    @Autowired
    private ObjectMapper objectMapper;

    private FeatureFlagDto testFeatureFlagDto;
    private String testFeatureKey;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testFeatureKey = "BASIC_MONITORING";
        testUserId = UUID.randomUUID();
        
        testFeatureFlagDto = new FeatureFlagDto();
        testFeatureFlagDto.setId(UUID.randomUUID());
        testFeatureFlagDto.setFeatureKey(testFeatureKey);
        testFeatureFlagDto.setFeatureName("Basic Monitoring");
        testFeatureFlagDto.setDescription("Basic energy monitoring feature");
        testFeatureFlagDto.setIsGloballyEnabled(true);
        testFeatureFlagDto.setIsPaidFeature(false);
        testFeatureFlagDto.setFeatureCategory("MONITORING");
    }

    @Test
    void getAllFeatureFlags_Success_ReturnsList() throws Exception {
        // Given
        List<FeatureFlagDto> features = Arrays.asList(testFeatureFlagDto);
        when(featureFlagService.getAllFeatureFlags()).thenReturn(features);

        // When & Then
        mockMvc.perform(get("/api/feature-flags/features"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].featureKey").value(testFeatureKey));

        verify(featureFlagService).getAllFeatureFlags();
    }

    @Test
    void getFeatureFlagByKey_Success_ReturnsFeatureFlag() throws Exception {
        // Given
        when(featureFlagService.getFeatureFlagByKey(testFeatureKey)).thenReturn(Optional.of(testFeatureFlagDto));

        // When & Then
        mockMvc.perform(get("/api/feature-flags/features/{featureKey}", testFeatureKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.featureKey").value(testFeatureKey))
                .andExpect(jsonPath("$.featureName").value("Basic Monitoring"));

        verify(featureFlagService).getFeatureFlagByKey(testFeatureKey);
    }

    @Test
    void getFeatureFlagByKey_NotFound_ReturnsNotFound() throws Exception {
        // Given
        when(featureFlagService.getFeatureFlagByKey(testFeatureKey)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/feature-flags/features/{featureKey}", testFeatureKey))
                .andExpect(status().isNotFound());

        verify(featureFlagService).getFeatureFlagByKey(testFeatureKey);
    }

    @Test
    void getGloballyEnabledFeatures_Success_ReturnsList() throws Exception {
        // Given
        List<FeatureFlagDto> features = Arrays.asList(testFeatureFlagDto);
        when(featureFlagService.getGloballyEnabledFeatures()).thenReturn(features);

        // When & Then
        mockMvc.perform(get("/api/feature-flags/features/globally-enabled"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(featureFlagService).getGloballyEnabledFeatures();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateFeatureFlag_Success_ReturnsUpdatedFeatureFlag() throws Exception {
        // Given
        testFeatureFlagDto.setIsGloballyEnabled(false);
        when(featureFlagService.updateFeatureFlag(eq(testFeatureKey), any(FeatureFlagDto.class))).thenReturn(testFeatureFlagDto);

        // When & Then
        mockMvc.perform(put("/api/feature-flags/features/{featureKey}", testFeatureKey)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testFeatureFlagDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isGloballyEnabled").value(false));

        verify(featureFlagService).updateFeatureFlag(eq(testFeatureKey), any(FeatureFlagDto.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateFeatureFlag_Error_ReturnsBadRequest() throws Exception {
        // Given
        when(featureFlagService.updateFeatureFlag(eq(testFeatureKey), any(FeatureFlagDto.class)))
                .thenThrow(new RuntimeException("Error updating feature flag"));

        // When & Then
        mockMvc.perform(put("/api/feature-flags/features/{featureKey}", testFeatureKey)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testFeatureFlagDto)))
                .andExpect(status().isBadRequest());

        verify(featureFlagService).updateFeatureFlag(eq(testFeatureKey), any(FeatureFlagDto.class));
    }

    @Test
    void getUserFeatureAccess_Success_ReturnsAccess() throws Exception {
        // Given
        UserFeatureAccessDto accessDto = new UserFeatureAccessDto();
        accessDto.setUserId(testUserId.toString());
        accessDto.setCurrentPlan("FREEMIUM");
        accessDto.setEnabledFeatures(Arrays.asList("BASIC_MONITORING"));
        accessDto.setDisabledFeatures(Arrays.asList("ADVANCED_ANALYTICS"));
        accessDto.setHasActiveSubscription(false);
        when(featureFlagService.getUserFeatureAccess(testUserId)).thenReturn(accessDto);

        // When & Then
        mockMvc.perform(get("/api/feature-flags/user-access/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUserId.toString()))
                .andExpect(jsonPath("$.currentPlan").value("FREEMIUM"));

        verify(featureFlagService).getUserFeatureAccess(testUserId);
    }

    @Test
    void isFeatureEnabledForUser_Success_ReturnsBoolean() throws Exception {
        // Given
        when(featureFlagService.isFeatureEnabledForUser(testFeatureKey, testUserId)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/feature-flags/check/{featureKey}/user/{userId}", testFeatureKey, testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(featureFlagService).isFeatureEnabledForUser(testFeatureKey, testUserId);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void toggleFeatureFlag_Success_ReturnsToggledFeatureFlag() throws Exception {
        // Given
        testFeatureFlagDto.setIsGloballyEnabled(false);
        when(featureFlagService.updateFeatureFlag(eq(testFeatureKey), any(FeatureFlagDto.class))).thenReturn(testFeatureFlagDto);

        // When & Then
        mockMvc.perform(post("/api/feature-flags/toggle/{featureKey}", testFeatureKey)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isGloballyEnabled").value(false));

        verify(featureFlagService).updateFeatureFlag(eq(testFeatureKey), any(FeatureFlagDto.class));
    }
}

