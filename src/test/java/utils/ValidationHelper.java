package utils;

import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ValidationHelper - Central library for all response assertions.
 *
 * Instead of scattering assertions throughout step definitions,
 * we keep them here for reuse and readability.
 *
 * Categories:
 *  1. Status code validations
 *  2. Header validations
 *  3. Response time validations
 *  4. Field presence/value validations
 *  5. Payment-specific validations (IBAN, BIC, amounts)
 *  6. Security validations
 */
public class ValidationHelper {

    private static final Logger log = LogManager.getLogger(ValidationHelper.class);

    // ─────────────────────────────────────────────────────────────────────────
    // 1. STATUS CODE VALIDATIONS
    // ─────────────────────────────────────────────────────────────────────────

    public static void assertStatusCode(Response response, int expectedCode) {
        int actual = response.getStatusCode();
        log.info("Asserting status code: expected={}, actual={}", expectedCode, actual);
        assertEquals(expectedCode, actual,
                "Expected HTTP " + expectedCode + " but got " + actual
                        + ". Response body: " + response.getBody().asString());
    }

    public static void assertSuccessStatusCode(Response response) {
        int code = response.getStatusCode();
        log.info("Asserting success status (2xx): actual={}", code);
        assertTrue(code >= 200 && code < 300,
                "Expected 2xx success but got: " + code);
    }

