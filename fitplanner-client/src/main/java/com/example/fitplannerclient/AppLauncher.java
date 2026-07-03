package com.example.fitplannerclient;

import com.example.fitplannerclient.config.ConfigurationManager;
import com.example.fitplannerclient.context.ApplicationContext;
import com.example.fitplannerclient.exception.ConfigException;
import com.example.fitplannerclient.ui.cli.CliEngine;
import javafx.application.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppLauncher {

    private static final Logger logger = LoggerFactory.getLogger(AppLauncher.class);

    public static void main(String[] args) {
        try {
            ConfigurationManager configManager = new ConfigurationManager();

            switch (configManager.getTypeOfUI()) {
                case CLI -> {
                    System.setProperty("org.slf4j.simpleLogger.logFile", "cli.log");
                    ApplicationContext applicationContext = new ApplicationContext(configManager.getApiUrl());
                    CliEngine cliEngine = new CliEngine(applicationContext.getSessionManager());
                    cliEngine.start();
                }
                case JAVAFX ->
                    // JavaFxApp costruisce da sé il proprio ApplicationContext in start()
                    Application.launch(JavaFxApp.class, args);
            }
        } catch (ConfigException e) {
            logger.error("Configuration error: {}", e.getMessage());
            System.exit(1);
        }

    }
}
