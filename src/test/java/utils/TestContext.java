package utils;

import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * TestContext - Thread-safe state container shared across Cucumber step definitions.
 *
 * In Cucumber, each scenario runs step-by-step across multiple classes.
 * We need a way to share data between steps (e.g., store the response from one
 * step and validate it in the next step).
 *
 * This class is injected via PicoContainer (Cucumber's DI framework).
 *
 * Example:
 *   Step 1: "When user sends POST /payments"  → stores Response in context
 *   Step 2: "Then validate payment ID exists" → reads Response from context
 *
 * Think of it as a "backpack" that travels through all steps in a scenario.
 */
public class TestContext {

    private static final Logger log = LogManager.getLogger(TestContext.class);

    // The HTTP response from the most recent API call
    private Response response;

    // Generic data store for sharing any key-value data between steps
    private final Map<String, Object> scenarioData = new HashMap<>();

    // Track test execution timing
    private long requestStartTime;
    private long requestEndTime;

    // ─────────────────────────────────────────────────────────────────────────
    // RESPONSE MANAGEMENT
    // ─────────────────────────────────────────────────────────────────────────

    public void setResponse(Response response) {
        this.response = response;
        log.debug("Response stored in context. Status: {}", response.getStatusCode());
    }

    public Response getResponse() {
        if (response == null) {
            throw new IllegalStateException("No response stored in context. " +
                    "Did you forget to call an API endpoint first?");
        }
        return response;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TIMING
    // ─────────────────────────────────────────────────────────────────────────

    public void startTimer() {
        this.requestStartTime = System.currentTimeMillis();
    }

    public void stopTimer() {
        this.requestEndTime = System.currentTimeMillis();
    }

    public long getResponseTimeMs() {
        return requestEndTime - requestStartTime;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SCENARIO DATA STORE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Store any data for use in later steps.
     * Examples:
     *   context.store("paymentId", "PAY-12345");
     *   context.store("debtorAccount", "DE89370400440532013000");
     */
    public void store(String key, Object value) {
        scenarioData.put(key, value);
        log.debug("Stored in context: {} = {}", key, value);
    }

    /**
     * Retrieve stored data.
     */
    public Object retrieve(String key) {
        Object value = scenarioData.get(key);
        if (value == null) {
            log.warn("No value found in context for key: {}", key);
        }
        return value;
    }

    public String retrieveString(String key) {
        Object val = retrieve(key);
        return val != null ? val.toString() : null;
    }

    public Integer retrieveInt(String key) {
        Object val = retrieve(key);
        return val != null ? Integer.parseInt(val.toString()) : null;
    }

    /**
     * Quick check: does a value exist in context?
     */
    public boolean has(String key) {
        return scenarioData.containsKey(key);
    }

    /**
     * Clear everything - called after each scenario to prevent data leakage.
     */
    public void clear() {
        response = null;
        scenarioData.clear();
        requestStartTime = 0;
        requestEndTime = 0;
        log.debug("TestContext cleared for next scenario");
    }
}
