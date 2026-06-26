package com.documentconverter.desktop.ui.screens.home;

import java.util.logging.Logger;

import com.documentconverter.desktop.ui.navigation.AppNavigator;
import com.documentconverter.desktop.ui.navigation.AppScreen;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

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
        AppNavigator.navigate(event, AppScreen.IMAGE_TO_PDF);
    }

    /**
     * Navega para a tela de conversão de PDF para imagens.
     */
    @FXML
    private void onPdfToImage(ActionEvent event) {
        LOGGER.info("Navegando para: PDF → Imagem");
        AppNavigator.navigate(event, AppScreen.PDF_TO_IMAGE);
    }

    /**
     * Navega para a tela de conversão de PDF para DOCX.
     */
    @FXML
    private void onPdfToDocx(ActionEvent event) {
        LOGGER.info("Navegando para: PDF → DOCX");
        AppNavigator.navigate(event, AppScreen.PDF_TO_DOCX);
    }

    /**
     * Navega para a tela de conversão de DOCX para PDF.
     */
    @FXML
    private void onDocxToPdf(ActionEvent event) {
        LOGGER.info("Navegando para: DOCX → PDF");
        AppNavigator.navigate(event, AppScreen.DOCX_TO_PDF);
    }

    /**
     * Navega para a tela de histórico de conversões.
     */
    @FXML
    private void onHistory(ActionEvent event) {
        LOGGER.info("Navegando para: Histórico");
        AppNavigator.navigate(event, AppScreen.HISTORY);
    }

    @FXML
    private void initialize() {
        LOGGER.info("Menu principal carregado.");
    }
}
