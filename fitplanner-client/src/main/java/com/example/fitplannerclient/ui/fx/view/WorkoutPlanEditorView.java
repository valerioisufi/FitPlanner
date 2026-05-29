package com.example.fitplannerclient.ui.fx.view;

import com.example.fitplannerclient.bean.plan.*;
import com.example.fitplannerclient.ui.fx.components.Icon;
import com.example.fitplannerclient.ui.fx.view.plan.BadgeComponent;
import com.example.fitplannerclient.ui.fx.view.plan.EditBadgeModal;
import com.example.fitplannerclient.ui.fx.view.plan.EditNodeNameModal;
import com.example.fitplannerclient.ui.fx.view.plan.PlanNodeComponent;
import com.example.fitplannerclient.ui.fx.view.plan.PlanViewer;
import com.example.fitplannerclient.ui.fx.view.plan.ManageSessionsModal;
import com.example.fitplannerclient.bean.exercise.ExerciseDescriptionBean;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import com.example.fitplannerclient.ui.fx.components.ModalOverlay;
import java.util.List;
import java.util.function.Consumer;

public class WorkoutPlanEditorView extends StackPane {

    private final BorderPane mainPane = new BorderPane();

    private final ComboBox<WorkoutSessionBean> sessionComboBox = new ComboBox<>();
    private final Button btnManageSessions = new Button("Gestisci sessioni");
    private final Button btnSavePlan = new Button("Salva");
    private final Button btnCancel = new Button("Annulla");

    private final Button btnUndo = new Button();
    private final Button btnRedo = new Button();

    // Center area
    private final PlanViewer planViewer = new PlanViewer();

    // Right sidebar controls
    private final VBox rightSidebar = new VBox(20);
    private final TextField searchField = new TextField();
    private final VBox exerciseListContainer = new VBox(10);
    
    // Modals
    private final ModalOverlay modalOverlay;
    private final EditNodeNameModal editNodeModal;
    private final EditBadgeModal editBadgeModal;
    private final ManageSessionsModal manageSessionsModal;

    // Callbacks
    private Consumer<WorkoutSessionBean> onSessionSelected;
    private Runnable onManageSessionsRequested;
    private Runnable onSavePlanClicked;
    private Runnable onCancelClicked;

    public WorkoutPlanEditorView() {
        // Set custom top toolbar
        mainPane.setTop(createTopToolbar());

        // Main Layout: Center Panel, Right Sidebar
        HBox mainLayout = new HBox(20);
        mainLayout.setPadding(new Insets(20));

        // --- CENTER PANEL (Tree Editor) ---
        VBox centerPanel = createCenterPanel();
        HBox.setHgrow(centerPanel, Priority.ALWAYS);

        // --- RIGHT SIDEBAR (Exercise Library & Badges Palette) ---
        rightSidebar.setPrefWidth(300);
        rightSidebar.setPadding(new Insets(10));
        rightSidebar.getStyleClass().add("card");
        createRightSidebar();
        HBox.setHgrow(rightSidebar, Priority.NEVER);

        mainLayout.getChildren().addAll(centerPanel, rightSidebar);
        mainPane.setCenter(mainLayout);

        editNodeModal = new EditNodeNameModal();
        editBadgeModal = new EditBadgeModal();
        manageSessionsModal = new ManageSessionsModal();
        modalOverlay = new ModalOverlay(new VBox()); // Placeholder, will set content dynamically

        editNodeModal.setOnCloseAction(modalOverlay::hide);
        editBadgeModal.setOnCloseAction(modalOverlay::hide);
        manageSessionsModal.setOnCloseAction(modalOverlay::hide);

        this.getChildren().addAll(mainPane, modalOverlay);

        setupModalsLogic();
    }

