package com.documentconverter.desktop.javafx;

import com.documentconverter.desktop.ui.navigation.AppNavigator;
import com.documentconverter.desktop.ui.navigation.AppScreen;
import com.documentconverter.desktop.ui.theme.AppIcons;
import com.documentconverter.desktop.ui.theme.AppTheme;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe principal do aplicativo Document Converter.
 * <p>
 * Responsável por inicializar a interface JavaFX e carregar o menu principal
 * a partir do arquivo FXML.
 * </p>
 */
public class App extends Application {

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            Scene scene = AppTheme.createScene(AppNavigator.load(AppScreen.HOME));

            primaryStage.setTitle("Document Converter");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);

            try {
                InputStream iconStream = getClass().getResourceAsStream(AppIcons.WINDOW_ICON);
                if (iconStream != null) {
                    primaryStage.getIcons().add(new Image(iconStream));
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Ícone da aplicação não encontrado.", e);
            }

            primaryStage.show();

            LOGGER.info("Aplicação Document Converter iniciada com sucesso.");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao iniciar a aplicação.", e);
            System.exit(1);
        }
    }
}
