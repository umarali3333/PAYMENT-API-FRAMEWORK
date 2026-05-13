package steps;

import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import io.restassured.RestAssured;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.BaseAPI;
import utils.TestContext;

/**
 * Hooks - Cucumber lifecycle callbacks.
 *
 * Runs setup/teardown code around scenarios and the entire test suite.
 *
 * Execution order:
 *   BeforeAll  → once before all scenarios
 *   Before     → before each scenario
 *   [Steps run]
 *   After      → after each scenario
 *   AfterAll   → once after all scenarios
 */
public class Hooks {

    private static final Logger log = LogManager.getLogger(Hooks.class);

    private final TestContext context;

    // Cucumber injects TestContext via constructor (PicoContainer DI)
    public Hooks(TestContext context) {
        this.context = context;
    }

    @BeforeAll
    public static void globalSetup() {
        log.info("========================================");
        log.info("  ISO 20022 Payment API Test Suite");
        log.info("  Starting test execution...");
        log.info("========================================");

        // Initialize RestAssured global settings
        BaseAPI.setup();

        // Enable detailed logging for debugging
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Before
    public void beforeScenario(Scenario scenario) {
        log.info("─────────────────────────────────────────");
        log.info("SCENARIO STARTED: {}", scenario.getName());
        log.info("Tags: {}", scenario.getSourceTagNames());
        log.info("─────────────────────────────────────────");

        // Clear context before each scenario to prevent data leakage
        context.clear();
    }

    @After
    public void afterScenario(Scenario scenario) {
        // Log result
        if (scenario.isFailed()) {
            log.error("SCENARIO FAILED: {}", scenario.getName());

            // Attach last response to report if available
            try {
                String lastResponse = context.getResponse().getBody().asString();
                scenario.attach(
                        ("Last API Response:\n" + lastResponse).getBytes(),
                        "text/plain",
                        "Last API Response"
                );
            } catch (Exception e) {
                log.warn("Could not attach response to failed scenario: {}", e.getMessage());
            }
        } else {
            log.info("SCENARIO PASSED: {}", scenario.getName());
        }

        // Always clean up context
        context.clear();
    }

    @AfterAll
    public static void globalTeardown() {
        log.info("========================================");
        log.info("  Test Execution Complete");
        log.info("  Check target/cucumber-reports for results");
        log.info("========================================");

        // Reset RestAssured to defaults
        BaseAPI.reset();
    }
}
