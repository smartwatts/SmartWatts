package com.smartwatts.integration.apidocs;

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
public class ApiDocsServiceIntegrationTest {

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = System.getProperty("api-docs.service.url", "http://localhost:8091");
        RestAssured.baseURI = baseUrl;
    }

    @Test
    void testGetServices() {
        // Test getting all discovered services
        given()
                .when()
                .get("/api-docs/services")
                .then()
                .statusCode(200)
                .body("service", equalTo("SmartWatts API Docs Aggregator"))
                .body("version", equalTo("1.0.0"))
                .body("status", equalTo("running"))
                .body("discovered-services", notNullValue())
                .body("total-services", notNullValue());
    }

    @Test
    void testGetHealth() {
        // Test health check endpoint
        given()
                .when()
                .get("/api-docs/health")
                .then()
                .statusCode(200)
                .body("status", equalTo("UP"))
                .body("service", equalTo("api-docs-service"))
                .body("timestamp", notNullValue());
    }

    @Test
    void testGetInfo() {
        // Test info endpoint
        given()
                .when()
                .get("/api-docs/info")
                .then()
                .statusCode(200)
                .body("name", equalTo("SmartWatts API Documentation Service"))
                .body("description", equalTo("Aggregated API documentation for all SmartWatts microservices"))
                .body("swagger-ui", equalTo("/swagger-ui.html"))
                .body("openapi", equalTo("/v3/api-docs"))
                .body("services-endpoint", equalTo("/api-docs/services"));
    }

    @Test
    void testServiceDiscoveryIntegration() {
        // Test that service discovery is working
        Response response = given()
                .when()
                .get("/api-docs/services")
                .then()
                .statusCode(200)
                .extract()
                .response();

        int totalServices = response.jsonPath().getInt("total-services");
        assertTrue(totalServices >= 0, "Total services should be non-negative");
    }
}

