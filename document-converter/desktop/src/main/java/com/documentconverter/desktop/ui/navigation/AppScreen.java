package com.documentconverter.desktop.ui.navigation;

public enum AppScreen {
    HOME("/fxml/MainMenuView.fxml"),
    IMAGE_TO_PDF("/fxml/ImageToPdfView.fxml"),
    PDF_TO_IMAGE("/fxml/PdfToImageView.fxml"),
    PDF_TO_DOCX("/fxml/PdfToDocxView.fxml"),
    DOCX_TO_PDF("/fxml/DocxToPdfView.fxml"),
    HISTORY("/fxml/HistoryView.fxml");

    private final String fxmlPath;

    AppScreen(String fxmlPath) {
        this.fxmlPath = fxmlPath;
    }

    public String fxmlPath() {
        return fxmlPath;
    }
}
