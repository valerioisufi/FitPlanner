package com.example.fitplannerclient.ui.cli;

public interface CliView {

    CliView execute(CliEngine engine);

    void stop();

}
