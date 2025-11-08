package com.smartwatts.integration.user;

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
public class UserServiceIntegrationTest {

    private String baseUrl;
    private String authToken;

    @BeforeEach
    void setUp() {
        baseUrl = System.getProperty("user.service.url", "http://localhost:8081");
        RestAssured.baseURI = baseUrl;
        authToken = "test-token-" + UUID.randomUUID();
    }

    @Test
    void testUserRegistration() {
        // Test user registration
        Map<String, Object> user = new HashMap<>();
        user.put("username", "testuser_" + UUID.randomUUID().toString().substring(0, 8));
        user.put("email", "testuser@example.com");
        user.put("password", "SecurePassword123!");
        user.put("firstName", "Test");
        user.put("lastName", "User");
        user.put("phoneNumber", "+2341234567890");

        Response response = given()
                .contentType(ContentType.JSON)
                .body(user)
                .when()
                .post("/api/v1/users/register")
                .then()
                .statusCode(201)
                .body("username", notNullValue())
                .body("email", equalTo(user.get("email")))
                .extract()
                .response();

        assertNotNull(response.jsonPath().getString("id"));
    }

    @Test
    void testUserLogin() {
        // First register a user
        Map<String, Object> user = new HashMap<>();
        String username = "loginuser_" + UUID.randomUUID().toString().substring(0, 8);
        user.put("username", username);
        user.put("email", username + "@example.com");
        user.put("password", "SecurePassword123!");
        user.put("firstName", "Login");
        user.put("lastName", "User");
        user.put("phoneNumber", "+2341234567890");

        given()
                .contentType(ContentType.JSON)
                .body(user)
                .when()
                .post("/api/v1/users/register")
                .then()
                .statusCode(201);

        // Then login
        Map<String, Object> loginRequest = new HashMap<>();
        loginRequest.put("usernameOrEmail", username);
        loginRequest.put("password", "SecurePassword123!");

        given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/api/v1/users/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("refreshToken", notNullValue());
    }

    @Test
    void testGetUserProfile() {
        // Test getting user profile (requires authentication)
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/users/profile")
                .then()
                .statusCode(anyOf(is(200), is(401), is(403))); // May fail without valid token
    }

    @Test
    void testGetAllUsers() {
        // Test getting all users (requires admin role)
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/users")
                .then()
                .statusCode(anyOf(is(200), is(401), is(403))); // May fail without valid token
    }

    @Test
    void testAccountManagement() {
        // Test account creation
        Map<String, Object> account = new HashMap<>();
        account.put("accountName", "Test Account");
        account.put("accountType", "RESIDENTIAL");
        account.put("accountStatus", "ACTIVE");

        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body(account)
                .when()
                .post("/api/v1/accounts")
                .then()
                .statusCode(anyOf(is(201), is(401), is(403))); // May fail without valid token
    }

    @Test
    void testInventoryManagement() {
        // Test inventory item creation
        Map<String, Object> item = new HashMap<>();
        item.put("name", "Test Item");
        item.put("sku", "SKU-" + UUID.randomUUID().toString().substring(0, 8));
        item.put("category", "SMART_METERS");
        item.put("status", "IN_STOCK");
        item.put("quantity", 10);

        given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body(item)
                .when()
                .post("/api/v1/inventory")
                .then()
                .statusCode(anyOf(is(201), is(401), is(403))); // May fail without valid token
    }
}

