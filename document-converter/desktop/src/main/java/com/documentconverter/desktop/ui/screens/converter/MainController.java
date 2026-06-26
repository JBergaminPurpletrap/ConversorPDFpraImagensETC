package com.documentconverter.desktop.ui.screens.converter;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.documentconverter.core.services.ConversionService;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Controlador principal da aplicação Document Converter.
 * <p>
 * Gerencia a interação do usuário com a tela principal, incluindo seleção
 * múltipla de imagens, escolha do formato de saída, escolha do destino via
 * SaveDialog (na FX Thread) e acionamento da conversão em background.
 * </p>
 *
 * <p>No MVP, os formatos suportados são:</p>
 * <ul>
 *   <li>JPG / JPEG / PNG → PDF (múltiplas imagens, multipágina)</li>
 *   <li>PDF → JPG / JPEG / PNG</li>
 * </ul>
 */
public class MainController {

    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());

    @FXML
    private Button btnSelectFiles;

    @FXML
    private TextField txtFileCount;

    @FXML
    private ListView<String> lstFiles;

    @FXML
    private ComboBox<String> cmbOutputFormat;

    @FXML
    private Label lblDpi;

    @FXML
    private ComboBox<String> cmbDpi;

    @FXML
    private Button btnConvert;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label lblStatus;

    // Serviço de conversão
    private final ConversionService conversionService;

    // Lista de arquivos selecionados
    private List<File> selectedFiles;

    /**
     * Construtor padrão. Inicializa os serviços necessários.
     */
    public MainController() {
        this.conversionService = new ConversionService();
    }

    /**
     * Inicializa o controlador após o FXML ter sido carregado.
     */
    @FXML
    public void initialize() {
        // Configura os formatos de saída disponíveis no ComboBox
        cmbOutputFormat.getItems().addAll(
                "PDF (Converter imagem para PDF)",
                "JPG (Converter PDF para JPG)",
                "JPEG (Converter PDF para JPEG)",
                "PNG (Converter PDF para PNG)"
        );
        cmbOutputFormat.getSelectionModel().selectFirst();

        // Configura as opções de DPI (oculto por padrão)
        for (int dpi : ConversionService.SUPPORTED_PDF_TO_IMAGE_DPI_VALUES) {
            cmbDpi.getItems().add(String.valueOf(dpi));
        }
        cmbDpi.getSelectionModel().select("300"); // Default 300 DPI

        // Oculta campos de DPI (só aparece quando selecionar PDF)
        lblDpi.setVisible(false);
        lblDpi.setManaged(false);
        cmbDpi.setVisible(false);
        cmbDpi.setManaged(false);

        // Desabilita o botão Converter até que arquivos sejam selecionados
        btnConvert.setDisable(true);

        // Configura a barra de progresso
        progressBar.setProgress(0.0);

        // Limpa a mensagem de status
        lblStatus.setText("Selecione uma ou mais imagens para começar.");

        LOGGER.info("Controlador principal inicializado.");
    }

    /**
     * Abre um seletor de arquivos para o usuário escolher uma ou mais imagens.
     */
    @FXML
    private void onSelectFiles(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Imagens");

        // Define filtros para os tipos de arquivo suportados
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(
                        "Imagens (JPG, JPEG, PNG)", "*.jpg", "*.jpeg", "*.png"),
                new FileChooser.ExtensionFilter("Arquivos PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("Todos os Arquivos", "*.*")
        );

        // Obtém o estágio atual
        Stage stage = (Stage) btnSelectFiles.getScene().getWindow();

        // Abre o diálogo de seleção múltipla
        List<File> files = fileChooser.showOpenMultipleDialog(stage);

        if (files != null && !files.isEmpty()) {
            selectedFiles = files;
            updateFileListDisplay();

            // Ajusta automaticamente o formato de saída e habilita o botão
            adjustOutputFormatBasedOnInput(files.get(0));
            btnConvert.setDisable(false);

            LOGGER.log(Level.INFO, "{0} arquivo(s) selecionado(s).", files.size());
        }
    }

    /**
     * Atualiza o campo de texto e a ListView com os arquivos selecionados.
     */
    private void updateFileListDisplay() {
        int count = selectedFiles.size();
        txtFileCount.setText(count + " arquivo(s) selecionado(s)");

        lstFiles.getItems().clear();
        for (File file : selectedFiles) {
            lstFiles.getItems().add(file.getName());
        }
    }

    /**
     * Ajusta automaticamente a seleção do ComboBox com base na extensão
     * do primeiro arquivo selecionado. Também mostra/oculta o campo DPI.
     *
     * @param file primeiro arquivo da seleção
     */
    private void adjustOutputFormatBasedOnInput(File file) {
        String name = file.getName().toLowerCase();

        if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png")) {
            cmbOutputFormat.getSelectionModel().select("PDF (Converter imagem para PDF)");
            // Oculta DPI para conversão imagem → PDF
            lblDpi.setVisible(false);
            lblDpi.setManaged(false);
            cmbDpi.setVisible(false);
            cmbDpi.setManaged(false);
        } else if (name.endsWith(".pdf")) {
            cmbOutputFormat.getSelectionModel().select("JPG (Converter PDF para JPG)");
            // Mostra DPI para conversão PDF → imagem
            lblDpi.setVisible(true);
            lblDpi.setManaged(true);
            cmbDpi.setVisible(true);
            cmbDpi.setManaged(true);
        }
    }

    /**
     * Inicia o processo de conversão.
     * <p>
     * Abre o SaveDialog na FX Thread para o usuário escolher onde salvar o PDF,
     * depois executa a conversão em background via {@link Task}.
     * </p>
     */
    @FXML
    private void onConvert(ActionEvent event) {
        if (selectedFiles == null || selectedFiles.isEmpty()) {
            lblStatus.setText("Nenhum arquivo selecionado.");
            return;
        }

        String outputFormat = cmbOutputFormat.getSelectionModel().getSelectedItem();
        if (outputFormat == null || outputFormat.isEmpty()) {
            lblStatus.setText("Selecione um formato de saída.");
            return;
        }

        // Verifica o tipo de entrada: imagens ou PDF
        String firstFileName = selectedFiles.get(0).getName().toLowerCase();
        boolean isInputPdf = firstFileName.endsWith(".pdf");

        Stage ownerStage = (Stage) btnConvert.getScene().getWindow();

        if (isInputPdf) {
            // PDF → Imagem: usa DirectoryChooser (único arquivo)
            handlePdfToImageConversion(ownerStage, outputFormat);
        } else {
            // Imagem → PDF: usa SaveChooser
            handleImageToPdfConversion(ownerStage);
        }
    }

    /**
     * Abre SaveDialog e inicia conversão de imagens para PDF.
     */
    private void handleImageToPdfConversion(Stage ownerStage) {
        // Abre o SaveDialog na FX Thread
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar PDF como");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Arquivo PDF", "*.pdf"));
        fileChooser.setInitialFileName("documento_convertido.pdf");

        File chosenFile = fileChooser.showSaveDialog(ownerStage);
        if (chosenFile == null) {
            lblStatus.setText("Conversão cancelada pelo usuário.");
            return;
        }

        // Garante extensão .pdf (final para uso em lambda)
        final File outputFile;
        if (!chosenFile.getName().toLowerCase().endsWith(".pdf")) {
            outputFile = new File(chosenFile.getAbsolutePath() + ".pdf");
        } else {
            outputFile = chosenFile;
        }

        // Desabilita controles durante a conversão
        setControlsDisabled(true);

        // Cria a Task para conversão em background
        Task<Void> conversionTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                LOGGER.log(Level.INFO, "Convertendo {0} imagem(ns) para PDF: {1}",
                        new Object[]{selectedFiles.size(), outputFile.getAbsolutePath()});

                updateProgress(-1, 1);
                conversionService.convertImagesToPdf(selectedFiles, outputFile);
                updateProgress(1, 1);

                return null;
            }
        };

        conversionTask.setOnSucceeded(e -> {
            setControlsDisabled(false);
            lblStatus.setText("Conversão concluída com sucesso!\n"
                    + "Arquivo salvo em: " + outputFile.getAbsolutePath());
            progressBar.setProgress(1.0);
            LOGGER.log(Level.INFO, "PDF salvo em: {0}", outputFile.getAbsolutePath());
        });

        conversionTask.setOnFailed(e -> {
            setControlsDisabled(false);
            progressBar.setProgress(0.0);
            handleConversionError(conversionTask.getException());
        });

        new Thread(conversionTask).start();
    }

    /**
     * Abre DirectoryChooser e inicia conversão de PDF para imagens com progresso.
     */
    private void handlePdfToImageConversion(Stage ownerStage, String outputFormat) {
        // Abre DirectoryChooser na FX Thread
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Selecionar diretório para salvar as imagens");

        File selectedDir = directoryChooser.showDialog(ownerStage);
        if (selectedDir == null) {
            lblStatus.setText("Conversão cancelada pelo usuário.");
            return;
        }

        final File pdfFile = selectedFiles.get(0);
        final String imageFormat = extractImageFormat(outputFormat);

        // Lê o DPI selecionado (efetivamente final para uso em lambda)
        final int dpi = readSelectedDpi();

        setControlsDisabled(true);

        // Usa Task no desktop e chama o core por método síncrono.
        Task<List<File>> conversionTask = new Task<>() {
            @Override
            protected List<File> call() throws Exception {
                updateProgress(0, 1);
                return conversionService.convertPdfToImages(pdfFile, selectedDir, imageFormat, dpi,
                        (current, total) -> updateProgress(current, total));
            }
        };

        // Liga o progresso da Task à barra de progresso
        progressBar.progressProperty().bind(conversionTask.progressProperty());

        conversionTask.setOnSucceeded(e -> {
            progressBar.progressProperty().unbind();
            setControlsDisabled(false);

            List<File> images = conversionTask.getValue();
            String msg = "Conversão concluída com sucesso!\n"
                    + images.size() + " imagem(ns) salva(s) em: " + selectedDir.getAbsolutePath();
            lblStatus.setText(msg);
            progressBar.setProgress(1.0);
            LOGGER.log(Level.INFO, "{0} imagem(ns) salva(s) em: {1} (DPI: {2})",
                    new Object[]{images.size(), selectedDir.getAbsolutePath(), dpi});
        });

        conversionTask.setOnFailed(e -> {
            progressBar.progressProperty().unbind();
            setControlsDisabled(false);
            progressBar.setProgress(0.0);
            handleConversionError(conversionTask.getException());
        });

        new Thread(conversionTask).start();
    }

    /**
     * Trata erros de conversão exibindo mensagens amigáveis.
     */
    private void handleConversionError(Throwable exception) {
        String errorMessage = exception != null ? exception.getMessage() : "Erro desconhecido";

        Throwable cause = exception != null ? exception.getCause() : null;
        if (cause != null && cause.getMessage() != null) {
            errorMessage = cause.getMessage();
        }

        if (errorMessage != null && errorMessage.contains("Formato de imagem não suportado")) {
            lblStatus.setText("Erro: " + errorMessage);
        } else if (errorMessage != null && errorMessage.contains("Não foi possível ler a imagem")) {
            lblStatus.setText("Erro: Um dos arquivos de imagem pode estar corrompido.");
        } else if (errorMessage != null && errorMessage.contains("Arquivo não encontrado")) {
            lblStatus.setText("Erro: Arquivo não encontrado. Selecione arquivos válidos.");
        } else if (errorMessage != null && errorMessage.contains("permissão")) {
            lblStatus.setText("Erro: Sem permissão de escrita no diretório de destino.");
        } else {
            lblStatus.setText("Erro ao converter: "
                    + (errorMessage != null ? errorMessage : "Erro desconhecido"));
        }

        LOGGER.log(Level.SEVERE, "Erro durante a conversão.", exception);
    }

    /**
     * Habilita ou desabilita os controles da interface durante a conversão.
     */
    private void setControlsDisabled(boolean disabled) {
        btnSelectFiles.setDisable(disabled);
        btnConvert.setDisable(disabled);
        cmbOutputFormat.setDisable(disabled);
        cmbDpi.setDisable(disabled);
        lstFiles.setDisable(disabled);

        if (disabled) {
            progressBar.progressProperty().unbind();
            progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            lblStatus.setText("Convertendo... Aguarde.");
        }
    }

    /**
     * Lê o valor selecionado no ComboBox de DPI.
     *
     * @return DPI selecionado, ou 300 como padrão
     */
    private int readSelectedDpi() {
        try {
            String dpiText = cmbDpi.getSelectionModel().getSelectedItem();
            if (dpiText != null) {
                return Integer.parseInt(dpiText);
            }
        } catch (NumberFormatException e) {
            // Ignora, retorna padrão
        }
        return 300;
    }

    /**
     * Extrai o formato de imagem a partir do texto do ComboBox.
     */
    private String extractImageFormat(String outputFormat) {
        if (outputFormat == null) {
            return "jpg";
        }
        String upper = outputFormat.toUpperCase();
        if (upper.contains("JPG") || upper.contains("JPEG")) {
            return "jpg";
        } else if (upper.contains("PNG")) {
            return "png";
        }
        return "jpg";
    }
}
