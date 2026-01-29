package com.example.fitplannerclient;

import javafx.stage.Stage;
import com.example.fitplannerclient.ui.gui1.NavigatorGui1;
import com.example.fitplannerclient.ui.gui2.NavigatorGui2;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public abstract class Navigator {

    private static class Wrapper {
        // La JVM caricherà questa classe e inizializzerà INSTANCE
        // solo quando Navigator.getInstance() verrà chiamato per la prima volta.
        static final Navigator INSTANCE = createInstance();

        private static Navigator createInstance() {
            Logger logger = Logger.getLogger(Navigator.class.getName());
            Properties prop = new Properties();

            // Cerca il file nel Classpath (src/main/resources)
            try (InputStream input = Navigator.class.getClassLoader().getResourceAsStream("config.properties")) {

                String uiType = "A"; // Valore di default

                if (input == null) {
                    logger.info("Attenzione: config.properties non trovato. Uso default A.");
                } else {
                    prop.load(input);
                    // Legge la proprietà 'ui.type', default "A" se chiave mancante
                    uiType = prop.getProperty("ui.type", "A");
                }

                if ("B".equalsIgnoreCase(uiType)) {
                    return new NavigatorGui2();
                } else {
                    return new NavigatorGui1(); // Default
                }

            } catch (IOException ex) {
                throw new ExceptionInInitializerError("Impossibile caricare la configurazione UI: " + ex.getMessage());
            }
        }
    }

    public static Navigator getInstance() {
        return Wrapper.INSTANCE;
    }


    protected abstract void requireAuthentication(Stage stage);

    public abstract void startHomeController(Stage stage);

    public abstract void startViewPlanController(Stage stage);

}
