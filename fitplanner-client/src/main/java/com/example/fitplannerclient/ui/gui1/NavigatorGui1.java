package com.example.fitplannerclient.ui.gui1;

import com.example.fitplannerclient.service.AuthenticationBoundary;
import com.example.fitplannerclient.service.SessionManager;
import com.example.fitplannerclient.Navigator;
import javafx.application.Platform;

public class NavigatorGui1 extends Navigator {

    @Override
    protected void requireAuthentication() {
        AuthenticationController authenticationController = new AuthenticationController(new AuthenticationBoundary());
        Platform.runLater(() -> authenticationController.start(this.getStage()));

    }

    @Override
    public void startHomeController() {
        if(!SessionManager.getInstance().isLoggedIn()) {
            requireAuthentication();
        } else {
            HomeController homeController = new HomeController();
            Platform.runLater(() -> homeController.start(this.getStage()));
        }
    }

    @Override
    public void startViewPlanController() {
        ViewWorkoutPlanController viewWorkoutPlanController = new ViewWorkoutPlanController();
        Platform.runLater(() -> viewWorkoutPlanController.start(getStage()));
    }


}
