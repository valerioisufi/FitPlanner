package com.example.fitplannerclient.ui.fx.view.dashboard;

import com.example.fitplannerclient.ui.fx.components.CardListView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import java.util.List;

public class TrainerHomeView extends BorderPane {

    private final VBox contentBox;
    private final Label welcomeTitle;
    private final Label welcomeSubtitle;

    public TrainerHomeView(Node header) {
        if (header != null) {
            this.setTop(header);
        }

        contentBox = new VBox(24);
        contentBox.setPadding(new Insets(32));
        contentBox.setAlignment(Pos.TOP_LEFT);

        // --- Welcome Section ---
        BorderPane welcomeSection = new BorderPane();
        
        VBox titles = new VBox(8);
        welcomeTitle = new Label("Benvenuto in FitPlanner!");
        welcomeTitle.getStyleClass().add("heading-h1");
        welcomeSubtitle = new Label("Oggi è il momento perfetto per superare i tuoi limiti.");
        welcomeSubtitle.getStyleClass().add("body-base");
        titles.getChildren().addAll(welcomeTitle, welcomeSubtitle);

        welcomeSection.setLeft(titles);
        contentBox.getChildren().add(welcomeSection);

        ScrollPane scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        this.setCenter(scrollPane);
    }

    public void setInviteCode(String code) {
        if (code != null) {
            VBox codeBox = new VBox(4);
            codeBox.setAlignment(Pos.CENTER_RIGHT);
            Label lbl = new Label("Il tuo Codice Invito:");
            lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: -fx-color-text-light;");
            Label codeLabel = new Label(code);
            codeLabel.setStyle("-fx-font-family: 'Space Grotesk Bold'; -fx-font-size: 16px; -fx-text-fill: -fx-radix-blue-11; -fx-background-color: -fx-radix-blue-3; -fx-padding: 4px 12px; -fx-background-radius: 6px;");
            codeBox.getChildren().addAll(lbl, codeLabel);

            BorderPane welcomeSection = (BorderPane) contentBox.getChildren().get(0);
            welcomeSection.setRight(codeBox);
        }
    }

    public void setWelcomeMessage(String title, String subtitle) {
        welcomeTitle.setText(title);
        welcomeSubtitle.setText(subtitle);
    }

    public void showTrainerDashboard(Runnable onGoToLibrary, Runnable onGoToPlans) {
        while (contentBox.getChildren().size() > 1) {
            contentBox.getChildren().remove(1);
        }

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);

        // Card 1: Libreria Esercizi
        VBox card1 = new VBox(15);
        card1.getStyleClass().add("card");
        card1.setPrefWidth(300);
        Label title1 = new Label("Libreria Esercizi");
        title1.getStyleClass().add("heading-h2");
        Label desc1 = new Label("Visualizza, crea, modifica e cancella gli esercizi della libreria globale.");
        desc1.getStyleClass().add("body-base");
        desc1.setWrapText(true);
        Button btn1 = new Button("Gestisci Esercizi");
        btn1.getStyleClass().add("button-primary");
        btn1.setOnAction(e -> onGoToLibrary.run());
        card1.getChildren().addAll(title1, desc1, btn1);

        // Card 2: Gestione Piani
        VBox card2 = new VBox(15);
        card2.getStyleClass().add("card");
        card2.setPrefWidth(300);
        Label title2 = new Label("Gestione Piani");
        title2.getStyleClass().add("heading-h2");
        Label desc2 = new Label("Crea nuovi schemi di allenamento settimanali e assegnali ai tuoi atleti.");
        desc2.getStyleClass().add("body-base");
        desc2.setWrapText(true);
        Button btn2 = new Button("Pianifica Allenamenti");
        btn2.getStyleClass().add("button-primary");
        btn2.setOnAction(e -> onGoToPlans.run());
        card2.getChildren().addAll(title2, desc2, btn2);

        grid.add(card1, 0, 0);
        grid.add(card2, 1, 0);

        contentBox.getChildren().add(grid);
    }

    public void showAthleteList(List<com.example.fitplannerclient.bean.profile.ProfileBean> athletes, java.util.function.Consumer<com.example.fitplannerclient.bean.profile.ProfileBean> onAthleteSelected) {
        VBox athletesSection = new VBox(15);
        athletesSection.setPadding(new Insets(20, 0, 0, 0));

        Label sectionTitle = new Label("I Miei Atleti");
        sectionTitle.getStyleClass().add("heading-h2");
        athletesSection.getChildren().add(sectionTitle);

        Label nameHeader = new Label("Nome Atleta");
        nameHeader.setPrefWidth(250);
        Label emailHeader = new Label("Email");

        CardListView<com.example.fitplannerclient.bean.profile.ProfileBean> cardListView = new CardListView<>(List.of(nameHeader, emailHeader));
        
        cardListView.setRowRenderer((athlete, isLast) -> {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add(isLast ? "list-row-last" : "list-row");
            row.setStyle("-fx-cursor: hand;");
            row.setOnMouseClicked(e -> onAthleteSelected.accept(athlete));

            VBox nameBox = new VBox(4);
            nameBox.prefWidthProperty().bind(nameHeader.widthProperty());
            nameBox.minWidthProperty().bind(nameHeader.widthProperty());
            nameBox.maxWidthProperty().bind(nameHeader.widthProperty());

            Label nameLabel = new Label(athlete.getFirstName() + " " + athlete.getLastName());
            nameLabel.getStyleClass().add("body-base");
            nameBox.getChildren().addAll(nameLabel);

            Label emailLabel = new Label(athlete.getContactEmail());
            emailLabel.getStyleClass().addAll("body-small", "text-color-light");
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label viewLabel = new Label("Vedi Dettagli ➔");
            viewLabel.setStyle("-fx-text-fill: -fx-radix-blue-11; -fx-font-family: 'Space Grotesk Medium'; -fx-font-size: 13px;");

            row.getChildren().addAll(nameBox, emailLabel, spacer, viewLabel);
            
            // Add hover effect
            row.setOnMouseEntered(e -> row.setStyle("-fx-cursor: hand; -fx-background-color: -fx-radix-slate-2; -fx-background-radius: 8px;"));
            row.setOnMouseExited(e -> row.setStyle("-fx-cursor: hand; -fx-background-color: transparent; -fx-background-radius: 8px;"));

            return row;
        });

        cardListView.setItems(athletes, "Non hai ancora nessun atleta collegato.");
        athletesSection.getChildren().add(cardListView);

        contentBox.getChildren().add(athletesSection);
    }
}
