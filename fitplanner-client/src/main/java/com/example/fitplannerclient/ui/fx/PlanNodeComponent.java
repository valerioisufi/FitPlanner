package com.example.fitplannerclient.ui.fx;

import com.example.fitplannerclient.dto.plan.ExerciseModifierBean;
import com.example.fitplannerclient.dto.plan.FlowDecoratorBean;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.List;

public class PlanNodeComponent extends VBox {
    private Label nameLabel;
    private FlowPane badgesBox;
    private HBox inlineDecoratorsBox;

    public PlanNodeComponent(String nodeName) {
        this.setMaxWidth(Double.MAX_VALUE);
        this.getStyleClass().add("plan-node");

        this.setCache(true);
        this.setCacheHint(javafx.scene.CacheHint.SPEED);

        this.nameLabel = new Label(nodeName);
        nameLabel.getStyleClass().add("heading-h3");

        inlineDecoratorsBox = new HBox(8);
        inlineDecoratorsBox.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox headerBox = new HBox(24, nameLabel, inlineDecoratorsBox, spacer);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        badgesBox = new FlowPane(8, 8);
        badgesBox.setAlignment(Pos.CENTER_LEFT);

        this.getChildren().addAll(headerBox, badgesBox);
    }

    private Region createBadge(String name, String value, BadgeColor color) {
        HBox badge = new HBox(4);
        badge.setAlignment(Pos.BASELINE_LEFT);

        String colorSuffix = color.name().toLowerCase();
        badge.getStyleClass().addAll("badge", "badge-bg-" + colorSuffix);

        Label nameLabel = new Label(name.toUpperCase());
        nameLabel.getStyleClass().addAll("label-micro", "badge-label-micro-" + colorSuffix);

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().addAll("label-badge-value", "badge-label-value-" + colorSuffix);

        badge.getChildren().addAll(nameLabel, valueLabel);
        return badge;
    }

    private Region createChevronSeparator() {
        Region chevron = new Region();
        chevron.getStyleClass().add("chevron-separator");
        return chevron;
    }

    private enum BadgeColor {
        BLUE, GREEN, RED, YELLOW, GRAY, VIOLET
    }

    // Helper method to auto-assign colors based on keywords in the name
    private BadgeColor resolveColorFromName(String name) {
        if (name == null) return BadgeColor.GRAY;

        String lower = name.toLowerCase();
        if (lower.contains("set") || lower.contains("loop")) return BadgeColor.BLUE;
        if (lower.contains("rep")) return BadgeColor.YELLOW;
        if (lower.contains("tut") || lower.contains("dur") || lower.contains("time")) return BadgeColor.VIOLET;
        if (lower.contains("rpe")) return BadgeColor.RED;
        if (lower.contains("rest")) return BadgeColor.GREEN;

        return BadgeColor.GRAY;
    }

    public void setModifiers(List<ExerciseModifierBean> modifiers) {
        if (modifiers == null || modifiers.isEmpty()) {
            badgesBox.setVisible(false);
            badgesBox.setManaged(false);
            return;
        }

        badgesBox.setVisible(true);
        badgesBox.setManaged(true);
        badgesBox.getChildren().clear();

        for (ExerciseModifierBean modifier : modifiers) {
            BadgeColor color = resolveColorFromName(modifier.getName());
            Region badge = createBadge(modifier.getName(), modifier.getValue(), color);
            badgesBox.getChildren().add(badge);
        }
    }

    public void setFlowDecorators(List<FlowDecoratorBean> flowDecorators) {
        if (flowDecorators == null || flowDecorators.isEmpty()) return;

        inlineDecoratorsBox.getChildren().clear();

        for (int i = 0; i < flowDecorators.size(); i++) {
            FlowDecoratorBean flowDecorator = flowDecorators.get(i);
            String typeName = flowDecorator.getType().name().replace("_", " ");
            BadgeColor color = resolveColorFromName(typeName);
            Region badge = createBadge(typeName, flowDecorator.getValue(), color);

            inlineDecoratorsBox.getChildren().add(badge);

            if (i < flowDecorators.size() - 1) {
                inlineDecoratorsBox.getChildren().add(createChevronSeparator());
            }
        }
    }
}