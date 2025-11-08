package com.smartwatts.integration.device;

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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(classes = {IntegrationTestConfiguration.class, TestContainersConfig.class})
public class DeviceServiceIntegrationTest {

    private String baseUrl;
    private UUID testUserId;
    private String authToken;

    @BeforeEach
    void setUp() {
        baseUrl = System.getProperty("device.service.url", "http://localhost:8084");
        RestAssured.baseURI = baseUrl;
        testUserId = UUID.randomUUID();
        authToken = "test-token-" + UUID.randomUUID();
    }

    @Test
    void testDeviceRegistrationAndManagement() {
        // Register a new device
        Map<String, Object> device = new HashMap<>();
        device.put("userId", testUserId.toString());
        device.put("deviceId", "METER-001");
        device.put("name", "Smart Meter 001");
        device.put("description", "Main energy meter");
        device.put("deviceType", "SMART_METER");
        device.put("protocol", "MQTT");
        device.put("connectionString", "tcp://localhost:1883");
        device.put("locationLat", 6.5244);
        device.put("locationLng", 3.3792);

        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(device)
                .when()
                .post("/api/v1/devices")
                .then()
                .statusCode(201)
                .extract()
                .response();

        String deviceId = response.jsonPath().getString("id");
        assertNotNull(deviceId);

        // Get device by ID
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/devices/" + deviceId)
                .then()
                .statusCode(200)
                .body("id", equalTo(deviceId))
                .body("name", equalTo("Smart Meter 001"))
                .body("deviceType", equalTo("SMART_METER"));

        // Get user devices
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/devices/user/" + testUserId)
                .then()
                .statusCode(200)
                .body("content", hasSize(greaterThan(0)))
                .body("content[0].userId", equalTo(testUserId.toString()));
    }

    @Test
    void testGeneratorHealthMonitoring() {
        // Test generator health endpoint
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/devices/generator/" + testUserId + "/health")
                .then()
                .statusCode(200)
                .body("userId", equalTo(testUserId.toString()))
                .body("generatorId", notNullValue())
                .body("status", notNullValue())
                .body("runtimeHours", notNullValue())
                .body("batteryVoltage", notNullValue())
                .body("batteryStatus", notNullValue())
                .body("oilLevel", notNullValue())
                .body("coolantTemperature", notNullValue())
                .body("fuelLevel", notNullValue());
    }

    @Test
    void testFuelConsumptionTracking() {
        // Test fuel consumption endpoint
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/devices/generator/" + testUserId + "/fuel-consumption")
                .then()
                .statusCode(200)
                .body("userId", equalTo(testUserId.toString()))
                .body("generatorId", notNullValue())
                .body("totalFuelUsed", notNullValue())
                .body("averageDailyUsage", notNullValue())
                .body("costPerLiter", notNullValue())
                .body("totalFuelCost", notNullValue())
                .body("efficiency", notNullValue())
                .body("costPerKwh", notNullValue());
    }

    @Test
    void testMaintenanceScheduleManagement() {
        // Test maintenance schedule endpoint
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/devices/generator/" + testUserId + "/maintenance")
                .then()
                .statusCode(200)
                .body("userId", equalTo(testUserId.toString()))
                .body("generatorId", notNullValue())
                .body("nextOilChange", notNullValue())
                .body("nextAirFilterChange", notNullValue())
                .body("nextSparkPlugChange", notNullValue())
                .body("nextMajorService", notNullValue())
                .body("maintenanceHistory", notNullValue())
                .body("alerts", notNullValue());
    }

    @Test
    void testGeneratorRuntimeHistory() {
        // Test runtime history endpoint
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/devices/generator/" + testUserId + "/runtime-history")
                .then()
                .statusCode(200)
                .body("", hasSize(greaterThan(0)))
                .body("[0].eventId", notNullValue())
                .body("[0].startTime", notNullValue())
                .body("[0].endTime", notNullValue())
                .body("[0].duration", notNullValue())
                .body("[0].reason", notNullValue())
                .body("[0].fuelUsed", notNullValue())
                .body("[0].cost", notNullValue())
                .body("[0].powerGenerated", notNullValue());
    }

    @Test
    void testDeviceConfigurationManagement() {
        // Register a device first
        Map<String, Object> device = new HashMap<>();
        device.put("userId", testUserId.toString());
        device.put("deviceId", "CONFIG-001");
        device.put("name", "Config Test Device");
        device.put("deviceType", "SMART_METER");

        Response deviceResponse = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(device)
                .when()
                .post("/api/v1/devices")
                .then()
                .statusCode(201)
                .extract()
                .response();

        String deviceId = deviceResponse.jsonPath().getString("id");

        // Save device configuration
        Map<String, Object> config = new HashMap<>();
        config.put("deviceId", UUID.fromString(deviceId));
        config.put("configKey", "sampling_rate");
        config.put("configValue", "60");
        config.put("description", "Energy reading sampling rate in seconds");

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(config)
                .when()
                .post("/api/v1/devices/" + deviceId + "/configurations")
                .then()
                .statusCode(201);

        // Get device configurations
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/devices/" + deviceId + "/configurations")
                .then()
                .statusCode(200)
                .body("", hasSize(greaterThan(0)))
                .body("[0].configKey", equalTo("sampling_rate"))
                .body("[0].configValue", equalTo("60"));
    }

    @Test
    void testDeviceEventTracking() {
        // Register a device first
        Map<String, Object> device = new HashMap<>();
        device.put("userId", testUserId.toString());
        device.put("deviceId", "EVENT-001");
        device.put("name", "Event Test Device");
        device.put("deviceType", "SMART_METER");

        Response deviceResponse = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(device)
                .when()
                .post("/api/v1/devices")
                .then()
                .statusCode(201)
                .extract()
                .response();

        String deviceId = deviceResponse.jsonPath().getString("id");

        // Get device events
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/devices/" + deviceId + "/events")
                .then()
                .statusCode(200)
                .body("content", notNullValue());
    }

    @Test
    void testDeviceStatusUpdates() {
        // Register a device first
        Map<String, Object> device = new HashMap<>();
        device.put("userId", testUserId.toString());
        device.put("deviceId", "STATUS-001");
        device.put("name", "Status Test Device");
        device.put("deviceType", "SMART_METER");

        Response deviceResponse = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(device)
                .when()
                .post("/api/v1/devices")
                .then()
                .statusCode(201)
                .extract()
                .response();

        String deviceId = deviceResponse.jsonPath().getString("id");

        // Update device status
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(Map.of("status", "INACTIVE"))
                .when()
                .put("/api/v1/devices/" + deviceId + "/status")
                .then()
                .statusCode(200)
                .body("status", equalTo("INACTIVE"));

        // Update connection status
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(Map.of("connectionStatus", "ONLINE"))
                .when()
                .put("/api/v1/devices/" + deviceId + "/connection-status")
                .then()
                .statusCode(200)
                .body("connectionStatus", equalTo("ONLINE"));
    }

    @Test
    void testDeviceMaintenanceTracking() {
        // Test devices needing maintenance
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/devices/maintenance-needed")
                .then()
                .statusCode(200)
                .body("", notNullValue());

        // Test devices needing calibration
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/devices/calibration-needed")
                .then()
                .statusCode(200)
                .body("", notNullValue());
    }
}





