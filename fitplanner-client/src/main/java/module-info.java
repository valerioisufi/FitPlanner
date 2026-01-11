module FitPlanner {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.example.fitplannerclient to javafx.fxml;
    exports org.example.fitplannerclient;
}