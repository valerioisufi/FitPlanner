package com.example.fitplannerclient.ui.fx.guicontroller;

import com.example.fitplannerclient.ui.fx.GuiController;
import com.example.fitplannerclient.ui.fx.GuiManager;
import com.example.fitplannerclient.ui.fx.view.HomeView;

public class HomeController implements GuiController {
    HomeView view;
    HeaderController headerController;


    public HomeController() {
        headerController = new HeaderController();
        view = new HomeView(headerController.getView());
    }

    @Override
    public void start(GuiManager guiManager) {
        guiManager.setView(view);
    }
}
