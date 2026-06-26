package com.documentconverter.android.util

import android.content.Context

class AppSettings(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    var ocrEnabled: Boolean
        get() = preferences.getBoolean(KEY_OCR_ENABLED, true)
        set(value) = preferences.edit().putBoolean(KEY_OCR_ENABLED, value).apply()

    var ocrDpi: Int
        get() = preferences.getInt(KEY_OCR_DPI, DEFAULT_OCR_DPI)
        set(value) = preferences.edit().putInt(KEY_OCR_DPI, value.coerceIn(150, 300)).apply()

    companion object {
        private const val PREFERENCES_NAME = "document_converter_settings"
        private const val KEY_OCR_ENABLED = "ocr_enabled"
        private const val KEY_OCR_DPI = "ocr_dpi"
        const val DEFAULT_OCR_DPI = 200
    }
}
