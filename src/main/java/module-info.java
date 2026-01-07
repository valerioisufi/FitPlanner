module FitPlanner {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.example.fitplanner to javafx.fxml;
    exports org.example.fitplanner;
}