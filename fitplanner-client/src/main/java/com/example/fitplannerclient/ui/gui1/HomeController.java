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
        Parent root = (Parent) view.getRootNode();

        // CORREZIONE:
        // Invece di 'new Scene(root)', passiamo anche larghezza e altezza attuali dello stage.
        // Questo dice alla scena: "Occupami tutto lo spazio che c'è già".
        Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());

        stage.setScene(scene);
        stage.show();
    }
}
