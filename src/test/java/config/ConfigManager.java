package config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.Properties;

/**
 * ConfigManager - Reads environment configuration from properties files.
 * In real projects, you switch environments by passing: -Denv=staging
 *
 * Usage:
 *   ConfigManager.getBaseUrl()         → https://api.sandbox.bank.com
 *   ConfigManager.getProperty("key")   → any custom property
 */
public class ConfigManager {

    private static final Logger log = LogManager.getLogger(ConfigManager.class);
    private static final Properties props = new Properties();

    static {
        // Read environment: default = "dev", override with -Denv=staging
        String env = System.getProperty("env", "dev");
        String configFile = "config/" + env + ".properties";

        try (InputStream in = ConfigManager.class.getClassLoader().getResourceAsStream(configFile)) {
            if (in != null) {
                props.load(in);
                log.info("Loaded config for environment: {}", env);
            } else {
                log.warn("Config file not found: {}. Using defaults.", configFile);
                loadDefaults();
            }
        } catch (Exception e) {
            log.error("Failed to load config file: {}", e.getMessage());
            loadDefaults();
        }
    }

    private static void loadDefaults() {
        // JSONPlaceholder is a free mock REST API - perfect for demo/learning
        // Replace with your real payment gateway URL in production
        props.setProperty("base.url", "https://jsonplaceholder.typicode.com");
        props.setProperty("payment.api.url", "https://jsonplaceholder.typicode.com");
        props.setProperty("auth.token", "Bearer demo-token-replace-in-real-project");
        props.setProperty("api.key", "demo-api-key-replace-in-real-project");
        props.setProperty("timeout.connect", "10000");
        props.setProperty("timeout.read", "30000");
        props.setProperty("max.response.time.ms", "5000");
    }

    public static String getBaseUrl() {
        return props.getProperty("base.url");
    }

    public static String getAuthToken() {
        return props.getProperty("auth.token");
    }

    public static String getApiKey() {
        return props.getProperty("api.key");
    }

    public static int getConnectTimeout() {
        return Integer.parseInt(props.getProperty("timeout.connect", "10000"));
    }

    public static int getReadTimeout() {
        return Integer.parseInt(props.getProperty("timeout.read", "30000"));
    }

    public static long getMaxResponseTimeMs() {
        return Long.parseLong(props.getProperty("max.response.time.ms", "5000"));
    }

    public static String getProperty(String key) {
        return props.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }
}
