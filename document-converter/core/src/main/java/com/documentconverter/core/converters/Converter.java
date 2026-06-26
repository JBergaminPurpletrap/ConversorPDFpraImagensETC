package com.documentconverter.core.converters;

import com.documentconverter.core.models.ConversionRequest;
import com.documentconverter.core.models.ConversionResult;

/**
 * Contrato base para conversores do core.
 */
public interface Converter {

    String getOperation();

    ConversionResult convert(ConversionRequest request) throws Exception;
}
