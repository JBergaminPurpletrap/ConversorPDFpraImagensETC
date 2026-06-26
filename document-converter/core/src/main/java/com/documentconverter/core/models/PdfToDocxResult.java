package com.documentconverter.core.models;

public record PdfToDocxResult(int pageCount, int textCharacters, int imageCount, long elapsedMs) {
}
