package com.documentconverter.android.ui.model

import android.net.Uri

data class SelectedDocument(
    val uri: Uri,
    val displayName: String
)
