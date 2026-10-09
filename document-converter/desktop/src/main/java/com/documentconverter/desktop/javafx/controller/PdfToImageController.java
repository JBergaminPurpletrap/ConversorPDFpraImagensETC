package com.documentconverter.desktop.javafx.controller;

import java.io.File;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Controlador da tela de conversão de PDF para imagens.
 * Suporta seleção via clique ou arrastar/soltar (drag and drop).
 */
public class PdfToImageController {

    private static final Logger LOGGER = Logger.getLogger(PdfToImageController.class.getName());

    @FXML private Button btnSelectPdf;
    @FXML private TextField txtPdfFile;
    @FXML private VBox dropZone;
    @FXML private ComboBox<String> cmbImageFormat;
    @FXML private ComboBox<String> cmbDpi;
    @FXML private Button btnConvert;
    @FXML private Button btnCancel;
    @FXML private ProgressBar progressBar;
    @FXML private Label lblPercent;
    @FXML private Label lblStatus;

    private final ConversionService conversionService = new ConversionService();
    private final ConversionHistoryService historyService = new ConversionHistoryService();
    private File selectedPdf;
    private File currentOutputDir;
    private Task<?> currentTask;

    @FXML
    public void initialize() {
        cmbImageFormat.getItems().addAll("JPG", "JPEG", "PNG");
        cmbImageFormat.getSelectionModel().select("JPG");

        for (int dpi : ConversionService.SUPPORTED_PDF_TO_IMAGE_DPI_VALUES) {
            cmbDpi.getItems().add(String.valueOf(dpi));
        }
        cmbDpi.getSelectionModel().select("300");

        btnConvert.setDisable(true);
        progressBar.setProgress(0.0);
        lblStatus.setText("Selecione um arquivo PDF para começar.");

        setupDragAndDrop();

        LOGGER.info("Tela 'PDF → Imagem' carregada.");
    }

    /**
     * Configura drag and drop para PDF na dropZone.
     */
    private void setupDragAndDrop() {
        dropZone.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        dropZone.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasFiles()) {
                success = loadPdf(db.getFiles());
            }

