package com.documentconverter.core.converters;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * Conversor de imagens (JPG, JPEG, PNG) para PDF multipágina utilizando Apache PDFBox.
 * <p>
 * Cada imagem recebe uma página separada no PDF. As imagens são redimensionadas
 * proporcionalmente para caber na página A4 e centralizadas.
 * </p>
 */
public class ImageToPdfConverter {

    private static final Logger LOGGER = Logger.getLogger(ImageToPdfConverter.class.getName());

    /** Tamanho da página A4 em pontos (largura). */
    private static final float A4_WIDTH = PDRectangle.A4.getWidth();

    /** Tamanho da página A4 em pontos (altura). */
    private static final float A4_HEIGHT = PDRectangle.A4.getHeight();

    /** Margem interna em pontos para não colar a imagem nas bordas. */
    private static final float MARGIN = 20;

    /** Formatos de imagem suportados para conversão para PDF. */
    public static final String[] SUPPORTED_EXTENSIONS = {"jpg", "jpeg", "png"};

    /**
     * Converte uma lista de arquivos de imagem em um único PDF multipágina.
     * <p>
     * Cada imagem é inserida em uma página separada, redimensionada
     * proporcionalmente e centralizada. O PDF utiliza páginas A4.
     * </p>
     *
     * @param imageFiles lista de arquivos de imagem (JPG, JPEG, PNG)
     * @param outputFile arquivo PDF de saída
     * @throws IllegalArgumentException se a lista estiver vazia ou formato inválido
     * @throws IOException              se ocorrer erro de leitura/escrita
     */
    public void convert(List<File> imageFiles, File outputFile) throws IOException {
        convert(imageFiles, outputFile, null);
    }

    /**
     * Converte uma lista de imagens em PDF multipágina com callback de progresso.
     *
     * @param imageFiles       lista de imagens
     * @param outputFile       arquivo PDF de saída
     * @param progressCallback callback (imagemAtual, total) ou null
     * @throws IOException se erro
     */
    public void convert(List<File> imageFiles, File outputFile,
                        BiConsumer<Integer, Integer> progressCallback) throws IOException {
        if (imageFiles == null || imageFiles.isEmpty()) {
            throw new IllegalArgumentException("Nenhuma imagem selecionada para conversão.");
        }

        LOGGER.log(Level.INFO, "Iniciando conversão de {0} imagem(ns) para PDF: {1}",
                new Object[]{imageFiles.size(), outputFile.getAbsolutePath()});

        try (PDDocument document = new PDDocument()) {
            int pageNumber = 1;
            int total = imageFiles.size();

            for (File imageFile : imageFiles) {
                // Verifica cancelamento
                if (Thread.currentThread().isInterrupted()) {
                    throw new IOException("Conversão cancelada pelo usuário.");
                }
                // Valida extensão do arquivo
                String extension = getExtension(imageFile.getName());
                if (!isSupportedExtension(extension)) {
                    throw new IllegalArgumentException(
                            "Formato de imagem não suportado: " + extension +
                            " no arquivo '" + imageFile.getName() +
                            "'. Formatos aceitos: JPG, JPEG, PNG.");
                }

                LOGGER.log(Level.FINE, "Processando imagem {0}/{1}: {2}",
                        new Object[]{pageNumber, imageFiles.size(), imageFile.getName()});

                // Carrega a imagem
                BufferedImage bufferedImage = ImageIO.read(imageFile);
                if (bufferedImage == null) {
                    throw new IOException(
                            "Não foi possível ler a imagem '" + imageFile.getName() +
                            "'. O arquivo pode estar corrompido.");
                }

                // Adiciona a página com a imagem
                addImagePage(document, bufferedImage, extension);

                // Notifica progresso
                if (progressCallback != null) {
                    progressCallback.accept(pageNumber, total);
                }

                pageNumber++;
            }

            // Salva o documento PDF
            document.save(outputFile);
        }

        LOGGER.log(Level.INFO, "PDF multipágina gerado com sucesso: {0} ({1} página(s))",
                new Object[]{outputFile.getAbsolutePath(), imageFiles.size()});
    }

    /**
     * Adiciona uma página ao documento PDF contendo a imagem redimensionada e centralizada.
     *
     * @param document      documento PDF
     * @param bufferedImage imagem a ser inserida
     * @param extension     extensão original da imagem (para escolher compressão)
     * @throws IOException se ocorrer erro ao criar a página
     */
    private void addImagePage(PDDocument document, BufferedImage bufferedImage, String extension)
            throws IOException {

        int imgWidth = bufferedImage.getWidth();
        int imgHeight = bufferedImage.getHeight();

        // Calcula a área útil dentro da página A4 (respeitando as margens)
        float usableWidth = A4_WIDTH - 2 * MARGIN;
        float usableHeight = A4_HEIGHT - 2 * MARGIN;

        // Calcula a escala para que a imagem caiba na área útil mantendo a proporção
        float scale = Math.min(usableWidth / imgWidth, usableHeight / imgHeight);

        float scaledWidth = imgWidth * scale;
        float scaledHeight = imgHeight * scale;

        // Calcula a posição para centralizar a imagem na página
        float x = MARGIN + (usableWidth - scaledWidth) / 2;
        float y = MARGIN + (usableHeight - scaledHeight) / 2;

        // Cria a página A4
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        // Converte BufferedImage para PDImageXObject
        PDImageXObject pdImage;
        if ("png".equalsIgnoreCase(extension)) {
            pdImage = LosslessFactory.createFromImage(document, bufferedImage);
        } else {
            pdImage = org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory
                    .createFromImage(document, bufferedImage);
        }

        // Desenha a imagem na página na posição calculada
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.drawImage(pdImage, x, y, scaledWidth, scaledHeight);
        }
    }

    /**
     * Verifica se a extensão do arquivo está entre as suportadas.
     *
     * @param extension extensão do arquivo (sem ponto)
     * @return true se for um formato suportado
     */
    private boolean isSupportedExtension(String extension) {
        for (String supported : SUPPORTED_EXTENSIONS) {
            if (supported.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extrai a extensão de um nome de arquivo.
     *
     * @param fileName nome do arquivo
     * @return extensão em minúsculas, ou string vazia se não houver extensão
     */
    private String getExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1 || lastDot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDot + 1).toLowerCase();
    }
}