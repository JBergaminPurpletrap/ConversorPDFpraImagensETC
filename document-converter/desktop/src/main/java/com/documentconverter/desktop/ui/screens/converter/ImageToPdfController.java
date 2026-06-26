package com.documentconverter.desktop.ui.screens.converter;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.documentconverter.core.services.ConversionService;
import com.documentconverter.core.models.ConversionHistoryService;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import com.documentconverter.desktop.ui.navigation.AppNavigator;
import com.documentconverter.desktop.ui.navigation.AppScreen;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Controlador da tela de conversão de imagens para PDF.
 */
public class ImageToPdfController {

    private static final Logger LOGGER = Logger.getLogger(ImageToPdfController.class.getName());

    @FXML private Button btnSelectFiles;
    @FXML private TextField txtFileCount;
    @FXML private VBox dropZone;
    @FXML private ListView<String> lstFiles;
    @FXML private Button btnConvert;
    @FXML private Button btnCancel;
    @FXML private ProgressBar progressBar;
    @FXML private Label lblPercent;
    @FXML private Label lblStatus;

    private final ConversionService conversionService = new ConversionService();
    private final ConversionHistoryService historyService = new ConversionHistoryService();
    private List<File> selectedFiles;
    private Task<?> currentTask;
    private File currentOutputFile;

    @FXML
    public void initialize() {
        btnConvert.setDisable(true);
        progressBar.setProgress(0.0);
        lblStatus.setText("Selecione uma ou mais imagens para começar.");

        setupDragAndDrop();

        LOGGER.info("Tela 'Imagem → PDF' carregada.");
    }

    /**
     * Configura drag and drop na área central (dropZone).
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
                success = loadFiles(db.getFiles());
            }

            event.setDropCompleted(success);
            event.consume();
        });

        // Permite clicar na drop zone também
        dropZone.setOnMouseClicked(event -> {
            onSelectFiles(null);
        });
    }

    /**
     * Carrega a lista de arquivos na interface.
     */
    private boolean loadFiles(List<File> files) {
        List<File> invalidFiles = files.stream()
                .filter(file -> !isSupportedImage(file))
                .toList();

        if (!invalidFiles.isEmpty() && invalidFiles.size() == files.size()) {
            lblStatus.setText("Nenhuma imagem válida encontrada. Use JPG, JPEG ou PNG.");
            return false;
        }

        if (!invalidFiles.isEmpty()) {
            lblStatus.setText("Mistura de formatos não permitida. Use somente imagens JPG, JPEG ou PNG.");
            return false;
        }

        Map<String, File> uniqueFiles = new LinkedHashMap<>();
        for (File file : files) {
            uniqueFiles.putIfAbsent(file.getAbsolutePath().toLowerCase(), file);
        }

        selectedFiles = new ArrayList<>(uniqueFiles.values());
        txtFileCount.setText(selectedFiles.size() + " arquivo(s) selecionado(s)");

        // Mostra a ListView, esconde a drop zone
        dropZone.setVisible(false);
        dropZone.setManaged(false);
        lstFiles.setVisible(true);
        lstFiles.setManaged(true);

        lstFiles.getItems().clear();
        for (File f : selectedFiles) {
            lstFiles.getItems().add(f.getName());
        }
        btnConvert.setDisable(false);
        if (selectedFiles.size() < files.size()) {
            lblStatus.setText(selectedFiles.size() + " imagem(ns) carregada(s). Arquivos duplicados foram ignorados.");
        } else {
            lblStatus.setText(selectedFiles.size() + " imagem(ns) carregada(s).");
        }
        LOGGER.log(Level.INFO, "{0} imagem(ns) selecionada(s).", selectedFiles.size());
        return true;
    }

    @FXML
    private void onBack(ActionEvent event) {
        navigateToMainMenu();
    }

