package com.example.fitplannerclient.ui.gui1.view;

import com.example.fitplannercommon.ExerciseDescriptionBean;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;

import java.util.List;
import java.util.function.Consumer;

public class ExerciseLibraryView extends BaseView {

    private final Button btnAddExercise = new Button("+ Add New Exercise");
    private final VBox exerciseListContainer = new VBox(0); // Contenitore delle righe

    // Callback per gli eventi (simulazione)
    private Consumer<ExerciseDescriptionBean> onEditAction;
    private Consumer<ExerciseDescriptionBean> onDeleteAction;

    public ExerciseLibraryView() {
        // Applichiamo lo stile al bottone principale
        btnAddExercise.getStyleClass().add("button-primary");

        // Costruiamo il layout principale
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(30));
        mainLayout.setAlignment(Pos.TOP_CENTER);

        // 1. Header (Titolo + Bottone)
        HBox header = createHeaderSection();

        // 2. Card (La tabella degli esercizi)
        VBox tableCard = createTableCard();

        mainLayout.getChildren().addAll(header, tableCard);

        // Se BaseView estende StackPane o BorderPane
        this.setContent(mainLayout);
    }

    private HBox createHeaderSection() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        // Titoli
        VBox titles = new VBox(5);
        Label title = new Label("Exercise Library");
        title.getStyleClass().add("h1");

        Label subtitle = new Label("Manage your exercise database with details and video references");
        subtitle.getStyleClass().add("subtitle"); // O "paragraph" con colore light

        titles.getChildren().addAll(title, subtitle);

        // Spacer per spingere il bottone a destra
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(titles, spacer, btnAddExercise);
        return header;
    }

    private VBox createTableCard() {
        VBox card = new VBox();
        card.getStyleClass().add("card"); // Usa il tuo stile .card bianco con ombra
        card.setPadding(new Insets(0));   // Padding gestito internamente per le righe

        // Intestazione Tabella
        HBox tableHeader = new HBox(20);
        tableHeader.setPadding(new Insets(20, 24, 15, 24));

        Label lblName = new Label("Exercise Name");
        lblName.setStyle("-fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        lblName.setMinWidth(300);

        Label lblGroup = new Label("Muscle Group");
        lblGroup.setStyle("-fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        tableHeader.getChildren().addAll(lblName, lblGroup);

        // Lista Scrollabile (nel caso ci siano molti esercizi)
        ScrollPane scrollPane = new ScrollPane(exerciseListContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 0;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setBorder(null);

        card.getChildren().addAll(tableHeader, new Separator(), scrollPane);
        return card;
    }

    /**
     * Metodo chiamato dal Controller per popolare la lista
     */
    public void setExerciseList(List<ExerciseDescriptionBean> exercises) {
        exerciseListContainer.getChildren().clear();

        for (int i = 0; i < exercises.size(); i++) {
            ExerciseDescriptionBean ex = exercises.get(i);
            HBox row = createExerciseRow(ex);

            exerciseListContainer.getChildren().add(row);

            // Aggiunge un divisore tranne che per l'ultimo elemento
            if (i < exercises.size() - 1) {
                Separator sep = new Separator();
                // Margini per non toccare i bordi esatti
                sep.setPadding(new Insets(0, 24, 0, 24));
                exerciseListContainer.getChildren().add(sep);
            }
        }
    }

    private HBox createExerciseRow(ExerciseDescriptionBean exercise) {
        HBox row = new HBox(20);
        row.setPadding(new Insets(20, 24, 20, 24));
        row.setAlignment(Pos.CENTER_LEFT);

        // COLONNA 1: Nome e Descrizione
        VBox infoBox = new VBox(5);
        infoBox.setMinWidth(300);
        infoBox.setPrefWidth(300);

        Label nameLbl = new Label(exercise.getName());
        // Simulo .h3 ma un po' più piccolo o uso .label-field
        nameLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #171717;");

        Label descLbl = new Label(exercise.getDescription());
        descLbl.getStyleClass().add("subtitle");
        descLbl.setWrapText(true);

        infoBox.getChildren().addAll(nameLbl, descLbl);

        // COLONNA 2: Muscle Groups (Tags)
        FlowPane chipsContainer = new FlowPane();
        chipsContainer.setHgap(8);
        chipsContainer.setVgap(8);
        HBox.setHgrow(chipsContainer, Priority.ALWAYS); // Prende lo spazio centrale

        for (String muscle : exercise.getMuscleGroups()) {
            chipsContainer.getChildren().add(createChip(muscle));
        }

        // COLONNA 3: Azioni (Edit / Delete)
        HBox actionsBox = new HBox(15);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);
        actionsBox.setMinWidth(120);

        Button btnEdit = new Button("Edit");
        // Uso button-secondary per lo stile outline/trasparente, oppure uno stile custom clean
        btnEdit.getStyleClass().add("button-header");
        btnEdit.setOnAction(e -> { if(onEditAction != null) onEditAction.accept(exercise); });

        Button btnDelete = new Button("Delete");
        // Uso una classe inline per renderlo rosso solo nel testo, simile all'immagine
        btnDelete.getStyleClass().add("button-header");
        btnDelete.setStyle("-fx-text-fill: -color-accent;");
        btnDelete.setOnAction(e -> { if(onDeleteAction != null) onDeleteAction.accept(exercise); });

        actionsBox.getChildren().addAll(btnEdit, btnDelete);

        row.getChildren().addAll(infoBox, chipsContainer, actionsBox);
        return row;
    }

    private Label createChip(String text) {
        Label chip = new Label(text);
        // Stile "Chip" manuale basato sui colori del tuo CSS
        // Uso il colore di sfondo dei text-field (#F3F4F6) per coerenza
        chip.setStyle(
                "-fx-background-color: #F3F4F6;" +
                        "-fx-text-fill: #2C3E50;" +
                        "-fx-background-radius: 15px;" +
                        "-fx-padding: 4px 12px;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;"
        );
        return chip;
    }

    // Setters per le callback
    public void setOnAddAction(Runnable action) {
        btnAddExercise.setOnAction(e -> action.run());
    }
    public void setOnEditAction(Consumer<ExerciseDescriptionBean> action) { this.onEditAction = action; }
    public void setOnDeleteAction(Consumer<ExerciseDescriptionBean> action) { this.onDeleteAction = action; }
}