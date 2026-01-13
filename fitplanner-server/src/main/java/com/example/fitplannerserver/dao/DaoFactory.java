package com.example.fitplannerserver.dao;

import com.example.fitplannerserver.dao.database.DatabaseDaoFactory;
import com.example.fitplannerserver.dao.filesystem.FileSystemDaoFactory;
import com.example.fitplannerserver.dao.inmemory.InMemoryDaoFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public abstract class DaoFactory {
    private static class Wrapper {
        static final DaoFactory INSTANCE = createInstance();

        private static DaoFactory createInstance() {
            Logger logger = Logger.getLogger(DaoFactory.class.getName());
            Properties prop = new Properties();

            // Cerca il file nel Classpath (src/main/resources)
            try (InputStream input = DaoFactory.class.getClassLoader().getResourceAsStream("config.properties")) {

                PersistencyLayer persistencyLayerType = PersistencyLayer.IN_MEMORY; // Valore di default

                if (input == null) {
                    logger.info("Attenzione: config.properties non trovato. Uso default IN_MEMORY.");
                } else {
                    prop.load(input);
                    persistencyLayerType = PersistencyLayer.valueOf(prop.getProperty("persistency.layer", persistencyLayerType.toString()));
                }

                return switch (persistencyLayerType) {
                    case FILESYSTEM -> new FileSystemDaoFactory();
                    case DATABASE -> new DatabaseDaoFactory();
                    default -> new InMemoryDaoFactory();
                };

            } catch (IOException ex) {
                throw new ExceptionInInitializerError("Impossibile leggere il tipo di persistency layer da utilizzare: " + ex.getMessage());
            }
        }
    }

    public static DaoFactory getInstance() {
        return Wrapper.INSTANCE;
    }



}
