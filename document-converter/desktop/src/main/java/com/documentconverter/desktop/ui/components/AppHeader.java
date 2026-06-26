package com.documentconverter.desktop.ui.components;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class AppHeader extends HBox {

    public AppHeader(String title, Runnable onBack) {
        super(10);
        setAlignment(Pos.CENTER_LEFT);

        Button backButton = new Button("← Voltar");
        backButton.setOnAction(event -> onBack.run());

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("title-label");

        getChildren().addAll(backButton, titleLabel);
    }
}
