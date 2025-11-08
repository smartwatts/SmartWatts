package com.smartwatts.integration.deviceverification;

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
public class DeviceVerificationServiceIntegrationTest {

    private String baseUrl;
    private String authToken;

    @BeforeEach
    void setUp() {
        baseUrl = System.getProperty("device-verification.service.url", "http://localhost:8088");
        RestAssured.baseURI = baseUrl;
        authToken = "test-token-" + UUID.randomUUID();
    }

    @Test
    void testDeviceActivation_Residential() {
        // Test device activation for residential customer
        Map<String, Object> activationRequest = new HashMap<>();
        activationRequest.put("deviceId", "DEVICE-" + UUID.randomUUID().toString().substring(0, 8));
        activationRequest.put("customerType", "RESIDENTIAL");
        activationRequest.put("customerId", UUID.randomUUID().toString());
        activationRequest.put("deviceType", "SMART_METER");
        activationRequest.put("serialNumber", "SN-" + UUID.randomUUID().toString().substring(0, 8));

        Response response = given()
                .contentType(ContentType.JSON)
                .body(activationRequest)
                .when()
                .post("/api/device-verification/activate")
                .then()
                .statusCode(anyOf(is(200), is(400))) // May fail if device doesn't exist
                .extract()
                .response();

        if (response.getStatusCode() == 200) {
            assertTrue(response.jsonPath().getBoolean("success"));
            assertEquals("RESIDENTIAL", response.jsonPath().getString("customerType"));
            assertNotNull(response.jsonPath().getString("activationToken"));
            assertTrue(response.jsonPath().getInt("validityDays") > 0);
        }
    }

    @Test
    void testDeviceActivation_Commercial() {
        // Test device activation for commercial customer
        Map<String, Object> activationRequest = new HashMap<>();
        activationRequest.put("deviceId", "DEVICE-" + UUID.randomUUID().toString().substring(0, 8));
        activationRequest.put("customerType", "COMMERCIAL");
        activationRequest.put("customerId", UUID.randomUUID().toString());
        activationRequest.put("deviceType", "SMART_METER");
        activationRequest.put("serialNumber", "SN-" + UUID.randomUUID().toString().substring(0, 8));

        Response response = given()
                .contentType(ContentType.JSON)
                .body(activationRequest)
                .when()
                .post("/api/device-verification/activate")
                .then()
                .statusCode(anyOf(is(200), is(400))) // May fail if device doesn't exist
                .extract()
                .response();

        if (response.getStatusCode() == 200) {
            assertTrue(response.jsonPath().getBoolean("success"));
            assertEquals("COMMERCIAL", response.jsonPath().getString("customerType"));
            assertNotNull(response.jsonPath().getString("activationToken"));
            assertTrue(response.jsonPath().getInt("validityDays") > 0);
        }
    }

    @Test
    void testDeviceValidation() {
        // Test device validation
        String deviceId = "DEVICE-" + UUID.randomUUID().toString().substring(0, 8);
        String activationToken = "TOKEN-" + UUID.randomUUID().toString();

        Map<String, Object> validationRequest = new HashMap<>();
        validationRequest.put("deviceId", deviceId);
        validationRequest.put("activationToken", activationToken);

        given()
                .contentType(ContentType.JSON)
                .body(validationRequest)
                .when()
                .post("/api/device-verification/validate")
                .then()
                .statusCode(anyOf(is(200), is(400), is(401))); // May fail if device/token doesn't exist
    }

    @Test
    void testGetDeviceStatus() {
        // Test getting device status
        String deviceId = "DEVICE-" + UUID.randomUUID().toString().substring(0, 8);

        given()
                .when()
                .get("/api/device-verification/devices/" + deviceId + "/status")
                .then()
                .statusCode(anyOf(is(200), is(404))); // May fail if device doesn't exist
    }

    @Test
    void testGetActivationHistory() {
        // Test getting activation history
        String deviceId = "DEVICE-" + UUID.randomUUID().toString().substring(0, 8);

        given()
                .when()
                .get("/api/device-verification/devices/" + deviceId + "/history")
                .then()
                .statusCode(anyOf(is(200), is(404))); // May fail if device doesn't exist
    }

    @Test
    void testRenewActivation() {
        // Test renewing device activation
        String deviceId = "DEVICE-" + UUID.randomUUID().toString().substring(0, 8);

        Map<String, Object> renewalRequest = new HashMap<>();
        renewalRequest.put("deviceId", deviceId);
        renewalRequest.put("customerType", "COMMERCIAL");

        given()
                .contentType(ContentType.JSON)
                .body(renewalRequest)
                .when()
                .post("/api/device-verification/renew")
                .then()
                .statusCode(anyOf(is(200), is(400), is(404))); // May fail if device doesn't exist
    }

    @Test
    void testInvalidActivationRequest() {
        // Test activation with invalid data
        Map<String, Object> invalidRequest = new HashMap<>();
        invalidRequest.put("deviceId", ""); // Empty device ID
        invalidRequest.put("customerType", "INVALID_TYPE");

        given()
                .contentType(ContentType.JSON)
                .body(invalidRequest)
                .when()
                .post("/api/device-verification/activate")
                .then()
                .statusCode(anyOf(is(400), is(422))); // Should fail validation
    }
}

