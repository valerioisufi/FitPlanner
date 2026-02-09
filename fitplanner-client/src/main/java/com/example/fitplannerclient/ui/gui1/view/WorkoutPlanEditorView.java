package com.example.fitplannerclient.ui.gui1.view;

import com.example.fitplannercommon.ExerciseBean;
import com.example.fitplannercommon.ExerciseDescriptionBean;
import com.example.fitplannercommon.WorkoutSessionBean;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.function.BiConsumer;

public class WorkoutPlanEditorView extends BaseView {

    // Callback: Quando un esercizio viene droppato su un giorno
    // Arg 1: Il nome dell'esercizio droppato, Arg 2: L'indice del giorno (Day X)
    private BiConsumer<String, Integer> onExerciseDropped;

    private final FlowPane planGrid = new FlowPane();
    private final VBox libraryContainer = new VBox(10);
    private final TextField searchField = new TextField();

    // Helper per renderizzare il nome dell'esercizio partendo dal Bean
    private ExerciseNameResolver nameResolver;

    public interface ExerciseNameResolver {
        String resolveName(int exerciseType);
    }

    public WorkoutPlanEditorView() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        // --- SINISTRA: PIANO DI ALLENAMENTO ---
        VBox planArea = createPlanArea();

        // --- DESTRA: LIBRERIA ESERCIZI ---
        VBox libraryArea = createLibraryArea();

        // Split o BorderPane layout
        root.setCenter(planArea);
        root.setRight(libraryArea);
        BorderPane.setMargin(libraryArea, new Insets(0, 0, 0, 20)); // Spazio tra le due aree

