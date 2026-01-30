package com.example.fitplannerclient.ui.gui1;

import com.example.fitplannerclient.ui.gui1.view.HomeView;
import com.example.fitplannerclient.ui.GraphicController;

public class HomeController implements GraphicController {
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