    private void setupModalsLogic() {
        planViewer.setOnNodeEditRequest(node -> {
            editNodeModal.setInitialName(node.getNodeName()); 
            editNodeModal.setOnSaveAction(newName -> {
                node.updateName(newName);
                modalOverlay.hide();
            });
            modalOverlay.setContent(editNodeModal);
            modalOverlay.show();
        });
        planViewer.setOnBadgeEditRequest(badge -> {
            editBadgeModal.setInitialData(badge.getBadgeType(), badge.getName(), badge.getValue());
            editBadgeModal.setOnSaveAction((newName, newValue) -> {
                badge.updateBadge(newName, newValue);
                modalOverlay.hide();
            });
            modalOverlay.setContent(editBadgeModal);
            modalOverlay.show();
        });
    }

    private HBox createTopToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10, 20, 10, 20));
        toolbar.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        // --- LEFT: Session Selector & Undo/Redo ---
        HBox leftBox = new HBox(10);
        leftBox.setAlignment(Pos.CENTER_LEFT);

        // Cell factory for combo box to display session name
        sessionComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(WorkoutSessionBean session, boolean empty) {
                super.updateItem(session, empty);
                setText(empty || session == null ? null : session.getName());
            }
        });
        sessionComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(WorkoutSessionBean session, boolean empty) {
                super.updateItem(session, empty);
                setText(empty || session == null ? "Seleziona Giorno" : session.getName());
            }
        });
        sessionComboBox.setOnAction(e -> {
            WorkoutSessionBean selected = sessionComboBox.getSelectionModel().getSelectedItem();
            if (selected != null) {
                planViewer.setRootNode(selected.getPlanRoot());
                if (onSessionSelected != null) {
                    onSessionSelected.accept(selected);
                }
            }
        });
        sessionComboBox.setPrefWidth(150);

        btnManageSessions.getStyleClass().add("button-secondary");
        btnManageSessions.setOnAction(e -> { if(onManageSessionsRequested != null) onManageSessionsRequested.run(); });

        btnUndo.setGraphic(new Icon("undo-icon", List.of("button-header-icon")));
        btnUndo.getStyleClass().add("button-secondary");

        btnRedo.setGraphic(new Icon("redo-icon", List.of("button-header-icon")));
        btnRedo.getStyleClass().add("button-secondary");

        leftBox.getChildren().addAll(sessionComboBox, btnManageSessions, new Separator(Orientation.VERTICAL), btnUndo, btnRedo);

        // --- CENTER: Draggable Decorators/Modifiers Palette ---
        HBox paletteBox = new HBox(10);
        paletteBox.setAlignment(Pos.CENTER_LEFT);

        ScrollPane scrollPalette = new ScrollPane(paletteBox);
        scrollPalette.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPalette.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPalette.setFitToHeight(true);
        scrollPalette.setMinHeight(50);
        
        HBox.setHgrow(scrollPalette, Priority.ALWAYS);

        // --- RIGHT: Cancel and Save Actions ---
        HBox rightBox = new HBox(10);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        btnCancel.getStyleClass().add("button-secondary");
        btnCancel.setOnAction(e -> { if(onCancelClicked != null) onCancelClicked.run(); });

        btnSavePlan.getStyleClass().add("button-primary");
        btnSavePlan.setOnAction(e -> { if(onSavePlanClicked != null) onSavePlanClicked.run(); });

        rightBox.getChildren().addAll(btnCancel, btnSavePlan);

        toolbar.getChildren().addAll(leftBox, new Separator(Orientation.VERTICAL), scrollPalette, new Separator(Orientation.VERTICAL), rightBox);

        return toolbar;
    }

    private VBox createCenterPanel() {
        VBox panel = new VBox(15);
        panel.getStyleClass().add("card");
        panel.setPadding(new Insets(20));

        VBox.setVgrow(planViewer, Priority.ALWAYS);

        panel.getChildren().add(planViewer);
        return panel;
    }

    private void createRightSidebar() {
        Label lblLibrary = new Label("Libreria Esercizi");
        lblLibrary.getStyleClass().add("heading-h2");

        searchField.setPromptText("Cerca esercizio...");

        ScrollPane scrollEx = new ScrollPane(exerciseListContainer);
        scrollEx.setFitToWidth(true);
        scrollEx.setPrefHeight(250);
        VBox.setVgrow(scrollEx, Priority.ALWAYS);

        rightSidebar.getChildren().addAll(lblLibrary, searchField, scrollEx);
    }

    private HBox createDraggableBadge(String label, Object bean, BadgeComponent.BadgeType type) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 8; -fx-background-radius: 6; -fx-border-color: #E2E8F0; -fx-border-radius: 6; -fx-cursor: hand;");

        Label name = new Label(label);
        name.setStyle("-fx-font-family: 'Space Grotesk Medium'; -fx-font-size: 12px;");

        row.getChildren().add(name);

        row.setOnDragDetected(e -> {
            Dragboard db = row.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            content.putString(type.name());
            db.setContent(content);

            // Initiate the drag context in PlanNodeComponent
            PlanNodeComponent.initiateExternalDrag(bean, type);

            e.consume();
        });

        return row;
    }

    public void setExercises(List<ExerciseDescriptionBean> exercises) {
        exerciseListContainer.getChildren().clear();
        for (ExerciseDescriptionBean ex : exercises) {
            VBox item = new VBox(5);
            item.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 10; -fx-background-radius: 8; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-cursor: hand;");

            Label name = new Label(ex.getName());
            name.setStyle("-fx-font-family: 'Space Grotesk Bold'; -fx-font-size: 13px; -fx-text-fill: -fx-color-text-body;");

            FlowPane tags = new FlowPane(4, 4);
            if (ex.getMuscleGroups() != null) {
                for (String m : ex.getMuscleGroups()) {
                    Label tag = new Label(m);
                    tag.setStyle("-fx-background-color: #E2E8F0; -fx-text-fill: #475569; -fx-padding: 2 6; -fx-background-radius: 4; -fx-font-size: 9px;");
                    tags.getChildren().add(tag);
                }
            }

            item.getChildren().addAll(name, tags);

            item.setOnDragDetected(e -> {
                Dragboard db = item.startDragAndDrop(TransferMode.COPY);
                ClipboardContent content = new ClipboardContent();
                content.putString("EXERCISE:" + ex.getName());
                db.setContent(content);
                e.consume();
            });

            exerciseListContainer.getChildren().add(item);
        }
    }



    public void setPlan(WorkoutPlanBean plan) {
        sessionComboBox.getItems().clear();
        if (plan != null && plan.getSessions() != null) {
            sessionComboBox.getItems().addAll(plan.getSessions());
            if (!plan.getSessions().isEmpty()) {
                sessionComboBox.getSelectionModel().selectFirst();
            }
        }
    }

    public void disableEditing(boolean disable) {
        btnManageSessions.setVisible(!disable);
        btnSavePlan.setVisible(!disable);
        rightSidebar.setVisible(!disable);
        rightSidebar.setManaged(!disable);
    }

    // Callback setters
    public void setOnSessionSelected(Consumer<WorkoutSessionBean> callback) { this.onSessionSelected = callback; }
    public void setOnManageSessionsRequested(Runnable callback) { this.onManageSessionsRequested = callback; }
    
    public void showManageSessionsModal(int cycleLength, List<WorkoutSessionBean> sessions, java.util.function.BiConsumer<Integer, List<WorkoutSessionBean>> onSave) {
        manageSessionsModal.setInitialData(cycleLength, sessions);
        manageSessionsModal.setOnSaveAction((newCycleLength, updatedSessions) -> {
            modalOverlay.hide();
            onSave.accept(newCycleLength, updatedSessions);
        });
        modalOverlay.setContent(manageSessionsModal);
        modalOverlay.show();
    }

    public void setOnSavePlanClicked(Runnable callback) { this.onSavePlanClicked = callback; }
    public void setOnCancelClicked(Runnable callback) { this.onCancelClicked = callback; }
}
