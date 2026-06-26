package com.documentconverter.core.converters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;

import com.documentconverter.core.models.PdfToDocxResult;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.Test;

public class PdfToDocxConverterTest {

    @Test
    public void convertExtractsTextToDocx() throws Exception {
        File tempDir = Files.createTempDirectory("pdf-to-docx-test").toFile();
        File pdf = new File(tempDir, "input.pdf");
        File docx = new File(tempDir, "output.docx");

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                content.newLineAtOffset(80, 700);
                content.showText("Documento de teste para DOCX");
                content.endText();
            }
            document.save(pdf);
        }

        PdfToDocxResult result = new PdfToDocxConverter().convert(pdf, docx);

        assertTrue(docx.exists());
        assertEquals(1, result.pageCount());
        assertTrue(result.textCharacters() > 0);
    }
}
