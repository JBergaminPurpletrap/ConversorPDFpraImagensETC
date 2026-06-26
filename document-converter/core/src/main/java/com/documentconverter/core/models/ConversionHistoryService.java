package com.documentconverter.core.models;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Serviço de persistência local do histórico de conversões em JSON.
 */
public class ConversionHistoryService {

    public static final String OPERATION_IMAGE_TO_PDF = "IMAGE_TO_PDF";
    public static final String OPERATION_PDF_TO_IMAGE = "PDF_TO_IMAGE";
    public static final String OPERATION_DOCX_TO_PDF = "DOCX_TO_PDF";
    public static final String OPERATION_PDF_TO_DOCX = "PDF_TO_DOCX";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_CANCELED = "CANCELED";

    private static final Logger LOGGER = Logger.getLogger(ConversionHistoryService.class.getName());
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final Pattern OBJECT_PATTERN = Pattern.compile("\\{([^{}]*)}");
    private static final Pattern FIELD_PATTERN = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"");

    private final File historyFile;

    public ConversionHistoryService() {
        File historyDir = new File("history");
        this.historyFile = new File(historyDir, "conversion-history.json");
    }

    public void register(String operation, String source, String destination, String status) {
        ConversionHistory item = new ConversionHistory(
                LocalDate.now().toString(),
                LocalTime.now().format(TIME_FORMAT),
                operation,
                source,
                destination,
                status);
        save(item);
    }

    public List<ConversionHistory> findAll() {
        if (!historyFile.exists()) {
            return new ArrayList<>();
        }

        try {
            String json = Files.readString(historyFile.toPath(), StandardCharsets.UTF_8);
            return parseHistory(json);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Erro ao ler histórico de conversões.", e);
            return new ArrayList<>();
        }
    }

    public List<ConversionHistory> findLatestFirst() {
        List<ConversionHistory> items = findAll();
        Collections.reverse(items);
        return items;
    }

    private void save(ConversionHistory item) {
        List<ConversionHistory> items = findAll();
        items.add(item);

        File parent = historyFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            LOGGER.warning("Não foi possível criar pasta de histórico: " + parent.getAbsolutePath());
            return;
        }

        try {
            Files.writeString(historyFile.toPath(), toJson(items), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Erro ao salvar histórico de conversões.", e);
        }
    }

    private List<ConversionHistory> parseHistory(String json) {
        List<ConversionHistory> items = new ArrayList<>();
        Matcher objectMatcher = OBJECT_PATTERN.matcher(json);

        while (objectMatcher.find()) {
            String objectBody = objectMatcher.group(1);
            ConversionHistory item = new ConversionHistory();
            Matcher fieldMatcher = FIELD_PATTERN.matcher(objectBody);

            while (fieldMatcher.find()) {
                String field = fieldMatcher.group(1);
                String value = unescape(fieldMatcher.group(2));
                applyField(item, field, value);
            }

            if (item.getDate() != null && item.getOperation() != null) {
                items.add(item);
            }
        }

        return items;
    }

    private void applyField(ConversionHistory item, String field, String value) {
        switch (field) {
            case "date" -> item.setDate(value);
            case "time" -> item.setTime(value);
            case "operation" -> item.setOperation(value);
            case "source" -> item.setSource(value);
            case "destination" -> item.setDestination(value);
            case "status" -> item.setStatus(value);
            default -> {
            }
        }
    }

    private String toJson(List<ConversionHistory> items) {
        StringBuilder json = new StringBuilder();
        json.append("[\n");
        for (int i = 0; i < items.size(); i++) {
            ConversionHistory item = items.get(i);
            json.append("  {\n");
            appendField(json, "date", item.getDate(), true);
            appendField(json, "time", item.getTime(), true);
            appendField(json, "operation", item.getOperation(), true);
            appendField(json, "source", item.getSource(), true);
            appendField(json, "destination", item.getDestination(), true);
            appendField(json, "status", item.getStatus(), false);
            json.append("  }");
            if (i < items.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("]\n");
        return json.toString();
    }

    private void appendField(StringBuilder json, String field, String value, boolean comma) {
        json.append("    \"")
                .append(field)
                .append("\":\"")
                .append(escape(value))
                .append("\"");
        if (comma) {
            json.append(",");
        }
        json.append("\n");
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    private String unescape(String value) {
        StringBuilder result = new StringBuilder();
        boolean escaping = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (escaping) {
                switch (c) {
                    case 'n' -> result.append('\n');
                    case 'r' -> result.append('\r');
                    case '"' -> result.append('"');
                    case '\\' -> result.append('\\');
                    default -> result.append(c);
                }
                escaping = false;
            } else if (c == '\\') {
                escaping = true;
            } else {
                result.append(c);
            }
        }
        if (escaping) {
            result.append('\\');
        }
        return result.toString();
    }
}
