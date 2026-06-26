package com.documentconverter.desktop.ui.components;

import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class AppCard extends VBox {

    public AppCard(Node... children) {
        super(10, children);
        getStyleClass().add("app-card");
    }
}
