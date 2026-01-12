package org.example.fitplannerclient.ui.typeA;

import javafx.stage.Stage;
import org.example.fitplannerclient.Navigator;

public class NavigatorA extends Navigator {
    @Override
    public void startHomeController(Stage stage) {
        HomeController homeController = new HomeController();
        homeController.start(stage);
    }
}