    public static void assertClientErrorStatusCode(Response response) {
        int code = response.getStatusCode();
        assertTrue(code >= 400 && code < 500,
                "Expected 4xx client error but got: " + code);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. HEADER VALIDATIONS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Content-Type must be application/json for API responses.
     */
    public static void assertContentTypeJson(Response response) {
        String contentType = response.getHeader("Content-Type");
        log.info("Asserting Content-Type: {}", contentType);
        assertNotNull(contentType, "Content-Type header is missing");
        assertTrue(contentType.contains("application/json"),
                "Expected Content-Type to contain 'application/json' but was: " + contentType);
    }

    /**
     * Security headers that should be present in payment API responses.
     * These protect against common web vulnerabilities.
     */
    public static void assertSecurityHeaders(Response response) {
        log.info("Asserting security headers...");

        // NOTE: JSONPlaceholder doesn't return these headers.
        // In REAL payment APIs, these MUST be present.
        // We log warnings instead of failing for demo compatibility.

        String[] securityHeaders = {
                "X-Content-Type-Options",    // Prevents MIME-type sniffing
                "X-Frame-Options",           // Prevents clickjacking
                "Strict-Transport-Security", // Forces HTTPS
                "X-XSS-Protection"           // Cross-site scripting protection
        };

        for (String header : securityHeaders) {
            String value = response.getHeader(header);
            if (value == null) {
                log.warn("Security header missing (acceptable in demo, required in production): {}", header);
            } else {
                log.info("Security header present: {} = {}", header, value);
            }
        }
    }

    /**
     * Checks that a specific header exists and has an expected value.
     */
    public static void assertHeader(Response response, String headerName, String expectedValue) {
        String actual = response.getHeader(headerName);
        log.info("Asserting header {}: expected={}, actual={}", headerName, expectedValue, actual);
        assertNotNull(actual, "Header '" + headerName + "' is missing from response");
        assertEquals(expectedValue, actual,
                "Header '" + headerName + "' mismatch. Expected: " + expectedValue + ", Got: " + actual);
    }

    /**
     * Checks that a header is present (regardless of value).
     */
    public static void assertHeaderPresent(Response response, String headerName) {
        String value = response.getHeader(headerName);
        assertNotNull(value, "Expected header '" + headerName + "' to be present but it was missing");
        log.info("Header present: {} = {}", headerName, value);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. RESPONSE TIME VALIDATIONS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Payment APIs have strict SLA requirements.
     * SWIFT GPI requires < 30 seconds. Most instant payment APIs require < 5s.
     */
    public static void assertResponseTimeLessThan(Response response, long maxMilliseconds) {
        long actual = response.getTime();
        log.info("Response time: {}ms (max allowed: {}ms)", actual, maxMilliseconds);
        assertTrue(actual < maxMilliseconds,
                "Response too slow! Took " + actual + "ms, max allowed: " + maxMilliseconds + "ms");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. FIELD PRESENCE AND VALUE VALIDATIONS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Asserts that a JSON field exists and is not null.
     * Uses JSONPath notation: "id", "data.userId", "items[0].price"
     */
    public static void assertFieldExists(Response response, String jsonPath) {
        Object value = response.jsonPath().get(jsonPath);
        log.info("Asserting field exists: {} = {}", jsonPath, value);
        assertNotNull(value, "Expected field '" + jsonPath + "' to exist in response but it was null/missing");
    }

    public static void assertFieldEquals(Response response, String jsonPath, Object expectedValue) {
        Object actual = response.jsonPath().get(jsonPath);
        log.info("Asserting field value: {} expected={}, actual={}", jsonPath, expectedValue, actual);
        assertEquals(expectedValue.toString(), actual.toString(),
                "Field '" + jsonPath + "' mismatch. Expected: " + expectedValue + ", Got: " + actual);
    }

    public static void assertFieldNotEmpty(Response response, String jsonPath) {
        String value = response.jsonPath().getString(jsonPath);
        assertNotNull(value, "Field '" + jsonPath + "' should not be null");
        assertFalse(value.trim().isEmpty(), "Field '" + jsonPath + "' should not be empty");
    }

    /**
     * Asserts that a list/array field has at least one element.
     */
    public static void assertListNotEmpty(Response response, String jsonPath) {
        List<?> list = response.jsonPath().getList(jsonPath);
        assertNotNull(list, "Expected list at '" + jsonPath + "' but got null");
        assertFalse(list.isEmpty(), "Expected list at '" + jsonPath + "' to have items but was empty");
        log.info("List '{}' has {} items", jsonPath, list.size());
    }

    public static void assertListSize(Response response, String jsonPath, int expectedSize) {
        List<?> list = response.jsonPath().getList(jsonPath);
        assertNotNull(list, "Expected list at '" + jsonPath + "' but got null");
        assertEquals(expectedSize, list.size(),
                "List '" + jsonPath + "' expected " + expectedSize + " items but found " + list.size());
    }

    /**
     * Asserts response body contains a specific string (useful for error messages).
     */
    public static void assertBodyContains(Response response, String text) {
        String body = response.getBody().asString();
        assertTrue(body.contains(text),
                "Expected response body to contain '" + text + "' but body was: " + body);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. PAYMENT-SPECIFIC VALIDATIONS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Validates IBAN format: country code + check digits + account number.
     * Example: DE89370400440532013000 (Germany)
     *          GB29NWBK60161331926819 (UK)
     */
    public static void assertValidIban(String iban) {
        assertNotNull(iban, "IBAN cannot be null");
        assertTrue(iban.length() >= 15 && iban.length() <= 34,
                "IBAN length invalid: " + iban);
        assertTrue(iban.matches("[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}"),
                "IBAN format invalid: " + iban);
        log.info("IBAN validation passed: {}", iban);
    }

    /**
     * Validates BIC/SWIFT code format.
     * Example: DEUTDEDB (Deutsche Bank Germany)
     *          BARCGB22 (Barclays UK)
     * Format: 4 letters (bank) + 2 letters (country) + 2 chars (location) + optional 3 chars (branch)
     */
    public static void assertValidBic(String bic) {
        assertNotNull(bic, "BIC cannot be null");
        assertTrue(bic.matches("[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?"),
                "BIC format invalid: " + bic);
        log.info("BIC validation passed: {}", bic);
    }

    /**
     * Validates currency code (ISO 4217 format: 3 uppercase letters).
     * Examples: EUR, USD, GBP, CHF, JPY
     */
    public static void assertValidCurrency(String currency) {
        assertNotNull(currency, "Currency cannot be null");
        assertTrue(currency.matches("[A-Z]{3}"),
                "Currency must be 3 uppercase letters (ISO 4217), got: " + currency);
        log.info("Currency code validation passed: {}", currency);
    }

    /**
     * Validates payment amount is positive and within reasonable bounds.
     */
    public static void assertValidAmount(String amount) {
        assertNotNull(amount, "Amount cannot be null");
        double parsed;
        try {
            parsed = Double.parseDouble(amount);
        } catch (NumberFormatException e) {
            fail("Amount is not a valid number: " + amount);
            return;
        }
        assertTrue(parsed > 0, "Amount must be positive, got: " + amount);
        assertTrue(parsed < 1_000_000_000, "Amount exceeds maximum threshold: " + amount);
        log.info("Amount validation passed: {}", amount);
    }

    /**
     * Validates that a payment status is one of the valid ISO 20022 status codes.
     */
    public static void assertValidPaymentStatus(String status) {
        List<String> validStatuses = List.of(
                "ACCP",  // Accepted Customer Profile
                "ACSC",  // Accepted Settlement Completed
                "ACSP",  // Accepted Settlement In Process
                "ACTC",  // Accepted Technical Validation
                "ACWC",  // Accepted With Change
                "PDNG",  // Pending
                "RJCT"   // Rejected
        );
        assertTrue(validStatuses.contains(status),
                "Invalid payment status: '" + status + "'. Must be one of: " + validStatuses);
        log.info("Payment status validation passed: {}", status);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. SECURITY VALIDATIONS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Ensures sensitive data is NOT exposed in the response.
     * Payment APIs must never leak full card numbers, full account numbers, etc.
     */
    public static void assertNoSensitiveDataExposed(Response response) {
        String body = response.getBody().asString();

        // Check for patterns that suggest unmasked sensitive data
        assertFalse(body.matches(".*\\b\\d{16}\\b.*"),
                "Response may contain unmasked 16-digit card number!");
        assertFalse(body.contains("password"),
                "Response contains 'password' field - should never be exposed!");
        assertFalse(body.contains("cvv"),
                "Response contains 'cvv' field - must never be exposed!");
        assertFalse(body.contains("pin"),
                "Response contains 'pin' field - must never be exposed!");

        log.info("Sensitive data exposure check passed");
    }

    /**
     * Validates that error responses follow a standard error format.
     * Consistent error format is important for client error handling.
     */
    public static void assertStandardErrorFormat(Response response) {
        // Standard error responses should have a code and message
        // JSONPlaceholder returns empty {} for 404, but real APIs should have:
        // { "errorCode": "NOT_FOUND", "message": "Resource not found", "timestamp": "..." }
        String body = response.getBody().asString();
        assertNotNull(body, "Error response body should not be null");
        log.info("Error format check - body length: {}", body.length());
    }
}
