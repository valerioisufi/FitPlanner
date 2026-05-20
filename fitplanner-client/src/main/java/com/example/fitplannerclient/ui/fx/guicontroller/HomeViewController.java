package com.example.fitplannerclient.ui.fx.guicontroller;

import com.example.fitplannerclient.ui.fx.GuiController;
import com.example.fitplannerclient.ui.fx.GuiManager;
import com.example.fitplannerclient.ui.fx.view.HomeView;
import javafx.scene.layout.Pane;

public class HomeViewController implements GuiController {
    HomeView view;
    HeaderViewController headerViewController;


    public HomeViewController() {
        headerViewController = new HeaderViewController();
        view = new HomeView(headerViewController.getView());
    }

    @Override
    public Pane getView() {
        return this.view;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop(){}
}
