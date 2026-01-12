package org.example.fitplannerclient.ui.typeA;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class RegistrationView extends VBox {
    public RegistrationView() {
        // 1. CREAZIONE DELLA CARD (Contenitore principale bianco)
        VBox card = new VBox();
        card.getStyleClass().add("card-panel");

        // --- SEZIONE SUPERIORE (Top) ---
        // Progress Bar e Close Button
        ProgressBar progressBar = new ProgressBar(0.33); // 33% pieno
        progressBar.setMaxWidth(Double.MAX_VALUE); // Si allarga quanto possibile
        progressBar.getStyleClass().add("custom-progress-bar");

        Label closeIcon = new Label("✕"); // Carattere Unicode per la X
        closeIcon.getStyleClass().add("close-icon");

        // Usiamo un BorderPane per mettere la barra a sinistra e la X a destra
        BorderPane topHeader = new BorderPane();
        topHeader.setCenter(progressBar);
        topHeader.setRight(closeIcon);
        BorderPane.setMargin(progressBar, new Insets(0, 15, 0, 0)); // Spazio tra barra e X
        BorderPane.setAlignment(closeIcon, Pos.CENTER);

        // Testi Intestazione
        Label stepLabel = new Label("Step 1 of 3");
        stepLabel.getStyleClass().add("label-step");

        Label titleLabel = new Label("User Profile");
        titleLabel.getStyleClass().add("label-title");

        Label subLabel = new Label("Tell us a bit about yourself to personalize your experience");
        subLabel.getStyleClass().add("label-subtitle");
        subLabel.setWrapText(true); // Permette al testo di andare a capo

        // --- SEZIONE CENTRALE (Form) ---
        VBox formContainer = new VBox(15); // 15px spazio verticale tra i campi

        // Creiamo i campi usando un metodo helper (vedi sotto) per non ripetere codice
        VBox ageField = createField("Age", "Enter your age");
        VBox weightField = createField("Weight (kg)", "Enter your weight");
        VBox heightField = createField("Height (cm)", "Enter your height");

        formContainer.getChildren().addAll(ageField, weightField, heightField);

        // --- SEZIONE INFERIORE (Footer) ---
        Separator separator = new Separator();
        separator.setPadding(new Insets(20, 0, 20, 0)); // Spazio sopra e sotto la linea

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT); // Allinea i bottoni a destra (Next)

        Button btnBack = new Button("Back");
        btnBack.getStyleClass().add("button-back");

        // Spacer per spingere i bottoni ai lati opposti
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnNext = new Button("Next");
        btnNext.getStyleClass().add("button-next");

        // Nell'immagine "Back" è a sinistra e "Next" a destra
        buttonBox.getChildren().addAll(btnBack, spacer, btnNext);

        // --- ASSEMBLAGGIO ---
        card.getChildren().addAll(
                topHeader,
                stepLabel,
                titleLabel,
                subLabel,
                formContainer,
                separator,
                buttonBox
        );

        // Setup della Scena
        StackPane root = new StackPane(card); // StackPane centra la card nella finestra
        this.getChildren().add(root);
    }

    // Metodo helper per creare velocemente etichetta + campo di testo
    private VBox createField(String labelText, String placeholder) {
        Label label = new Label(labelText);
        label.getStyleClass().add("label-field");

        TextField textField = new TextField();
        textField.setPromptText(placeholder);
        textField.getStyleClass().add("text-field"); // Applica lo stile CSS

        VBox fieldGroup = new VBox(5); // 5px spazio tra label e input
        fieldGroup.getChildren().addAll(label, textField);
        return fieldGroup;
    }



}
