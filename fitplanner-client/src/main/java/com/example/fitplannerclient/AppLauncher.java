package com.example.fitplannerclient;

import com.example.fitplannerclient.service.SessionManager;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Objects;

public class AppLauncher extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("FitPlanner");
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/app_icon.png"))));
        stage.setMinWidth(1200);
        stage.setMinHeight(800);

        SessionManager.getInstance().logout();

        Navigator navigator = Navigator.getInstance();
        navigator.setPrimaryStage(stage);
        navigator.startHomeController();
    }

    /* ==========================================
     * 1. MAIN LAYOUT (Lo scheletro dell'App)
     * Corrisponde alla struttura fissa con Sidebar
     * ========================================== */
    public class MainLayout extends BorderPane {

        public MainLayout() {
            // A. Costruzione Sidebar
            VBox sidebar = new VBox(10); // Spacing 10
            sidebar.getStyleClass().add("sidebar");
            sidebar.setPrefWidth(250);
            sidebar.setPadding(new Insets(20));

            // Titolo App
            Label appTitle = new Label("FITNESS PRO");
            appTitle.getStyleClass().add("h2");
            appTitle.setStyle("-fx-text-fill: white;"); // Override locale rapido

            // Bottoni Navigazione
            Button btnDash = createSidebarButton("Dashboard", () -> setContent(new DashboardView()));
            Button btnWork = createSidebarButton("Workout", () -> setContent(new WorkoutView())); // Placeholder
            Button btnLib  = createSidebarButton("Library", () -> setContent(new LibraryView())); // Esempio Scroll

            sidebar.getChildren().addAll(appTitle, new Region() {{ setMinHeight(20); }}, btnDash, btnWork, btnLib);

            // B. Assemblaggio
            this.setLeft(sidebar);
            this.setCenter(new StackPane()); // Placeholder centrale
        }

        // Metodo helper per creare bottoni sidebar coerenti
        private Button createSidebarButton(String text, Runnable action) {
            Button btn = new Button(text);
            btn.getStyleClass().add("sidebar-item");
            btn.setMaxWidth(Double.MAX_VALUE); // Fill width
            btn.setOnAction(e -> action.run());
            return btn;
        }

        // Metodo per cambiare la schermata centrale (Navigazione)
        public void setContent(Node view) {
            this.setCenter(view);
        }
    }

    /* ==========================================
     * 2. VISTA DASHBOARD (Schermata Specifica)
     * Ricostruisce la "Dashboard Atleta.jpg" con Glass Header
     * ========================================== */
    public class DashboardView extends StackPane {

        public DashboardView() {
            // A. Il Contenuto Scrollabile
            VBox content = new VBox(20); // Spacing verticale tra le sezioni
            // IMPORTANTE: Padding superiore di 90px per evitare che il contenuto finisca sotto l'header all'inizio
            content.setPadding(new Insets(90, 30, 30, 30));

            // -- Header del contenuto --
            Label title = new Label("Benvenuto, Atleta");
            title.getStyleClass().add("h1");

            Label subtitle = new Label("Ecco i tuoi progressi di oggi");
            subtitle.getStyleClass().add("subtitle");

            // -- Griglia Statistiche (Cards) --
            FlowPane statsContainer = new FlowPane();
            statsContainer.setHgap(20);
            statsContainer.setVgap(20);

            statsContainer.getChildren().addAll(
                    new StatCard("Calorie Bruciate", "1,250", "kcal", "icon-fire.png"),
                    new StatCard("Workout Completati", "12", "sessioni", "icon-dumbbell.png"),
                    new StatCard("Tempo Totale", "4h 30m", "questa sett.", "icon-clock.png"),
                    new StatCard("Peso Attuale", "75.4", "kg", "icon-scale.png"),
                    // Aggiungo card extra per testare lo scroll
                    new StatCard("Battito Medio", "72", "bpm", "icon-heart.png"),
                    new StatCard("Ore Sonno", "7.5", "ore", "icon-sleep.png")
            );

            // -- Sezione Prossimo Workout --
            Label sectionTitle = new Label("Prossimo Allenamento");
            sectionTitle.getStyleClass().add("h2");

            HBox nextWorkoutCard = new HBox(20);
            nextWorkoutCard.getStyleClass().add("card");
            nextWorkoutCard.setAlignment(Pos.CENTER_LEFT);

            VBox workoutInfo = new VBox(5);
            Label wTitle = new Label("Upper Body Power");
            wTitle.getStyleClass().add("h3");
            Label wDesc = new Label("45 Min • Alta Intensità");
            wDesc.getStyleClass().add("subtitle");
            workoutInfo.getChildren().addAll(wTitle, wDesc);

            Button startBtn = new Button("Inizia Ora");
            startBtn.getStyleClass().add("button-primary");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            nextWorkoutCard.getChildren().addAll(workoutInfo, spacer, startBtn);

            // Assemblaggio contenuto
            content.getChildren().addAll(title, subtitle, statsContainer, sectionTitle, nextWorkoutCard);

            // ScrollPane
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true); // <--- ABILITA SCORRIMENTO ADATTIVO
            scrollPane.getStyleClass().add("bg-main");

            // B. Il "Glass Header" (Barra superiore stile iPhone)
            HBox glassHeader = new HBox();
            glassHeader.setPrefHeight(70); // Altezza fissa
            glassHeader.setMaxHeight(70);
            glassHeader.setAlignment(Pos.CENTER_LEFT);
            glassHeader.setPadding(new Insets(0, 30, 0, 30));

            // Stile Inline per l'effetto trasparenza (RGBA alpha 0.85)
            // Aggiungiamo anche un bordo inferiore sottile e un'ombra leggera
            glassHeader.setStyle(
                    "-fx-background-color: rgba(255, 255, 255, 0.85);" +
                            "-fx-border-color: rgba(200, 200, 200, 0.4);" +
                            "-fx-border-width: 0 0 1 0;" + // Solo bordo sotto
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);"
            );

            // Elementi nell'header (es. Titolo pagina o Utente)
            Label headerTitle = new Label("Dashboard");
            headerTitle.getStyleClass().add("h2");
            headerTitle.setStyle("-fx-font-size: 18px;"); // Un po' più piccolo dell'h1

            Region headerSpacer = new Region();
            HBox.setHgrow(headerSpacer, Priority.ALWAYS);

            Button profileBtn = new Button("Profilo");
            profileBtn.getStyleClass().add("button-secondary");

            glassHeader.getChildren().addAll(headerTitle, headerSpacer, profileBtn);

            // C. Assemblaggio Finale (Layering)
            // Lo scrollPane sta sotto, l'header sta sopra
            this.getChildren().addAll(scrollPane, glassHeader);
            StackPane.setAlignment(glassHeader, Pos.TOP_CENTER);
        }
    }

    /* ==========================================
     * 3. COMPONENTE RIUTILIZZABILE (Widget)
     * Sostituisce la duplicazione di codice FXML
     * ========================================== */
    public class StatCard extends VBox {

        public StatCard(String title, String value, String unit, String iconPath) {
            // Setup Stile Base
            this.getStyleClass().add("card");
            this.setPrefWidth(200);
            this.setSpacing(10);
            this.setAlignment(Pos.CENTER_LEFT);

            // Contenuto
            Label lblTitle = new Label(title);
            lblTitle.getStyleClass().add("subtitle");

            HBox valueContainer = new HBox(5);
            valueContainer.setAlignment(Pos.BASELINE_LEFT);

            Label lblValue = new Label(value);
            lblValue.getStyleClass().add("h2"); // Numero grande

            Label lblUnit = new Label(unit);
            lblUnit.getStyleClass().add("subtitle"); // Unità piccola a fianco

            valueContainer.getChildren().addAll(lblValue, lblUnit);

            // Aggiungi tutto al VBox (this)
            this.getChildren().addAll(lblTitle, valueContainer);

            // Qui potresti aggiungere l'ImageView per l'icona
        }
    }

    // Classi Placeholder per evitare errori di compilazione nell'esempio
    public class WorkoutView extends StackPane {
        public WorkoutView() { getChildren().add(new Label("Workout Interface Here")); }
    }

    // ESEMPIO DI LISTA SCROLLABILE (LIBRARY)
    public class LibraryView extends BorderPane {
        public LibraryView() {
            // 1. Creiamo un contenitore lungo per simulare molti dati
            VBox listContainer = new VBox(15);
            listContainer.setPadding(new Insets(20));

            Label title = new Label("Libreria Esercizi");
            title.getStyleClass().add("h1");
            listContainer.getChildren().add(title);

            // Aggiungiamo 20 elementi finti
            for (int i = 1; i <= 20; i++) {
                HBox item = new HBox(20);
                item.getStyleClass().add("card"); // Riutilizziamo lo stile card
                item.setPadding(new Insets(15));
                item.setAlignment(Pos.CENTER_LEFT);

                Label name = new Label("Esercizio #" + i);
                name.getStyleClass().add("h3");

                Label category = new Label("Categoria " + (i % 3 == 0 ? "Cardio" : "Forza"));
                category.getStyleClass().add("subtitle");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Button action = new Button("Dettagli");
                action.getStyleClass().add("button-secondary");

                item.getChildren().addAll(name, category, spacer, action);
                listContainer.getChildren().add(item);
            }

            // 2. Avvolgiamo il container nel ScrollPane
            ScrollPane scrollPane = new ScrollPane(listContainer);

            // 3. IMPOSTAZIONI CRUCIALI PER LO SCROLL
            scrollPane.setFitToWidth(true);   // Il contenuto si allarga alla finestra
            scrollPane.setFitToHeight(false); // L'altezza è infinita (scrollabile)

            // Rimuoviamo il bordo predefinito dello ScrollPane che a volte è brutto
            scrollPane.setStyle("-fx-background-color:transparent;");

            this.setCenter(scrollPane);
        }
    }
}