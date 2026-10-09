package com.documentconverter.desktop.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
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
            // Carrega o layout FXML do menu principal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainMenuView.fxml"));
            VBox root = loader.load();

            // Configura a cena (largura maior para acomodar o menu)
            Scene scene = new Scene(root, 600, 500);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

            // Configura o estágio (janela principal)
            primaryStage.setTitle("Document Converter");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);

            // Tenta carregar um ícone para a aplicação
            try {
                InputStream iconStream = getClass().getResourceAsStream("/icon.png");
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