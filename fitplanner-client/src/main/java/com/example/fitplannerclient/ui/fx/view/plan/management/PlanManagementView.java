package com.example.fitplannerclient.ui.fx.view.plan.management;


import com.example.fitplannerclient.bean.plan.WorkoutPlanSummaryBean;
import com.example.fitplannerclient.bean.profile.ProfileBean;
import com.example.fitplannerclient.ui.fx.components.CardListView;
import com.example.fitplannerclient.ui.fx.components.Icon;
import com.example.fitplannerclient.ui.fx.components.utils.MenuUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.List;
import java.util.function.Consumer;

public class PlanManagementView extends BorderPane {

    private static final String BODY_BASE_CLASS = "body-base";
    private static final String BUTTON_HEADER_ICON = "button-header-icon";
    private static final String TEXT_COLOR_LIGHT = "text-color-light";

    private final CardListView<WorkoutPlanSummaryBean> cardListView;
    private List<ProfileBean> athletesCache; // todo da spostare al livello controller
    
    private final AssignPlanModal assignModal;

    private Runnable onNewPlanAction;
    private Consumer<WorkoutPlanSummaryBean> onAssignButtonClick;
    private Consumer<WorkoutPlanSummaryBean> onEditAction;
    private Consumer<WorkoutPlanSummaryBean> onCloneAction;
    private Consumer<WorkoutPlanSummaryBean> onDeleteAction;

    public PlanManagementView() {

        VBox contentBox = new VBox();
        contentBox.setPadding(new Insets(32));
        contentBox.setSpacing(24);

        // --- HEADER ---
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label subtitle = new Label("Crea, modifica, assegna o duplica i tuoi piani di allenamento");
        subtitle.getStyleClass().addAll(BODY_BASE_CLASS, TEXT_COLOR_LIGHT);
        titleBox.getChildren().addAll(subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("Nuovo Piano");
        addBtn.getStyleClass().add("button-primary");
        addBtn.setGraphic(new Icon("plus-icon", List.of("button-primary-icon")));
        addBtn.setMinWidth(Region.USE_PREF_SIZE);
        addBtn.setOnAction(e -> {
            if (onNewPlanAction != null) onNewPlanAction.run();
        });

        header.getChildren().addAll(titleBox, spacer, addBtn);

        // --- LIST CONTAINER ---
        Label nameHeader = new Label("Nome Piano");
        nameHeader.setPrefWidth(300);
        
        Label assignHeader = new Label("Assegnato a");
        assignHeader.setPrefWidth(200);
        
        cardListView = new CardListView<>(List.of(nameHeader, assignHeader));
        cardListView.setRowRenderer(this::createPlanRow);

        contentBox.getChildren().addAll(header, cardListView);

        ScrollPane mainScroll = new ScrollPane(contentBox);
        mainScroll.setFitToWidth(true);
        this.setCenter(mainScroll);

        assignModal = new AssignPlanModal();
    }

    public void setHeaderView(Node headerView) {
        this.setTop(headerView);
    }

    public AssignPlanModal getAssignModal() {
        return assignModal;
    }

    public void showModal(WorkoutPlanSummaryBean plan, List<ProfileBean> athletes) {
        assignModal.setPlan(plan, athletes);
    }

    public void setPlansList(List<WorkoutPlanSummaryBean> plans, List<ProfileBean> athletes) {
        this.athletesCache = athletes;
        cardListView.setItems(plans, "Nessun piano di allenamento presente.");
    }

    private HBox createPlanRow(WorkoutPlanSummaryBean plan, boolean isLast) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add(isLast ? "list-row-last" : "list-row");

        Label nameLbl = new Label(plan.getPlanTitle() != null ? plan.getPlanTitle() : "Senza Nome");
        nameLbl.getStyleClass().add(BODY_BASE_CLASS);
        nameLbl.setPrefWidth(300);

        VBox assignBox = buildAssignBox(plan);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actionsBox = buildActionsBox(plan);

        row.getChildren().addAll(nameLbl, assignBox, spacer, actionsBox);
        return row;
    }

