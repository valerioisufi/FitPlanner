package com.example.fitplannerclient.ui.fx.view.plan.editor.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class BadgeComponent extends HBox {
    private static final Logger logger = LoggerFactory.getLogger(BadgeComponent.class);

    private final String planNodeId;
    private final BadgeType badgeType;

    private String name;
    private String value;
    private BadgeColor color;

    private Consumer<BadgeComponent> onEditClicked;

    public BadgeComponent(String planNodeId, BadgeType badgeType, String name, String value, String displayValue, BadgeColor color) {
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

        Label valueLabel = new Label(displayValue);
        valueLabel.getStyleClass().addAll("label-badge-value", "badge-label-value-" + colorSuffix);

        this.getChildren().addAll(nameLabel, valueLabel);

        this.setOnMouseClicked(e -> {
            logger.info("BadgeComponent clicked! type={}, name={}, value={}", badgeType, name, value);
            if (onEditClicked != null) {
                logger.info("onEditClicked is NOT null, calling accept...");
                onEditClicked.accept(this);
                e.consume();
            } else {
                logger.info("onEditClicked IS NULL!");
            }
        });
    }

    public BadgeComponent(String planNodeId, BadgeType badgeType, String name, String value, BadgeColor color) {
        this(planNodeId, badgeType, name, value, value, color);
    }

    public void setOnEditClicked(Consumer<BadgeComponent> onEditClicked) {
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
        return getPlanNodeId();
    }

    public void updateBadge(String newName, String newValue, String newDisplayValue) {
        this.name = newName;
        this.value = newValue;
        ((Label) this.getChildren().get(0)).setText(newName.toUpperCase());
        ((Label) this.getChildren().get(1)).setText(newDisplayValue);
    }

    public void updateBadge(String newName, String newValue) {
        updateBadge(newName, newValue, newValue);
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
        BLUE, GREEN, RED, YELLOW, GRAY, VIOLET, ORANGE, AMBER, TEAL, CYAN, INDIGO, PINK
    }

    public static BadgeColor resolveColorFromName(String name, BadgeType type) {
        if (name == null) return BadgeColor.GRAY;
        String lower = name.toLowerCase();
        
        if (type == BadgeType.MODIFIER) {
            return resolveModifierColor(lower);
        } else if (type == BadgeType.DECORATOR) {
            return resolveDecoratorColor(lower);
        }
        
        return BadgeColor.GRAY;
    }

    private static BadgeColor resolveModifierColor(String lower) {
        if (lower.contains("reps")) return BadgeColor.VIOLET;
        if (lower.contains("weight")) return BadgeColor.YELLOW;
        if (lower.contains("rpe")) return BadgeColor.ORANGE;
        if (lower.contains("tut")) return BadgeColor.CYAN;
        return BadgeColor.GRAY;
    }

    private static BadgeColor resolveDecoratorColor(String lower) {
        if (lower.contains("interval")) return BadgeColor.INDIGO;
        if (lower.contains("time") || lower.contains("limit")) return BadgeColor.PINK;
        if (lower.contains("loop")) return BadgeColor.RED;
        if (lower.contains("progression")) return BadgeColor.AMBER;
        if (lower.contains("rest")) return BadgeColor.GRAY;
        return BadgeColor.GRAY;
    }
}
