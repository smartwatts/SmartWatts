package com.smartwatts.integration.facility;

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
public class FacilityServiceIntegrationTest {

    private String baseUrl;
    private String authToken;

    @BeforeEach
    void setUp() {
        baseUrl = System.getProperty("facility.service.url", "http://localhost:8086");
        RestAssured.baseURI = baseUrl;
        authToken = "test-token-" + UUID.randomUUID();
    }

    @Test
    void testCreateSpace() {
        // Test space creation
        Map<String, Object> space = new HashMap<>();
        space.put("name", "Test Space");
        space.put("description", "Test facility space");
        space.put("status", "ACTIVE");
        space.put("area", 100.0);
        space.put("capacity", 50);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(space)
                .when()
                .post("/api/v1/spaces")
                .then()
                .statusCode(201)
                .body("name", equalTo(space.get("name")))
                .extract()
                .response();

        assertNotNull(response.jsonPath().getString("id"));
    }

    @Test
    void testGetAllSpaces() {
        // Test getting all spaces
        given()
                .when()
                .get("/api/v1/spaces")
                .then()
                .statusCode(200)
                .body("", notNullValue());
    }

    @Test
    void testGetSpaceById() {
        // First create a space
        Map<String, Object> space = new HashMap<>();
        space.put("name", "Test Space " + UUID.randomUUID().toString().substring(0, 8));
        space.put("description", "Test facility space");
        space.put("status", "ACTIVE");
        space.put("area", 100.0);
        space.put("capacity", 50);

        Response createResponse = given()
                .contentType(ContentType.JSON)
                .body(space)
                .when()
                .post("/api/v1/spaces")
                .then()
                .statusCode(201)
                .extract()
                .response();

        Long spaceId = createResponse.jsonPath().getLong("id");

        // Then get it by ID
        given()
                .when()
                .get("/api/v1/spaces/" + spaceId)
                .then()
                .statusCode(200)
                .body("id", equalTo(spaceId.intValue()))
                .body("name", notNullValue());
    }

    @Test
    void testUpdateSpace() {
        // First create a space
        Map<String, Object> space = new HashMap<>();
        space.put("name", "Test Space " + UUID.randomUUID().toString().substring(0, 8));
        space.put("description", "Test facility space");
        space.put("status", "ACTIVE");
        space.put("area", 100.0);
        space.put("capacity", 50);

        Response createResponse = given()
                .contentType(ContentType.JSON)
                .body(space)
                .when()
                .post("/api/v1/spaces")
                .then()
                .statusCode(201)
                .extract()
                .response();

        Long spaceId = createResponse.jsonPath().getLong("id");

        // Then update it
        Map<String, Object> updateSpace = new HashMap<>();
        updateSpace.put("name", "Updated Space");
        updateSpace.put("description", "Updated description");
        updateSpace.put("status", "ACTIVE");
        updateSpace.put("area", 150.0);
        updateSpace.put("capacity", 75);

        given()
                .contentType(ContentType.JSON)
                .body(updateSpace)
                .when()
                .put("/api/v1/spaces/" + spaceId)
                .then()
                .statusCode(200)
                .body("name", equalTo("Updated Space"))
                .body("area", equalTo(150.0f));
    }

    @Test
    void testDeleteSpace() {
        // First create a space
        Map<String, Object> space = new HashMap<>();
        space.put("name", "Test Space " + UUID.randomUUID().toString().substring(0, 8));
        space.put("description", "Test facility space");
        space.put("status", "ACTIVE");
        space.put("area", 100.0);
        space.put("capacity", 50);

        Response createResponse = given()
                .contentType(ContentType.JSON)
                .body(space)
                .when()
                .post("/api/v1/spaces")
                .then()
                .statusCode(201)
                .extract()
                .response();

        Long spaceId = createResponse.jsonPath().getLong("id");

        // Then delete it
        given()
                .when()
                .delete("/api/v1/spaces/" + spaceId)
                .then()
                .statusCode(204);
    }

    @Test
    void testGetSpacesByStatus() {
        // Test getting spaces by status
        given()
                .param("status", "ACTIVE")
                .when()
                .get("/api/v1/spaces")
                .then()
                .statusCode(200)
                .body("", notNullValue());
    }
}

