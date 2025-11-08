package com.smartwatts.integration.energy;

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
public class EnergyServiceIntegrationTest {

    private String baseUrl;
    private UUID testUserId;
    private String authToken;

    @BeforeEach
    void setUp() {
        baseUrl = System.getProperty("energy.service.url", "http://localhost:8082");
        RestAssured.baseURI = baseUrl;
        testUserId = UUID.randomUUID();
        authToken = "test-token-" + UUID.randomUUID();
    }

    @Test
    void testEnergyReadingsCRUD() {
        // Create energy reading
        Map<String, Object> reading = new HashMap<>();
        reading.put("userId", testUserId.toString());
        reading.put("deviceId", "METER-001");
        reading.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        reading.put("powerConsumption", 2.5);
        reading.put("voltage", 240.0);
        reading.put("current", 10.4);
        reading.put("frequency", 50.0);
        reading.put("powerFactor", 0.95);
        reading.put("source", "GRID");

        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(reading)
                .when()
                .post("/api/v1/energy/readings")
                .then()
                .statusCode(201)
                .extract()
                .response();

        String readingId = response.jsonPath().getString("id");
        assertNotNull(readingId);

        // Get energy reading
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/energy/readings/" + readingId)
                .then()
                .statusCode(200)
                .body("id", equalTo(readingId))
                .body("powerConsumption", equalTo(2.5f))
                .body("voltage", equalTo(240.0f));

        // Get user energy readings
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/energy/readings/user/" + testUserId)
                .then()
                .statusCode(200)
                .body("content", hasSize(greaterThan(0)))
                .body("content[0].userId", equalTo(testUserId.toString()));
    }

    @Test
    void testMultiSourceEnergyTracking() {
        // Create readings from different sources
        String[] sources = {"GRID", "SOLAR", "INVERTER", "GENERATOR"};
        
        for (String source : sources) {
            Map<String, Object> reading = new HashMap<>();
            reading.put("userId", testUserId.toString());
            reading.put("deviceId", "METER-" + source);
            reading.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            reading.put("powerConsumption", 1.5);
            reading.put("source", source);

            given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + authToken)
                    .body(reading)
                    .when()
                    .post("/api/v1/energy/readings")
                    .then()
                    .statusCode(201);
        }

        // Test source breakdown
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/energy/source-breakdown/" + testUserId)
                .then()
                .statusCode(200)
                .body("userId", equalTo(testUserId.toString()))
                .body("sources", hasKey("GRID"))
                .body("sources", hasKey("SOLAR"))
                .body("sources", hasKey("INVERTER"))
                .body("sources", hasKey("GENERATOR"));
    }

    @Test
    void testDiscoStatusMonitoring() {
        // Test DisCo status endpoint
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/energy/disco-status/" + testUserId)
                .then()
                .statusCode(200)
                .body("userId", equalTo(testUserId.toString()))
                .body("disco", notNullValue())
                .body("availability", notNullValue())
                .body("outageHistory", notNullValue());
    }

    @Test
    void testVoltageQualityMonitoring() {
        // Test voltage quality endpoint
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/energy/voltage-quality/" + testUserId)
                .then()
                .statusCode(200)
                .body("userId", equalTo(testUserId.toString()))
                .body("voltage", notNullValue())
                .body("fluctuations", notNullValue())
                .body("powerFactor", notNullValue());
    }

    @Test
    void testGridStabilityMetrics() {
        // Test grid stability endpoint
        given()
                .when()
                .get("/api/v1/energy/grid-stability")
                .then()
                .statusCode(200)
                .body("voltage", notNullValue())
                .body("frequency", notNullValue())
                .body("phaseBalance", notNullValue())
                .body("timestamp", notNullValue());
    }

    @Test
    void testEnergyConsumptionAnalytics() {
        // Create multiple readings for analytics
        for (int i = 0; i < 5; i++) {
            Map<String, Object> reading = new HashMap<>();
            reading.put("userId", testUserId.toString());
            reading.put("deviceId", "METER-001");
            reading.put("timestamp", LocalDateTime.now().minusHours(i).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            reading.put("powerConsumption", 2.0 + i);
            reading.put("source", "GRID");

            given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + authToken)
                    .body(reading)
                    .when()
                    .post("/api/v1/energy/readings")
                    .then()
                    .statusCode(201);
        }

        // Test consumption analytics
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/energy/consumption/user/" + testUserId)
                .then()
                .statusCode(200)
                .body("userId", equalTo(testUserId.toString()))
                .body("totalConsumption", notNullValue())
                .body("averageConsumption", notNullValue());
    }

    @Test
    void testEnergyAlerts() {
        // Create high consumption reading to trigger alert
        Map<String, Object> reading = new HashMap<>();
        reading.put("userId", testUserId.toString());
        reading.put("deviceId", "METER-001");
        reading.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        reading.put("powerConsumption", 10.0); // High consumption
        reading.put("source", "GRID");

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(reading)
                .when()
                .post("/api/v1/energy/readings")
                .then()
                .statusCode(201);

        // Check for alerts
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/energy/alerts/user/" + testUserId)
                .then()
                .statusCode(200)
                .body("content", notNullValue());
    }
}





