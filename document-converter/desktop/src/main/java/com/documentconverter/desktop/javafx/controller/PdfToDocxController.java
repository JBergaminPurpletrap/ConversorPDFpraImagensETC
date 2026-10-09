package com.documentconverter.desktop.javafx.controller;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.documentconverter.core.models.ConversionHistoryService;
import com.documentconverter.core.models.PdfToDocxResult;
import com.documentconverter.core.services.ConversionService;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Controlador da tela de conversão de PDF para DOCX.
 */
public class PdfToDocxController {

    private static final Logger LOGGER = Logger.getLogger(PdfToDocxController.class.getName());

    @FXML private Button btnSelectPdf;
    @FXML private TextField txtPdfFile;
    @FXML private VBox dropZone;
    @FXML private Button btnConvert;
    @FXML private Button btnCancel;
    @FXML private ProgressBar progressBar;
    @FXML private Label lblPercent;
    @FXML private Label lblStatus;

    private final ConversionService conversionService = new ConversionService();
    private final ConversionHistoryService historyService = new ConversionHistoryService();
    private File selectedPdf;
    private File currentOutputFile;
    private Task<?> currentTask;

    @FXML
    private void initialize() {
        btnConvert.setDisable(true);
        progressBar.setProgress(0.0);
        lblStatus.setText("Selecione um arquivo PDF para começar.");
        setupDragAndDrop();
        LOGGER.info("Tela 'PDF → DOCX' carregada.");
    }

    @FXML
    private void onBack(ActionEvent event) {
        navigateToMainMenu();
    }

    @FXML
    private void onSelectPdf(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar PDF");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Arquivos PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("Todos os Arquivos", "*.*"));

        Stage stage = (Stage) btnSelectPdf.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            loadPdf(file);
        }
    }

    @FXML
    private void onCancel(ActionEvent event) {
        if (currentTask != null && currentTask.isRunning()) {
            currentTask.cancel(true);
            lblStatus.setText("Conversão cancelada pelo usuário.");
            registerHistory(ConversionHistoryService.STATUS_CANCELED);
            setControlsDisabled(false);
            btnCancel.setVisible(false);
            btnCancel.setManaged(false);
        }
    }

    @FXML
    private void onConvert(ActionEvent event) {
        if (selectedPdf == null) {
            return;
        }

        Stage stage = (Stage) btnConvert.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar DOCX como");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documento Word", "*.docx"));
        fileChooser.setInitialFileName(replaceExtension(selectedPdf.getName(), ".docx"));

        File chosen = fileChooser.showSaveDialog(stage);
        if (chosen == null) {
            lblStatus.setText("Conversão cancelada.");
            return;
        }

        File outputFile = chosen.getName().toLowerCase().endsWith(".docx")
                ? chosen
                : new File(chosen.getAbsolutePath() + ".docx");
        currentOutputFile = outputFile;

        setControlsDisabled(true);
        btnCancel.setVisible(true);
        btnCancel.setManaged(true);

        LOGGER.log(Level.INFO, "PDF → DOCX solicitado | origem: {0} | destino: {1}",
                new Object[]{selectedPdf.getAbsolutePath(), outputFile.getAbsolutePath()});

        currentTask = new Task<PdfToDocxResult>() {
            @Override
            protected PdfToDocxResult call() throws Exception {
                updateProgress(-1, 1);
                PdfToDocxResult result = conversionService.convertPdfToDocx(selectedPdf, outputFile);
                updateProgress(1, 1);
                return result;
            }
        };
        progressBar.progressProperty().bind(currentTask.progressProperty());

        currentTask.setOnSucceeded(e -> {
            progressBar.progressProperty().unbind();
            setControlsDisabled(false);
            btnCancel.setVisible(false);
            btnCancel.setManaged(false);
            progressBar.setProgress(1.0);
            lblPercent.setText("100%");

            PdfToDocxResult result = (PdfToDocxResult) currentTask.getValue();
            lblStatus.setText("Conversão concluída!\nPáginas: " + result.pageCount()
                    + " | caracteres: " + result.textCharacters()
                    + " | imagens: " + result.imageCount()
                    + "\nArquivo salvo em: " + outputFile.getAbsolutePath());
            registerHistory(ConversionHistoryService.STATUS_SUCCESS);
        });

        currentTask.setOnFailed(e -> {
            progressBar.progressProperty().unbind();
            setControlsDisabled(false);
            btnCancel.setVisible(false);
            btnCancel.setManaged(false);
            progressBar.setProgress(0.0);
            lblPercent.setText("");
            registerHistory(ConversionHistoryService.STATUS_FAILED);
            handleError(currentTask.getException());
        });

        Thread thread = new Thread(currentTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void setupDragAndDrop() {
        dropZone.setOnDragOver(event -> {
            Dragboard dragboard = event.getDragboard();
            if (dragboard.hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        dropZone.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean success = false;
            if (dragboard.hasFiles()) {
                success = loadPdf(dragboard.getFiles());
            }
            event.setDropCompleted(success);
            event.consume();
        });

        dropZone.setOnMouseClicked(event -> onSelectPdf(null));
    }

    private boolean loadPdf(List<File> files) {
        if (files.size() > 1) {
            lblStatus.setText("Solte apenas um arquivo PDF por vez.");
            return false;
        }
        return loadPdf(files.get(0));
    }

    private boolean loadPdf(File file) {
        if (!isPdfFile(file)) {
            lblStatus.setText("Arquivo inválido. Use somente PDF.");
            return false;
        }

        selectedPdf = file;
        txtPdfFile.setText(file.getAbsolutePath());
        dropZone.setVisible(false);
        dropZone.setManaged(false);
        btnConvert.setDisable(false);
        lblStatus.setText("PDF carregado: " + file.getName());
        LOGGER.log(Level.INFO, "PDF carregado para DOCX: {0}", file.getAbsolutePath());
        return true;
    }

    private boolean isPdfFile(File file) {
        return file.getName().toLowerCase().endsWith(".pdf");
    }

    private void setControlsDisabled(boolean disabled) {
        btnSelectPdf.setDisable(disabled);
        btnConvert.setDisable(disabled);
        if (disabled) {
            progressBar.progressProperty().unbind();
            progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            lblPercent.setText("");
            lblStatus.setText("Extraindo texto e imagens... Aguarde.");
        }
    }

    private void registerHistory(String status) {
        if (selectedPdf == null || currentOutputFile == null) {
            return;
        }
        historyService.register(
                ConversionHistoryService.OPERATION_PDF_TO_DOCX,
                selectedPdf.getName(),
                currentOutputFile.getAbsolutePath(),
                status);
    }

    private void handleError(Throwable throwable) {
        String message = throwable != null ? throwable.getMessage() : "Erro desconhecido";
        Throwable cause = throwable != null ? throwable.getCause() : null;
        if (cause != null && cause.getMessage() != null) {
            message = cause.getMessage();
        }
        lblStatus.setText("Erro: " + message);
        LOGGER.log(Level.SEVERE, "Erro na conversão PDF → DOCX.", throwable);
    }

    private String replaceExtension(String fileName, String extension) {
        int dot = fileName.lastIndexOf('.');
        if (dot == -1) {
            return fileName + extension;
        }
        return fileName.substring(0, dot) + extension;
    }

    private void navigateToMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainMenuView.fxml"));
            VBox root = loader.load();
            Stage stage = (Stage) btnSelectPdf.getScene().getWindow();
            Scene scene = new Scene(root, 600, 500);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao voltar ao menu.", e);
        }
    }
}
