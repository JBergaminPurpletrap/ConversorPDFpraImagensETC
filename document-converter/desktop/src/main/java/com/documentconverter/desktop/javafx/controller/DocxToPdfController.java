package com.documentconverter.desktop.javafx.controller;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.documentconverter.core.models.ConversionHistoryService;
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
 * Controlador da tela de conversão de DOCX para PDF.
 */
public class DocxToPdfController {

    private static final Logger LOGGER = Logger.getLogger(DocxToPdfController.class.getName());

    @FXML private Button btnSelectDocx;
    @FXML private TextField txtDocxFile;
    @FXML private VBox dropZone;
    @FXML private Button btnConvert;
    @FXML private Button btnCancel;
    @FXML private ProgressBar progressBar;
    @FXML private Label lblPercent;
    @FXML private Label lblStatus;

    private final ConversionService conversionService = new ConversionService();
    private final ConversionHistoryService historyService = new ConversionHistoryService();
    private File selectedDocx;
    private File currentOutputFile;
    private Task<?> currentTask;
    private Instant conversionStart;

    @FXML
    private void initialize() {
        btnConvert.setDisable(true);
        progressBar.setProgress(0.0);
        lblStatus.setText("Selecione um arquivo DOCX para começar.");
        setupDragAndDrop();
        LOGGER.info("Tela 'DOCX → PDF' carregada.");
    }

    @FXML
    private void onBack(ActionEvent event) {
        navigateToMainMenu();
    }

    @FXML
    private void onSelectDocx(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar DOCX");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Documentos Word", "*.docx"),
                new FileChooser.ExtensionFilter("Todos os Arquivos", "*.*"));

        Stage stage = (Stage) btnSelectDocx.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            loadDocx(file);
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
        if (selectedDocx == null) {
            return;
        }

        Stage stage = (Stage) btnConvert.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar PDF como");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivo PDF", "*.pdf"));
        fileChooser.setInitialFileName(replaceExtension(selectedDocx.getName(), ".pdf"));

        File chosen = fileChooser.showSaveDialog(stage);
        if (chosen == null) {
            lblStatus.setText("Conversão cancelada.");
            return;
        }

        File outputFile = chosen.getName().toLowerCase().endsWith(".pdf")
                ? chosen
                : new File(chosen.getAbsolutePath() + ".pdf");

        currentOutputFile = outputFile;
        conversionStart = Instant.now();
        setControlsDisabled(true);
        btnCancel.setVisible(true);
        btnCancel.setManaged(true);

        LOGGER.log(Level.INFO, "DOCX → PDF solicitado | origem: {0} | destino: {1}",
                new Object[]{selectedDocx.getAbsolutePath(), outputFile.getAbsolutePath()});

        currentTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateProgress(-1, 1);
                conversionService.convertDocxToPdf(selectedDocx, outputFile);
                updateProgress(1, 1);
                return null;
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
            lblStatus.setText("Conversão concluída com sucesso!\nArquivo salvo em: " + outputFile.getAbsolutePath());
            registerHistory(ConversionHistoryService.STATUS_SUCCESS);
            logElapsed("DOCX → PDF concluído");
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
            logElapsed("DOCX → PDF falhou");
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
                success = loadDocx(dragboard.getFiles());
            }
            event.setDropCompleted(success);
            event.consume();
        });

        dropZone.setOnMouseClicked(event -> onSelectDocx(null));
    }

    private boolean loadDocx(List<File> files) {
        if (files.size() > 1) {
            lblStatus.setText("Solte apenas um arquivo DOCX por vez.");
            return false;
        }
        return loadDocx(files.get(0));
    }

    private boolean loadDocx(File file) {
        if (!isDocxFile(file)) {
            lblStatus.setText("Arquivo inválido. Use somente DOCX.");
            return false;
        }

        selectedDocx = file;
        txtDocxFile.setText(file.getAbsolutePath());
        dropZone.setVisible(false);
        dropZone.setManaged(false);
        btnConvert.setDisable(false);
        lblStatus.setText("DOCX carregado: " + file.getName());
        LOGGER.log(Level.INFO, "DOCX carregado: {0}", file.getAbsolutePath());
        return true;
    }

    private boolean isDocxFile(File file) {
        return file.getName().toLowerCase().endsWith(".docx");
    }

    private void setControlsDisabled(boolean disabled) {
        btnSelectDocx.setDisable(disabled);
        btnConvert.setDisable(disabled);
        if (disabled) {
            progressBar.progressProperty().unbind();
            progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            lblPercent.setText("");
            lblStatus.setText("Convertendo com LibreOffice... Aguarde.");
        }
    }

    private void registerHistory(String status) {
        if (selectedDocx == null || currentOutputFile == null) {
            return;
        }
        historyService.register(
                ConversionHistoryService.OPERATION_DOCX_TO_PDF,
                selectedDocx.getName(),
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
        LOGGER.log(Level.SEVERE, "Erro na conversão DOCX → PDF.", throwable);
    }

    private void logElapsed(String event) {
        if (conversionStart == null) {
            return;
        }
        long elapsedMs = Duration.between(conversionStart, Instant.now()).toMillis();
        LOGGER.log(Level.INFO, "{0} em {1} ms | origem: {2} | destino: {3}",
                new Object[]{event, elapsedMs, selectedDocx.getAbsolutePath(), currentOutputFile.getAbsolutePath()});
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
            Stage stage = (Stage) btnSelectDocx.getScene().getWindow();
            Scene scene = new Scene(root, 600, 500);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao voltar ao menu.", e);
        }
    }
}
