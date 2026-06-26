package com.documentconverter.desktop.ui.components;

import javafx.scene.control.TextField;

public final class AppTextField {

    private AppTextField() {
    }

    public static TextField readOnly(String promptText) {
        TextField textField = new TextField();
        textField.setPromptText(promptText);
        textField.setEditable(false);
        return textField;
    }
}
