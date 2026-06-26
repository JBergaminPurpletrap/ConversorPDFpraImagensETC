package com.documentconverter.core.converters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Test;

public class ImageToPdfConverterTest {

    @Test
    public void convertCreatesOnePdfPagePerImage() throws Exception {
        File tempDir = Files.createTempDirectory("image-to-pdf-test").toFile();
        File image = new File(tempDir, "sample.png");
        File output = new File(tempDir, "output.pdf");

        BufferedImage bufferedImage = new BufferedImage(80, 60, BufferedImage.TYPE_INT_RGB);
        bufferedImage.getGraphics().setColor(Color.GREEN);
        bufferedImage.getGraphics().fillRect(0, 0, 80, 60);
        ImageIO.write(bufferedImage, "png", image);

        new ImageToPdfConverter().convert(List.of(image), output);

        assertTrue(output.exists());
        try (PDDocument document = Loader.loadPDF(output)) {
            assertEquals(1, document.getNumberOfPages());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertRejectsUnsupportedImageExtension() throws Exception {
        File tempDir = Files.createTempDirectory("image-to-pdf-invalid-test").toFile();
        File invalid = new File(tempDir, "sample.gif");
        Files.writeString(invalid.toPath(), "not an image");

        new ImageToPdfConverter().convert(List.of(invalid), new File(tempDir, "output.pdf"));
    }
}
