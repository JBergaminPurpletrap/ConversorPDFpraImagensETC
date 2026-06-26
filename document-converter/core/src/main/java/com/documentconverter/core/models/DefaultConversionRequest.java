package com.documentconverter.core.models;

import java.io.File;
import java.util.Collections;
import java.util.Map;

public class DefaultConversionRequest implements ConversionRequest {

    private final String operation;
    private final File source;
    private final File destination;
    private final Map<String, Object> options;

    public DefaultConversionRequest(String operation, File source, File destination, Map<String, Object> options) {
        this.operation = operation;
        this.source = source;
        this.destination = destination;
        this.options = options == null ? Collections.emptyMap() : Map.copyOf(options);
    }

    @Override
    public String getOperation() {
        return operation;
    }

    @Override
    public File getSource() {
        return source;
    }

    @Override
    public File getDestination() {
        return destination;
    }

    @Override
    public Map<String, Object> getOptions() {
        return options;
    }
}
