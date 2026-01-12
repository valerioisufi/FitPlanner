package com.example.fitplannerclient.ui.gui1;

import javafx.stage.Stage;
import com.example.fitplannerclient.Navigator;

public class NavigatorA extends Navigator {
    @Override
    public void startHomeController(Stage stage) {
        HomeController homeController = new HomeController();
        homeController.start(stage);
    }
}
