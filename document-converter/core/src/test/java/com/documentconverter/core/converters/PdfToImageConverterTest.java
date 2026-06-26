package com.documentconverter.core.converters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.Test;

public class PdfToImageConverterTest {

    @Test
    public void convertCreatesImageForEachPdfPage() throws Exception {
        File tempDir = Files.createTempDirectory("pdf-to-image-test").toFile();
        File pdf = new File(tempDir, "input.pdf");
        File outputDir = new File(tempDir, "images");

        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage());
            document.addPage(new PDPage());
            document.save(pdf);
        }

        List<File> images = new PdfToImageConverter().convert(pdf, outputDir, "png", 150);

        assertEquals(2, images.size());
        assertTrue(new File(outputDir, "pagina_1.png").exists());
        assertTrue(new File(outputDir, "pagina_2.png").exists());
    }
}
