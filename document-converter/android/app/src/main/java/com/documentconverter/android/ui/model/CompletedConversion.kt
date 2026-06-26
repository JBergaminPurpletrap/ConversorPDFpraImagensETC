package com.documentconverter.android.ui.model

import android.net.Uri

data class CompletedConversion(
    val title: String,
    val message: String,
    val outputUris: List<Uri>,
    val mimeType: String
)
