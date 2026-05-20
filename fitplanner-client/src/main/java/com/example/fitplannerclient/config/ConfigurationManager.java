package com.example.fitplannerclient.config;

import com.example.fitplannerclient.exception.ConfigException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigurationManager {
    private final Properties properties;

    public ConfigurationManager() {
        this.properties = new Properties();
        loadProperties();
    }

    private void loadProperties() {
        try (InputStream input = getClass().getResourceAsStream("/config.properties")) {
            if (input == null) {
                throw new ConfigException("Cannot find config.properties in the classpath");
            }
            properties.load(input);
        } catch (IOException ex) {
            throw new ConfigException("Error reading config.properties");
        }
    }

    /**
     * Gets the base API URL.
     * Throws an exception immediately if the required property is missing.
     */
    public String getApiUrl() {
        String url = properties.getProperty("api.url");
        if (url == null || url.isBlank()) {
            throw new ConfigException("Property 'api.url' is missing or empty in config.properties");
        }
        return url;
    }

    // You can easily add more typed properties here later, with default fallbacks
    // public int getTimeoutSeconds() {
    //     String timeout = properties.getProperty("http.timeout", "30");
    //     return Integer.parseInt(timeout);
    // }
}