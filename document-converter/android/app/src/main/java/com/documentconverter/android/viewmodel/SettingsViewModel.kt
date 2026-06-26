package com.documentconverter.android.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.documentconverter.android.util.AppSettings

class SettingsViewModel(context: Context) {
    private val settings = AppSettings(context.applicationContext)

    var ocrEnabled by mutableStateOf(settings.ocrEnabled)
        private set

    var ocrDpi by mutableIntStateOf(settings.ocrDpi)
        private set

    fun updateOcrEnabled(enabled: Boolean) {
        ocrEnabled = enabled
        settings.ocrEnabled = enabled
    }

    fun updateOcrDpi(dpi: Int) {
        val normalized = dpi.coerceIn(MIN_OCR_DPI, MAX_OCR_DPI)
        ocrDpi = normalized
        settings.ocrDpi = normalized
    }

    companion object {
        const val MIN_OCR_DPI = 150
        const val MAX_OCR_DPI = 300
    }
}
