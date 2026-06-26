package com.documentconverter.desktop.ui.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;

public class LoadingPanel extends VBox {

    public LoadingPanel(String message) {
        super(10);
        setAlignment(Pos.CENTER);
        getStyleClass().add("loading-panel");
        getChildren().addAll(new ProgressIndicator(), new Label(message));
    }
}
