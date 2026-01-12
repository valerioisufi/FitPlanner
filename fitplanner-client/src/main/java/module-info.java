module FitPlanner {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;

    opens org.example.fitplannerclient to javafx.fxml;
    exports org.example.fitplannerclient;
}