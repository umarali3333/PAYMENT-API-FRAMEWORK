package utils;

import config.ConfigManager;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static io.restassured.RestAssured.given;

/**
 * AuthManager - Centralized authentication handler.
 *
 * Supports multiple auth strategies commonly used in banking/payment APIs:
 *
 * 1. Bearer Token (OAuth2 / JWT) - Most common in modern APIs
 * 2. API Key (Header-based) - Common for internal microservices
 * 3. Basic Auth (Username/Password) - Legacy systems
 * 4. OAuth2 Client Credentials - Machine-to-machine (bank-to-bank)
 * 5. mTLS (Mutual TLS) - Required by PSD2/Open Banking standards
 *
 * ISO 20022 / Cross-border payments often use OAuth2 + mTLS together.
 */
public class AuthManager {

    private static final Logger log = LogManager.getLogger(AuthManager.class);

    // ─────────────────────────────────────────────────────────────────────────
    // 1. BEARER TOKEN (JWT / OAuth2 Access Token)
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds "Authorization: Bearer <token>" header.
     * Used by: Open Banking APIs, PSD2 TPP authentication, SWIFT GPI
     */
    public static RequestSpecBuilder withBearerToken(RequestSpecBuilder spec) {
        String token = ConfigManager.getAuthToken();
        log.debug("Attaching Bearer token auth");
        return spec.addHeader("Authorization", token);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. API KEY (Header)
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds "x-api-key" header.
     * Used by: Internal banking microservices, payment gateways like Stripe, Adyen
     */
    public static RequestSpecBuilder withApiKey(RequestSpecBuilder spec) {
        String apiKey = ConfigManager.getApiKey();
        log.debug("Attaching API Key auth");
        return spec.addHeader("x-api-key", apiKey);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. BASIC AUTH
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds Basic Auth (Base64 encoded username:password).
     * Used by: Legacy SWIFT APIs, internal admin endpoints
     */
    public static RequestSpecBuilder withBasicAuth(RequestSpecBuilder spec, String username, String password) {
        log.debug("Attaching Basic Auth for user: {}", username);
        return spec.addHeader("Authorization",
                "Basic " + java.util.Base64.getEncoder()
                        .encodeToString((username + ":" + password).getBytes()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. OAUTH2 CLIENT CREDENTIALS (machine-to-machine)
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Fetches a fresh OAuth2 token using client_credentials grant.
     *
     * In real cross-border payment systems (SWIFT, Visa B2B, Mastercard Send),
     * your system authenticates as a "client" to get a short-lived token.
     *
     * NOTE: Using JSONPlaceholder as dummy endpoint. Replace with real token URL.
     */
    public static String fetchOAuth2Token(String tokenUrl, String clientId, String clientSecret) {
        log.info("Fetching OAuth2 token from: {}", tokenUrl);

        // In real projects, this would be your actual token endpoint
        // e.g., https://api.bank.com/oauth2/token
        // This returns a mock token for demo purposes
        try {
            String response = given()
                    .contentType("application/x-www-form-urlencoded")
                    .formParam("grant_type", "client_credentials")
                    .formParam("client_id", clientId)
                    .formParam("client_secret", clientSecret)
                    .when()
                    .post(tokenUrl)
                    .then()
                    .extract()
                    .path("access_token");

            log.info("OAuth2 token fetched successfully");
            return response;
        } catch (Exception e) {
            log.warn("OAuth2 token fetch failed (expected in demo mode): {}", e.getMessage());
            return "demo-oauth-token";
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. ISO 20022 SPECIFIC HEADERS
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds ISO 20022 standard headers used in cross-border payments.
     * These headers are required by SWIFT GPI, TARGET2, SEPA Instant, etc.
     */
    public static RequestSpecBuilder withISO20022Headers(RequestSpecBuilder spec) {
        log.debug("Attaching ISO 20022 standard headers");
        return spec
                .addHeader("X-Request-ID", java.util.UUID.randomUUID().toString())  // Unique per request
                .addHeader("X-Correlation-ID", java.util.UUID.randomUUID().toString())  // For tracing across systems
                .addHeader("X-Channel-ID", "API")                                   // Channel identifier
                .addHeader("X-Message-Type", "pacs.008.001.08")                     // ISO 20022 message type
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPER: Get fully authenticated RequestSpec
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns a ready-to-use spec with all standard headers applied.
     * Use this in step definitions for quick setup.
     */
    public static RequestSpecification getAuthenticatedSpec() {
        RequestSpecBuilder specBuilder = new RequestSpecBuilder()
                .setBaseUri(ConfigManager.getBaseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON);

        withBearerToken(specBuilder);
        withISO20022Headers(specBuilder);

        return specBuilder.build();
    }
}
