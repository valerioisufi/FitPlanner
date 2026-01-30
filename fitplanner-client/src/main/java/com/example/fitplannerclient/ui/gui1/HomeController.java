package com.example.fitplannerclient.ui.gui1;

import com.example.fitplannerclient.ui.gui1.view.HomeView;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.example.fitplannerclient.ui.GraphicController;

import java.util.Objects;

public class HomeController implements GraphicController {
    HomeView view;
    HeaderController headerController;


    public HomeController() {
        headerController = new HeaderController();
        view = new HomeView(headerController.getView());
    }

    @Override
    public void start(Stage stage) {
        stage.setScene(new Scene((Parent) this.view.getRootNode()));
        stage.show();
    }
}
