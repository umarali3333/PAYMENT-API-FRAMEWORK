package steps;

import config.ConfigManager;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.TestContext;
import utils.ValidationHelper;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CommonStepDefinitions - Reusable step definitions shared across all feature files.
 *
 * These steps handle:
 * - Environment setup
 * - Status code assertions
 * - Header assertions
 * - Performance assertions
 * - Generic field assertions
 */
public class CommonStepDefinitions {

    private static final Logger log = LogManager.getLogger(CommonStepDefinitions.class);

    private final TestContext context;

    public CommonStepDefinitions(TestContext context) {
        this.context = context;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GIVEN - Setup steps
    // ─────────────────────────────────────────────────────────────────────────

    @Given("user sets API base URL")
    public void userSetsApiBaseUrl() {
        log.info("Base URL configured: {}", ConfigManager.getBaseUrl());
        // BaseAPI.setup() already configured this in Hooks
        // This step is here to match your existing feature file style
    }

    @Given("user is authenticated with a valid token")
    public void userIsAuthenticatedWithValidToken() {
        log.info("Authentication: Using Bearer token from config");
        context.store("authType", "bearer");
    }

    @Given("user uses API key authentication")
    public void userUsesApiKeyAuthentication() {
        log.info("Authentication: Using API Key from config");
        context.store("authType", "apikey");
    }

    @Given("user has no authentication credentials")
    public void userHasNoAuthenticationCredentials() {
        log.info("Authentication: None (testing unauthorized access)");
        context.store("authType", "none");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // THEN - Status code assertions
    // ─────────────────────────────────────────────────────────────────────────

    @Then("validate status code is {int}")
    public void validateStatusCodeIs(int expectedCode) {
        Response response = context.getResponse();
        ValidationHelper.assertStatusCode(response, expectedCode);
    }

    @Then("validate response is successful")
    public void validateResponseIsSuccessful() {
        ValidationHelper.assertSuccessStatusCode(context.getResponse());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // THEN - Header assertions
    // ─────────────────────────────────────────────────────────────────────────

    @Then("validate header {string} is present")
    public void validateHeaderIsPresent(String headerName) {
        ValidationHelper.assertHeaderPresent(context.getResponse(), headerName);
    }

    @Then("validate header {string} equals {string}")
    public void validateHeaderEquals(String headerName, String expectedValue) {
        ValidationHelper.assertHeader(context.getResponse(), headerName, expectedValue);
    }

    @Then("validate response has Content-Type JSON")
    public void validateContentTypeJson() {
        ValidationHelper.assertContentTypeJson(context.getResponse());
    }

    @Then("validate header {string}")
    public void validateHeader(String headerName) {
        // From your existing feature file style
        ValidationHelper.assertHeaderPresent(context.getResponse(), headerName);
    }

    @Then("validate security headers are present")
    public void validateSecurityHeaders() {
        ValidationHelper.assertSecurityHeaders(context.getResponse());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // THEN - Performance assertions
    // ─────────────────────────────────────────────────────────────────────────

    @Then("validate response time less than {int}")
    public void validateResponseTimeLessThan(int maxMs) {
        ValidationHelper.assertResponseTimeLessThan(context.getResponse(), maxMs);
    }

    @Then("validate response time is within SLA of {int} milliseconds")
    public void validateResponseTimeWithinSla(int maxMs) {
        ValidationHelper.assertResponseTimeLessThan(context.getResponse(), maxMs);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // THEN - Field assertions
    // ─────────────────────────────────────────────────────────────────────────

    @Then("validate response contains {string}")
    public void validateResponseContains(String fieldName) {
        ValidationHelper.assertFieldExists(context.getResponse(), fieldName);
    }

    @Then("validate field {string} equals {string}")
    public void validateFieldEquals(String fieldPath, String expectedValue) {
        ValidationHelper.assertFieldEquals(context.getResponse(), fieldPath, expectedValue);
    }

    @Then("validate field {string} is not empty")
    public void validateFieldIsNotEmpty(String fieldPath) {
        ValidationHelper.assertFieldNotEmpty(context.getResponse(), fieldPath);
    }

    @Then("validate response list {string} is not empty")
    public void validateResponseListNotEmpty(String listPath) {
        ValidationHelper.assertListNotEmpty(context.getResponse(), listPath);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // THEN - Security assertions
    // ─────────────────────────────────────────────────────────────────────────

    @Then("validate no sensitive data is exposed")
    public void validateNoSensitiveDataExposed() {
        ValidationHelper.assertNoSensitiveDataExposed(context.getResponse());
    }

    @Then("validate error response has standard format")
    public void validateErrorResponseHasStandardFormat() {
        ValidationHelper.assertStandardErrorFormat(context.getResponse());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AND - Additional chained validations
    // ─────────────────────────────────────────────────────────────────────────

    @And("validate response body is not empty")
    public void validateResponseBodyIsNotEmpty() {
        String body = context.getResponse().getBody().asString();
        assertNotNull(body);
        assertFalse(body.trim().isEmpty(), "Response body should not be empty");
    }

    @And("store response field {string} as {string}")
    public void storeResponseFieldAs(String fieldPath, String contextKey) {
        Object value = context.getResponse().jsonPath().get(fieldPath);
        assertNotNull(value, "Cannot store null value for field: " + fieldPath);
        context.store(contextKey, value);
        log.info("Stored {} = {} in context", contextKey, value);
    }
}
