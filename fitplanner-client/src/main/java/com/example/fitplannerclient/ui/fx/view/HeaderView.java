package com.example.fitplannerclient.ui.fx.view;

import com.example.fitplannerclient.ui.fx.components.Icon;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class HeaderView extends HBox {

    // Change Runnable to Consumer<Node>
    public record MenuConfig(
            String title,
            String icon,
            Consumer<Node> action
    ) {}

    private final List<Button> menuButtons = new ArrayList<>();

    public HeaderView(List<MenuConfig> navItems, int activeNavItemIndex, List<MenuConfig> menuItems) {
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
            btn.getStyleClass().addAll("button-header");

            if (i == activeNavItemIndex) {
                btn.getStyleClass().add("button-header-active");
            }

            if (item.action() != null) {
                // Pass the button as the anchor node
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

        // buttonsContainer
        HBox buttonsContainer = new HBox(16);
        buttonsContainer.setFillHeight(false);
        buttonsContainer.setAlignment(Pos.CENTER_RIGHT);

        for (MenuConfig item : menuItems) {
            HBox btn = new HBox(4);
            btn.setAlignment(Pos.CENTER);
            btn.setFillHeight(false);

            if (item.icon() != null) {
                Icon icon = new Icon(item.icon(), List.of("button-header-icon"));
                btn.getChildren().add(icon);
            }

            if(item.title() != null) {
                Label text = new Label(item.title());
                btn.getChildren().add(text);
            }

            btn.getStyleClass().addAll("button-header");

            if(item.action() != null) {
                btn.setOnMousePressed(e -> item.action().accept(btn));
            }

            buttonsContainer.getChildren().add(btn);
        }

        this.getChildren().addAll(titleContainer, spacer, buttonsContainer);
    }

    public List<Button> getMenuButtons() {
        return menuButtons;
    }
}