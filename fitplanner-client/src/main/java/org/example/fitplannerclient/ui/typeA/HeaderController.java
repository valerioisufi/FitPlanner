package org.example.fitplannerclient.ui.typeA;

import javafx.scene.Parent;

import java.util.List;

public class HeaderController {
    HeaderView headerView;

    public HeaderController() {
        List<HeaderView.MenuConfig> menuItems = List.of(
                new HeaderView.MenuConfig("Home", "home-icon"),
                new HeaderView.MenuConfig("Piano", "plan-icon"),
                new HeaderView.MenuConfig("Progressi", "progress-icon")
        );
        headerView = new HeaderView(menuItems, 0);
    }

    public Parent getView() {
        return headerView;
    }


}
