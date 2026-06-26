package com.documentconverter.core.converters;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Conversor DOCX para PDF usando LibreOffice em modo headless.
 */
public class DocxToPdfConverter {

    private static final Logger LOGGER = Logger.getLogger(DocxToPdfConverter.class.getName());
    private static final long TIMEOUT_SECONDS = 120;

    public void convert(File inputFile, File outputFile) throws IOException, InterruptedException {
        validateInput(inputFile);
        validateOutput(outputFile);

        File soffice = findLibreOfficeExecutable();
        File outputDir = outputFile.getParentFile();
        if (outputDir == null) {
            outputDir = new File(".");
        }

        String expectedName = replaceExtension(inputFile.getName(), ".pdf");
        File generatedFile = new File(outputDir, expectedName);

        Instant start = Instant.now();
        LOGGER.log(Level.INFO, "Iniciando DOCX → PDF | origem: {0} | destino: {1}",
                new Object[]{inputFile.getAbsolutePath(), outputFile.getAbsolutePath()});

        List<String> command = List.of(
                soffice.getAbsolutePath(),
                "--headless",
                "--nologo",
                "--nofirststartwizard",
                "--convert-to",
                "pdf",
                "--outdir",
                outputDir.getAbsolutePath(),
                inputFile.getAbsolutePath());

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        String output = readProcessOutput(process);

        boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new IOException("Tempo limite excedido ao converter DOCX para PDF.");
        }

        if (process.exitValue() != 0) {
            throw new IOException("LibreOffice falhou ao converter o DOCX. Saída: " + output);
        }

        if (!generatedFile.exists()) {
            throw new IOException("PDF não foi gerado. O DOCX pode estar corrompido ou bloqueado.");
        }

        if (!generatedFile.getCanonicalFile().equals(outputFile.getCanonicalFile())) {
            if (outputFile.exists() && !outputFile.delete()) {
                throw new IOException("Não foi possível sobrescrever o PDF de destino: " + outputFile.getAbsolutePath());
            }
            if (!generatedFile.renameTo(outputFile)) {
                throw new IOException("Não foi possível mover o PDF gerado para: " + outputFile.getAbsolutePath());
            }
        }

        long elapsedMs = Duration.between(start, Instant.now()).toMillis();
        LOGGER.log(Level.INFO, "DOCX → PDF concluído em {0} ms | origem: {1} | destino: {2}",
                new Object[]{elapsedMs, inputFile.getAbsolutePath(), outputFile.getAbsolutePath()});
    }

    private void validateInput(File inputFile) throws IOException {
        if (inputFile == null) {
            throw new IllegalArgumentException("Nenhum arquivo DOCX selecionado.");
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
        if (!inputFile.getName().toLowerCase(Locale.ROOT).endsWith(".docx")) {
            throw new IllegalArgumentException("Formato inválido. Selecione um arquivo .docx.");
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

    private File findLibreOfficeExecutable() throws IOException {
        List<File> candidates = new ArrayList<>();
        addCandidate(candidates, System.getProperty("documentconverter.libreoffice.path"));
        addCandidate(candidates, System.getenv("LIBREOFFICE_PATH"));

        String libreOfficeHome = System.getenv("LIBREOFFICE_HOME");
        if (libreOfficeHome != null && !libreOfficeHome.isBlank()) {
            candidates.add(new File(libreOfficeHome, "program/soffice.exe"));
            candidates.add(new File(libreOfficeHome, "soffice.exe"));
        }

        candidates.add(new File("C:/Program Files/LibreOffice/program/soffice.exe"));
        candidates.add(new File("C:/Program Files (x86)/LibreOffice/program/soffice.exe"));
        candidates.add(new File(System.getProperty("user.home"), "AppData/Local/Programs/LibreOffice/program/soffice.exe"));
        addBundledLibreOfficeCandidates(candidates);
        candidates.addAll(findSofficeOnPath());

        for (File candidate : candidates) {
            if (candidate.exists() && candidate.isFile()) {
                return candidate;
            }
        }

        throw new IOException("LibreOffice não encontrado. Instale o LibreOffice ou configure o caminho do soffice.exe.");
    }

    private void addCandidate(List<File> candidates, String path) {
        if (path == null || path.isBlank()) {
            return;
        }

        File file = new File(path.trim());
        if (file.isDirectory()) {
            candidates.add(new File(file, "program/soffice.exe"));
            candidates.add(new File(file, "soffice.exe"));
        } else {
            candidates.add(file);
        }
    }

    private List<File> findSofficeOnPath() {
        String path = System.getenv("PATH");
        if (path == null || path.isBlank()) {
            return List.of();
        }

        return Arrays.stream(path.split(File.pathSeparator))
                .filter(entry -> entry != null && !entry.isBlank())
                .map(entry -> new File(entry, "soffice.exe"))
                .toList();
    }

    private void addBundledLibreOfficeCandidates(List<File> candidates) {
        List<File> baseDirs = new ArrayList<>();
        baseDirs.add(new File(System.getProperty("user.dir")));

        try {
            File codeLocation = new File(DocxToPdfConverter.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());
            File codeDir = codeLocation.isFile() ? codeLocation.getParentFile() : codeLocation;
            if (codeDir != null) {
                baseDirs.add(codeDir);
                File parent = codeDir.getParentFile();
                if (parent != null) {
                    baseDirs.add(parent);
                }
            }
        } catch (URISyntaxException | SecurityException e) {
            LOGGER.log(Level.FINE, "Não foi possível detectar diretório da aplicação para LibreOffice embutido.", e);
        }

        for (File baseDir : baseDirs) {
            candidates.add(new File(baseDir, "libreoffice/program/soffice.exe"));
            candidates.add(new File(baseDir, "LibreOffice/program/soffice.exe"));
            candidates.add(new File(baseDir, "program/soffice.exe"));
        }
    }

    private String readProcessOutput(Process process) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
        }
        return output.toString().trim();
    }

    private String replaceExtension(String fileName, String extension) {
        int dot = fileName.lastIndexOf('.');
        if (dot == -1) {
            return fileName + extension;
        }
        return fileName.substring(0, dot) + extension;
    }
}
