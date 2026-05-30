package com.example.fitplannerclient.ui.fx.view.plan.editor.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class BadgeComponent extends HBox {
    private final String planNodeId;
    private final BadgeType badgeType;

    private String name;
    private String value;
    private BadgeColor color;

    private java.util.function.Consumer<BadgeComponent> onEditClicked;

    public BadgeComponent(String planNodeId, BadgeType badgeType, String name, String value, BadgeColor color) {
        this.planNodeId = planNodeId;
        this.badgeType = badgeType;
        this.name = name;
        this.value = value;
        this.color = color;

        this.setSpacing(4);
        this.setAlignment(Pos.BASELINE_LEFT);

        String colorSuffix = color.name().toLowerCase();
        this.getStyleClass().addAll("badge", "badge-bg-" + colorSuffix);

        Label nameLabel = new Label(name.toUpperCase());
        nameLabel.getStyleClass().addAll("label-micro", "badge-label-micro-" + colorSuffix);

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().addAll("label-badge-value", "badge-label-value-" + colorSuffix);

        this.getChildren().addAll(nameLabel, valueLabel);

        this.setOnMouseClicked(e -> {
            if (!e.isDragDetect() && onEditClicked != null) {
                onEditClicked.accept(this);
            }
        });
    }

    public void setOnEditClicked(java.util.function.Consumer<BadgeComponent> onEditClicked) {
        this.onEditClicked = onEditClicked;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getPlanNodeId() {
        return planNodeId;
    }

    public String getBadgeId() {
        return planNodeId;
    }

    public void updateBadge(String newName, String newValue) {
        this.name = newName;
        this.value = newValue;
        ((Label) this.getChildren().get(0)).setText(newName.toUpperCase());
        ((Label) this.getChildren().get(1)).setText(newValue);
    }

    public BadgeType getBadgeType() {
        return badgeType;
    }

    public BadgeComponent cloneBadge() {
        return new BadgeComponent(planNodeId, badgeType, name, value, color);
    }


    public enum BadgeType{
        MODIFIER, DECORATOR
    }

    public enum BadgeColor {
        BLUE, GREEN, RED, YELLOW, GRAY, VIOLET
    }

    public static BadgeColor resolveColorFromName(String name) {
        if (name == null) return BadgeColor.GRAY;
        String lower = name.toLowerCase();
        if (lower.contains("set") || lower.contains("loop")) return BadgeColor.BLUE;
        if (lower.contains("rep")) return BadgeColor.YELLOW;
        if (lower.contains("tut") || lower.contains("dur") || lower.contains("time")) return BadgeColor.VIOLET;
        if (lower.contains("rpe")) return BadgeColor.RED;
        if (lower.contains("rest")) return BadgeColor.GREEN;
        return BadgeColor.GRAY;
    }
}
