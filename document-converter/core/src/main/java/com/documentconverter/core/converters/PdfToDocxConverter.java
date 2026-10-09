package com.documentconverter.core.converters;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import com.documentconverter.core.models.PdfToDocxResult;

/**
 * Conversor inicial de PDF para DOCX.
 * Extrai texto com PDFBox e gera DOCX com Apache POI.
 */
public class PdfToDocxConverter {

    private static final Logger LOGGER = Logger.getLogger(PdfToDocxConverter.class.getName());
    private static final int MAX_IMAGE_WIDTH_EMU = Units.toEMU(420);

    public PdfToDocxResult convert(File inputFile, File outputFile) throws IOException {
        validateInput(inputFile);
        validateOutput(outputFile);

        Instant start = Instant.now();
        LOGGER.log(Level.INFO, "Iniciando PDF → DOCX | origem: {0} | destino: {1}",
                new Object[]{inputFile.getAbsolutePath(), outputFile.getAbsolutePath()});

        try (PDDocument pdf = Loader.loadPDF(inputFile);
             XWPFDocument docx = new XWPFDocument()) {

            if (pdf.isEncrypted()) {
                throw new IOException("PDF protegido por senha.");
            }

            int pageCount = pdf.getNumberOfPages();
            if (pageCount == 0) {
                throw new IOException("PDF vazio.");
            }

            PDFTextStripper textStripper = new PDFTextStripper();
            textStripper.setSortByPosition(true);

            int totalCharacters = 0;
            int extractedImages = 0;

            for (int pageNumber = 1; pageNumber <= pageCount; pageNumber++) {
                textStripper.setStartPage(pageNumber);
                textStripper.setEndPage(pageNumber);
                String pageText = normalizeText(textStripper.getText(pdf));
                totalCharacters += pageText.length();

                addPageText(docx, pageText, pageNumber);
                extractedImages += addPageImages(docx, pdf.getPage(pageNumber - 1), pageNumber);

                if (pageNumber < pageCount) {
                    docx.createParagraph().createRun().addBreak(org.apache.poi.xwpf.usermodel.BreakType.PAGE);
                }
            }

            if (totalCharacters == 0 && extractedImages == 0) {
                throw new IOException("Nenhum texto ou imagem foi extraído. O PDF pode ser escaneado ou corrompido.");
            }

            try (FileOutputStream output = new FileOutputStream(outputFile)) {
                docx.write(output);
            }

            long elapsedMs = Duration.between(start, Instant.now()).toMillis();
            LOGGER.log(Level.INFO,
                    "PDF → DOCX concluído em {0} ms | páginas: {1} | caracteres: {2} | imagens: {3}",
                    new Object[]{elapsedMs, pageCount, totalCharacters, extractedImages});

            return new PdfToDocxResult(pageCount, totalCharacters, extractedImages, elapsedMs);
        } catch (IOException e) {
            String message = e.getMessage();
            if (message != null && message.toLowerCase(Locale.ROOT).contains("password")) {
                throw new IOException("PDF protegido por senha.", e);
            }
            throw e;
        }
    }

    private void validateInput(File inputFile) throws IOException {
        if (inputFile == null) {
            throw new IllegalArgumentException("Nenhum arquivo PDF selecionado.");
        }
        if (!inputFile.exists()) {
            throw new IllegalArgumentException("Arquivo inexistente: " + inputFile.getAbsolutePath());
        }
        if (!inputFile.isFile()) {
            throw new IllegalArgumentException("O caminho selecionado não é um arquivo.");
        }
        if (!inputFile.canRead()) {
            throw new IOException("Arquivo bloqueado ou sem permissão de leitura: " + inputFile.getAbsolutePath());
        }
        if (!inputFile.getName().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new IllegalArgumentException("Formato inválido. Selecione um arquivo .pdf.");
        }
    }

