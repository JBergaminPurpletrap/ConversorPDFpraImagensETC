package com.documentconverter.core.converters;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * Conversor de PDF para imagens (JPG, JPEG, PNG) utilizando Apache PDFBox.
 * <p>
 * Cada página do PDF é convertida em um arquivo de imagem separado.
 * A nomenclatura segue o padrão: pagina_1.jpg, pagina_2.jpg, ...
 * </p>
 */
public class PdfToImageConverter {

    private static final Logger LOGGER = Logger.getLogger(PdfToImageConverter.class.getName());

    /** Resolução padrão em DPI para renderização das páginas. */
    public static final int DEFAULT_DPI = 300;

    /** Valores de DPI suportados. */
    public static final int[] SUPPORTED_DPI_VALUES = {150, 300, 600};

    /**
     * Converte cada página de um PDF em arquivos de imagem.
     *
     * @param pdfFile     arquivo PDF de entrada
     * @param outputDir   diretório onde as imagens serão salvas
     * @param imageFormat formato da imagem (jpg, jpeg, png)
     * @param dpi         resolução em DPI
     * @return lista de arquivos de imagem gerados
     * @throws IOException se ocorrer erro de leitura/escrita
     */
    public List<File> convert(File pdfFile, File outputDir, String imageFormat, int dpi)
            throws IOException {
        return convert(pdfFile, outputDir, imageFormat, dpi, null, null);
    }

    /**
     * Converte cada página de um PDF em arquivos de imagem, com callback de progresso.
     *
     * @param pdfFile           arquivo PDF de entrada
     * @param outputDir         diretório de saída
     * @param imageFormat       formato (jpg, jpeg, png)
     * @param dpi               resolução em DPI
     * @param progressCallback  callback (paginaAtual, totalPaginas) ou null
     * @return lista de arquivos de imagem gerados
     * @throws IOException se ocorrer erro
     */
    public List<File> convert(File pdfFile, File outputDir, String imageFormat,
                              int dpi, BiConsumer<Integer, Integer> progressCallback)
            throws IOException {
        return convert(pdfFile, outputDir, imageFormat, dpi, progressCallback, null);
    }

    public List<File> convert(File pdfFile, File outputDir, String imageFormat,
                              int dpi, BiConsumer<Integer, Integer> progressCallback,
                              String password)
            throws IOException {

        // Valida extensão
        String extension = getExtension(pdfFile.getName()).toLowerCase();
        if (!"pdf".equals(extension)) {
            throw new IllegalArgumentException(
                    "Formato de arquivo não suportado: " + extension + ". É necessário um PDF.");
        }

        // Garante diretório de saída
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IOException("Não foi possível criar o diretório: " + outputDir.getAbsolutePath());
        }

        if (!outputDir.canWrite()) {
            throw new IOException("Sem permissão de escrita no diretório: " + outputDir.getAbsolutePath());
        }

        // Normaliza formato
        String normalizedFormat = normalizeImageFormat(imageFormat);

        LOGGER.log(Level.INFO, "Convertendo PDF → {0} | DPI: {1} | Destino: {2}",
                new Object[]{normalizedFormat.toUpperCase(), dpi, outputDir.getAbsolutePath()});

        List<File> generatedImages = new ArrayList<>();

        try (PDDocument document = loadPdf(pdfFile, password)) {
            int totalPages = document.getNumberOfPages();
            if (totalPages == 0) {
                throw new IOException("O PDF não contém nenhuma página.");
            }

            PDFRenderer renderer = new PDFRenderer(document);

            for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
                // Renderiza a página no DPI especificado
                BufferedImage image = renderer.renderImageWithDPI(pageIndex, dpi, ImageType.RGB);

                // Nome padronizado: pagina_1.jpg, pagina_2.jpg, ...
                String fileName = String.format("pagina_%d.%s", pageIndex + 1, normalizedFormat);
                File outputFile = new File(outputDir, fileName);

                // Evita sobrescrita
                int counter = 1;
                while (outputFile.exists()) {
                    fileName = String.format("pagina_%d_%d.%s",
                            pageIndex + 1, counter++, normalizedFormat);
                    outputFile = new File(outputDir, fileName);
                }

                // Salva imagem
                boolean written = ImageIO.write(image, normalizedFormat, outputFile);
                if (!written) {
                    throw new IOException("Falha ao salvar imagem no formato: " + normalizedFormat);
                }

                generatedImages.add(outputFile);

                // Notifica progresso
                if (progressCallback != null) {
                    progressCallback.accept(pageIndex + 1, totalPages);
                }
            }
        } catch (IOException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("Password")) {
                throw new IOException("O PDF está protegido por senha.", e);
            }
            throw e;
        }

        LOGGER.log(Level.INFO, "{0} imagem(ns) gerada(s) em: {1}",
                new Object[]{generatedImages.size(), outputDir.getAbsolutePath()});

        return generatedImages;
    }

    /**
     * Retorna o número de páginas de um PDF sem converter.
     *
     * @param pdfFile arquivo PDF
     * @return número de páginas
     * @throws IOException se não for possível ler o PDF
     */
    public int getPageCount(File pdfFile) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            return document.getNumberOfPages();
        }
    }

    /**
     * Sobrecarga que usa DPI padrão (300).
     */
    public List<File> convert(File pdfFile, File outputDir, String imageFormat)
            throws IOException {
        return convert(pdfFile, outputDir, imageFormat, DEFAULT_DPI, null);
    }

    private PDDocument loadPdf(File pdfFile, String password) throws IOException {
        if (password == null || password.isBlank()) {
            return Loader.loadPDF(pdfFile);
        }
        return Loader.loadPDF(pdfFile, password);
    }

    /**
     * Normaliza formato da imagem: "jpeg" → "jpg".
     */
    private String normalizeImageFormat(String format) {
        if (format == null) return "jpg";
        String lower = format.toLowerCase().trim();
        return "jpeg".equals(lower) ? "jpg" : lower;
    }

    private String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return (dot == -1 || dot == fileName.length() - 1) ? "" : fileName.substring(dot + 1);
    }
}
