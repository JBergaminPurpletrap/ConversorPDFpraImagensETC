package com.documentconverter.android.converter

import android.net.Uri

data class ConversionResult(
    val success: Boolean,
    val message: String,
    val elapsedMs: Long? = null,
    val details: String = "",
    val outputUris: List<Uri> = emptyList()
) {
    companion object {
        fun success(
            message: String,
            elapsedMs: Long,
            details: String = "",
            outputUris: List<Uri> = emptyList()
        ) = ConversionResult(true, message, elapsedMs, details, outputUris)

        fun failure(message: String, details: String = "") =
            ConversionResult(false, message, null, details)
    }
}
