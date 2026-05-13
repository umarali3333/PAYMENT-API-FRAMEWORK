package utils;

import config.ConfigManager;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintStream;
import java.io.ByteArrayOutputStream;

/**
 * BaseAPI - Foundation class for all API interactions.
 *
 * Sets up RestAssured with:
 * - Base URL
 * - Default headers
 * - Logging (requests + responses)
 * - Default response validations
 * - Timeouts
 *
 * Every Step Definition uses this as the starting point.
 */
public class BaseAPI {

    private static final Logger log = LogManager.getLogger(BaseAPI.class);

    // Thread-safe log capture (for reporting)
    private static final ByteArrayOutputStream requestLog = new ByteArrayOutputStream();
    private static final ByteArrayOutputStream responseLog = new ByteArrayOutputStream();

    /**
     * Call this once before all tests (in Hooks).
     * Sets up RestAssured global defaults.
     */
    public static void setup() {
        RestAssured.baseURI = ConfigManager.getBaseUrl();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        log.info("BaseAPI initialized. Base URL: {}", ConfigManager.getBaseUrl());
    }

    /**
     * Resets any global state between tests.
     */
    public static void reset() {
        RestAssured.reset();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REQUEST SPECIFICATIONS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Standard request spec - used for most authenticated API calls.
     */
    public static RequestSpecification getDefaultSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(ConfigManager.getBaseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addHeader("Authorization", ConfigManager.getAuthToken())
                .addHeader("X-Request-ID", java.util.UUID.randomUUID().toString())
                .addHeader("X-Correlation-ID", java.util.UUID.randomUUID().toString())
                .log(LogDetail.ALL)         // Logs request details for debugging
                .build();
    }

    /**
     * Payment-specific spec - includes ISO 20022 required headers.
     */
    public static RequestSpecification getPaymentSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(ConfigManager.getBaseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addHeader("Authorization", ConfigManager.getAuthToken())
                .addHeader("x-api-key", ConfigManager.getApiKey())
                .addHeader("X-Request-ID", java.util.UUID.randomUUID().toString())
                .addHeader("X-Correlation-ID", java.util.UUID.randomUUID().toString())
                .addHeader("X-Message-Type", "pacs.008.001.08")   // ISO 20022 Credit Transfer
                .addHeader("X-Channel-ID", "API")
                .addHeader("X-Idempotency-Key", java.util.UUID.randomUUID().toString()) // Prevent duplicate payments!
                .log(LogDetail.ALL)
                .build();
    }

    /**
     * Unauthenticated spec - used for negative auth tests.
     */
    public static RequestSpecification getUnauthenticatedSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(ConfigManager.getBaseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                // Intentionally NO auth headers
                .log(LogDetail.ALL)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RESPONSE SPECIFICATIONS (pre-built validations)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Validates 200 OK with JSON content type.
     */
    public static ResponseSpecification expectOK() {
        return new ResponseSpecBuilder()
                .expectStatusCode(200)
                .expectContentType(ContentType.JSON)
                .build();
    }

    /**
     * Validates 201 Created (after successful payment initiation).
     */
    public static ResponseSpecification expectCreated() {
        return new ResponseSpecBuilder()
                .expectStatusCode(201)
                .expectContentType(ContentType.JSON)
                .build();
    }

    /**
     * Validates 400 Bad Request (validation errors).
     */
    public static ResponseSpecification expectBadRequest() {
        return new ResponseSpecBuilder()
                .expectStatusCode(400)
                .build();
    }

    /**
     * Validates 401 Unauthorized (auth failure).
     */
    public static ResponseSpecification expectUnauthorized() {
        return new ResponseSpecBuilder()
                .expectStatusCode(401)
                .build();
    }

    /**
     * Validates 403 Forbidden (insufficient permissions).
     */
    public static ResponseSpecification expectForbidden() {
        return new ResponseSpecBuilder()
                .expectStatusCode(403)
                .build();
    }

    /**
     * Validates 404 Not Found.
     */
    public static ResponseSpecification expectNotFound() {
        return new ResponseSpecBuilder()
                .expectStatusCode(404)
                .build();
    }

    /**
     * Validates 422 Unprocessable Entity (business rule violations).
     * Common in payment APIs: insufficient funds, invalid account, etc.
     */
    public static ResponseSpecification expectUnprocessable() {
        return new ResponseSpecBuilder()
                .expectStatusCode(422)
                .build();
    }
}
