package com.documentconverter.core.models;

import java.io.File;

/**
 * Resultado padronizado de uma conversão.
 */
public interface ConversionResult {

    String getOperation();

    File getSource();

    File getDestination();

    String getStatus();

    long getElapsedMs();
}
