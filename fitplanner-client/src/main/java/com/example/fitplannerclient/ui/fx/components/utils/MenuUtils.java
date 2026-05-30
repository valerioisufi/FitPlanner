package com.example.fitplannerclient.ui.fx.components.utils;

import com.example.fitplannerclient.ui.fx.components.Icon;
import javafx.geometry.Pos;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.util.List;

public class MenuUtils {

    public static CustomMenuItem createCustomMenuItem(String text, String iconName, String iconClass, String textStyle, Runnable action) {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);

        if (iconName != null) {
            Icon icon = new Icon(iconName, List.of(iconClass != null ? iconClass : "button-header-icon"));
            box.getChildren().add(icon);
        }

        Label label = new Label(text);
        label.getStyleClass().add("body-base");
        if (textStyle != null) {
            label.setStyle(textStyle);
        }

        box.getChildren().add(label);

        CustomMenuItem item = new CustomMenuItem(box);
        item.setHideOnClick(true);
        item.setOnAction(e -> {
            if (action != null) {
                action.run();
            }
        });

        return item;
    }
}
