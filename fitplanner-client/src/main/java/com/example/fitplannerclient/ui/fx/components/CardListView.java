package com.example.fitplannerclient.ui.fx.components;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.BiFunction;

public class CardListView<T> extends VBox {

    private final VBox itemsContainer;
    private BiFunction<T, Boolean, HBox> rowRenderer;

    public CardListView(List<Label> headerLabels) {
        this.getStyleClass().add("card");

        HBox listHeader = new HBox();
        listHeader.getStyleClass().add("list-header-row");

        for (Label label : headerLabels) {
            label.getStyleClass().add("heading-h3");
            listHeader.getChildren().add(label);
        }

        itemsContainer = new VBox();

        this.getChildren().addAll(listHeader, itemsContainer);
    }

    public void setRowRenderer(BiFunction<T, Boolean, HBox> rowRenderer) {
        this.rowRenderer = rowRenderer;
    }

    public void setItems(List<T> items, String emptyMessage) {
        itemsContainer.getChildren().clear();

        if (items == null || items.isEmpty()) {
            Label emptyLbl = new Label(emptyMessage);
            emptyLbl.getStyleClass().add("body-base");
            emptyLbl.setPadding(new Insets(16));
            itemsContainer.getChildren().add(emptyLbl);
            return;
        }

        if (rowRenderer == null) {
            throw new IllegalStateException("Row renderer must be set before adding items");
        }

        for (int i = 0; i < items.size(); i++) {
            boolean isLast = (i == items.size() - 1);
            HBox row = rowRenderer.apply(items.get(i), isLast);
            itemsContainer.getChildren().add(row);
        }
    }
}
