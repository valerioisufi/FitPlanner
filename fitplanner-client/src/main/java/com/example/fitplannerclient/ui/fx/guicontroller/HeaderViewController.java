package com.example.fitplannerclient.ui.fx.guicontroller;

import com.example.fitplannerclient.bean.profile.ProfileBean;
import com.example.fitplannerclient.ui.fx.Navigator;
import com.example.fitplannerclient.controller.profile.ProfileManager;
import com.example.fitplannerclient.ui.fx.view.HeaderView;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class HeaderViewController {
    private final HeaderView headerView;

    private ContextMenu notificationMenu;
    private ContextMenu accountMenu;

    public HeaderViewController(int activeIndex, ProfileManager profileManager) {
        ProfileBean profile = profileManager.getCachedProfile();
        boolean isTrainer = profile != null && profile.getProfileType() == ProfileBean.ProfileType.TRAINER;

        List<HeaderView.MenuConfig> navItems = isTrainer ? getTrainerHeaderItems() : getAthleteHeaderItems();
        List<HeaderView.MenuConfig> menuItems = getMenuItems();

        headerView = new HeaderView(navItems, activeIndex, menuItems);
    }

    private List<HeaderView.MenuConfig> getAthleteHeaderItems() {
        return List.of(
                new HeaderView.MenuConfig("Home", null, node -> Navigator.getInstance().goHome()),
                new HeaderView.MenuConfig("Il Mio Piano", null, node -> {})
        );
    }

    private List<HeaderView.MenuConfig> getTrainerHeaderItems() {
        return List.of(
                new HeaderView.MenuConfig("Home", null, node -> Navigator.getInstance().goHome()),
                new HeaderView.MenuConfig("Libreria Esercizi", null, node -> Navigator.getInstance().goToExerciseLibrary()),
                new HeaderView.MenuConfig("Gestione Piani", null, node -> Navigator.getInstance().goToPlanManagement())
        );
    }

    private List<HeaderView.MenuConfig> getMenuItems() {
        List<HeaderView.MenuConfig> menuItems = new ArrayList<>();

        // Add notification menu config, pointing to our new method
        menuItems.add(new HeaderView.MenuConfig(null, "notification-icon", this::showNotificationMenu));

        menuItems.add(new HeaderView.MenuConfig(null, "profile-icon", this::showAccountMenu));

        return menuItems;
    }

    private void showNotificationMenu(Node anchorNode) {
        if (notificationMenu != null && notificationMenu.isShowing()) {
            notificationMenu.hide();
            return;
        }

        // lazy Initialization
        if (notificationMenu == null) {
            notificationMenu = new ContextMenu();

            notificationMenu.setAutoFix(true);   // Prevents it from going off the physical screen
            notificationMenu.setAutoHide(true);  // Closes automatically when clicking elsewhere

            VBox content = new VBox(10);
            content.setStyle("-fx-padding: 10; -fx-background-color: white;");

            Label notif1 = new Label("Il tuo allenatore ha aggiornato la scheda");
            Label notif2 = new Label("Nuovo messaggio da Marco");

            content.getChildren().addAll(notif1, notif2);

            CustomMenuItem customItem = new CustomMenuItem(content);
            customItem.setHideOnClick(false);
            notificationMenu.getItems().add(customItem);
        }

        // 4. Show the menu first so JavaFX calculates its layout bounds
        notificationMenu.show(anchorNode, Side.BOTTOM, 0, 5);

        // 5. Shift the menu to the left so it aligns with the right edge of the icon
        // This prevents the menu from overflowing outside the right side of your app window
//        double shiftLeft = notificationMenu.getWidth() - anchorNode.getBoundsInLocal().getWidth();
//        notificationMenu.setX(notificationMenu.getX() - shiftLeft);
    }

    private void showAccountMenu(Node anchorNode) {
        if (accountMenu != null && accountMenu.isShowing()) {
            accountMenu.hide();
            return;
        }

        if (accountMenu == null) {
            accountMenu = new ContextMenu();
            accountMenu.setAutoFix(true);
            accountMenu.setAutoHide(true);

            MenuItem profileItem = new MenuItem("Profilo");
            profileItem.setOnAction(e -> Navigator.getInstance().goToProfile());

            MenuItem logoutItem = new MenuItem("Logout");
            logoutItem.setOnAction(e -> Navigator.getInstance().logout());

            accountMenu.getItems().addAll(profileItem, logoutItem);
        }

        accountMenu.show(anchorNode, Side.BOTTOM, 0, 5);
    }

    public Parent getView() {
        return headerView;
    }
}