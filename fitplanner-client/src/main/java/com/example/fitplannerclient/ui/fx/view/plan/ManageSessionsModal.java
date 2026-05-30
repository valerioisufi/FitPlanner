package com.example.fitplannerclient.ui.fx.view.plan;

import com.example.fitplannerclient.bean.plan.WorkoutSessionBean;
import com.example.fitplannerclient.bean.plan.PlanNodeBean;
import com.example.fitplannerclient.bean.plan.NodeType;
import com.example.fitplannerclient.bean.plan.FlowDecoratorBean;
import com.example.fitplannerclient.bean.plan.FlowDecoratorType;
import com.example.fitplannerclient.ui.fx.components.Icon;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ManageSessionsModal extends VBox {

    private final TextField cycleLengthField;
    private final VBox sessionsContainer;
    
    private List<WorkoutSessionBean> currentSessions;
    
    private BiConsumer<Integer, List<WorkoutSessionBean>> onSaveAction;
    private Runnable onCloseAction;

    public ManageSessionsModal() {
        this.getStyleClass().add("card");
        this.setPadding(new Insets(32));
        this.setSpacing(24);
        this.setMaxWidth(600);
        this.setMaxHeight(Region.USE_PREF_SIZE);

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.TOP_LEFT);
        VBox titleBox = new VBox(4);
        Label titleLabel = new Label("Gestisci Sessioni");
        titleLabel.getStyleClass().add("heading-h2");
        titleBox.getChildren().add(titleLabel);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button closeBtn = new Button();
        closeBtn.getStyleClass().add("button-header");
        closeBtn.setGraphic(new Icon("x-icon", List.of("button-header-icon")));
        closeBtn.setOnAction(e -> { if (onCloseAction != null) onCloseAction.run(); });
        header.getChildren().addAll(titleBox, spacer, closeBtn);

        // Cycle length
        HBox cycleLengthBox = new HBox(12);
        cycleLengthBox.setAlignment(Pos.CENTER_LEFT);
        Label cycleLabel = new Label("Durata ciclo (giorni):");
        cycleLabel.getStyleClass().add("label-field");
        cycleLengthField = new TextField();
        cycleLengthField.setPrefWidth(80);
        cycleLengthField.getStyleClass().add("text-field");
        cycleLengthBox.getChildren().addAll(cycleLabel, cycleLengthField);

        // Sessions header
        HBox sessionsHeaderBox = new HBox();
        sessionsHeaderBox.setAlignment(Pos.CENTER_LEFT);
        
        Label sessionsLabel = new Label("Sessioni");
        sessionsLabel.getStyleClass().add("heading-h3");
        
        Region sessionSpacer = new Region();
        HBox.setHgrow(sessionSpacer, Priority.ALWAYS);
        
        Button addSessionBtn = new Button("Aggiungi Sessione");
        addSessionBtn.getStyleClass().add("button-secondary");
        addSessionBtn.setGraphic(new com.example.fitplannerclient.ui.fx.components.Icon("plus-icon", List.of("button-header-icon")));
        addSessionBtn.setOnAction(e -> addNewSessionRow());
        
        sessionsHeaderBox.getChildren().addAll(sessionsLabel, sessionSpacer, addSessionBtn);

        sessionsContainer = new VBox(8);
        ScrollPane scrollPane = new ScrollPane(sessionsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(250);

        // Footer
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(16, 0, 0, 0));
        footer.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 1 0 0 0;");
        Button cancelBtn = new Button("Annulla");
        cancelBtn.getStyleClass().add("button-secondary");
        cancelBtn.setOnAction(e -> { if (onCloseAction != null) onCloseAction.run(); });
        Button saveBtn = new Button("Salva");
        saveBtn.getStyleClass().add("button-primary");
        saveBtn.setOnAction(e -> save());
        footer.getChildren().addAll(cancelBtn, saveBtn);

        this.getChildren().addAll(header, cycleLengthBox, sessionsHeaderBox, scrollPane, footer);
    }

    public void setInitialData(int cycleLength, List<WorkoutSessionBean> sessions) {
        this.cycleLengthField.setText(String.valueOf(cycleLength));
        // Deep copy sessions so we can discard changes on cancel
        this.currentSessions = new ArrayList<>();
        for (WorkoutSessionBean s : sessions) {
            // we don't strictly need a deep clone if we only apply changes on save,
            // but setting properties immediately mutates original beans.
            // Let's create shallow copies for the beans we edit, or just mutate and live with it.
            // Mutating directly is simpler for this scope. If user cancels, we'd need to undo.
            // For true undo, we'd copy them.
            WorkoutSessionBean copy = new WorkoutSessionBean(s.getName(), s.getDay(), s.getPlanRoot());
            this.currentSessions.add(copy);
        }
        refreshSessionsList();
    }

    private void refreshSessionsList() {
        sessionsContainer.getChildren().clear();
        for (WorkoutSessionBean session : currentSessions) {
            sessionsContainer.getChildren().add(createSessionRow(session));
        }
    }

    private HBox createSessionRow(WorkoutSessionBean session) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8));
        row.setStyle("-fx-border-color: #E0E0E0; -fx-border-radius: 6; -fx-border-width: 1; -fx-background-color: #FAFAFA;");

        Label nameLabel = new Label("Nome:");
        nameLabel.getStyleClass().add("label-field");
        TextField nameField = new TextField(session.getName());
        nameField.getStyleClass().add("text-field");
        HBox.setHgrow(nameField, Priority.ALWAYS);
        nameField.textProperty().addListener((obs, oldV, newV) -> session.setName(newV));

        Label dayLabel = new Label("Giorno:");
        dayLabel.getStyleClass().add("label-field");
        TextField dayField = new TextField(String.valueOf(session.getDay()));
        dayField.setPrefWidth(60);
        dayField.getStyleClass().add("text-field");
        dayField.textProperty().addListener((obs, oldV, newV) -> {
            try {
                if (!newV.isEmpty()) {
                    session.setDay(Integer.parseInt(newV));
                }
            } catch (NumberFormatException ignored) {}
        });

        Button removeBtn = new Button();
        removeBtn.getStyleClass().add("button-transparent");
        // Using "trash-icon" or similar. Since "x-icon" is present, let's use it with danger class.
        removeBtn.setGraphic(new Icon("x-icon", List.of("button-header-danger-icon")));
        removeBtn.setOnAction(e -> {
            currentSessions.remove(session);
            refreshSessionsList();
        });

        row.getChildren().addAll(nameLabel, nameField, dayLabel, dayField, removeBtn);
        return row;
    }

    private void addNewSessionRow() {
        int maxDay = currentSessions.stream().mapToInt(WorkoutSessionBean::getDay).max().orElse(-1);
        int newDay = maxDay + 1;
        String name = String.valueOf(newDay);

        PlanNodeBean rootNode = new PlanNodeBean("root-" + java.util.UUID.randomUUID(), "Sessione Giorno " + newDay, NodeType.BLOCK);
        rootNode.addFlowDecorator(new FlowDecoratorBean(java.util.UUID.randomUUID().toString(), FlowDecoratorType.REST, "90s"));
        
        WorkoutSessionBean newSession = new WorkoutSessionBean(name, newDay, rootNode);
        currentSessions.add(newSession);
        refreshSessionsList();
    }

    private void save() {
        if (onSaveAction != null) {
            try {
                int cl = Integer.parseInt(cycleLengthField.getText().trim());
                onSaveAction.accept(cl, currentSessions);
            } catch (NumberFormatException e) {
                cycleLengthField.setStyle("-fx-border-color: red;");
            }
        }
    }

    public void setOnSaveAction(BiConsumer<Integer, List<WorkoutSessionBean>> onSaveAction) {
        this.onSaveAction = onSaveAction;
    }

    public void setOnCloseAction(Runnable onCloseAction) {
        this.onCloseAction = onCloseAction;
    }
}
