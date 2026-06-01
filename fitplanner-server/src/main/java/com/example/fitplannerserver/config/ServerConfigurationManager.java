package com.example.fitplannerserver.config;

import com.example.fitplannerserver.dao.PersistencyLayer;
import com.example.fitplannerserver.exception.SystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ServerConfigurationManager {
    private static final Logger logger = LoggerFactory.getLogger(ServerConfigurationManager.class);

    private final Properties configProps;

    private static class Wrapper {
        static final ServerConfigurationManager INSTANCE = new ServerConfigurationManager();
    }

    public static ServerConfigurationManager getInstance() {
        return Wrapper.INSTANCE;
    }

    private ServerConfigurationManager() {
        configProps = new Properties();

        loadProperties("config.properties", configProps);
    }

    private void loadProperties(String fileName, Properties properties) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {

            if (input == null) {
                logger.warn("File di configurazione non trovato (verranno usati i valori di default): {}", fileName);
            } else {
                properties.load(input);
            }

        } catch (IOException ex) {
            throw new SystemException("Errore durante la lettura di " + fileName, ex);
        }
    }

    // --- Metodi di utilità per config.properties ---

    public String getConfigProperty(String key, String defaultValue) {
        return configProps.getProperty(key, defaultValue);
    }

    private PersistencyLayer persistencyLayer;

    public PersistencyLayer getPersistencyLayer() {
        if (persistencyLayer != null) {
            return persistencyLayer;
        }

        String persistencyLayerString = getConfigProperty("persistency.layer", "IN_MEMORY");

        try {
            persistencyLayer = PersistencyLayer.valueOf(persistencyLayerString);
        } catch (IllegalArgumentException ex) {
            logger.warn("Valore non valido per 'persistency.layer' in config.properties: {}. Usando IN_MEMORY come default.", persistencyLayerString);
            persistencyLayer = PersistencyLayer.IN_MEMORY;
        }

        return persistencyLayer;
    }

    public String getDbUrl() {
        String url = configProps.getProperty("db.url");

        if (getPersistencyLayer() == PersistencyLayer.DATABASE && (url == null || url.trim().isEmpty())) {
            throw new IllegalArgumentException("Il parametro 'db.url' non può essere nullo o vuoto in config.properties");
        }

        return url;
    }

    public String getDbUser() {
        return configProps.getProperty("db.user");
    }

    public String getDbPassword() {
        return configProps.getProperty("db.password");
    }

    public int getDbPoolSize() {
        return Integer.parseInt(configProps.getProperty("db.pool.size", "10"));
    }
}
