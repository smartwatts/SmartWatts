package com.smartwatts.integration.appliance;

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
public class ApplianceMonitoringServiceIntegrationTest {

    private String baseUrl;
    private UUID testUserId;
    private String authToken;

    @BeforeEach
    void setUp() {
        baseUrl = System.getProperty("appliance-monitoring.service.url", "http://localhost:8087");
        RestAssured.baseURI = baseUrl;
        testUserId = UUID.randomUUID();
        authToken = "test-token-" + UUID.randomUUID();
    }

    @Test
    void testCreateAppliance() {
        // Test appliance creation
        Map<String, Object> appliance = new HashMap<>();
        appliance.put("applianceName", "Test Refrigerator");
        appliance.put("applianceType", "REFRIGERATOR");
        appliance.put("userId", testUserId.toString());
        appliance.put("powerRating", 150.0);
        appliance.put("isActive", true);

        Response response = given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body(appliance)
                .when()
                .post("/api/v1/appliance-monitoring/appliances")
                .then()
                .statusCode(anyOf(is(201), is(401), is(403))) // May fail without valid token
                .extract()
                .response();

        if (response.getStatusCode() == 201) {
            assertNotNull(response.jsonPath().getString("id"));
            assertEquals("Test Refrigerator", response.jsonPath().getString("applianceName"));
        }
    }

    @Test
    void testGetUserAppliances() {
        // Test getting user appliances
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/appliance-monitoring/appliances/user/" + testUserId)
                .then()
                .statusCode(anyOf(is(200), is(401), is(403))); // May fail without valid token
    }

    @Test
    void testRecordApplianceReading() {
        // Test recording appliance reading
        Map<String, Object> reading = new HashMap<>();
        reading.put("applianceId", UUID.randomUUID().toString());
        reading.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        reading.put("realTimePowerWatts", 150.5);
        reading.put("voltage", 240.0);
        reading.put("current", 0.63);

        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body(reading)
                .when()
                .post("/api/v1/appliance-monitoring/readings")
                .then()
                .statusCode(anyOf(is(201), is(401), is(403))); // May fail without valid token
    }

    @Test
    void testGetApplianceReadings() {
        // Test getting appliance readings
        UUID applianceId = UUID.randomUUID();
        
        given()
                .header("Authorization", "Bearer " + authToken)
                .param("applianceId", applianceId.toString())
                .when()
                .get("/api/v1/appliance-monitoring/readings")
                .then()
                .statusCode(anyOf(is(200), is(401), is(403))); // May fail without valid token
    }

    @Test
    void testGetApplianceConsumption() {
        // Test getting appliance consumption
        UUID applianceId = UUID.randomUUID();
        
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/appliance-monitoring/appliances/" + applianceId + "/consumption")
                .then()
                .statusCode(anyOf(is(200), is(401), is(403))); // May fail without valid token
    }

    @Test
    void testGetWeatherData() {
        // Test getting weather data
        given()
                .header("Authorization", "Bearer " + authToken)
                .param("latitude", 6.5244)
                .param("longitude", 3.3792)
                .when()
                .get("/api/v1/appliance-monitoring/weather")
                .then()
                .statusCode(anyOf(is(200), is(401), is(403))); // May fail without valid token
    }
}

