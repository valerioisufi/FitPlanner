package com.example.fitplannerclient.ui.fx.view.plan;

import com.example.fitplannerclient.bean.plan.WorkoutPlanBean;
import com.example.fitplannerclient.bean.plan.WorkoutPlanSummaryBean;
import com.example.fitplannerclient.bean.profile.ProfileBean;
import com.example.fitplannerclient.ui.fx.components.CardListView;
import com.example.fitplannerclient.ui.fx.components.Icon;
import com.example.fitplannerclient.ui.fx.components.ModalOverlay;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.List;
import java.util.function.Consumer;

public class PlanManagementView extends BorderPane {

    private final CardListView<WorkoutPlanSummaryBean> cardListView;
    
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
        subtitle.getStyleClass().addAll("body-base", "text-color-light");
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
        
        Label sessionsHeader = new Label("Sessioni");
        sessionsHeader.setPrefWidth(150);
        
        cardListView = new CardListView<>(List.of(nameHeader, sessionsHeader));
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

    public void showModal(WorkoutPlanBean plan, List<ProfileBean> athletes) {
        assignModal.setPlan(plan, athletes);
    }

    public void setPlansList(List<WorkoutPlanSummaryBean> plans) {
        cardListView.setItems(plans, "Nessun piano di allenamento presente.");
    }

    private HBox createPlanRow(WorkoutPlanSummaryBean plan, boolean isLast) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add(isLast ? "list-row-last" : "list-row");

        // Plan Name
        Label nameLbl = new Label(plan.getPlanTitle() != null ? plan.getPlanTitle() : "Senza Nome");
        nameLbl.getStyleClass().add("body-base");
        nameLbl.setPrefWidth(300);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Actions
        HBox actionsBox = new HBox(12);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);

        Button optionsBtn = new Button();
        optionsBtn.getStyleClass().add("button-header");
        optionsBtn.setGraphic(new Icon("dots-vertical-icon", List.of("button-header-icon")));
        
        optionsBtn.setOnAction(e -> {
            ContextMenu menu = new ContextMenu();
            menu.setAutoHide(true);
            
            menu.getItems().addAll(
                createCustomMenuItem("Modifica", "edit-icon", "button-header-icon", null, () -> {
                    if (onEditAction != null) onEditAction.accept(plan);
                }),
                createCustomMenuItem("Assegna", "plus-icon", "button-header-icon", null, () -> {
                    if (onAssignButtonClick != null) onAssignButtonClick.accept(plan);
                }),
                createCustomMenuItem("Duplica", "copy-icon", "button-header-icon", null, () -> {
                    if (onCloneAction != null) onCloneAction.accept(plan);
                }),
                createCustomMenuItem("Elimina", "delete-icon", "button-header-danger-icon", "-fx-text-fill: #ef4444;", () -> {
                    if (onDeleteAction != null) onDeleteAction.accept(plan);
                })
            );
            
            menu.show(optionsBtn, Side.BOTTOM, 0, 5);
        });

        actionsBox.getChildren().add(optionsBtn);



        row.getChildren().addAll(nameLbl, spacer, actionsBox);
        return row;
    }

    private CustomMenuItem createCustomMenuItem(String text, String iconName, String iconClass, String textStyle, Runnable action) {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);

        if (iconName != null) {
            Icon icon = new Icon(iconName, List.of(iconClass != null ? iconClass : "button-header-icon"));
            box.getChildren().add(icon);
        }

        Label label = new Label(text);
        label.getStyleClass().add("body-base");
        if (textStyle != null) {
            label.setStyle(textStyle);
        }

        box.getChildren().add(label);

        CustomMenuItem item = new CustomMenuItem(box);
        item.setHideOnClick(true);
        item.setOnAction(e -> action.run());

        return item;
    }

    public void setOnAssignButtonClick(Consumer<WorkoutPlanSummaryBean> onAssignButtonClick) { this.onAssignButtonClick = onAssignButtonClick; }
    public void setOnNewPlanAction(Runnable onNewPlanAction) { this.onNewPlanAction = onNewPlanAction; }
    public void setOnEditAction(Consumer<WorkoutPlanSummaryBean> onEditAction) { this.onEditAction = onEditAction; }
    public void setOnCloneAction(Consumer<WorkoutPlanSummaryBean> onCloneAction) { this.onCloneAction = onCloneAction; }
    public void setOnDeleteAction(Consumer<WorkoutPlanSummaryBean> onDeleteAction) { this.onDeleteAction = onDeleteAction; }

}