    @FXML
    private void onSelectFiles(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Selecionar Imagens");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagens (JPG, JPEG, PNG)", "*.jpg", "*.jpeg", "*.png"),
                new FileChooser.ExtensionFilter("Todos os Arquivos", "*.*"));

        Stage stage = (Stage) btnSelectFiles.getScene().getWindow();
        List<File> files = fc.showOpenMultipleDialog(stage);

        if (files != null && !files.isEmpty()) {
            loadFiles(files);
        }
    }

    @FXML
    private void onCancel(ActionEvent event) {
        if (currentTask != null && currentTask.isRunning()) {
            currentTask.cancel(true);
            lblStatus.setText("Conversão cancelada pelo usuário.");
            registerHistory(currentOutputFile, ConversionHistoryService.STATUS_CANCELED);
            setControlsDisabled(false);
        }
    }

    @FXML
    private void onConvert(ActionEvent event) {
        if (selectedFiles == null || selectedFiles.isEmpty()) return;

        Stage stage = (Stage) btnConvert.getScene().getWindow();

        FileChooser fc = new FileChooser();
        fc.setTitle("Salvar PDF como");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivo PDF", "*.pdf"));
        fc.setInitialFileName("documento_convertido.pdf");

        File chosen = fc.showSaveDialog(stage);
        if (chosen == null) {
            lblStatus.setText("Conversão cancelada.");
            return;
        }

        final File outputFile = !chosen.getName().toLowerCase().endsWith(".pdf")
                ? new File(chosen.getAbsolutePath() + ".pdf") : chosen;

        setControlsDisabled(true);
        btnCancel.setVisible(true);
        btnCancel.setManaged(true);
        currentOutputFile = outputFile;

        // Usa Task com progresso real no módulo desktop.
        int total = selectedFiles.size();
        currentTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateProgress(0, total);
                conversionService.convertImagesToPdf(selectedFiles, outputFile,
                        (current, max) -> updateProgress(current, max));
                return null;
            }
        };

        // Liga barra de progresso e percentual
        progressBar.progressProperty().bind(currentTask.progressProperty());
        currentTask.progressProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                int percent = (int) Math.round(newVal.doubleValue() * 100);
                lblPercent.setText(percent + "%");
            }
        });

        currentTask.setOnSucceeded(e -> {
            progressBar.progressProperty().unbind();
            setControlsDisabled(false);
            btnCancel.setVisible(false);
            btnCancel.setManaged(false);
            lblPercent.setText("100%");
            lblStatus.setText("Conversão concluída com sucesso!\nArquivo salvo em: " + outputFile.getAbsolutePath());
            progressBar.setProgress(1.0);
            registerHistory(outputFile, ConversionHistoryService.STATUS_SUCCESS);
            LOGGER.log(Level.INFO, "PDF salvo em: {0}", outputFile.getAbsolutePath());
        });

        currentTask.setOnFailed(e -> {
            progressBar.progressProperty().unbind();
            setControlsDisabled(false);
            btnCancel.setVisible(false);
            btnCancel.setManaged(false);
            progressBar.setProgress(0.0);
            lblPercent.setText("");
            registerHistory(outputFile, ConversionHistoryService.STATUS_FAILED);
            handleError(currentTask.getException());
        });

        new Thread(currentTask).start();
    }

    private void navigateToMainMenu() {
        Stage stage = (Stage) btnSelectFiles.getScene().getWindow();
        AppNavigator.navigate(stage, AppScreen.HOME);
    }

    private void setControlsDisabled(boolean disabled) {
        btnSelectFiles.setDisable(disabled);
        btnConvert.setDisable(disabled);
        lstFiles.setDisable(disabled);
        if (disabled) {
            progressBar.progressProperty().unbind();
            progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            lblPercent.setText("");
            lblStatus.setText("Convertendo... Aguarde.");
        }
    }

    private boolean isSupportedImage(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");
    }

    private void registerHistory(File outputFile, String status) {
        if (selectedFiles == null || selectedFiles.isEmpty() || outputFile == null) {
            return;
        }
        String source = selectedFiles.stream()
                .map(File::getName)
                .collect(Collectors.joining(", "));
        historyService.register(
                ConversionHistoryService.OPERATION_IMAGE_TO_PDF,
                source,
                outputFile.getAbsolutePath(),
                status);
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
        progressBar.setProgress(0.0);
        LOGGER.log(Level.SEVERE, "Erro na conversão.", t);
    }
}
