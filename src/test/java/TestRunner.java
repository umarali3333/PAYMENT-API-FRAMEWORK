import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * TestRunner - Entry point for running all Cucumber BDD tests.
 *
 * Run from command line:
 *   mvn test                                    → Run all tests
 *   mvn test -Dcucumber.filter.tags="@smoke"    → Run only @smoke tagged tests
 *   mvn test -Dcucumber.filter.tags="@get"      → Run only GET tests
 *   mvn test -Denv=staging                      → Run against staging environment
 *
 * Run from VS Code:
 *   Right-click TestRunner.java → Run
 *   Or click the green ▶ button in the test class
 *
 * Reports generated at: target/cucumber-reports/
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME,
        value = "pretty, html:target/cucumber-reports/cucumber-report.html, json:target/cucumber-reports/cucumber.json")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME,
        value = "steps, utils, config")
@ConfigurationParameter(key = Constants.FEATURES_PROPERTY_NAME,
        value = "src/test/resources/features")
public class TestRunner {
    // This class body is intentionally empty.
    // The annotations above configure Cucumber.
}
