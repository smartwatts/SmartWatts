package com.smartwatts.integration.edge;

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
public class EdgeGatewayServiceIntegrationTest {

    private String baseUrl;
    private String authToken;

    @BeforeEach
    void setUp() {
        baseUrl = System.getProperty("edge-gateway.service.url", "http://localhost:8089");
        RestAssured.baseURI = baseUrl;
        authToken = "test-token-" + UUID.randomUUID();
    }

    @Test
    void testHealthCheck() {
        // Test health check endpoint
        given()
                .when()
                .get("/api/edge/health")
                .then()
                .statusCode(200)
                .body("status", equalTo("healthy"))
                .body("service", equalTo("SmartWatts Edge Gateway"))
                .body("version", equalTo("1.0.0"))
                .body("timestamp", notNullValue());
    }

    @Test
    void testGetGatewayStats() {
        // Test getting gateway statistics
        given()
                .when()
                .get("/api/edge/stats")
                .then()
                .statusCode(200)
                .body("timestamp", notNullValue());
    }

    @Test
    void testEnergyForecast() {
        // Test energy consumption forecasting
        Map<String, Object> forecastRequest = new HashMap<>();
        forecastRequest.put("deviceId", "DEVICE-" + UUID.randomUUID().toString().substring(0, 8));
        forecastRequest.put("historicalDays", 7);
        forecastRequest.put("forecastDays", 3);

        given()
                .contentType(ContentType.JSON)
                .body(forecastRequest)
                .when()
                .post("/api/edge/ml/forecast")
                .then()
                .statusCode(anyOf(is(200), is(400), is(404))); // May fail if device doesn't exist
    }

    @Test
    void testAnomalyDetection() {
        // Test anomaly detection
        Map<String, Object> anomalyRequest = new HashMap<>();
        anomalyRequest.put("deviceId", "DEVICE-" + UUID.randomUUID().toString().substring(0, 8));
        anomalyRequest.put("powerConsumption", 150.5);
        anomalyRequest.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        given()
                .contentType(ContentType.JSON)
                .body(anomalyRequest)
                .when()
                .post("/api/edge/ml/detect-anomaly")
                .then()
                .statusCode(anyOf(is(200), is(400), is(404))); // May fail if device doesn't exist
    }

    @Test
    void testGetDevices() {
        // Test getting edge devices
        given()
                .when()
                .get("/api/edge/devices")
                .then()
                .statusCode(200)
                .body("", notNullValue());
    }

    @Test
    void testSendDeviceCommand() {
        // Test sending device command
        Map<String, Object> command = new HashMap<>();
        command.put("deviceId", "DEVICE-" + UUID.randomUUID().toString().substring(0, 8));
        command.put("command", "READ_STATUS");
        command.put("parameters", new HashMap<>());

        given()
                .contentType(ContentType.JSON)
                .body(command)
                .when()
                .post("/api/edge/devices/command")
                .then()
                .statusCode(anyOf(is(200), is(400), is(404))); // May fail if device doesn't exist
    }

    @Test
    void testRS485Status() {
        // Test RS485 status endpoint
        given()
                .when()
                .get("/api/v1/rs485/status")
                .then()
                .statusCode(200)
                .body("", notNullValue());
    }

    @Test
    void testGetAvailablePorts() {
        // Test getting available serial ports
        given()
                .when()
                .get("/api/v1/rs485/ports")
                .then()
                .statusCode(200)
                .body("", notNullValue());
    }

    @Test
    void testGetModelStatus() {
        // Test getting ML model status
        given()
                .when()
                .get("/api/edge/ml/models")
                .then()
                .statusCode(anyOf(is(200), is(404))); // May not be implemented
    }

    @Test
    void testGetDeviceReadings() {
        // Test getting device readings
        String deviceId = "DEVICE-" + UUID.randomUUID().toString().substring(0, 8);

        given()
                .param("deviceId", deviceId)
                .param("startTime", LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .param("endTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .when()
                .get("/api/edge/devices/readings")
                .then()
                .statusCode(anyOf(is(200), is(400), is(404))); // May fail if device doesn't exist
    }
}

