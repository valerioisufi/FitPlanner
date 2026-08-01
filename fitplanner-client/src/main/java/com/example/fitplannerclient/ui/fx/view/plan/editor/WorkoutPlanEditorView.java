package com.example.fitplannerclient.ui.fx.view.plan.editor;

import com.example.fitplannerclient.bean.exercise.ExerciseDescriptionBean;
import com.example.fitplannerclient.bean.plan.PlanNodeBean;
import com.example.fitplannerclient.bean.plan.WorkoutPlanBean;
import com.example.fitplannerclient.bean.plan.WorkoutSessionBean;
import com.example.fitplannerclient.ui.fx.components.Icon;
import com.example.fitplannerclient.ui.fx.view.plan.editor.components.BadgeComponent;
import com.example.fitplannerclient.ui.fx.view.plan.editor.dnd.DragConstants;
import com.example.fitplannerclient.ui.fx.view.plan.editor.modal.*;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class WorkoutPlanEditorView extends BorderPane {

    private static final String BUTTON_HEADER_CLASS = "button-header";
    private static final String BUTTON_HEADER_ICON_CLASS = "button-header-icon";

    private final Label planNameLabel = new Label();
    private final EditPlanNameModal editPlanNameModal;
    private final ComboBox<WorkoutSessionBean> sessionComboBox = new ComboBox<>();
    private final Button btnManageSessions = new Button();
    private final Button btnSavePlan = new Button("Salva");
    private final Button btnCancel = new Button("Annulla");

    private final Button btnUndo = new Button();
    private final Button btnRedo = new Button();

    // Center area
    private final PlanViewer planViewer = new PlanViewer();

    // Right sidebar controls (Toolbox)
    private final VBox rightSidebar = new VBox(20);
    private final VBox protocolBlocksContainer = new VBox(8);
    private final VBox decoratorsContainer = new VBox(8);
    private final VBox modifiersContainer = new VBox(8);
    
    // Modals
    private final EditNodeNameModal editNodeModal;
    private final EditBadgeModal editBadgeModal;
    private final ManageSessionsModal manageSessionsModal;
    private final SelectExerciseModal selectExerciseModal;
    private final EditProtocolModal editProtocolModal;

    // Callbacks
    private Consumer<WorkoutSessionBean> onSessionSelected;
    private Runnable onManageSessionsRequested;
    private Runnable onSavePlanClicked;
    private Runnable onCancelClicked;
    private Runnable onUndoClicked;
    private Runnable onRedoClicked;

    // Callback for generic modal actions
    private Consumer<Node> onShowModalRequested;
    private Runnable onHideModalRequested;
    private Consumer<String> onPlanNameChanged;
    private WorkoutPlanBean currentPlan;

    public WorkoutPlanEditorView() {
        // Set custom top toolbar
        this.setTop(createTopToolbar());

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
        this.setCenter(mainLayout);

        editNodeModal = new EditNodeNameModal();
        editBadgeModal = new EditBadgeModal();
        manageSessionsModal = new ManageSessionsModal();
        selectExerciseModal = new SelectExerciseModal();
        editProtocolModal = new EditProtocolModal();
        editPlanNameModal = new EditPlanNameModal();

        editNodeModal.setOnCloseAction(() -> { if (onHideModalRequested != null) onHideModalRequested.run(); });
        editBadgeModal.setOnCloseAction(() -> { if (onHideModalRequested != null) onHideModalRequested.run(); });
        manageSessionsModal.setOnCloseAction(() -> { if (onHideModalRequested != null) onHideModalRequested.run(); });
        selectExerciseModal.setOnCloseAction(() -> { if (onHideModalRequested != null) onHideModalRequested.run(); });
        editProtocolModal.setOnCloseAction(() -> { if (onHideModalRequested != null) onHideModalRequested.run(); });
        editPlanNameModal.setOnCloseAction(() -> { if (onHideModalRequested != null) onHideModalRequested.run(); });
    }

    private HBox createTopToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10, 20, 10, 20));
        toolbar.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        // --- LEFT: Plan Name, Session Selector & Undo/Redo ---
        HBox leftBox = new HBox(10);
        leftBox.setAlignment(Pos.CENTER_LEFT);

        setupPlanNameLabel();
        setupSessionComboBox();
        sessionComboBox.setPrefWidth(200);

        btnManageSessions.setGraphic(new Icon("calendar-icon", List.of(BUTTON_HEADER_ICON_CLASS)));
        btnManageSessions.getStyleClass().add(BUTTON_HEADER_CLASS);
        btnManageSessions.setOnAction(e -> { if(onManageSessionsRequested != null) onManageSessionsRequested.run(); });

        btnUndo.setGraphic(new Icon("undo-icon", List.of(BUTTON_HEADER_ICON_CLASS)));
        btnUndo.getStyleClass().add(BUTTON_HEADER_CLASS);

        btnRedo.setGraphic(new Icon("redo-icon", List.of(BUTTON_HEADER_ICON_CLASS)));
        btnRedo.getStyleClass().add(BUTTON_HEADER_CLASS);

        btnUndo.setOnAction(e -> { if(onUndoClicked != null) onUndoClicked.run(); });
        btnRedo.setOnAction(e -> { if(onRedoClicked != null) onRedoClicked.run(); });

        leftBox.getChildren().addAll(planNameLabel, new Separator(Orientation.VERTICAL), sessionComboBox, btnManageSessions, new Separator(Orientation.VERTICAL), btnUndo, btnRedo);

        // --- CENTER: Spacer ---
        Region centerSpacer = new Region();
        HBox.setHgrow(centerSpacer, Priority.ALWAYS);

        // --- RIGHT: Cancel and Save Actions ---
        HBox rightBox = new HBox(10);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        btnCancel.getStyleClass().add("button-secondary");
        btnCancel.setOnAction(e -> { if(onCancelClicked != null) onCancelClicked.run(); });

        btnSavePlan.getStyleClass().add("button-primary");
        btnSavePlan.setOnAction(e -> { if(onSavePlanClicked != null) onSavePlanClicked.run(); });

        rightBox.getChildren().addAll(btnCancel, btnSavePlan);

        toolbar.getChildren().addAll(leftBox, centerSpacer, rightBox);

        return toolbar;
    }

    private void setupPlanNameLabel() {
        planNameLabel.getStyleClass().add("heading-h1");
        planNameLabel.setStyle("-fx-cursor: hand; -fx-padding: 0 10 0 0;");
        planNameLabel.setOnMouseClicked(e -> {
            if (currentPlan != null) {
                editPlanNameModal.setInitialName(currentPlan.getName());
                editPlanNameModal.setOnSaveAction(newName -> {
                    if (onPlanNameChanged != null) {
                        onPlanNameChanged.accept(newName);
                    }
                    if (onHideModalRequested != null) onHideModalRequested.run();
                });
                if (onShowModalRequested != null) onShowModalRequested.accept(editPlanNameModal);
            }
        });
    }

    private void setupSessionComboBox() {
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
        sessionComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                planViewer.setRootNode(newVal.getPlanRoot());
                if (onSessionSelected != null) {
                    onSessionSelected.accept(newVal);
                }
            }
        });
    }

    public void setOnUndoClicked(Runnable onUndoClicked) {
        this.onUndoClicked = onUndoClicked;
    }

    public void setOnRedoClicked(Runnable onRedoClicked) {
        this.onRedoClicked = onRedoClicked;
    }

    public SelectExerciseModal getSelectExerciseModal() { return selectExerciseModal; }

    public EditProtocolModal getEditProtocolModal() { return editProtocolModal; }

    public EditBadgeModal getEditBadgeModal() { return editBadgeModal; }

    public EditNodeNameModal getEditNodeModal() { return editNodeModal; }

    public PlanViewer getPlanViewer() {
        return planViewer;
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
        Label lblToolbox = new Label("Toolbox");
        lblToolbox.getStyleClass().add("heading-h2");

        Accordion accordion = new Accordion();

        // 1. Nodi Base
        VBox baseNodesBox = new VBox(8);
        baseNodesBox.setPadding(new Insets(10));
        baseNodesBox.getChildren().addAll(
            createToolboxItem("Esercizio", "TOOLBOX:EXERCISE", null),
            createToolboxItem("Blocco", "TOOLBOX:BLOCK", null)
        );
        TitledPane tpBase = new TitledPane("Nodi Base", baseNodesBox);

        // 2. Protocolli
        protocolBlocksContainer.setPadding(new Insets(10));
        TitledPane tpProtocols = new TitledPane("Protocolli", protocolBlocksContainer);

        // 3. Decoratori
        decoratorsContainer.setPadding(new Insets(10));
        TitledPane tpDecorators = new TitledPane("Decoratori", decoratorsContainer);

        // 4. Modificatori
        modifiersContainer.setPadding(new Insets(10));
        TitledPane tpModifiers = new TitledPane("Modificatori", modifiersContainer);

        accordion.getPanes().addAll(tpBase, tpProtocols, tpDecorators, tpModifiers);
        accordion.setExpandedPane(tpBase);

        VBox.setVgrow(accordion, Priority.ALWAYS);

        rightSidebar.getChildren().addAll(lblToolbox, accordion);
    }

    private VBox createToolboxItem(String label, String dragPayload, Node graphic) {
        VBox item = new VBox(5);

        if (graphic != null) {
            item.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
            item.getChildren().add(graphic);
        } else {
            item.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 10; -fx-background-radius: 8; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-cursor: hand;");
            Label name = new Label(label);
            name.setStyle("-fx-font-family: 'Space Grotesk Bold'; -fx-font-size: 13px; -fx-text-fill: -fx-color-text-body;");
            item.getChildren().add(name);

            item.setOnMouseEntered(e -> item.setStyle("-fx-background-color: #E2E8F0; -fx-padding: 10; -fx-background-radius: 8; -fx-border-color: #CBD5E1; -fx-border-radius: 8; -fx-cursor: hand;"));
            item.setOnMouseExited(e -> item.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 10; -fx-background-radius: 8; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-cursor: hand;"));
        }

        item.setOnDragDetected(e -> {
            Dragboard db = item.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            content.put(DragConstants.FITPLANNER_FORMAT, dragPayload);
            db.setContent(content);

            SnapshotParameters snapParams = new SnapshotParameters();
            snapParams.setFill(Color.TRANSPARENT);
            db.setDragView(item.snapshot(snapParams, null));

            e.consume();
        });

        return item;
    }

    public void setExercises(List<ExerciseDescriptionBean> exercises) {
        selectExerciseModal.setExercises(exercises);
    }

    public void setProtocolBlocks(List<PlanNodeBean> protocols) {
        protocolBlocksContainer.getChildren().clear();
        for (PlanNodeBean protocol : protocols) {
            protocolBlocksContainer.getChildren().add(
                createToolboxItem(protocol.getName(), "TOOLBOX:PROTOCOL:" + protocol.getResourceId(), null)
            );
        }
    }

    public void setDecorators(List<String> decoratorTypes) {
        decoratorsContainer.getChildren().clear();
        for (String type : decoratorTypes) {
            BadgeComponent badge = new BadgeComponent(null, BadgeComponent.BadgeType.DECORATOR, type, "", BadgeComponent.resolveColorFromName(type, BadgeComponent.BadgeType.DECORATOR));
            decoratorsContainer.getChildren().add(
                createToolboxItem(type, "TOOLBOX:DECORATOR:" + type, badge)
            );
        }
    }

    public void setModifiers(List<String> modifierTypes) {
        modifiersContainer.getChildren().clear();
        for (String type : modifierTypes) {
            BadgeComponent badge = new BadgeComponent(null, BadgeComponent.BadgeType.MODIFIER, type, "", BadgeComponent.resolveColorFromName(type, BadgeComponent.BadgeType.MODIFIER));
            modifiersContainer.getChildren().add(
                createToolboxItem(type, "TOOLBOX:MODIFIER:" + type, badge)
            );
        }
    }

    public void setPlan(WorkoutPlanBean plan) {
        this.currentPlan = plan;
        
        int selectedDay = -1;
        WorkoutSessionBean currentSelected = sessionComboBox.getSelectionModel().getSelectedItem();
        if (currentSelected != null) {
            selectedDay = currentSelected.getDay();
        }

        sessionComboBox.getItems().clear();

        if (plan != null) {
            planNameLabel.setText(plan.getName() != null ? plan.getName() : "Nuovo Piano");
            if (plan.getSessions() != null) {
                sessionComboBox.getItems().addAll(plan.getSessions());

                if (!plan.getSessions().isEmpty()) {
                    int finalSelectedDay = selectedDay;
                    WorkoutSessionBean toSelect = plan.getSessions().stream()
                            .filter(s -> s.getDay() == finalSelectedDay)
                            .findFirst()
                            .orElse(plan.getSessions().getFirst());
                    sessionComboBox.getSelectionModel().select(toSelect);
                }
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
    
    public void showManageSessionsModal(int cycleLength, List<WorkoutSessionBean> sessions) {
        manageSessionsModal.setInitialData(cycleLength, sessions);
        if (onShowModalRequested != null) onShowModalRequested.accept(manageSessionsModal);
    }

    public void setOnCycleLengthChanged(Consumer<Integer> callback) {
        manageSessionsModal.setOnCycleLengthChanged(callback);
    }

    public void setOnSessionNameChanged(BiConsumer<Integer, String> callback) {
        manageSessionsModal.setOnSessionNameChanged(callback);
    }

    public void setOnSessionDayChanged(BiConsumer<Integer, Integer> callback) {
        manageSessionsModal.setOnSessionDayChanged(callback);
    }

    public void setOnSessionAdded(Consumer<Integer> callback) {
        manageSessionsModal.setOnSessionAdded(callback);
    }

    public void setOnSessionRemoved(Consumer<Integer> callback) {
        manageSessionsModal.setOnSessionRemoved(callback);
    }

    public void setOnSavePlanClicked(Runnable callback) { this.onSavePlanClicked = callback; }
    public void setOnCancelClicked(Runnable callback) { this.onCancelClicked = callback; }

    public void setOnShowModalRequested(Consumer<Node> onShowModalRequested) { this.onShowModalRequested = onShowModalRequested; }
    public void setOnHideModalRequested(Runnable onHideModalRequested) { this.onHideModalRequested = onHideModalRequested; }
    public void setOnPlanNameChanged(Consumer<String> callback) { this.onPlanNameChanged = callback; }
}
