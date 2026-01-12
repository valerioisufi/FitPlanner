module FitPlanner {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;

    opens com.example.fitplannerclient to javafx.fxml;
    exports com.example.fitplannerclient;
}