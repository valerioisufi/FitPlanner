package com.example.fitplannerclient.ui.fx.components;

import javafx.scene.layout.Region;

import java.util.List;

public class Icon extends Region {

    public Icon(String iconName) {
        this(iconName, 24);
    }

    public Icon(String iconName, int size) {
        this.getStyleClass().add(iconName);
        this.setPrefSize(size, size);
    }

    public Icon(String iconName, List<String> additionalClasses) {
        this(iconName, 24, additionalClasses);
    }

    public Icon(String iconName, int size, List<String> additionalClasses) {
        this(iconName, size);
        this.getStyleClass().addAll(additionalClasses);
    }
}
