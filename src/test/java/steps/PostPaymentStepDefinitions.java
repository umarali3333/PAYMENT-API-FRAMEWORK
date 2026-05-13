package steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import models.PaymentRequestBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.BaseAPI;
import utils.TestContext;
import utils.ValidationHelper;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

/**
 * PostPaymentStepDefinitions - Step definitions for POST (create) requests.
 *
 * Covers:
 * - Initiate payment (happy path)
 * - Validation errors (missing fields, invalid data)
 * - Duplicate payment prevention (idempotency)
 * - Business rule violations (insufficient funds, limits)
 * - ISO 20022 payment flows
 */
public class PostPaymentStepDefinitions {

    private static final Logger log = LogManager.getLogger(PostPaymentStepDefinitions.class);

    private final TestContext context;

    public PostPaymentStepDefinitions(TestContext context) {
        this.context = context;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST REQUEST VARIANTS
    // ─────────────────────────────────────────────────────────────────────────

    @When("user sends POST request to {string}")
    public void userSendsPostRequestTo(String endpoint) {
        // Generic POST with a simple demo body for JSONPlaceholder
        String body = PaymentRequestBuilder.buildDemoPaymentPost("1000.00", "EUR");
        log.info("Sending POST to: {}", endpoint);

        Response response = given()
                .spec(BaseAPI.getDefaultSpec())
                .body(body)
                .when()
                .post(endpoint)
                .then()
                .extract()
                .response();

        context.setResponse(response);
        log.info("POST response: HTTP {}", response.getStatusCode());
    }

    @When("user initiates a cross-border payment of {string} {string} from IBAN {string} to IBAN {string}")
    public void userInitiatesCrossBorderPayment(String amount, String currency,
                                                String debtorIban, String creditorIban) {
        log.info("Initiating cross-border payment: {} {} from {} to {}",
                amount, currency, debtorIban, creditorIban);

        // Build ISO 20022 pacs.008 payment
        String body = PaymentRequestBuilder.buildCreditTransfer(
                amount, currency,
                debtorIban, "DEUTDEDB",
                creditorIban, "BARCGB22"
        );

        // For demo, we post to /posts since JSONPlaceholder is our mock API
        Response response = given()
                .spec(BaseAPI.getPaymentSpec())
                .body(body)
                .when()
                .post("/posts")
                .then()
                .extract()
                .response();

        context.setResponse(response);
        context.store("paymentAmount", amount);
        context.store("paymentCurrency", currency);
        context.store("debtorIban", debtorIban);
        context.store("creditorIban", creditorIban);
    }

    @When("user submits payment with missing amount field")
    public void userSubmitsPaymentWithMissingAmount() {
        log.info("Testing missing amount validation...");
        String body = PaymentRequestBuilder.buildMissingAmountPayload();

        Response response = given()
                .spec(BaseAPI.getDefaultSpec())
                .body(body)
                .when()
                .post("/posts")
                .then()
                .extract()
                .response();

        context.setResponse(response);
    }

    @When("user submits payment with negative amount {string}")
    public void userSubmitsPaymentWithNegativeAmount(String amount) {
        log.info("Testing negative amount validation: {}", amount);
        String body = PaymentRequestBuilder.buildNegativeAmountPayload();

        Response response = given()
                .spec(BaseAPI.getDefaultSpec())
                .body(body)
                .when()
                .post("/posts")
                .then()
                .extract()
                .response();

        context.setResponse(response);
    }

    @When("user submits payment with invalid IBAN")
    public void userSubmitsPaymentWithInvalidIban() {
        log.info("Testing invalid IBAN validation...");
        String body = PaymentRequestBuilder.buildInvalidIbanPayload();

        Response response = given()
                .spec(BaseAPI.getDefaultSpec())
                .body(body)
                .when()
                .post("/posts")
                .then()
                .extract()
                .response();

        context.setResponse(response);
    }

    @When("user submits payment exceeding daily limit")
    public void userSubmitsPaymentExceedingDailyLimit() {
        log.info("Testing daily limit validation...");
        String body = PaymentRequestBuilder.buildExceedsLimitPayload();

        Response response = given()
                .spec(BaseAPI.getDefaultSpec())
                .body(body)
                .when()
                .post("/posts")
                .then()
                .extract()
                .response();

        context.setResponse(response);
    }

    @When("user submits duplicate payment with same idempotency key")
    public void userSubmitsDuplicatePaymentWithSameIdempotencyKey() {
        log.info("Testing idempotency - duplicate payment submission...");
        String idempotencyKey = "IDEM-KEY-12345-FIXED";
        String body = PaymentRequestBuilder.buildDemoPaymentPost("500.00", "USD");

        // First request
        given()
                .spec(BaseAPI.getDefaultSpec())
                .header("X-Idempotency-Key", idempotencyKey)
                .body(body)
                .when()
                .post("/posts")
                .then()
                .extract()
                .response();

        // Second request with SAME idempotency key - should be detected as duplicate
        Response response = given()
                .spec(BaseAPI.getDefaultSpec())
                .header("X-Idempotency-Key", idempotencyKey)
                .body(body)
                .when()
                .post("/posts")
                .then()
                .extract()
                .response();

        context.setResponse(response);
        context.store("idempotencyKey", idempotencyKey);
    }

    @When("user sends unauthenticated POST request to {string}")
    public void userSendsUnauthenticatedPostRequest(String endpoint) {
        log.info("Sending unauthenticated POST to: {} (expect 401)", endpoint);
        String body = PaymentRequestBuilder.buildDemoPaymentPost("100.00", "EUR");

        Response response = given()
                .spec(BaseAPI.getUnauthenticatedSpec())
                .body(body)
                .when()
                .post(endpoint)
                .then()
                .extract()
                .response();

        context.setResponse(response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // THEN - POST-specific validations
    // ─────────────────────────────────────────────────────────────────────────

    @Then("validate payment was created successfully")
    public void validatePaymentWasCreatedSuccessfully() {
        Response response = context.getResponse();
        // JSONPlaceholder returns 201 for POST
        int status = response.getStatusCode();
        assertTrue(status == 200 || status == 201,
                "Expected 200 or 201 but got: " + status);
        log.info("Payment creation validated: HTTP {}", status);
    }

    @Then("validate response has a payment ID")
    public void validateResponseHasPaymentId() {
        // JSONPlaceholder returns 'id' field
        ValidationHelper.assertFieldExists(context.getResponse(), "id");
        Object id = context.getResponse().jsonPath().get("id");
        context.store("createdPaymentId", id);
        log.info("Payment ID received: {}", id);
    }

    @Then("validate stored IBAN {string} is in correct format")
    public void validateStoredIbanIsInCorrectFormat(String contextKey) {
        String iban = context.retrieveString(contextKey);
        ValidationHelper.assertValidIban(iban);
    }

    @Then("validate payment amount {string} is valid")
    public void validatePaymentAmountIsValid(String contextKey) {
        String amount = context.retrieveString(contextKey);
        ValidationHelper.assertValidAmount(amount);
    }

    @Then("validate payment currency {string} is a valid ISO 4217 code")
    public void validatePaymentCurrencyIsValid(String contextKey) {
        String currency = context.retrieveString(contextKey);
        ValidationHelper.assertValidCurrency(currency);
    }
}