    private VBox buildAssignBox(WorkoutPlanSummaryBean plan) {
        VBox assignBox = new VBox(2);
        assignBox.setPrefWidth(200);
        
        String assignedId = plan.getAssignedTo();
        ProfileBean assignedAthlete = null;
        if (assignedId != null && !assignedId.isEmpty() && athletesCache != null) {
            assignedAthlete = athletesCache.stream()
                .filter(a -> assignedId.equals(a.getUserId()))
                .findFirst().orElse(null);
        }

        if (assignedAthlete != null) {
            Label athleteName = new Label(assignedAthlete.getFirstName() + " " + assignedAthlete.getLastName());
            athleteName.getStyleClass().add(BODY_BASE_CLASS);
            Label athleteEmail = new Label(assignedAthlete.getContactEmail());
            athleteEmail.getStyleClass().addAll("body-small", TEXT_COLOR_LIGHT);
            assignBox.getChildren().addAll(athleteName, athleteEmail);
        } else {
            Label nessunoLbl = new Label("Nessuno");
            nessunoLbl.getStyleClass().addAll("body-small", TEXT_COLOR_LIGHT);
            assignBox.getChildren().add(nessunoLbl);
        }
        return assignBox;
    }

    private HBox buildActionsBox(WorkoutPlanSummaryBean plan) {
        HBox actionsBox = new HBox(12);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);

        Button optionsBtn = new Button();
        optionsBtn.getStyleClass().add("button-header");
        optionsBtn.setGraphic(new Icon("dots-vertical-icon", List.of(BUTTON_HEADER_ICON)));
        
        optionsBtn.setOnMousePressed(e -> {
            ContextMenu existingMenu = (ContextMenu) optionsBtn.getProperties().get("activeMenu");
            if (existingMenu != null && existingMenu.isShowing()) {
                existingMenu.hide();
                return;
            }
            
            ContextMenu menu = new ContextMenu();
            menu.setAutoHide(true);
            optionsBtn.getProperties().put("activeMenu", menu);
            
            menu.getItems().addAll(
                MenuUtils.createCustomMenuItem("Modifica", "edit-icon", BUTTON_HEADER_ICON, null, () -> {
                    if (onEditAction != null) onEditAction.accept(plan);
                }),
                MenuUtils.createCustomMenuItem("Assegna", "plus-icon", BUTTON_HEADER_ICON, null, () -> {
                    if (onAssignButtonClick != null) onAssignButtonClick.accept(plan);
                }),
                MenuUtils.createCustomMenuItem("Duplica", "copy-icon", BUTTON_HEADER_ICON, null, () -> {
                    if (onCloneAction != null) onCloneAction.accept(plan);
                }),
                MenuUtils.createCustomMenuItem("Elimina", "delete-icon", "button-header-danger-icon", "-fx-text-fill: #ef4444;", () -> {
                    if (onDeleteAction != null) onDeleteAction.accept(plan);
                })
            );
            
            menu.show(optionsBtn, Side.BOTTOM, 0, 5);
        });

        actionsBox.getChildren().add(optionsBtn);
        return actionsBox;
    }

    public void setOnAssignButtonClick(Consumer<WorkoutPlanSummaryBean> onAssignButtonClick) { this.onAssignButtonClick = onAssignButtonClick; }
    public void setOnNewPlanAction(Runnable onNewPlanAction) { this.onNewPlanAction = onNewPlanAction; }
    public void setOnEditAction(Consumer<WorkoutPlanSummaryBean> onEditAction) { this.onEditAction = onEditAction; }
    public void setOnCloneAction(Consumer<WorkoutPlanSummaryBean> onCloneAction) { this.onCloneAction = onCloneAction; }
    public void setOnDeleteAction(Consumer<WorkoutPlanSummaryBean> onDeleteAction) { this.onDeleteAction = onDeleteAction; }

}
