package com.smartwatts.integration.billing;

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
public class BillingServiceIntegrationTest {

    private String baseUrl;
    private UUID testUserId;
    private String authToken;

    @BeforeEach
    void setUp() {
        baseUrl = System.getProperty("billing.service.url", "http://localhost:8083");
        RestAssured.baseURI = baseUrl;
        testUserId = UUID.randomUUID();
        authToken = "test-token-" + UUID.randomUUID();
    }

    @Test
    void testBillGenerationWithMytoTariffs() {
        // Test MYTO tariff retrieval
        given()
                .when()
                .get("/api/v1/billing/myto-tariff/R1")
                .then()
                .statusCode(200)
                .body("customerClass", equalTo("R1"))
                .body("rates", notNullValue())
                .body("currency", equalTo("NGN"));

        // Test different customer classes
        String[] customerClasses = {"R1", "R2", "R3", "C1", "C2"};
        for (String customerClass : customerClasses) {
            given()
                    .when()
                    .get("/api/v1/billing/myto-tariff/" + customerClass)
                    .then()
                    .statusCode(200)
                    .body("customerClass", equalTo(customerClass));
        }
    }

    @Test
    void testPrepaidTokenBalanceTracking() {
        // Test token balance retrieval
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/billing/prepaid-tokens/" + testUserId)
                .then()
                .statusCode(200)
                .body("userId", equalTo(testUserId.toString()))
                .body("currentBalance", notNullValue())
                .body("consumptionRate", notNullValue())
                .body("daysUntilDepletion", notNullValue())
                .body("meterNumber", notNullValue())
                .body("disco", notNullValue());
    }

    @Test
    void testTokenPurchaseFlow() {
        // Test token purchase
        Map<String, Object> purchaseRequest = new HashMap<>();
        purchaseRequest.put("userId", testUserId.toString());
        purchaseRequest.put("amount", 5000.0);
        purchaseRequest.put("tokenAmount", "100 kWh");
        purchaseRequest.put("paymentMethod", "Bank Transfer");
        purchaseRequest.put("disco", "EKEDC");
        purchaseRequest.put("vendingAgent", "AGENT-001");
        purchaseRequest.put("transactionReference", "TXN-" + UUID.randomUUID());

        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(purchaseRequest)
                .when()
                .post("/api/v1/billing/prepaid-tokens/purchase")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"))
                .body("token", notNullValue())
                .body("transactionId", notNullValue())
                .extract()
                .response();

        String transactionId = response.jsonPath().getString("transactionId");
        assertNotNull(transactionId);
    }

    @Test
    void testTokenPurchaseHistory() {
        // Test token history retrieval
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/billing/prepaid-tokens/" + testUserId + "/history")
                .then()
                .statusCode(200)
                .body("", hasSize(greaterThan(0)))
                .body("[0].transactionId", notNullValue())
                .body("[0].amount", notNullValue())
                .body("[0].purchaseDate", notNullValue());
    }

    @Test
    void testDiscoBillingIntegration() {
        // Test DisCo billing info
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/billing/disco-billing/" + testUserId)
                .then()
                .statusCode(200)
                .body("userId", equalTo(testUserId.toString()))
                .body("disco", notNullValue())
                .body("accountNumber", notNullValue())
                .body("meterNumber", notNullValue())
                .body("customerName", notNullValue())
                .body("tariffClass", notNullValue());
    }

    @Test
    void testBillGeneration() {
        // Create energy consumption data first
        Map<String, Object> consumption = new HashMap<>();
        consumption.put("userId", testUserId.toString());
        consumption.put("period", "2024-01");
        consumption.put("totalConsumption", 150.5);
        consumption.put("gridConsumption", 100.0);
        consumption.put("solarConsumption", 50.5);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(consumption)
                .when()
                .post("/api/v1/billing/consumption")
                .then()
                .statusCode(201);

        // Generate bill
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .post("/api/v1/billing/users/" + testUserId + "/bills/generate")
                .then()
                .statusCode(200)
                .body("billId", notNullValue())
                .body("totalAmount", notNullValue())
                .body("status", equalTo("GENERATED"));

        // Get user bills
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/billing/users/" + testUserId + "/bills")
                .then()
                .statusCode(200)
                .body("content", hasSize(greaterThan(0)))
                .body("content[0].userId", equalTo(testUserId.toString()));
    }

    @Test
    void testCostProjections() {
        // Test cost projections
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/billing/users/" + testUserId + "/projections")
                .then()
                .statusCode(200)
                .body("userId", equalTo(testUserId.toString()))
                .body("projections", notNullValue())
                .body("projections.threeMonth", notNullValue())
                .body("projections.sixMonth", notNullValue())
                .body("projections.twelveMonth", notNullValue());
    }

    @Test
    void testPaymentProcessing() {
        // Test payment processing
        Map<String, Object> payment = new HashMap<>();
        payment.put("amount", 15000.0);
        payment.put("paymentMethod", "Bank Transfer");
        payment.put("reference", "PAY-" + UUID.randomUUID());

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(payment)
                .when()
                .post("/api/v1/billing/users/" + testUserId + "/payments")
                .then()
                .statusCode(200)
                .body("paymentId", notNullValue())
                .body("status", equalTo("PROCESSED"));
    }

    @Test
    void testBillingAnalytics() {
        // Test billing analytics
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/v1/billing/users/" + testUserId + "/analytics")
                .then()
                .statusCode(200)
                .body("userId", equalTo(testUserId.toString()))
                .body("totalSpent", notNullValue())
                .body("averageMonthlyBill", notNullValue())
                .body("costTrends", notNullValue());
    }
}





