package com.example.fitplannerclient.ui.fx.guicontroller;

import com.example.fitplannerclient.ui.fx.view.HeaderView;
import javafx.scene.Parent;

import java.util.List;

public class HeaderViewController {
    HeaderView headerView;

    public HeaderViewController() {
        List<HeaderView.MenuConfig> menuItems = getAthleteHeaderItems();
        headerView = new HeaderView(menuItems, 0, getMenuItems());
    }

    private List<HeaderView.MenuConfig> getAthleteHeaderItems(){
        List<HeaderView.MenuConfig> menuItems = List.of(
                new HeaderView.MenuConfig("Home", null, null),
                new HeaderView.MenuConfig("Piano", null, null),
                new HeaderView.MenuConfig("Progressi", null, null)
        );
        return menuItems;
    }

    private List<HeaderView.MenuConfig> getTrainerHeaderItems(){
        List<HeaderView.MenuConfig> menuItems = List.of(
                new HeaderView.MenuConfig("Home", null, null),
                new HeaderView.MenuConfig("Libreria", null, null),
                new HeaderView.MenuConfig("Atleti", null, null)
        );
        return menuItems;
    }

    private List<HeaderView.MenuConfig> getMenuItems(){
        List<HeaderView.MenuConfig> menuItems = List.of(
                new HeaderView.MenuConfig(null, "notification-icon", null),
                new HeaderView.MenuConfig(null, "profile-icon", null)
        );
        return menuItems;
    }

    public Parent getView() {
        return headerView;
    }


}
