package com.documentconverter.desktop.ui.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class EmptyStatePanel extends VBox {

    public EmptyStatePanel(String title, String message) {
        super(6);
        setAlignment(Pos.CENTER);
        getStyleClass().add("empty-state-panel");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-label");
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("subtitle-label");

        getChildren().addAll(titleLabel, messageLabel);
    }
}
