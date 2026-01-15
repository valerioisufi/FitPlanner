module fitplannerclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires java.net.http;
    requires tools.jackson.databind;
    requires fitplannercommon;
    requires java.prefs;

    opens com.example.fitplannerclient to javafx.fxml;
    exports com.example.fitplannerclient;
}