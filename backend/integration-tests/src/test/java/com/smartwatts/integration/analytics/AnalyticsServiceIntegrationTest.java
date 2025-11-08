package com.smartwatts.integration.analytics;

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
public class AnalyticsServiceIntegrationTest {

    private String baseUrl;
    private UUID testUserId;
    private String authToken;

    @BeforeEach
    void setUp() {
        baseUrl = System.getProperty("analytics.service.url", "http://localhost:8085");
        RestAssured.baseURI = baseUrl;
        testUserId = UUID.randomUUID();
        authToken = "test-token-" + UUID.randomUUID();
    }

    @Test
    void testDashboardStatisticsAggregation() {
        // Test dashboard stats endpoint
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/analytics/dashboard-stats")
                .then()
                .statusCode(200)
                .body("totalConsumption", notNullValue())
                .body("totalCost", notNullValue())
                .body("averageEfficiency", notNullValue())
                .body("activeDevices", notNullValue())
                .body("alertsCount", notNullValue());
    }

    @Test
    void testCostOptimizationRecommendations() {
        // Test cost optimization endpoint
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/analytics/cost-optimizations")
                .then()
                .statusCode(200)
                .body("", hasSize(greaterThan(0)))
                .body("[0].type", notNullValue())
                .body("[0].description", notNullValue())
                .body("[0].potentialSavings", notNullValue())
                .body("[0].priority", notNullValue());
    }

    @Test
    void testEfficiencyMetricsCalculations() {
        // Test efficiency metrics
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/analytics/efficiency-metrics/user/" + testUserId)
                .then()
                .statusCode(200)
                .body("userId", equalTo(testUserId.toString()))
                .body("overallEfficiency", notNullValue())
                .body("powerFactor", notNullValue())
                .body("energyLoss", notNullValue())
                .body("recommendations", notNullValue());
    }

    @Test
    void testForecastingAlgorithms() {
        // Test consumption forecast
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/analytics/consumption-forecast/user/" + testUserId)
                .then()
                .statusCode(200)
                .body("userId", equalTo(testUserId.toString()))
                .body("forecast", notNullValue())
                .body("forecast.daily", notNullValue())
                .body("forecast.weekly", notNullValue())
                .body("forecast.monthly", notNullValue())
                .body("confidence", notNullValue());

        // Test cost forecast
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/analytics/cost-forecast/user/" + testUserId)
                .then()
                .statusCode(200)
                .body("userId", equalTo(testUserId.toString()))
                .body("forecast", notNullValue())
                .body("forecast.threeMonth", notNullValue())
                .body("forecast.sixMonth", notNullValue())
                .body("forecast.twelveMonth", notNullValue());
    }

    @Test
    void testMultiSourceAnalysis() {
        // Test multi-source energy analysis
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/analytics/multi-source-analysis/" + testUserId)
                .then()
                .statusCode(200)
                .body("userId", equalTo(testUserId.toString()))
                .body("sources", notNullValue())
                .body("sources.GRID", notNullValue())
                .body("sources.SOLAR", notNullValue())
                .body("sources.GENERATOR", notNullValue())
                .body("costComparison", notNullValue())
                .body("recommendations", notNullValue());
    }

    @Test
    void testTokenConsumptionForecast() {
        // Test token consumption forecast
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/analytics/token-consumption-forecast/" + testUserId)
                .then()
                .statusCode(200)
                .body("userId", equalTo(testUserId.toString()))
                .body("currentBalance", notNullValue())
                .body("consumptionRate", notNullValue())
                .body("daysUntilDepletion", notNullValue())
                .body("recommendedPurchase", notNullValue())
                .body("forecast", notNullValue());
    }

    @Test
    void testGeneratorOptimizationRecommendations() {
        // Test generator optimization
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/analytics/generator-optimization/" + testUserId)
                .then()
                .statusCode(200)
                .body("", hasSize(greaterThan(0)))
                .body("[0].type", notNullValue())
                .body("[0].description", notNullValue())
                .body("[0].potentialSavings", notNullValue())
                .body("[0].priority", notNullValue());
    }

    @Test
    void testEnergyTrendsAnalysis() {
        // Test energy trends
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/analytics/energy-trends/user/" + testUserId)
                .then()
                .statusCode(200)
                .body("userId", equalTo(testUserId.toString()))
                .body("trends", notNullValue())
                .body("trends.daily", notNullValue())
                .body("trends.weekly", notNullValue())
                .body("trends.monthly", notNullValue())
                .body("patterns", notNullValue());
    }

    @Test
    void testPeakDemandAnalysis() {
        // Test peak demand analysis
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/analytics/peak-demand/user/" + testUserId)
                .then()
                .statusCode(200)
                .body("userId", equalTo(testUserId.toString()))
                .body("peakDemand", notNullValue())
                .body("peakHours", notNullValue())
                .body("averageDemand", notNullValue())
                .body("recommendations", notNullValue());
    }

    @Test
    void testESGComplianceMetrics() {
        // Test ESG compliance metrics
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/analytics/esg-metrics/user/" + testUserId)
                .then()
                .statusCode(200)
                .body("userId", equalTo(testUserId.toString()))
                .body("carbonFootprint", notNullValue())
                .body("renewablePercentage", notNullValue())
                .body("energyEfficiency", notNullValue())
                .body("sustainabilityScore", notNullValue());
    }

    @Test
    void testCustomReportGeneration() {
        // Test custom report generation
        Map<String, Object> reportRequest = new HashMap<>();
        reportRequest.put("reportType", "ENERGY_SUMMARY");
        reportRequest.put("startDate", LocalDateTime.now().minusMonths(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
        reportRequest.put("endDate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        reportRequest.put("includeCharts", true);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(reportRequest)
                .when()
                .post("/api/v1/analytics/reports/user/" + testUserId)
                .then()
                .statusCode(200)
                .body("reportId", notNullValue())
                .body("status", equalTo("GENERATED"))
                .body("downloadUrl", notNullValue());
    }
}





