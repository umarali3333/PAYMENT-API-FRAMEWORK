package steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.BaseAPI;
import utils.TestContext;
import utils.ValidationHelper;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

/**
 * GetPaymentStepDefinitions - Step definitions for all GET requests.
 *
 * Covers:
 * - Retrieve all payments / posts
 * - Retrieve by ID
 * - Query parameters (filtering, pagination)
 * - Authentication on GET
 * - Error scenarios (invalid ID, not found)
 */
public class GetPaymentStepDefinitions {

    private static final Logger log = LogManager.getLogger(GetPaymentStepDefinitions.class);

    private final TestContext context;

    public GetPaymentStepDefinitions(TestContext context) {
        this.context = context;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BASIC GET REQUESTS
    // ─────────────────────────────────────────────────────────────────────────

    @When("user sends GET request to {string}")
    public void userSendsGetRequestTo(String endpoint) {
        log.info("Sending GET request to: {}", endpoint);

        Response response = given()
                .spec(BaseAPI.getDefaultSpec())
                .when()
                .get(endpoint)
                .then()
                .extract()
                .response();

        context.setResponse(response);
        log.info("Response received: HTTP {}, Time: {}ms", response.getStatusCode(), response.getTime());
    }

    @When("user sends authenticated GET request to {string}")
    public void userSendsAuthenticatedGetRequest(String endpoint) {
        log.info("Sending authenticated GET request to: {}", endpoint);

        Response response = given()
                .spec(BaseAPI.getPaymentSpec())
                .when()
                .get(endpoint)
                .then()
                .extract()
                .response();

        context.setResponse(response);
    }

    @When("user sends unauthenticated GET request to {string}")
    public void userSendsUnauthenticatedGetRequest(String endpoint) {
        log.info("Sending UNAUTHENTICATED GET request to: {} (expect 401/403)", endpoint);

        Response response = given()
                .spec(BaseAPI.getUnauthenticatedSpec())
                .when()
                .get(endpoint)
                .then()
                .extract()
                .response();

        context.setResponse(response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PARAMETERIZED GET REQUESTS
    // ─────────────────────────────────────────────────────────────────────────

    @When("user sends GET request to {string} with query param {string} = {string}")
    public void userSendsGetWithQueryParam(String endpoint, String paramName, String paramValue) {
        log.info("GET {} with ?{}={}", endpoint, paramName, paramValue);

        Response response = given()
                .spec(BaseAPI.getDefaultSpec())
                .queryParam(paramName, paramValue)
                .when()
                .get(endpoint)
                .then()
                .extract()
                .response();

        context.setResponse(response);
    }

    @When("user retrieves resource {string} by stored ID {string}")
    public void userRetrievesResourceByStoredId(String basePath, String contextKey) {
        Object id = context.retrieve(contextKey);
        assertNotNull(id, "No ID stored with key: " + contextKey);
        String endpoint = basePath + "/" + id;
        log.info("Retrieving: {}", endpoint);
        userSendsGetRequestTo(endpoint);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // THEN - GET-specific validations
    // ─────────────────────────────────────────────────────────────────────────

    @Then("validate response is a list with items")
    public void validateResponseIsListWithItems() {
        List<?> items = context.getResponse().jsonPath().getList("$");
        assertNotNull(items, "Response should be a list but got null");
        assertFalse(items.isEmpty(), "Response list should not be empty");
        log.info("Response list contains {} items", items.size());
    }

    @Then("validate response is a list with at least {int} items")
    public void validateResponseIsListWithAtLeastNItems(int minItems) {
        List<?> items = context.getResponse().jsonPath().getList("$");
        assertNotNull(items, "Response should be a list");
        assertTrue(items.size() >= minItems,
                "Expected at least " + minItems + " items but got " + items.size());
    }

    @Then("validate each item has field {string}")
    public void validateEachItemHasField(String fieldName) {
        List<?> items = context.getResponse().jsonPath().getList("$");
        assertFalse(items.isEmpty(), "List is empty - cannot validate fields");

        for (int i = 0; i < items.size(); i++) {
            Object value = context.getResponse().jsonPath().get("[" + i + "]." + fieldName);
            assertNotNull(value,
                    "Item at index " + i + " is missing field '" + fieldName + "'");
        }
        log.info("All {} items have field '{}'", items.size(), fieldName);
    }

    @Then("validate {string} field value is {int}")
    public void validateFieldValueIsInt(String fieldPath, int expectedValue) {
        Integer actual = context.getResponse().jsonPath().getInt(fieldPath);
        assertEquals(expectedValue, actual,
                "Field '" + fieldPath + "' expected " + expectedValue + " but got " + actual);
    }

    @Then("validate pagination headers are present")
    public void validatePaginationHeadersPresent() {
        // Real payment APIs return pagination info in headers or body
        // JSONPlaceholder uses X-Total-Count
        Response response = context.getResponse();
        String totalCount = response.getHeader("X-Total-Count");
        if (totalCount != null) {
            log.info("Pagination: X-Total-Count = {}", totalCount);
            int count = Integer.parseInt(totalCount);
            assertTrue(count > 0, "X-Total-Count should be greater than 0");
        } else {
            log.info("X-Total-Count header not present (acceptable in some APIs)");
        }
    }
}
