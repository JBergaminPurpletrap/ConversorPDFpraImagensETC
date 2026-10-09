package com.documentconverter.core.services;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.documentconverter.core.converters.DocxToPdfConverter;
import com.documentconverter.core.converters.ImageToPdfConverter;
import com.documentconverter.core.converters.PdfToDocxConverter;
import com.documentconverter.core.converters.PdfToImageConverter;
import com.documentconverter.core.models.PdfToDocxResult;

/**
 * Serviço responsável por orquestrar as conversões de arquivos.
 * <p>
 * Valida o arquivo de entrada e delega a conversão ao conversor adequado.
 * O diálogo de salvamento (SaveDialog) é tratado pelo controller na FX Thread.
 * </p>
 */
public class ConversionService {

    private static final Logger LOGGER = Logger.getLogger(ConversionService.class.getName());
    public static final int[] SUPPORTED_PDF_TO_IMAGE_DPI_VALUES = PdfToImageConverter.SUPPORTED_DPI_VALUES;

    private final ImageToPdfConverter imageToPdfConverter;
    private final DocxToPdfConverter docxToPdfConverter;
    private final PdfToDocxConverter pdfToDocxConverter;
    private final PdfToImageService pdfToImageService;

    /**
     * Construtor padrão. Inicializa os conversores necessários.
     */
    public ConversionService() {
        this.imageToPdfConverter = new ImageToPdfConverter();
        this.docxToPdfConverter = new DocxToPdfConverter();
        this.pdfToDocxConverter = new PdfToDocxConverter();
        this.pdfToImageService = new PdfToImageService();
    }

    /**
     * Converte uma lista de imagens em um único PDF multipágina.
     *
     * @param imageFiles lista de arquivos de imagem
     * @param outputFile arquivo PDF de destino
     * @throws IOException se ocorrer erro
     */
    public void convertImagesToPdf(List<File> imageFiles, File outputFile) throws IOException {
        convertImagesToPdf(imageFiles, outputFile, null);
    }

    public void convertImagesToPdf(List<File> imageFiles, File outputFile,
                                   BiConsumer<Integer, Integer> progressCallback) throws IOException {
        validateImageFiles(imageFiles);
        validateOutputFile(outputFile);

        LOGGER.log(Level.INFO, "Iniciando conversão de {0} imagem(ns) para PDF: {1}",
                new Object[]{imageFiles.size(), outputFile.getAbsolutePath()});

        imageToPdfConverter.convert(imageFiles, outputFile, progressCallback);

        LOGGER.log(Level.INFO, "PDF gerado com sucesso: {0}", outputFile.getAbsolutePath());
    }

    /**
     * Converte um PDF em imagens (uma por página).
     *
     * @param pdfFile     arquivo PDF de entrada
     * @param outputDir   diretório de saída
     * @param imageFormat formato das imagens
     * @param dpi         resolução em DPI
     * @return lista de arquivos de imagem gerados
     * @throws IOException se ocorrer erro
     */
    public List<File> convertPdfToImages(File pdfFile, File outputDir,
                                         String imageFormat, int dpi)
            throws IOException {
        return convertPdfToImages(pdfFile, outputDir, imageFormat, dpi, null);
    }

    public List<File> convertPdfToImages(File pdfFile, File outputDir,
                                         String imageFormat, int dpi,
                                         BiConsumer<Integer, Integer> progressCallback)
            throws IOException {
        validateInputFile(pdfFile);
        validateOutputDir(outputDir);

        LOGGER.log(Level.INFO, "Iniciando conversão de PDF para {0} (DPI: {1}): {2} → {3}",
                new Object[]{imageFormat.toUpperCase(), dpi, pdfFile.getAbsolutePath(),
                        outputDir.getAbsolutePath()});

        List<File> generatedImages = pdfToImageService.convert(pdfFile, outputDir, imageFormat, dpi, progressCallback);

        LOGGER.log(Level.INFO, "{0} imagem(ns) gerada(s) em: {1}",
                new Object[]{generatedImages.size(), outputDir.getAbsolutePath()});

        return generatedImages;
    }

