package com.documentconverter.android.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.documentconverter.android.converter.DocxToPdfConverter
import com.documentconverter.android.ui.model.CompletedConversion
import com.documentconverter.android.ui.model.SelectedDocument
import com.documentconverter.android.ui.state.ConversionState
import com.documentconverter.android.util.ConversionLogger
import com.documentconverter.android.util.DocumentNameResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DocxToPdfViewModel(context: Context) {
    private val appContext = context.applicationContext
    private val converter = DocxToPdfConverter(appContext)
    private val nameResolver = DocumentNameResolver(appContext)
    private val logger = ConversionLogger(appContext)

    var selectedDocx by mutableStateOf<SelectedDocument?>(null)
        private set

    var state by mutableStateOf(ConversionState.idle("Selecione um DOCX para converter em PDF."))
        private set

    var completedConversion by mutableStateOf<CompletedConversion?>(null)
        private set

    fun setSelectedDocx(uri: Uri?) {
        if (uri == null) {
            return
        }
        selectedDocx = SelectedDocument(uri = uri, displayName = nameResolver.displayName(uri))
        completedConversion = null
        state = ConversionState.idle("DOCX selecionado.")
    }

    fun convert(outputPdfUri: Uri, scope: CoroutineScope) {
        val input = selectedDocx?.uri
        if (input == null) {
            state = ConversionState.error("Selecione um DOCX antes de converter.")
            return
        }

        scope.launch {
            state = ConversionState.running("Criando PDF...")
            completedConversion = null
            logger.conversionStarted("DOCX_TO_PDF_ANDROID", 1)
            val result = withContext(Dispatchers.IO) {
                converter.convert(input, outputPdfUri)
            }
            state = if (result.success) {
                logger.conversionCompleted(
                    operation = "DOCX_TO_PDF_ANDROID",
                    elapsedMs = result.elapsedMs ?: 0,
                    outputCount = result.outputUris.size
                )
                completedConversion = CompletedConversion(
                    title = "DOCX para PDF",
                    message = result.details,
                    outputUris = result.outputUris,
                    mimeType = PDF_MIME_TYPE
                )
                ConversionState.success(result.message, result.elapsedMs ?: 0, result.details)
            } else {
                logger.conversionFailed("DOCX_TO_PDF_ANDROID", result.message, result.details)
                ConversionState.error(result.message, result.details)
            }
        }
    }

    fun clear() {
        selectedDocx = null
        completedConversion = null
        state = ConversionState.idle("Selecione um DOCX para converter em PDF.")
    }

    companion object {
        const val DOCX_MIME_TYPE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        const val PDF_MIME_TYPE = "application/pdf"
    }
}