        this.setContent(root);
    }

    // =================================================================================
    //  SEZIONE PIANO (SINISTRA)
    // =================================================================================

    private VBox createPlanArea() {
        VBox container = new VBox(20);

        // Header
        VBox header = new VBox(5);
        Label title = new Label("Training Schedule");
        title.getStyleClass().add("h2");
        Label subtitle = new Label("Drag exercises from the library to the days.");
        subtitle.getStyleClass().add("subtitle");
        header.getChildren().addAll(title, subtitle);

        // Grid Scrollabile
        planGrid.setHgap(15);
        planGrid.setVgap(15);
        planGrid.setPadding(new Insets(5));

        // Impostiamo il FlowPane per ridimensionarsi con la finestra
        planGrid.prefWidthProperty().bind(container.widthProperty().subtract(20));

        ScrollPane scrollPane = new ScrollPane(planGrid);
        scrollPane.setFitToWidth(true); // Fondamentale per far funzionare il FlowPane
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        container.getChildren().addAll(header, scrollPane);
        return container;
    }

    /**
     * Ridisegna completamente la griglia dei giorni in base ai dati
     */
    public void renderPlan(List<WorkoutSessionBean> sessions) {
        planGrid.getChildren().clear();

        for (WorkoutSessionBean session : sessions) {
            VBox dayCard = createDayCard(session);
            planGrid.getChildren().add(dayCard);
        }

        // Aggiungi un pulsante "+" alla fine per aggiungere nuovi giorni (opzionale)
        Button addDayBtn = new Button("+ Add Day");
        addDayBtn.getStyleClass().add("button-secondary");
        addDayBtn.setPrefSize(200, 50);
        // planGrid.getChildren().add(addDayBtn); // Logica da implementare
    }

    private VBox createDayCard(WorkoutSessionBean session) {
        VBox card = new VBox();
        card.getStyleClass().add("day-card");
        card.setPrefWidth(220);
        card.setMinHeight(300); // Altezza minima per facilitare il drop

        // Header Giorno
        HBox header = new HBox();
        header.getStyleClass().add("day-card-header");
        header.setAlignment(Pos.CENTER_LEFT);

        Label dayLabel = new Label("Day " + session.getDay());
        dayLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        header.getChildren().add(dayLabel);

        // Lista Esercizi (Area di contenuto)
        VBox exerciseList = new VBox(8);
        exerciseList.getStyleClass().add("day-exercise-list");
        VBox.setVgrow(exerciseList, Priority.ALWAYS);

        if (session.getExercises() != null) {
            for (ExerciseBean ex : session.getExercises()) {
                exerciseList.getChildren().add(createPlannedExerciseItem(ex));
            }
        } else {
            Label emptyLbl = new Label("Drop exercise here");
            emptyLbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic; -fx-padding: 20 0 0 0;");
            emptyLbl.setMaxWidth(Double.MAX_VALUE);
            emptyLbl.setAlignment(Pos.CENTER);
            exerciseList.getChildren().add(emptyLbl);
        }

        card.getChildren().addAll(header, exerciseList);

        // --- DRAG AND DROP EVENTS (TARGET) ---

        // 1. Drag Over: Accetta se c'è una stringa (il nome dell'esercizio)
        card.setOnDragOver(event -> {
            if (event.getGestureSource() != card && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        // 2. Drag Entered: Feedback visivo (bordo blu)
        card.setOnDragEntered(event -> {
            if (event.getGestureSource() != card && event.getDragboard().hasString()) {
                card.getStyleClass().add("day-card-drag-hover");
            }
            event.consume();
        });

        // 3. Drag Exited: Rimuovi feedback
        card.setOnDragExited(event -> {
            card.getStyleClass().remove("day-card-drag-hover");
            event.consume();
        });

        // 4. Drag Dropped: Gestisci il dato
        card.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                String exerciseName = db.getString();
                if (onExerciseDropped != null) {
                    onExerciseDropped.accept(exerciseName, session.getDay());
                }
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });

        return card;
    }

    private HBox createPlannedExerciseItem(ExerciseBean ex) {
        HBox item = new HBox();
        item.getStyleClass().add("planned-exercise-item");

        // Risolviamo il nome usando l'ID (type)
        String resolvedName = (nameResolver != null) ? nameResolver.resolveName(ex.getType()) : "Exercise #" + ex.getType();

        Label name = new Label(resolvedName);
        name.setStyle("-fx-font-weight: bold;");

        Label details = new Label(ex.getSet() + " x " + ex.getRep());
        details.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

        VBox info = new VBox(2, name, details);
        item.getChildren().add(info);
        return item;
    }

    // =================================================================================
    //  SEZIONE LIBRERIA (DESTRA)
    // =================================================================================

    private VBox createLibraryArea() {
        VBox container = new VBox(15);
        container.setPrefWidth(320);
        container.setMinWidth(300);
        container.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: -border-color; -fx-border-width: 0 0 0 1;");
        container.setPadding(new Insets(0, 0, 0, 20));

        // Header
        Label lblLib = new Label("Exercise Library");
        lblLib.getStyleClass().add("h3");
        lblLib.setPadding(new Insets(10, 0, 0, 0));

        // Search
        searchField.setPromptText("Search exercises...");

        // Chips
        FlowPane chips = new FlowPane(8, 8);
        chips.getChildren().addAll(
                createChip("All", true), createChip("Legs", false),
                createChip("Chest", false), createChip("Back", false)
        );

        // Lista Scrollabile
        ScrollPane scrollPane = new ScrollPane(libraryContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setBorder(null);

        container.getChildren().addAll(lblLib, searchField, chips, scrollPane);
        return container;
    }

    private Label createChip(String text, boolean active) {
        Label chip = new Label(text);
        chip.getStyleClass().add("filter-chip");
        if (active) chip.getStyleClass().add("filter-chip-active");
        return chip;
    }

    public void populateLibrary(List<ExerciseDescriptionBean> exercises) {
        libraryContainer.getChildren().clear();
        for (ExerciseDescriptionBean ex : exercises) {
            libraryContainer.getChildren().add(createDraggableLibraryItem(ex));
        }
    }

    private VBox createDraggableLibraryItem(ExerciseDescriptionBean ex) {
        VBox item = new VBox(5);
        item.getStyleClass().add("library-item");

        Label name = new Label(ex.getName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        HBox tags = new HBox(5);
        for(String muscle : ex.getMuscleGroups()) {
            Label tag = new Label(muscle);
            tag.setStyle("-fx-background-color: #F1F5F9; -fx-padding: 2 6; -fx-background-radius: 4; -fx-font-size: 10px;");
            tags.getChildren().add(tag);
        }

        item.getChildren().addAll(name, tags);

        // --- DRAG AND DROP EVENTS (SOURCE) ---
        item.setOnDragDetected(event -> {
            // Inizia il drag
            Dragboard db = item.startDragAndDrop(TransferMode.COPY);

            // Mettiamo il nome dell'esercizio nella clipboard
            ClipboardContent content = new ClipboardContent();
            content.putString(ex.getName());
            db.setContent(content);

            // Opzionale: Imposta un'immagine snapshot dell'item come cursore
            // db.setDragView(item.snapshot(null, null));

            event.consume();
        });

        return item;
    }

    public void setOnExerciseDropped(BiConsumer<String, Integer> onExerciseDropped) {
        this.onExerciseDropped = onExerciseDropped;
    }

    public void setNameResolver(ExerciseNameResolver nameResolver) {
        this.nameResolver = nameResolver;
    }
}