package com.example.fitplannerclient.ui.fx.view.plan.management;

import com.example.fitplannerclient.bean.plan.WorkoutPlanSummaryBean;
import com.example.fitplannerclient.bean.profile.ProfileBean;
import com.example.fitplannerclient.ui.fx.components.Icon;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.List;
import java.util.function.Consumer;

public class AssignPlanModal extends VBox {

    private final ComboBox<ProfileBean> athleteComboBox;
    private final Label titleLabel;
    private final Label subtitleLabel;
    
    private WorkoutPlanSummaryBean currentPlan;

    private Consumer<ProfileBean> onAssignAction;
    private Runnable onCloseAction;

    public AssignPlanModal() {
        this.getStyleClass().add("card");
        this.setPadding(new Insets(32));
        this.setSpacing(24);
        this.setMaxWidth(500);
        this.setMaxHeight(Region.USE_PREF_SIZE);

        // --- HEADER ---
        HBox header = new HBox();
        header.setAlignment(Pos.TOP_LEFT);

        VBox titleBox = new VBox(4);
        titleLabel = new Label("Assegna Piano");
        titleLabel.getStyleClass().add("heading-h1");
        subtitleLabel = new Label("Seleziona l'atleta a cui assegnare il piano");
        subtitleLabel.getStyleClass().add("body-small");
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button();
        closeBtn.getStyleClass().add("button-header");
        closeBtn.setGraphic(new Icon("x-icon", List.of("button-header-icon")));
        closeBtn.setOnAction(e -> {
            if (onCloseAction != null) onCloseAction.run();
        });

        header.getChildren().addAll(titleBox, spacer, closeBtn);

        // --- FORM FIELD ---
        athleteComboBox = new ComboBox<>();
        athleteComboBox.setMaxWidth(Double.MAX_VALUE);
        athleteComboBox.setPromptText("Seleziona atleta...");
        athleteComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ProfileBean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getFirstName() + " " + item.getLastName() + " (" + item.getContactEmail() + ")");
            }
        });
        athleteComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(ProfileBean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getFirstName() + " " + item.getLastName());
            }
        });

        VBox athleteField = new VBox(8);
        Label athleteLabel = new Label("Atleta *");
        athleteLabel.getStyleClass().add("label-field");
        Label athleteHelper = new Label("Seleziona l'atleta dalla tendina");
        athleteHelper.getStyleClass().addAll("body-small", "text-color-light");
        athleteField.getChildren().addAll(athleteLabel, athleteComboBox, athleteHelper);

        // --- FOOTER ACTIONS ---
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(16, 0, 0, 0));
        footer.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 1 0 0 0;");

        Button cancelBtn = new Button("Annulla");
        cancelBtn.getStyleClass().add("button-secondary");
        cancelBtn.setOnAction(e -> {
            if (onCloseAction != null) onCloseAction.run();
        });

        Button assignBtn = new Button("Assegna Piano");
        assignBtn.getStyleClass().add("button-primary");
        assignBtn.setOnAction(e -> {
            if (onAssignAction != null && athleteComboBox.getValue() != null) {
                onAssignAction.accept(athleteComboBox.getValue());
            }
        });

        footer.getChildren().addAll(cancelBtn, assignBtn);

        this.getChildren().addAll(header, athleteField, footer);
    }

    public void setPlan(WorkoutPlanSummaryBean plan, List<ProfileBean> athletes) {
        this.currentPlan = plan;
        if (plan != null) {
            titleLabel.setText("Assegna " + (plan.getPlanTitle() != null ? plan.getPlanTitle() : "Senza Nome"));
        }
        
        athleteComboBox.getItems().clear();
        if (athletes != null) {
            athleteComboBox.getItems().addAll(athletes);
        }
        athleteComboBox.setValue(null);
    }

    public WorkoutPlanSummaryBean getCurrentPlan() {
        return currentPlan;
    }

    public void setOnAssignAction(Consumer<ProfileBean> onAssignAction) {
        this.onAssignAction = onAssignAction;
    }

    public void setOnCloseAction(Runnable onCloseAction) {
        this.onCloseAction = onCloseAction;
    }
}
