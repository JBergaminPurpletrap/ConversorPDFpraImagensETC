package com.documentconverter.desktop.ui.screens.history;

import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import com.documentconverter.core.models.ConversionHistory;
import com.documentconverter.core.models.ConversionHistoryService;
import com.documentconverter.desktop.ui.navigation.AppNavigator;
import com.documentconverter.desktop.ui.navigation.AppScreen;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * Controlador da tela de histórico de conversões.
 */
public class HistoryController {

    private static final Logger LOGGER = Logger.getLogger(HistoryController.class.getName());

    @FXML private Button btnBack;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbOperationFilter;
    @FXML private TableView<ConversionHistory> tblHistory;
    @FXML private TableColumn<ConversionHistory, String> colDate;
    @FXML private TableColumn<ConversionHistory, String> colTime;
    @FXML private TableColumn<ConversionHistory, String> colOperation;
    @FXML private TableColumn<ConversionHistory, String> colSource;
    @FXML private TableColumn<ConversionHistory, String> colDestination;
    @FXML private TableColumn<ConversionHistory, String> colStatus;
    @FXML private Label lblStatus;

    private final ConversionHistoryService historyService = new ConversionHistoryService();
    private final ObservableList<ConversionHistory> allItems = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        configureTable();
        configureFilters();
        loadHistory();
        LOGGER.info("Tela de histórico carregada.");
    }

    @FXML
    private void onBack(ActionEvent event) {
        AppNavigator.navigate(event, AppScreen.HOME);
    }

    private void configureTable() {
        colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate()));
        colTime.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTime()));
        colOperation.setCellValueFactory(data -> new SimpleStringProperty(formatOperation(data.getValue().getOperation())));
        colSource.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSource()));
        colDestination.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDestination()));
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(formatStatus(data.getValue().getStatus())));
    }

    private void configureFilters() {
        cmbOperationFilter.getItems().addAll("Todos", "Imagem → PDF", "PDF → Imagem", "PDF → DOCX", "DOCX → PDF");
        cmbOperationFilter.getSelectionModel().select("Todos");
        txtSearch.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        cmbOperationFilter.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());
    }

    private void loadHistory() {
        List<ConversionHistory> latest = historyService.findLatestFirst();
        allItems.setAll(latest);
        applyFilters();
    }

    private void applyFilters() {
        String search = normalize(txtSearch.getText());
        String operationFilter = cmbOperationFilter.getValue();

        List<ConversionHistory> filtered = allItems.stream()
                .filter(item -> matchesOperation(item, operationFilter))
                .filter(item -> matchesSearch(item, search))
                .toList();

        tblHistory.setItems(FXCollections.observableArrayList(filtered));
        lblStatus.setText(filtered.size() + " registro(s) encontrado(s).");
    }

    private boolean matchesOperation(ConversionHistory item, String operationFilter) {
        if (operationFilter == null || "Todos".equals(operationFilter)) {
            return true;
        }
        if ("Imagem → PDF".equals(operationFilter)) {
            return ConversionHistoryService.OPERATION_IMAGE_TO_PDF.equals(item.getOperation());
        }
        if ("PDF → Imagem".equals(operationFilter)) {
            return ConversionHistoryService.OPERATION_PDF_TO_IMAGE.equals(item.getOperation());
        }
        if ("PDF → DOCX".equals(operationFilter)) {
            return ConversionHistoryService.OPERATION_PDF_TO_DOCX.equals(item.getOperation());
        }
        if ("DOCX → PDF".equals(operationFilter)) {
            return ConversionHistoryService.OPERATION_DOCX_TO_PDF.equals(item.getOperation());
        }
        return true;
    }

    private boolean matchesSearch(ConversionHistory item, String search) {
        if (search.isBlank()) {
            return true;
        }
        return normalize(item.getSource()).contains(search)
                || normalize(item.getDestination()).contains(search);
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private String formatOperation(String operation) {
        if (ConversionHistoryService.OPERATION_IMAGE_TO_PDF.equals(operation)) {
            return "Imagem → PDF";
        }
        if (ConversionHistoryService.OPERATION_PDF_TO_IMAGE.equals(operation)) {
            return "PDF → Imagem";
        }
        if (ConversionHistoryService.OPERATION_PDF_TO_DOCX.equals(operation)) {
            return "PDF → DOCX";
        }
        if (ConversionHistoryService.OPERATION_DOCX_TO_PDF.equals(operation)) {
            return "DOCX → PDF";
        }
        return operation == null ? "" : operation;
    }

    private String formatStatus(String status) {
        if (ConversionHistoryService.STATUS_SUCCESS.equals(status)) {
            return "Sucesso";
        }
        if (ConversionHistoryService.STATUS_FAILED.equals(status)) {
            return "Erro";
        }
        if (ConversionHistoryService.STATUS_CANCELED.equals(status)) {
            return "Cancelada";
        }
        return status == null ? "" : status;
    }
}
