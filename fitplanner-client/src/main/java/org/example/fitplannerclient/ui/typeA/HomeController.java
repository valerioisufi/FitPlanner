package org.example.fitplannerclient.ui.typeA;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.fitplannerclient.ui.GraphicController;

import java.util.Objects;

public class HomeController implements GraphicController {
    HomeView view;
    HeaderController headerController;


    public HomeController() {
        headerController = new HeaderController();
        view = new HomeView(headerController.getView());

        String themeCss = Objects.requireNonNull(getClass().getResource("/style/theme1.css")).toExternalForm();
        String iconsCss = Objects.requireNonNull(getClass().getResource("/style/icons.css")).toExternalForm();
        view.getStylesheets().addAll(themeCss, iconsCss);
    }

    @Override
    public void start(Stage stage) {
        stage.setScene(new Scene(view));
        stage.show();
    }
}
