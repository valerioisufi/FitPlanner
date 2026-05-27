package com.example.fitplannerclient.ui.fx.view.plan;

import com.example.fitplannerclient.bean.plan.WorkoutPlanBean;
import com.example.fitplannerclient.bean.profile.ProfileBean;
import com.example.fitplannerclient.ui.fx.components.CardListView;
import com.example.fitplannerclient.ui.fx.components.Icon;
import com.example.fitplannerclient.ui.fx.components.ModalOverlay;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import java.util.List;
import java.util.function.Consumer;

public class PlanManagementView extends StackPane {

    private final BorderPane mainPane;
    private final CardListView<WorkoutPlanBean> cardListView;
    
    private final ModalOverlay modalOverlay;
    private final AssignPlanModal assignModal;

    private Runnable onNewPlanAction;
    private Consumer<WorkoutPlanBean> onEditAction;
    private Consumer<WorkoutPlanBean> onCloneAction;
    private Consumer<WorkoutPlanBean> onDeleteAction;

    public PlanManagementView() {
        mainPane = new BorderPane();

        VBox contentBox = new VBox();
        contentBox.setPadding(new Insets(32));
        contentBox.setSpacing(24);

        // --- HEADER ---
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label subtitle = new Label("Crea, modifica, assegna o duplica i tuoi piani di allenamento");
        subtitle.getStyleClass().addAll("body-base", "text-color-light");
        titleBox.getChildren().addAll(subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("Nuovo Piano");
        addBtn.getStyleClass().add("button-primary");
        addBtn.setGraphic(new Icon("plus-icon", List.of("button-primary-icon")));
        addBtn.setOnAction(e -> {
            if (onNewPlanAction != null) onNewPlanAction.run();
        });

        header.getChildren().addAll(titleBox, spacer, addBtn);

        // --- LIST CONTAINER ---
        Label nameHeader = new Label("Nome Piano");
        nameHeader.setPrefWidth(300);
        
        Label sessionsHeader = new Label("Sessioni");
        sessionsHeader.setPrefWidth(150);
        
        cardListView = new CardListView<>(List.of(nameHeader, sessionsHeader));
        cardListView.setRowRenderer(this::createPlanRow);

        contentBox.getChildren().addAll(header, cardListView);

        ScrollPane mainScroll = new ScrollPane(contentBox);
        mainScroll.setFitToWidth(true);
        mainScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        mainPane.setCenter(mainScroll);

        // --- MODAL OVERLAY ---
        assignModal = new AssignPlanModal();
        modalOverlay = new ModalOverlay(assignModal);

        this.getChildren().addAll(mainPane, modalOverlay);
    }

    public void setHeaderView(Node headerView) {
        mainPane.setTop(headerView);
    }

    public AssignPlanModal getAssignModal() {
        return assignModal;
    }

    public void showModal(WorkoutPlanBean plan, List<ProfileBean> athletes) {
        assignModal.setPlan(plan, athletes);
        modalOverlay.show();
    }

    public void hideModal() {
        modalOverlay.hide();
    }

    public void setPlansList(List<WorkoutPlanBean> plans) {
        cardListView.setItems(plans, "Nessun piano di allenamento presente.");
    }

    private HBox createPlanRow(WorkoutPlanBean plan, boolean isLast) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add(isLast ? "list-row-last" : "list-row");

        // Plan Name
        Label nameLbl = new Label(plan.getName() != null ? plan.getName() : "Senza Nome");
        nameLbl.getStyleClass().add("body-base");
        nameLbl.setPrefWidth(300);

        // Sessions Count
        int sessionCount = plan.getSessions() != null ? plan.getSessions().size() : 0;
        Label sessionsLbl = new Label(sessionCount + " giorni");
        sessionsLbl.getStyleClass().addAll("body-small", "text-color-light");
        sessionsLbl.setPrefWidth(150);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Actions
        HBox actionsBox = new HBox(12);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);

        Button editBtn = new Button("Modifica");
        editBtn.getStyleClass().add("button-header");
        editBtn.setGraphic(new Icon("edit-icon", List.of("button-header-icon")));
        editBtn.setOnAction(e -> {
            if (onEditAction != null) onEditAction.accept(plan);
        });
        
        Button assignBtn = new Button("Assegna");
        assignBtn.getStyleClass().add("button-header");
        assignBtn.setOnAction(e -> {
            if (onAssignButtonClick != null) {
                onAssignButtonClick.accept(plan);
            }
        });

        Button cloneBtn = new Button("Duplica");
        cloneBtn.getStyleClass().add("button-header");
        cloneBtn.setOnAction(e -> {
            if (onCloneAction != null) onCloneAction.accept(plan);
        });

        Button deleteBtn = new Button("Elimina");
        deleteBtn.getStyleClass().addAll("button-header", "button-header-danger");
        deleteBtn.setGraphic(new Icon("delete-icon", List.of("button-header-danger-icon")));
        deleteBtn.setOnAction(e -> {
            if (onDeleteAction != null) onDeleteAction.accept(plan);
        });

        actionsBox.getChildren().addAll(editBtn, assignBtn, cloneBtn, deleteBtn);



        row.getChildren().addAll(nameLbl, sessionsLbl, spacer, actionsBox);
        return row;
    }

    private Consumer<WorkoutPlanBean> onAssignButtonClick;

    public void setOnAssignButtonClick(Consumer<WorkoutPlanBean> onAssignButtonClick) {
        this.onAssignButtonClick = onAssignButtonClick;
    }
    public void setOnNewPlanAction(Runnable onNewPlanAction) { this.onNewPlanAction = onNewPlanAction; }
    public void setOnEditAction(Consumer<WorkoutPlanBean> onEditAction) { this.onEditAction = onEditAction; }
    public void setOnCloneAction(Consumer<WorkoutPlanBean> onCloneAction) { this.onCloneAction = onCloneAction; }
    public void setOnDeleteAction(Consumer<WorkoutPlanBean> onDeleteAction) { this.onDeleteAction = onDeleteAction; }
}
