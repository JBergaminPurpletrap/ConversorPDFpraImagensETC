package com.documentconverter.desktop.ui.navigation;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.documentconverter.desktop.ui.theme.AppTheme;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public final class AppNavigator {

    private static final Logger LOGGER = Logger.getLogger(AppNavigator.class.getName());

    private AppNavigator() {
    }

    public static void navigate(ActionEvent event, AppScreen screen) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        navigate(stage, screen);
    }

    public static void navigate(Stage stage, AppScreen screen) {
        try {
            Parent root = load(screen);
            Scene scene = AppTheme.createScene(root);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar tela: " + screen.fxmlPath(), e);
            showLoadError(screen, e);
        }
    }

    public static Parent load(AppScreen screen) throws java.io.IOException {
        URL fxmlUrl = AppNavigator.class.getResource(screen.fxmlPath());
        if (fxmlUrl == null) {
            throw new IllegalStateException("FXML não encontrado: " + screen.fxmlPath());
        }
        return new FXMLLoader(fxmlUrl).load();
    }

    private static void showLoadError(AppScreen screen, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro ao abrir tela");
        alert.setHeaderText("Não foi possível abrir a tela selecionada.");
        alert.setContentText("Tela: " + screen.fxmlPath() + "\nErro: " + describeError(e));
        alert.showAndWait();
    }

    private static String describeError(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) {
            current = current.getCause();
        }

        String message = current.getMessage();
        if (message == null || message.isBlank()) {
            message = error.getMessage();
        }
        if (message == null || message.isBlank()) {
            message = current.getClass().getSimpleName();
        }

        return current.getClass().getSimpleName() + ": " + message;
    }
}
