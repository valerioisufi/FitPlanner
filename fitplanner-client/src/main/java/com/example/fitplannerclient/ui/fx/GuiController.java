package com.example.fitplannerclient.ui.fx;

import javafx.scene.layout.Pane;

public interface GuiController {
    /**
     * Restituisce il componente visuale radice di questa vista
     * */
    Pane getView();

    /**
     * Chiamato quando la vista diventa attiva.
     * */
    void start();

    /**
     * Chiamato quando la vista viene sostituita
     * */
    void stop();

}