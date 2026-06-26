package com.documentconverter.core.services;

import com.documentconverter.core.converters.PdfToImageConverter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Serviço para conversão de PDF em imagens.
 * <p>
 * Orquestra a chamada ao {@link PdfToImageConverter}, registra logs
 * com tempo de execução e expõe callbacks de progresso.
 * </p>
 */
public class PdfToImageService {

    private static final Logger LOGGER = Logger.getLogger(PdfToImageService.class.getName());

    private final PdfToImageConverter converter;

    public PdfToImageService() {
        this.converter = new PdfToImageConverter();
    }

    /**
     * Converte um PDF em imagens, uma por página.
     *
     * @param pdfFile     arquivo PDF de entrada
     * @param outputDir   diretório de saída para as imagens
     * @param imageFormat formato (jpg, jpeg, png)
     * @param dpi         resolução em DPI
     * @return lista de arquivos de imagem gerados
     * @throws IOException se ocorrer erro de conversão
     */
    public List<File> convert(File pdfFile, File outputDir, String imageFormat, int dpi)
            throws IOException {
        return convert(pdfFile, outputDir, imageFormat, dpi, null);
    }

    public List<File> convert(File pdfFile, File outputDir, String imageFormat, int dpi,
                              BiConsumer<Integer, Integer> progressCallback)
            throws IOException {
        return convert(pdfFile, outputDir, imageFormat, dpi, progressCallback, null);
    }

    public List<File> convert(File pdfFile, File outputDir, String imageFormat, int dpi,
                              BiConsumer<Integer, Integer> progressCallback,
                              String password)
            throws IOException {

        long startTime = System.currentTimeMillis();

        LOGGER.log(Level.INFO, "PDF carregado: {0}", pdfFile.getAbsolutePath());

        List<File> result = converter.convert(pdfFile, outputDir, imageFormat, dpi, progressCallback, password);

        long elapsed = System.currentTimeMillis() - startTime;
        LOGGER.log(Level.INFO,
                "Conversão concluída. Páginas: {0} | DPI: {1} | Tempo: {2}ms | Pasta: {3}",
                new Object[]{result.size(), dpi, elapsed, outputDir.getAbsolutePath()});

        return result;
    }
}
