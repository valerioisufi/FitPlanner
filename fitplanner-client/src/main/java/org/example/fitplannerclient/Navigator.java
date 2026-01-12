package org.example.fitplannerclient;

import javafx.stage.Stage;
import org.example.fitplannerclient.ui.typeA.NavigatorA;
import org.example.fitplannerclient.ui.typeB.NavigatorB;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public abstract class Navigator {

    private static class Wrapper {
        // La JVM caricherà questa classe e inizializzerà INSTANCE
        // solo quando Navigator.getInstance() verrà chiamato per la prima volta.
        static final Navigator INSTANCE = createInstance();

        private static Navigator createInstance() {
            Properties prop = new Properties();

            // Cerca il file nel Classpath (src/main/resources)
            try (InputStream input = Navigator.class.getClassLoader().getResourceAsStream("config.properties")) {

                String uiType = "A"; // Valore di default

                if (input == null) {
                    System.out.println("Attenzione: config.properties non trovato. Uso default A.");
                } else {
                    prop.load(input);
                    // Legge la proprietà 'ui.type', default "A" se chiave mancante
                    uiType = prop.getProperty("ui.type", "A");
                }

                if ("B".equalsIgnoreCase(uiType)) {
                    return new NavigatorB();
                } else {
                    return new NavigatorA(); // Default
                }

            } catch (IOException ex) {
                throw new ExceptionInInitializerError("Impossibile caricare la configurazione UI: " + ex.getMessage());
            }
        }
    }

    public static Navigator getInstance() {
        return Wrapper.INSTANCE;
    }

    public abstract void startHomeController(Stage stage);
}
