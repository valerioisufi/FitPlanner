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
                throw new ConfigException("Impossibile trovare config.properties nel classpath");
            }

            properties.load(input);

        } catch (IOException ex) {
            throw new ConfigException("Errore durante la lettura di config.properties");
        }

    }

    /**
     * Gets the base API URL
     */
    public String getApiUrl() {
        String url = properties.getProperty("api.url");

        if (url == null || url.isBlank()) {
            throw new ConfigException("La proprietà 'api.url' è mancante o vuota in config.properties");
        }

        return url;
    }

}