            event.setDropCompleted(success);
            event.consume();
        });

        dropZone.setOnMouseClicked(event -> onSelectPdf(null));
    }

    /**
     * Carrega o PDF na interface e oculta a drop zone.
     */
    private boolean loadPdf(List<File> files) {
        if (files.size() > 1) {
            boolean hasPdf = files.stream().anyMatch(this::isPdfFile);
            lblStatus.setText(hasPdf
                    ? "Solte apenas um arquivo PDF por vez."
                    : "Nenhum PDF válido encontrado.");
            return false;
        }

        return loadPdf(files.get(0));
    }

    /**
     * Carrega o PDF na interface e oculta a drop zone.
     */
    private boolean loadPdf(File pdfFile) {
        if (!isPdfFile(pdfFile)) {
            lblStatus.setText("Arquivo inválido. Use somente PDF.");
            return false;
        }

        selectedPdf = pdfFile;
        txtPdfFile.setText(pdfFile.getAbsolutePath());
        dropZone.setVisible(false);
        dropZone.setManaged(false);
        btnConvert.setDisable(false);
        lblStatus.setText("PDF carregado: " + pdfFile.getName());
        LOGGER.log(Level.INFO, "PDF carregado via drag/drop: {0}", pdfFile.getAbsolutePath());
        return true;
    }

    @FXML
    private void onBack(ActionEvent event) {
        navigateToMainMenu();
    }

    @FXML
    private void onSelectPdf(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Selecionar PDF");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Arquivos PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("Todos os Arquivos", "*.*"));

        Stage stage = (Stage) btnSelectPdf.getScene().getWindow();
        File file = fc.showOpenDialog(stage);
        if (file != null) {
            loadPdf(file);
        }
    }

    @FXML
    private void onCancel(ActionEvent event) {
        if (currentTask != null && currentTask.isRunning()) {
            currentTask.cancel(true);
            lblStatus.setText("Conversão cancelada pelo usuário.");
            registerHistory(currentOutputDir, ConversionHistoryService.STATUS_CANCELED);
            setControlsDisabled(false);
            btnCancel.setVisible(false);
            btnCancel.setManaged(false);
        }
    }

    @FXML
    private void onConvert(ActionEvent event) {
        if (selectedPdf == null) return;

        Stage stage = (Stage) btnConvert.getScene().getWindow();

        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Selecionar pasta para salvar as imagens");
        File selectedDir = dc.showDialog(stage);
        if (selectedDir == null) {
            lblStatus.setText("Conversão cancelada.");
            return;
        }

        final File pdfFile = selectedPdf;
        final String imageFormat = cmbImageFormat.getSelectionModel().getSelectedItem();
        final int dpi = readSelectedDpi();

        setControlsDisabled(true);
        btnCancel.setVisible(true);
        btnCancel.setManaged(true);
        currentOutputDir = selectedDir;

        currentTask = new Task<List<File>>() {
            @Override
            protected List<File> call() throws Exception {
                updateProgress(0, 1);
                return conversionService.convertPdfToImages(pdfFile, selectedDir, imageFormat, dpi,
                        (current, total) -> updateProgress(current, total));
            }
        };

        progressBar.progressProperty().bind(currentTask.progressProperty());
        currentTask.progressProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                lblPercent.setText((int) Math.round(newVal.doubleValue() * 100) + "%");
            }
        });

        currentTask.setOnSucceeded(e -> {
            progressBar.progressProperty().unbind();
            setControlsDisabled(false);
            btnCancel.setVisible(false);
            btnCancel.setManaged(false);

            @SuppressWarnings("unchecked")
            List<File> images = (List<File>) currentTask.getValue();
            lblPercent.setText("100%");
            lblStatus.setText("Conversão concluída com sucesso!\n"
                    + images.size() + " imagem(ns) salva(s) em: " + selectedDir.getAbsolutePath());
            progressBar.setProgress(1.0);
            registerHistory(selectedDir, ConversionHistoryService.STATUS_SUCCESS);
            LOGGER.log(Level.INFO, "{0} imagem(ns) em: {1} (DPI: {2})",
                    new Object[]{images.size(), selectedDir.getAbsolutePath(), dpi});
        });

        currentTask.setOnFailed(e -> {
            progressBar.progressProperty().unbind();
            setControlsDisabled(false);
            btnCancel.setVisible(false);
            btnCancel.setManaged(false);
            progressBar.setProgress(0.0);
            lblPercent.setText("");
            registerHistory(selectedDir, ConversionHistoryService.STATUS_FAILED);
            handleError(currentTask.getException());
        });

        new Thread(currentTask).start();
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

    private int readSelectedDpi() {
        try {
            String text = cmbDpi.getSelectionModel().getSelectedItem();
            if (text != null) return Integer.parseInt(text);
        } catch (NumberFormatException e) { /* ignora */ }
        return 300;
    }

    private boolean isPdfFile(File file) {
        return file.getName().toLowerCase().endsWith(".pdf");
    }

    private void registerHistory(File outputDir, String status) {
        if (selectedPdf == null || outputDir == null) {
            return;
        }
        historyService.register(
                ConversionHistoryService.OPERATION_PDF_TO_IMAGE,
                selectedPdf.getName(),
                outputDir.getAbsolutePath(),
                status);
    }

    private void setControlsDisabled(boolean disabled) {
        btnSelectPdf.setDisable(disabled);
        btnConvert.setDisable(disabled);
        cmbImageFormat.setDisable(disabled);
        cmbDpi.setDisable(disabled);
        if (disabled) {
            progressBar.progressProperty().unbind();
            progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            lblPercent.setText("");
            lblStatus.setText("Convertendo... Aguarde.");
        }
    }

    private void handleError(Throwable t) {
        String msg = t != null ? t.getMessage() : "Erro desconhecido";
        Throwable cause = t != null ? t.getCause() : null;
        if (cause != null && cause.getMessage() != null) msg = cause.getMessage();
        if (msg != null && msg.contains("permissão")) {
            lblStatus.setText("Erro: Sem permissão de escrita.");
        } else {
            lblStatus.setText("Erro: " + (msg != null ? msg : "Erro desconhecido"));
        }
        LOGGER.log(Level.SEVERE, "Erro na conversão.", t);
    }
}
