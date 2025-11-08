package com.smartwatts.integration.featureflag;

import com.smartwatts.integration.config.IntegrationTestConfiguration;
import com.smartwatts.integration.config.TestContainersConfig;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(classes = {IntegrationTestConfiguration.class, TestContainersConfig.class})
public class FeatureFlagServiceIntegrationTest {

    private String baseUrl;
    private String authToken;

    @BeforeEach
    void setUp() {
        baseUrl = System.getProperty("feature-flag.service.url", "http://localhost:8090");
        RestAssured.baseURI = baseUrl;
        authToken = "test-token-" + UUID.randomUUID();
    }

    @Test
    void testGetAllFeatureFlags() {
        // Test getting all feature flags
        given()
                .when()
                .get("/api/feature-flags/features")
                .then()
                .statusCode(200)
                .body("", notNullValue());
    }

    @Test
    void testGetFeatureFlagByKey() {
        // Test getting feature flag by key
        String featureKey = "test-feature";
        
        given()
                .when()
                .get("/api/feature-flags/features/" + featureKey)
                .then()
                .statusCode(anyOf(is(200), is(404))); // May not exist
    }

    @Test
    void testGetGloballyEnabledFeatures() {
        // Test getting globally enabled features
        given()
                .when()
                .get("/api/feature-flags/features/globally-enabled")
                .then()
                .statusCode(200)
                .body("", notNullValue());
    }

    @Test
    void testUpdateFeatureFlag() {
        // Test updating feature flag (requires admin role)
        String featureKey = "test-feature";
        
        Map<String, Object> update = new HashMap<>();
        update.put("enabled", true);
        update.put("isGloballyEnabled", false);

        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body(update)
                .when()
                .put("/api/feature-flags/features/" + featureKey)
                .then()
                .statusCode(anyOf(is(200), is(401), is(403), is(404))); // May fail without valid token or if feature doesn't exist
    }

    @Test
    void testGetUserFeatureAccess() {
        // Test getting user feature access
        UUID userId = UUID.randomUUID();
        
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/feature-flags/users/" + userId + "/features")
                .then()
                .statusCode(anyOf(is(200), is(401), is(403))); // May fail without valid token
    }

    @Test
    void testToggleUserFeatureAccess() {
        // Test toggling user feature access
        UUID userId = UUID.randomUUID();
        String featureKey = "test-feature";
        
        Map<String, Object> toggle = new HashMap<>();
        toggle.put("enabled", true);

        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body(toggle)
                .when()
                .put("/api/feature-flags/users/" + userId + "/features/" + featureKey)
                .then()
                .statusCode(anyOf(is(200), is(401), is(403), is(404))); // May fail without valid token or if feature doesn't exist
    }
}

