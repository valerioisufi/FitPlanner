package com.example.fitplannerclient.ui.fx.view.common;

import com.example.fitplannerclient.ui.fx.components.Icon;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class HeaderView extends HBox {

    public static final String BUTTON_HEADER_CLASS = "button-header";

    public record MenuConfig(
            String title,
            String icon,
            Consumer<Node> action
    ) {}

    private final List<Button> menuButtons = new ArrayList<>();

    private ContextMenu notificationMenu;
    private final VBox notificationContent;
    private ContextMenu accountMenu;

    public HeaderView(List<MenuConfig> navItems, int activeNavItemIndex) {
        this.getStyleClass().add("header");
        this.setAlignment(Pos.CENTER_LEFT);

        // titleContainer
        HBox titleContainer = new HBox(32);
        titleContainer.setAlignment(Pos.CENTER_LEFT);

        Label textLogo = new Label("FIT PLANNER");
        textLogo.getStyleClass().add("brand-logo");

        // navContainer
        HBox navContainer = new HBox(24);
        navContainer.setAlignment(Pos.CENTER_RIGHT);

        int i = 0;
        for (MenuConfig item : navItems) {
            Button btn = new Button(item.title());
            btn.getStyleClass().addAll(BUTTON_HEADER_CLASS);

            if (i == activeNavItemIndex) {
                btn.getStyleClass().add("button-header-active");
            }

            if (item.action() != null) {
                btn.setOnAction(e -> item.action().accept(btn));
            }

            menuButtons.add(btn);
            navContainer.getChildren().add(btn);
            i++;
        }

        titleContainer.getChildren().addAll(textLogo, navContainer);

        // spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // buttonsContainer (right side)
        HBox buttonsContainer = new HBox(16);
        buttonsContainer.setFillHeight(false);
        buttonsContainer.setAlignment(Pos.CENTER_RIGHT);

        // Notification Icon
        HBox notifBtn = new HBox(4);
        notifBtn.setAlignment(Pos.CENTER);
        notifBtn.setFillHeight(false);
        notifBtn.getStyleClass().add(BUTTON_HEADER_CLASS);
        notifBtn.getChildren().add(new Icon("notification-icon", List.of("button-header-icon")));
        notifBtn.setOnMousePressed(e -> toggleNotificationMenu(notifBtn));

        // Profile Icon
        HBox profileBtn = new HBox(4);
        profileBtn.setAlignment(Pos.CENTER);
        profileBtn.setFillHeight(false);
        profileBtn.getStyleClass().add(BUTTON_HEADER_CLASS);
        profileBtn.getChildren().add(new Icon("profile-icon", List.of("button-header-icon")));
        profileBtn.setOnMousePressed(e -> toggleAccountMenu(profileBtn));

        buttonsContainer.getChildren().addAll(notifBtn, profileBtn);

        this.getChildren().addAll(titleContainer, spacer, buttonsContainer);

        // Initialize notification content
        notificationContent = new VBox(10);
        notificationContent.setStyle("-fx-padding: 10; -fx-background-color: white;");
    }

    public void setupAccountMenu(Runnable onProfileClicked, Runnable onLogoutClicked) {
        accountMenu = new ContextMenu();
        accountMenu.setAutoFix(true);
        accountMenu.setAutoHide(true);

        MenuItem profileItem = new MenuItem("Profilo");
        profileItem.setOnAction(e -> onProfileClicked.run());

        MenuItem logoutItem = new MenuItem("Logout");
        logoutItem.setOnAction(e -> onLogoutClicked.run());

        accountMenu.getItems().addAll(profileItem, logoutItem);
    }

    public void updateNotifications(List<String> notifications) {
        notificationContent.getChildren().clear();
        for (String notif : notifications) {
            notificationContent.getChildren().add(new Label(notif));
        }
    }

    private void toggleNotificationMenu(Node anchorNode) {
        if (notificationMenu != null && notificationMenu.isShowing()) {
            notificationMenu.hide();
            return;
        }

        if (notificationMenu == null) {
            notificationMenu = new ContextMenu();
            notificationMenu.setAutoFix(true);
            notificationMenu.setAutoHide(true);

            CustomMenuItem customItem = new CustomMenuItem(notificationContent);
            customItem.setHideOnClick(false);
            notificationMenu.getItems().add(customItem);
        }

        notificationMenu.show(anchorNode, Side.BOTTOM, 0, 5);
    }

    private void toggleAccountMenu(Node anchorNode) {
        if (accountMenu != null && accountMenu.isShowing()) {
            accountMenu.hide();
            return;
        }

        if (accountMenu != null) {
            accountMenu.show(anchorNode, Side.BOTTOM, 0, 5);
        }
    }

}