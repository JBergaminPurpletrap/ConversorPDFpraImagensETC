package com.documentconverter.core.models;

import java.io.File;
import java.util.Map;

/**
 * Entrada padronizada para uma conversão.
 */
public interface ConversionRequest {

    String getOperation();

    File getSource();

    File getDestination();

    Map<String, Object> getOptions();
}
