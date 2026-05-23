package com.example.fitplannerclient.ui.fx.view;

import com.example.fitplannerclient.ui.fx.components.FormField;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class ProfileView extends VBox {

    // --- Inputs ---
    private final TextField firstNameInput = new TextField();
    private final TextField lastNameInput = new TextField();
    private final TextField contactEmailInput = new TextField();
    private final TextField phoneInput = new TextField();

    // --- Fields ---
    private final FormField firstNameField = new FormField("Nome", "Inserisci il tuo nome", firstNameInput);
    private final FormField lastNameField = new FormField("Cognome", "Inserisci il tuo cognome", lastNameInput);
    private final FormField contactEmailField = new FormField("Email di contatto", "Inserisci la tua email", contactEmailInput);
    private final FormField phoneField = new FormField("Numero di telefono", "Inserisci il tuo telefono", phoneInput);

    // --- Labels & Buttons ---
    private final Label roleValueLabel = new Label();
    private final Button btnEdit = new Button("Modifica");
    private final Button btnSave = new Button("Salva");

    public ProfileView() {
        setSpacing(25);
        setPadding(new Insets(30));
        setAlignment(Pos.TOP_CENTER);

        VBox card = new VBox(25);
        card.getStyleClass().add("card");
        card.setMaxWidth(800);
        card.setAlignment(Pos.TOP_LEFT);

        // --- Header (Title + Role Box) ---
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Informazioni profilo");
        title.getStyleClass().add("heading-h2");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox roleBox = new HBox(10);
        roleBox.setAlignment(Pos.CENTER_RIGHT); // This ensures vertical centering of items
        Label roleLabel = new Label("Tipo di profilo:");
        roleLabel.getStyleClass().add("label-field");
        
        // Remove bottom padding/margin if any from label-field to align perfectly
        roleLabel.setStyle("-fx-padding: 0;");
        
        roleValueLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 0;");
        roleBox.getChildren().addAll(roleLabel, roleValueLabel);

        headerBox.getChildren().addAll(title, spacer, roleBox);

        // --- Horizontal Field Pairs ---
        HBox row1 = createFieldRow(firstNameField, lastNameField);
        HBox row2 = createFieldRow(contactEmailField, phoneField);

        // --- Actions Box ---
        HBox buttonBox = createButtonBox();

        // Add everything to card layout
        card.getChildren().addAll(headerBox, row1, row2, buttonBox);
        
        // Add card to main layout
        this.getChildren().add(card);

        // Initialize state
        setFieldsEditable(false);
        btnSave.setDisable(true);

        // Built-in edit behavior
        btnEdit.setOnAction(e -> {
            setFieldsEditable(true);
            btnEdit.setDisable(true);
            btnSave.setDisable(false);
        });
    }

    private HBox createFieldRow(FormField field1, FormField field2) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);

        // Make fields grow equally to fill the horizontal space
        HBox.setHgrow(field1, Priority.ALWAYS);
        HBox.setHgrow(field2, Priority.ALWAYS);
        field1.setMaxWidth(Double.MAX_VALUE);
        field2.setMaxWidth(Double.MAX_VALUE);

        row.getChildren().addAll(field1, field2);
        return row;
    }

    private HBox createButtonBox() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        btnEdit.getStyleClass().add("button-secondary");
        btnSave.getStyleClass().add("button-primary");

        buttonBox.getChildren().addAll(btnEdit, btnSave);
        return buttonBox;
    }

    private void setFieldsEditable(boolean editable) {
        firstNameInput.setEditable(editable);
        lastNameInput.setEditable(editable);
        contactEmailInput.setEditable(editable);
        phoneInput.setEditable(editable);
    }

    // --- Public API for Controllers ---

    /**
     * Resets the view back to read-only mode after saving.
     */
    public void finishEditing() {
        setFieldsEditable(false);
        btnEdit.setDisable(false);
        btnSave.setDisable(true);
    }

    public void setSaveAction(Runnable action) {
        btnSave.setOnAction(e -> action.run());
    }

    public void setRoleIndicator(String role) {
        roleValueLabel.setText(role);
    }

    public void setProfileData(String firstName, String lastName, String email, String phone) {
        firstNameInput.setText(firstName);
        lastNameInput.setText(lastName);
        contactEmailInput.setText(email);
        phoneInput.setText(phone);
    }

    // --- Getters ---
    public String getFirstName() { return firstNameInput.getText(); }
    public String getLastName() { return lastNameInput.getText(); }
    public String getContactEmail() { return contactEmailInput.getText(); }
    public String getPhoneNumber() { return phoneInput.getText(); }

    public FormField getFirstNameField() { return firstNameField; }
    public FormField getLastNameField() { return lastNameField; }
    public FormField getContactEmailField() { return contactEmailField; }
    public FormField getPhoneField() { return phoneField; }
}