    private void validateOutput(File outputFile) throws IOException {
        if (outputFile == null) {
            throw new IllegalArgumentException("Nenhum arquivo de destino informado.");
        }
        File parent = outputFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Não foi possível criar o diretório de saída: " + parent.getAbsolutePath());
        }
        if (parent != null && !parent.canWrite()) {
            throw new IOException("Falha de escrita no diretório: " + parent.getAbsolutePath());
        }
        if (outputFile.exists() && !outputFile.canWrite()) {
            throw new IOException("Arquivo de destino bloqueado ou sem permissão de escrita: " + outputFile.getAbsolutePath());
        }
    }

    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\r\n", "\n").replace('\r', '\n').trim();
    }

    private void addPageText(XWPFDocument docx, String pageText, int pageNumber) {
        if (pageText.isBlank()) {
            XWPFParagraph paragraph = docx.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setItalic(true);
            run.setColor("7F8C8D");
            run.setText("Página " + pageNumber + ": nenhum texto extraído.");
            return;
        }

        String[] blocks = pageText.split("\\n\\s*\\n");
        for (String block : blocks) {
            String cleaned = block.replaceAll("\\n+", " ").replaceAll("\\s+", " ").trim();
            if (cleaned.isEmpty()) {
                continue;
            }
            addParagraph(docx, cleaned);
        }
    }

    private void addParagraph(XWPFDocument docx, String text) {
        XWPFParagraph paragraph = docx.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(text);

        if (looksLikeTitle(text)) {
            paragraph.setAlignment(ParagraphAlignment.LEFT);
            run.setBold(true);
            run.setFontSize(16);
        } else {
            paragraph.setAlignment(ParagraphAlignment.BOTH);
            run.setFontSize(11);
        }
    }

    private boolean looksLikeTitle(String text) {
        if (text.length() > 90) {
            return false;
        }
        if (text.endsWith(".") || text.endsWith(",") || text.endsWith(";")) {
            return false;
        }
        long letters = text.chars().filter(Character::isLetter).count();
        if (letters == 0) {
            return false;
        }
        long uppercase = text.chars().filter(Character::isUpperCase).count();
        return uppercase >= letters * 0.65 || text.matches("^\\d+(\\.\\d+)*\\s+.+");
    }

    private int addPageImages(XWPFDocument docx, PDPage page, int pageNumber) throws IOException {
        List<BufferedImage> images = new ArrayList<>();
        collectImages(page.getResources(), images);

        int added = 0;
        for (BufferedImage image : images) {
            try {
                addImage(docx, image, "page_" + pageNumber + "_image_" + (added + 1) + ".png");
                added++;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Imagem ignorada ao converter página " + pageNumber + ".", e);
            }
        }
        return added;
    }

    private void collectImages(PDResources resources, List<BufferedImage> images) throws IOException {
        if (resources == null) {
            return;
        }
        for (COSName name : resources.getXObjectNames()) {
            PDXObject object = resources.getXObject(name);
            if (object instanceof PDImageXObject imageObject) {
                images.add(imageObject.getImage());
            } else if (object instanceof PDFormXObject formObject) {
                collectImages(formObject.getResources(), images);
            }
        }
    }

    private void addImage(XWPFDocument docx, BufferedImage image, String fileName) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ImageIO.write(image, "png", buffer);

        int widthEmu = Units.toEMU(image.getWidth());
        int heightEmu = Units.toEMU(image.getHeight());
        if (widthEmu > MAX_IMAGE_WIDTH_EMU) {
            double scale = (double) MAX_IMAGE_WIDTH_EMU / widthEmu;
            widthEmu = MAX_IMAGE_WIDTH_EMU;
            heightEmu = (int) Math.round(heightEmu * scale);
        }

        XWPFParagraph paragraph = docx.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = paragraph.createRun();
        try (ByteArrayInputStream input = new ByteArrayInputStream(buffer.toByteArray())) {
            run.addPicture(input, XWPFDocument.PICTURE_TYPE_PNG, fileName, widthEmu, heightEmu);
        }
    }

}
