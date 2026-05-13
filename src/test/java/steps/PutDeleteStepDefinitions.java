package steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.BaseAPI;
import utils.TestContext;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

/**
 * PutDeleteStepDefinitions - Step definitions for PUT, PATCH, DELETE operations.
 *
 * In payment systems these map to:
 * PUT    → Full update of a pending payment (modify before settlement)
 * PATCH  → Partial update (correct beneficiary name, reference)
 * DELETE → Cancel/recall a payment (only before settlement)
 */
public class PutDeleteStepDefinitions {

    private static final Logger log = LogManager.getLogger(PutDeleteStepDefinitions.class);

    private final TestContext context;

    public PutDeleteStepDefinitions(TestContext context) {
        this.context = context;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT - Full Update
    // ─────────────────────────────────────────────────────────────────────────

    @When("user sends PUT request to {string}")
    public void userSendsPutRequestTo(String endpoint) {
        log.info("Sending PUT request to: {}", endpoint);

        String body = """
                {
                  "id": 1,
                  "title": "Updated Payment Reference",
                  "body": "Payment amount amended to 1500.00 EUR per agreement",
                  "userId": 1
                }
                """;

        Response response = given()
                .spec(BaseAPI.getDefaultSpec())
                .body(body)
                .when()
                .put(endpoint)
                .then()
                .extract()
                .response();

        context.setResponse(response);
        log.info("PUT response: HTTP {}", response.getStatusCode());
    }

    @When("user updates payment {string} with amount {string} {string}")
    public void userUpdatesPaymentWithAmount(String paymentId, String amount, String currency) {
        log.info("Updating payment {} with new amount {} {}", paymentId, amount, currency);

        String body = String.format("""
                {
                  "title": "Payment Update %s %s",
                  "body": "Payment amended: %s %s",
                  "userId": 1
                }
                """, amount, currency, amount, currency);

        Response response = given()
                .spec(BaseAPI.getPaymentSpec())
                .body(body)
                .when()
                .put("/posts/" + paymentId)
                .then()
                .extract()
                .response();

        context.setResponse(response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PATCH - Partial Update
    // ─────────────────────────────────────────────────────────────────────────

    @When("user sends PATCH request to {string} with field {string} = {string}")
    public void userSendsPatchRequest(String endpoint, String field, String value) {
        log.info("Sending PATCH to: {} with {}={}", endpoint, field, value);

        String body = String.format("{\"%s\": \"%s\"}", field, value);

        Response response = given()
                .spec(BaseAPI.getDefaultSpec())
                .body(body)
                .when()
                .patch(endpoint)
                .then()
                .extract()
                .response();

        context.setResponse(response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE - Cancel Payment
    // ─────────────────────────────────────────────────────────────────────────

    @When("user sends DELETE request to {string}")
    public void userSendsDeleteRequestTo(String endpoint) {
        log.info("Sending DELETE request to: {}", endpoint);

        Response response = given()
                .spec(BaseAPI.getDefaultSpec())
                .when()
                .delete(endpoint)
                .then()
                .extract()
                .response();

        context.setResponse(response);
        log.info("DELETE response: HTTP {}", response.getStatusCode());
    }

    @When("user cancels payment with ID {string}")
    public void userCancelsPaymentWithId(String paymentId) {
        log.info("Cancelling payment ID: {}", paymentId);
        userSendsDeleteRequestTo("/posts/" + paymentId);
    }

    @When("user attempts to delete non-existent payment {string}")
    public void userAttemptsToDeleteNonExistentPayment(String paymentId) {
        log.info("Attempting to delete non-existent payment: {}", paymentId);
        userSendsDeleteRequestTo("/posts/" + paymentId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // THEN - Update/Delete validations
    // ─────────────────────────────────────────────────────────────────────────

    @Then("validate payment was updated successfully")
    public void validatePaymentUpdatedSuccessfully() {
        int status = context.getResponse().getStatusCode();
        assertTrue(status == 200 || status == 204,
                "Expected 200 or 204 for update but got: " + status);
        log.info("Payment update validated: HTTP {}", status);
    }

    @Then("validate payment was cancelled successfully")
    public void validatePaymentCancelledSuccessfully() {
        int status = context.getResponse().getStatusCode();
        assertTrue(status == 200 || status == 204,
                "Expected 200 or 204 for delete/cancel but got: " + status);
        log.info("Payment cancellation validated: HTTP {}", status);
    }

    @Then("validate updated field {string} contains {string}")
    public void validateUpdatedFieldContains(String fieldPath, String expectedValue) {
        String actual = context.getResponse().jsonPath().getString(fieldPath);
        assertNotNull(actual, "Field '" + fieldPath + "' missing in update response");
        assertTrue(actual.contains(expectedValue),
                "Field '" + fieldPath + "' expected to contain '" + expectedValue + "' but was: " + actual);
    }
}
