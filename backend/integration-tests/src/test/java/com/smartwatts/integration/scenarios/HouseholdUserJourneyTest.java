package com.smartwatts.integration.scenarios;

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
public class HouseholdUserJourneyTest {

    private String apiGatewayUrl;
    private UUID testUserId;
    private String authToken;
    private String deviceId;

    @BeforeEach
    void setUp() {
        apiGatewayUrl = System.getProperty("api.gateway.url", "http://localhost:8080");
        RestAssured.baseURI = apiGatewayUrl;
        testUserId = UUID.randomUUID();
        authToken = "test-token-" + UUID.randomUUID();
    }

    @Test
    void testCompleteHouseholdUserJourney() {
        // Step 1: User registers and logs in
        Map<String, Object> user = new HashMap<>();
        user.put("username", "household_user_" + testUserId.toString().substring(0, 8));
        user.put("email", "household@example.com");
        user.put("password", "SecurePassword123!");
        user.put("firstName", "John");
        user.put("lastName", "Doe");
        user.put("userType", "HOUSEHOLD");

        Response userResponse = given()
                .contentType(ContentType.JSON)
                .body(user)
                .when()
                .post("/api/v1/users/register")
                .then()
                .statusCode(201)
                .extract()
                .response();

        String userId = userResponse.jsonPath().getString("id");
        assertNotNull(userId);

        // Step 2: User adds smart meter device
        Map<String, Object> device = new HashMap<>();
        device.put("userId", userId);
        device.put("deviceId", "METER-HOUSEHOLD-001");
        device.put("name", "Main Energy Meter");
        device.put("description", "Primary energy meter for household");
        device.put("deviceType", "SMART_METER");
        device.put("protocol", "MQTT");
        device.put("connectionString", "tcp://localhost:1883");
        device.put("locationLat", 6.5244);
        device.put("locationLng", 3.3792);

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

        deviceId = deviceResponse.jsonPath().getString("id");
        assertNotNull(deviceId);

        // Step 3: System receives energy readings
        for (int i = 0; i < 5; i++) {
            Map<String, Object> reading = new HashMap<>();
            reading.put("userId", userId);
            reading.put("deviceId", "METER-HOUSEHOLD-001");
            reading.put("timestamp", LocalDateTime.now().minusHours(i).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            reading.put("powerConsumption", 2.0 + (i * 0.5));
            reading.put("voltage", 240.0);
            reading.put("current", 8.3 + (i * 0.2));
            reading.put("frequency", 50.0);
            reading.put("powerFactor", 0.95);
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

        // Step 4: Dashboard displays consumption data
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/analytics/dashboard-stats")
                .then()
                .statusCode(200)
                .body("totalConsumption", notNullValue())
                .body("totalCost", notNullValue())
                .body("averageEfficiency", notNullValue());

        // Step 5: User checks prepaid token balance
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/billing/prepaid-tokens/" + userId)
                .then()
                .statusCode(200)
                .body("userId", equalTo(userId))
                .body("currentBalance", notNullValue())
                .body("consumptionRate", notNullValue())
                .body("daysUntilDepletion", notNullValue());

        // Step 6: User monitors generator runtime
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/devices/generator/" + userId + "/health")
                .then()
                .statusCode(200)
                .body("userId", equalTo(userId))
                .body("generatorId", notNullValue())
                .body("status", notNullValue())
                .body("runtimeHours", notNullValue());

        // Step 7: User views cost breakdown by source
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/energy/source-breakdown/" + userId)
                .then()
                .statusCode(200)
                .body("userId", equalTo(userId))
                .body("sources", notNullValue())
                .body("totalCost", notNullValue())
                .body("costBreakdown", notNullValue());

        // Step 8: User gets energy efficiency recommendations
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/analytics/cost-optimizations")
                .then()
                .statusCode(200)
                .body("", hasSize(greaterThan(0)))
                .body("[0].type", notNullValue())
                .body("[0].description", notNullValue())
                .body("[0].potentialSavings", notNullValue());

        // Step 9: User checks DisCo status
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/energy/disco-status/" + userId)
                .then()
                .statusCode(200)
                .body("userId", equalTo(userId))
                .body("disco", notNullValue())
                .body("availability", notNullValue());

        // Step 10: User views voltage quality
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/energy/voltage-quality/" + userId)
                .then()
                .statusCode(200)
                .body("userId", equalTo(userId))
                .body("voltage", notNullValue())
                .body("fluctuations", notNullValue())
                .body("powerFactor", notNullValue());

        // Step 11: User gets consumption forecast
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/analytics/consumption-forecast/user/" + userId)
                .then()
                .statusCode(200)
                .body("userId", equalTo(userId))
                .body("forecast", notNullValue())
                .body("confidence", notNullValue());

        // Step 12: User generates energy report
        Map<String, Object> reportRequest = new HashMap<>();
        reportRequest.put("reportType", "ENERGY_SUMMARY");
        reportRequest.put("startDate", LocalDateTime.now().minusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE));
        reportRequest.put("endDate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(reportRequest)
                .when()
                .post("/api/v1/analytics/reports/user/" + userId)
                .then()
                .statusCode(200)
                .body("reportId", notNullValue())
                .body("status", equalTo("GENERATED"));
    }
}