    /**
     * Cria uma Task para conversão de DOCX para PDF via LibreOffice headless.
     *
     * @param docxFile arquivo DOCX de entrada
     * @param outputFile arquivo PDF de saída
     * @return Task de conversão
     */
    public void convertDocxToPdf(File docxFile, File outputFile) throws IOException, InterruptedException {
        validateDocxFile(docxFile);
        validateOutputFile(outputFile);
        docxToPdfConverter.convert(docxFile, outputFile);
    }

    /**
     * Cria uma Task para conversão inicial de PDF para DOCX.
     *
     * @param pdfFile arquivo PDF de entrada
     * @param outputFile arquivo DOCX de saída
     * @return Task com métricas da conversão
     */
    public PdfToDocxResult convertPdfToDocx(File pdfFile, File outputFile) throws IOException {
        validateInputFile(pdfFile);
        validateOutputFile(outputFile);
        if (!pdfFile.getName().toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Formato inválido. Selecione um arquivo .pdf.");
        }

        return pdfToDocxConverter.convert(pdfFile, outputFile);
    }

    /**
     * Valida a lista de imagens de entrada.
     *
     * @param imageFiles lista de arquivos de imagem
     * @throws IllegalArgumentException se a lista for inválida
     */
    private void validateImageFiles(List<File> imageFiles) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            throw new IllegalArgumentException("Nenhuma imagem selecionada.");
        }
        for (File file : imageFiles) {
            if (file == null || !file.exists()) {
                throw new IllegalArgumentException(
                        "Arquivo não encontrado: " + (file != null ? file.getAbsolutePath() : "null"));
            }
            if (!file.isFile()) {
                throw new IllegalArgumentException(
                        "O caminho não é um arquivo: " + file.getAbsolutePath());
            }
            if (!file.canRead()) {
                throw new IllegalArgumentException(
                        "O arquivo não pode ser lido: " + file.getAbsolutePath());
            }
        }
    }

    /**
     * Valida o arquivo PDF de entrada.
     *
     * @param inputFile arquivo PDF
     * @throws IllegalArgumentException se o arquivo for inválido
     */
    private void validateInputFile(File inputFile) {
        if (inputFile == null) {
            throw new IllegalArgumentException("Nenhum arquivo selecionado.");
        }
        if (!inputFile.exists()) {
            throw new IllegalArgumentException("Arquivo não encontrado: " + inputFile.getAbsolutePath());
        }
        if (!inputFile.isFile()) {
            throw new IllegalArgumentException("O caminho selecionado não é um arquivo.");
        }
        if (!inputFile.canRead()) {
            throw new IllegalArgumentException("O arquivo não pode ser lido. Verifique as permissões.");
        }
    }

    /**
     * Valida arquivo DOCX de entrada.
     */
    private void validateDocxFile(File inputFile) {
        validateInputFile(inputFile);
        if (!inputFile.getName().toLowerCase().endsWith(".docx")) {
            throw new IllegalArgumentException("Formato inválido. Selecione um arquivo .docx.");
        }
    }

    /**
     * Valida o arquivo de saída (deve ser um caminho válido).
     *
     * @param outputFile arquivo de saída
     * @throws IOException se o diretório pai não existir ou não puder ser criado
     */
    private void validateOutputFile(File outputFile) throws IOException {
        if (outputFile == null) {
            throw new IllegalArgumentException("Nenhum arquivo de destino informado.");
        }
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException(
                        "Não foi possível criar o diretório: " + parentDir.getAbsolutePath());
            }
        }
        if (parentDir != null && !parentDir.canWrite()) {
            throw new IOException(
                    "Sem permissão de escrita no diretório: " + parentDir.getAbsolutePath());
        }
    }

    /**
     * Valida o diretório de saída para imagens.
     *
     * @param outputDir diretório de saída
     * @throws IOException se o diretório não existir ou não puder ser criado
     */
    private void validateOutputDir(File outputDir) throws IOException {
        if (outputDir == null) {
            throw new IllegalArgumentException("Nenhum diretório de destino informado.");
        }
        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                throw new IOException(
                        "Não foi possível criar o diretório: " + outputDir.getAbsolutePath());
            }
        }
        if (!outputDir.canWrite()) {
            throw new IOException(
                    "Sem permissão de escrita no diretório: " + outputDir.getAbsolutePath());
        }
    }

}
