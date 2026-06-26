package com.documentconverter.core.models;

import java.io.File;

public class DefaultConversionResult implements ConversionResult {

    private final String operation;
    private final File source;
    private final File destination;
    private final String status;
    private final long elapsedMs;

    public DefaultConversionResult(String operation, File source, File destination, String status, long elapsedMs) {
        this.operation = operation;
        this.source = source;
        this.destination = destination;
        this.status = status;
        this.elapsedMs = elapsedMs;
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
    public String getStatus() {
        return status;
    }

    @Override
    public long getElapsedMs() {
        return elapsedMs;
    }
}
