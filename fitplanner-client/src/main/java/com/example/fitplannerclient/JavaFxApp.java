package com.example.fitplannerclient;

import com.example.fitplannerclient.config.ConfigurationManager;
import com.example.fitplannerclient.context.ApplicationContext;
import com.example.fitplannerclient.exception.ConfigException;
import com.example.fitplannerclient.ui.fx.GuiManager;
import com.example.fitplannerclient.ui.fx.Navigator;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaFxApp extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("FitPlanner");
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/app_icon.png"))));

        try {
            ConfigurationManager configManager = new ConfigurationManager();
            ApplicationContext applicationContext = new ApplicationContext(configManager.getApiUrl());

            GuiManager guiManager = new GuiManager(stage);
            Navigator navigator = new Navigator(guiManager, applicationContext.getSessionController());

            // Start the flow
            navigator.goHome();

            stage.show();
        } catch (ConfigException configException) {
            Logger.getLogger(JavaFxApp.class.getName())
                    .log(Level.SEVERE, "Errore durante la lettura della configurazione", configException);
            System.exit(1);
        }
    }

}
