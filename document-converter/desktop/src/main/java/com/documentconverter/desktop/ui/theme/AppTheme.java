package com.documentconverter.desktop.ui.theme;

import java.net.URL;

import javafx.scene.Parent;
import javafx.scene.Scene;

public final class AppTheme {

    public static final String STYLESHEET = "/styles.css";

    private AppTheme() {
    }

    public static Scene createScene(Parent root) {
        Scene scene = new Scene(root, AppSpacing.WINDOW_WIDTH, AppSpacing.WINDOW_HEIGHT);
        apply(scene);
        return scene;
    }

    public static void apply(Scene scene) {
        URL stylesUrl = AppTheme.class.getResource(STYLESHEET);
        if (stylesUrl != null) {
            scene.getStylesheets().add(stylesUrl.toExternalForm());
        }
    }
}
