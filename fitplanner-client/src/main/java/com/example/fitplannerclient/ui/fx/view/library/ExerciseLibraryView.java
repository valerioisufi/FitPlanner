package com.example.fitplannerclient.ui.fx.view.library;

import com.example.fitplannerclient.bean.exercise.ExerciseDescriptionBean;
import com.example.fitplannerclient.ui.fx.components.Icon;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import com.example.fitplannerclient.ui.fx.components.ModalOverlay;
import com.example.fitplannerclient.ui.fx.components.CardListView;
import javafx.scene.layout.*;

import java.util.List;
import java.util.function.Consumer;

public class ExerciseLibraryView extends StackPane {

    private final BorderPane mainPane;
    private final CardListView<ExerciseDescriptionBean> cardListView;
    private final Label nameHeader;

    private final ModalOverlay modalOverlay;
    private final EditExerciseModal editModal;

    private Runnable onAddAction;
    private Consumer<ExerciseDescriptionBean> onEditAction;
    private Consumer<ExerciseDescriptionBean> onDeleteAction;

    public ExerciseLibraryView() {
        mainPane = new BorderPane();

        VBox contentBox = new VBox();
        contentBox.setPadding(new Insets(32));
        contentBox.setSpacing(24);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label subtitle = new Label("Gestisci il database degli esercizi con descrizioni dettagliate, gruppi muscolari e suggerimenti tecnici");

        subtitle.setWrapText(true);

        subtitle.getStyleClass().add("body-base");
        subtitle.getStyleClass().add("text-color-light");
        titleBox.getChildren().addAll(subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("Nuovo esercizio");
        addBtn.getStyleClass().add("button-primary");

        Icon addIcon = new Icon("plus-icon");
        addIcon.getStyleClass().add("button-primary-icon");
        addBtn.setGraphic(addIcon);

        addBtn.setMinWidth(Region.USE_PREF_SIZE);
        addBtn.setOnAction(e -> {
            if (onAddAction != null) onAddAction.run();
        });

        header.getChildren().addAll(titleBox, spacer, addBtn);

        // --- LIST CONTAINER ---
        nameHeader = new Label("Nome Esercizio");
        nameHeader.setPrefWidth(400);
        nameHeader.setMinWidth(150);

        Label muscleHeader = new Label("Gruppi Muscolari");

        cardListView = new CardListView<>(List.of(nameHeader, muscleHeader));
        cardListView.setRowRenderer(this::createExerciseRow);

        contentBox.getChildren().addAll(header, cardListView);

        ScrollPane mainScroll = new ScrollPane(contentBox);
        mainScroll.setFitToWidth(true);
        mainPane.setCenter(mainScroll);

        // --- MODAL OVERLAY ---
        editModal = new EditExerciseModal();
        modalOverlay = new ModalOverlay(editModal);

        this.getChildren().addAll(mainPane, modalOverlay);
    }

    public void setHeaderView(Node headerView) {
        mainPane.setTop(headerView);
    }

    public EditExerciseModal getEditModal() {
        return editModal;
    }

    public void showModal(ExerciseDescriptionBean exerciseToEdit) {
        editModal.setExercise(exerciseToEdit);
        modalOverlay.show();
    }

    public void hideModal() {
        modalOverlay.hide();
    }

    public void setExerciseList(List<ExerciseDescriptionBean> exercises) {
        cardListView.setItems(exercises, "Nessun esercizio presente nella libreria.");
    }

    private HBox createExerciseRow(ExerciseDescriptionBean exercise, boolean isLast) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add(isLast ? "list-row-last" : "list-row");

        // info column
        VBox infoBox = new VBox(4);
        infoBox.prefWidthProperty().bind(nameHeader.widthProperty());
        infoBox.minWidthProperty().bind(nameHeader.widthProperty());
        infoBox.maxWidthProperty().bind(nameHeader.widthProperty());
        
        Label nameLbl = new Label(exercise.getName() != null ? exercise.getName() : "");
        nameLbl.getStyleClass().add("body-base");
        
        Label descLbl = new Label(exercise.getExecution() != null ? exercise.getExecution() : "");
        descLbl.getStyleClass().addAll("body-small");
        descLbl.setWrapText(true);
        descLbl.setMaxWidth(380);
        descLbl.setMaxHeight(38);

        infoBox.getChildren().addAll(nameLbl, descLbl);

        // tags column
        FlowPane tagsPane = new FlowPane(8, 8);
        tagsPane.setAlignment(Pos.CENTER_LEFT);
        if (exercise.getMuscleGroups() != null) {
            for (String tag : exercise.getMuscleGroups()) {
                tagsPane.getChildren().add(createBadge(tag, "badge-bg-gray", "badge-label-value-gray"));
            }
        }
        HBox.setHgrow(tagsPane, Priority.ALWAYS);

        // actions column
        HBox actionsBox = new HBox(16);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);

        Button editBtn = new Button("Modifica");
        editBtn.getStyleClass().add("button-header");
        Icon editIcon = new Icon("edit-icon");
        editIcon.getStyleClass().add("button-header-icon");

        editBtn.setGraphic(editIcon);
        editBtn.setMinWidth(Region.USE_PREF_SIZE);
        editBtn.setOnAction(e -> {
            if (onEditAction != null) onEditAction.accept(exercise);
        });

        Button deleteBtn = new Button("Elimina");
        deleteBtn.getStyleClass().addAll("button-header", "button-header-danger");
        Icon deleteIcon = new Icon("delete-icon");
        deleteIcon.getStyleClass().add("button-header-danger-icon");
        deleteBtn.setGraphic(deleteIcon);
        deleteBtn.setMinWidth(Region.USE_PREF_SIZE);
        deleteBtn.setOnAction(e -> {
            if (onDeleteAction != null) onDeleteAction.accept(exercise);
        });

        actionsBox.getChildren().addAll(editBtn, deleteBtn);

        row.getChildren().addAll(infoBox, tagsPane, actionsBox);
        return row;
    }

    private HBox createBadge(String text, String bgClass, String textClass) {
        HBox badge = new HBox();
        badge.getStyleClass().addAll("badge", bgClass);

        Label label = new Label(text);
        label.getStyleClass().add(textClass);

        badge.getChildren().add(label);
        return badge;
    }

    public void setOnAddAction(Runnable onAddAction) {
        this.onAddAction = onAddAction;
    }
    public void setOnEditAction(Consumer<ExerciseDescriptionBean> onEditAction) {
        this.onEditAction = onEditAction;
    }
    public void setOnDeleteAction(Consumer<ExerciseDescriptionBean> onDeleteAction) {
        this.onDeleteAction = onDeleteAction;
    }

}