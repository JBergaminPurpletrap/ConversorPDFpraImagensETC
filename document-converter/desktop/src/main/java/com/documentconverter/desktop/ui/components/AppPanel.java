package com.documentconverter.desktop.ui.components;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class AppPanel extends VBox {

    public AppPanel(Node... children) {
        super(12, children);
        setPadding(new Insets(20));
        getStyleClass().add("app-panel");
    }
}
