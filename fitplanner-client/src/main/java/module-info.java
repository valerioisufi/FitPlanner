module fitplannerclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires java.net.http;
    requires tools.jackson.databind;
    requires fitplannercommon;
    requires java.prefs;
    requires javafx.web;
    requires jdk.httpserver;

    opens com.example.fitplannerclient to javafx.fxml;
    opens com.example.fitplannerclient.serializer to tools.jackson.databind;
    exports com.example.fitplannerclient;
}