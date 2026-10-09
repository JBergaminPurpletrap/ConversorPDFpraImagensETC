package com.documentconverter.desktop.javafx.controller;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * Controlador da tela de menu principal.
 * <p>
 * Exibe duas opções: "Imagem → PDF" e "PDF → Imagem".
 * Cada opção navega para sua respectiva tela de conversão.
 * </p>
 */
public class MainMenuController {

    private static final Logger LOGGER = Logger.getLogger(MainMenuController.class.getName());

    @FXML
    private Button btnImageToPdf;

    @FXML
    private Button btnPdfToImage;

    @FXML
    private Button btnPdfToDocx;

    @FXML
    private Button btnDocxToPdf;

    @FXML
    private Button btnHistory;

    /**
     * Navega para a tela de conversão de imagens para PDF.
     */
    @FXML
    private void onImageToPdf(ActionEvent event) {
        LOGGER.info("Navegando para: Imagem → PDF");
        loadScreen(event, "/fxml/ImageToPdfView.fxml");
    }

    /**
     * Navega para a tela de conversão de PDF para imagens.
     */
    @FXML
    private void onPdfToImage(ActionEvent event) {
        LOGGER.info("Navegando para: PDF → Imagem");
        loadScreen(event, "/fxml/PdfToImageView.fxml");
    }

    /**
     * Navega para a tela de conversão de PDF para DOCX.
     */
    @FXML
    private void onPdfToDocx(ActionEvent event) {
        LOGGER.info("Navegando para: PDF → DOCX");
        loadScreen(event, "/fxml/PdfToDocxView.fxml");
    }

    /**
     * Navega para a tela de conversão de DOCX para PDF.
     */
    @FXML
    private void onDocxToPdf(ActionEvent event) {
        LOGGER.info("Navegando para: DOCX → PDF");
        loadScreen(event, "/fxml/DocxToPdfView.fxml");
    }

    /**
     * Navega para a tela de histórico de conversões.
     */
    @FXML
    private void onHistory(ActionEvent event) {
        LOGGER.info("Navegando para: Histórico");
        loadScreen(event, "/fxml/HistoryView.fxml");
    }

    /**
     * Carrega uma nova tela no mesmo Stage.
     *
     * @param event evento disparado pelo botão do menu
     * @param fxmlPath caminho do arquivo FXML
     */
    private void loadScreen(ActionEvent event, String fxmlPath) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new IllegalStateException("FXML não encontrado: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 600, 500);
            URL stylesUrl = getClass().getResource("/styles.css");
            if (stylesUrl != null) {
                scene.getStylesheets().add(stylesUrl.toExternalForm());
            }
            stage.setScene(scene);
            stage.centerOnScreen();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar tela: " + fxmlPath, e);
            showLoadError(fxmlPath, e);
        }
    }

    private void showLoadError(String fxmlPath, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro ao abrir tela");
        alert.setHeaderText("Não foi possível abrir a tela selecionada.");
        alert.setContentText("Tela: " + fxmlPath + "\nErro: " + describeError(e));
        alert.showAndWait();
    }

    private String describeError(Throwable error) {
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

    @FXML
    private void initialize() {
        LOGGER.info("Menu principal carregado.");
    }
}
