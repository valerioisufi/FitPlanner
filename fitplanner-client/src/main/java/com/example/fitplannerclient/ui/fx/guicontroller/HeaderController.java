package com.example.fitplannerclient.ui.fx.guicontroller;

import com.example.fitplannerclient.ui.fx.view.HeaderView;
import javafx.scene.Parent;

import java.util.List;

public class HeaderController {
    HeaderView headerView;

    public HeaderController() {
        List<HeaderView.MenuConfig> menuItems = getAthleteHeaderItems();
        headerView = new HeaderView(menuItems, 0);
    }

    private List<HeaderView.MenuConfig> getAthleteHeaderItems(){
        List<HeaderView.MenuConfig> menuItems = List.of(
                new HeaderView.MenuConfig("Home", "home-icon"),
                new HeaderView.MenuConfig("Piano", "plan-icon"),
                new HeaderView.MenuConfig("Progressi", "progress-icon")
        );
        return menuItems;
    }

    public Parent getView() {
        return headerView;
    }


}
