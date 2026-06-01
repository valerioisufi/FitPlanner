package com.example.fitplannerclient.ui.fx.view.library;

import com.example.fitplannerclient.bean.exercise.ExerciseDescriptionBean;
import com.example.fitplannerclient.ui.fx.components.FormField;
import com.example.fitplannerclient.ui.fx.components.Icon;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EditExerciseModal extends VBox {

    private final FormField nameField;
    private final FormField descField;
    private final FlowPane activeTagsPane;
    private final TextField customTagInput;

    private final Button cancelBtn;
    private final Button updateBtn;
    private final Button closeBtn;
    private final Label titleLabel;
    private final Label subtitleLabel;

    private String currentExerciseId;
    private List<String> currentTags;

    private Consumer<ExerciseDescriptionBean> onSaveAction;
    private Runnable onCancelAction;
    private Runnable onCloseAction;

    public EditExerciseModal() {
        this.getStyleClass().add("card");
        this.setPadding(new Insets(32));
        this.setSpacing(24);
        this.setMaxWidth(600);
        this.setMaxHeight(Region.USE_PREF_SIZE);
        this.currentTags = new ArrayList<>();

        // --- HEADER ---
        HBox header = new HBox();
        header.setAlignment(Pos.TOP_LEFT);

        VBox titleBox = new VBox(4);
        titleLabel = new Label("Modifica Esercizio");
        titleLabel.getStyleClass().add("heading-h1");
        subtitleLabel = new Label("Aggiorna i dettagli dell'esercizio qui sotto");
        subtitleLabel.getStyleClass().add("body-small");
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        closeBtn = new Button();
        closeBtn.getStyleClass().add("button-header");
        closeBtn.setGraphic(new Icon("x-icon", List.of("button-header-icon")));
        closeBtn.setOnAction(e -> {
            if (onCloseAction != null) onCloseAction.run();
        });

        header.getChildren().addAll(titleBox, spacer, closeBtn);

        // --- FORM FIELDS ---
        TextField nameInput = new TextField();
        nameInput.getStyleClass().add("text-field");
        nameField = new FormField("Nome Esercizio *", "Inserisci il nome", nameInput);

        TextArea descArea = new TextArea();
        descArea.getStyleClass().add("text-area");
        descArea.setPrefRowCount(3);
        descArea.setWrapText(true);
        descField = new FormField("Descrizione", "Descrivi l'esercizio, fornisci suggerimenti sull'esecuzione o sui benefici...", descArea);

        // --- TAGS SECTION ---
        VBox tagsSection = new VBox(12);
        Label tagsLabel = new Label("Gruppi Muscolari (Tag)");
        tagsLabel.getStyleClass().add("label-field");

        // tag selezionati
        activeTagsPane = new FlowPane(8, 8);
        activeTagsPane.setPadding(new Insets(12));
        activeTagsPane.getStyleClass().add("tags-container");

        // selezione rapida
        VBox quickSelectBox = new VBox(8);
        Label qsLabel = new Label("Selezione rapida:");
        qsLabel.getStyleClass().addAll("body-small", "text-color-light");

        FlowPane qsTagsPane = new FlowPane(8, 8);
        String[] defaultTags = {"Pettorali", "Dorsali", "Spalle", "Bicipiti", "Tricipiti", "Avambracci", "Quadricipiti", "Femorali", "Glutei", "Polpacci", "Addominali", "Core"};
        for (String t : defaultTags) {
            Button tagBtn = new Button(t);
            tagBtn.getStyleClass().addAll("badge", "badge-outline");
            tagBtn.setOnAction(e -> addTag(t));
            qsTagsPane.getChildren().add(tagBtn);
        }

        // input per tag custom
        HBox customTagBox = new HBox(12);
        customTagInput = new TextField();
        customTagInput.setPromptText("Aggiungi gruppo muscolare personalizzato...");
        customTagInput.getStyleClass().add("text-field");
        customTagInput.setTextFormatter(new TextFormatter<>(change -> 
            change.getControlNewText().length() <= 50 ? change : null));

        HBox.setHgrow(customTagInput, Priority.ALWAYS);
        Button addTagBtn = new Button("Aggiungi");
        addTagBtn.getStyleClass().add("button-secondary");
        addTagBtn.setMinWidth(Region.USE_PREF_SIZE);
        addTagBtn.setOnAction(e -> {
            String text = customTagInput.getText();
            if (text != null && !text.trim().isEmpty()) {
                addTag(text.trim());
                customTagInput.clear();
            }
        });
        customTagBox.getChildren().addAll(customTagInput, addTagBtn);

        quickSelectBox.getChildren().addAll(qsLabel, qsTagsPane, customTagBox);
        tagsSection.getChildren().addAll(tagsLabel, activeTagsPane, quickSelectBox);

        // --- FOOTER ACTIONS ---
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(16, 0, 0, 0));
        footer.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 1 0 0 0;");

        cancelBtn = new Button("Annulla");
        cancelBtn.getStyleClass().add("button-secondary");
        cancelBtn.setMinWidth(Region.USE_PREF_SIZE);
        cancelBtn.setOnAction(e -> {
            if (onCancelAction != null) onCancelAction.run();
        });

        updateBtn = new Button("Salva Esercizio");
        updateBtn.getStyleClass().add("button-primary");
        updateBtn.setMinWidth(Region.USE_PREF_SIZE);
        updateBtn.setOnAction(e -> {
            if (onSaveAction != null) onSaveAction.accept(getExerciseBean());
        });

        footer.getChildren().addAll(cancelBtn, updateBtn);

        this.getChildren().addAll(header, nameField, descField, tagsSection, footer);
    }

    public void setExercise(ExerciseDescriptionBean bean) {
        if (bean == null) {
            currentExerciseId = null;
            nameField.clear();
            descField.clear();
            currentTags.clear();
            titleLabel.setText("Nuovo Esercizio");
            subtitleLabel.setText("Crea un nuovo esercizio nella libreria");
            updateBtn.setText("Crea Esercizio");
        } else {
            currentExerciseId = bean.getExerciseId();
            nameField.setText(bean.getName() != null ? bean.getName() : "");
            descField.setText(bean.getExecution() != null ? bean.getExecution() : "");
            currentTags.clear();
            if (bean.getMuscleGroups() != null) {
                currentTags.addAll(bean.getMuscleGroups());
            }
            titleLabel.setText("Modifica Esercizio");
            subtitleLabel.setText("Aggiorna i dettagli dell'esercizio");
            updateBtn.setText("Salva Esercizio");
        }
        refreshTagsUI();
    }

    public ExerciseDescriptionBean getExerciseBean() {
        ExerciseDescriptionBean bean = new ExerciseDescriptionBean();
        bean.setExerciseId(currentExerciseId);
        bean.setName(nameField.getText());
        bean.setExecution(descField.getText());
        bean.setMuscleGroups(new ArrayList<>(currentTags));
        return bean;
    }

    public void setOnSaveAction(Consumer<ExerciseDescriptionBean> onSaveAction) {
        this.onSaveAction = onSaveAction;
    }

    public void setOnCancelAction(Runnable onCancelAction) {
        this.onCancelAction = onCancelAction;
    }

    public void setOnCloseAction(Runnable onCloseAction) {
        this.onCloseAction = onCloseAction;
    }

    public FormField getNameField() {
        return nameField;
    }

    public FormField getDescField() {
        return descField;
    }

    private void addTag(String tag) {
        if (!currentTags.contains(tag)) {
            currentTags.add(tag);
            refreshTagsUI();
        }
    }

    private void removeTag(String tag) {
        currentTags.remove(tag);
        refreshTagsUI();
    }

    private void refreshTagsUI() {
        activeTagsPane.getChildren().clear();
        for (String tag : currentTags) {
            activeTagsPane.getChildren().add(createRemovableBadge(tag));
        }
    }

    private HBox createRemovableBadge(String text) {
        HBox badge = new HBox(8);
        badge.setAlignment(Pos.CENTER);
        badge.getStyleClass().addAll("badge", "badge-bg-dark");

        Label label = new Label(text);
        label.getStyleClass().add("badge-label-dark");

        Icon closeIcon = new Icon("x-icon", 10);
        closeIcon.getStyleClass().add("tag-close-icon");

        Button removeTagBtn = new Button();
        removeTagBtn.setGraphic(closeIcon);
        removeTagBtn.getStyleClass().add("button-transparent");
        removeTagBtn.setMinSize(10, 10);
        removeTagBtn.setOnAction(e -> removeTag(text));

        badge.getChildren().addAll(label, removeTagBtn);
        return badge;
    }
}