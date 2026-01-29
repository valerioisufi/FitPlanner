package com.example.fitplannerclient.ui.gui1;

import com.example.fitplannerclient.service.AuthenticationBoundary;
import com.example.fitplannerclient.service.SessionManager;
import com.example.fitplannerclient.ui.gui1.view.RegistrationView;
import javafx.stage.Stage;
import com.example.fitplannerclient.Navigator;

public class NavigatorGui1 extends Navigator {

    @Override
    protected void requireAuthentication(Stage stage) {
        AuthenticationController authenticationController = new AuthenticationController(new AuthenticationBoundary());
        authenticationController.start(stage);
    }

    @Override
    public void startHomeController(Stage stage) {
        if(!SessionManager.getInstance().isLoggedIn()) {
            requireAuthentication(stage);
        } else {
            HomeController homeController = new HomeController();
            homeController.start(stage);
        }
    }

    @Override
    public void startViewPlanController(Stage stage) {
        ViewWorkoutPlanController viewWorkoutPlanController = new ViewWorkoutPlanController();
        viewWorkoutPlanController.start(stage);
    }


}
