package com.example.fitplannerclient.ui.gui1.view;

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

public abstract class BaseView extends BorderPane{

    protected BaseView() {
    }

    public void setHeader(Node header) {
        this.setTop(header);
    }

    public void setContent(Node content) {
        this.setCenter(content);
    }


}
