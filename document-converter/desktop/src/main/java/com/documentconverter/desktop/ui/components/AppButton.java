package com.documentconverter.desktop.ui.components;

import javafx.scene.control.Button;

public final class AppButton {

    private AppButton() {
    }

    public static Button primary(String text, Runnable action) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(event -> action.run());
        return button;
    }

    public static Button menu(String text, Runnable action) {
        Button button = primary(text, action);
        button.getStyleClass().add("menu-button");
        return button;
    }
}
