package com.documentconverter.android.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.documentconverter.android.converter.PdfToDocxConverter
import com.documentconverter.android.converter.PdfToDocxConverter.PdfInspectionResult
import com.documentconverter.android.ui.model.CompletedConversion
import com.documentconverter.android.ui.model.SelectedDocument
import com.documentconverter.android.ui.state.ConversionState
import com.documentconverter.android.util.ConversionLogger
import com.documentconverter.android.util.DocumentNameResolver
import com.documentconverter.android.util.AppSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PdfToDocxViewModel(context: Context) {
    private val appContext = context.applicationContext
    private val converter = PdfToDocxConverter(appContext)
    private val nameResolver = DocumentNameResolver(appContext)
    private val logger = ConversionLogger(appContext)
    private val settings = AppSettings(appContext)

    var selectedPdf by mutableStateOf<SelectedDocument?>(null)
        private set

    var pageCount by mutableStateOf<Int?>(null)
        private set

    var isProtected by mutableStateOf(false)
        private set

    var showPasswordDialog by mutableStateOf(false)
        private set

    var pdfPassword by mutableStateOf("")
        private set

    var state by mutableStateOf(ConversionState.idle("Selecione um PDF para converter em DOCX."))
        private set

    var completedConversion by mutableStateOf<CompletedConversion?>(null)
        private set

    fun updatePassword(password: String) {
        pdfPassword = password
    }

    fun dismissPasswordDialog() {
        showPasswordDialog = false
    }

    fun setSelectedPdf(uri: Uri?, scope: CoroutineScope) {
        if (uri == null) {
            clear()
            state = ConversionState.idle("Selecao de PDF cancelada.")
            return
        }

        selectedPdf = SelectedDocument(uri = uri, displayName = nameResolver.displayName(uri))
        pageCount = null
        isProtected = false
        showPasswordDialog = false
        pdfPassword = ""
        completedConversion = null
        state = ConversionState.running("Verificando PDF...")

        scope.launch {
            val inspection = withContext(Dispatchers.IO) {
                converter.inspect(uri)
            }
            when (inspection) {
                is PdfInspectionResult.Readable -> {
                    pageCount = inspection.pageCount
                    state = ConversionState.idle(
                        "PDF selecionado com ${inspection.pageCount} pagina(s)."
                    )
                }

                PdfInspectionResult.PasswordProtected -> {
                    isProtected = true
                    showPasswordDialog = true
                    state = ConversionState.idle(PdfToDocxConverter.PROTECTED_PDF_MESSAGE)
                }

                is PdfInspectionResult.Error -> {
                    selectedPdf = null
                    state = ConversionState.error(
                        "Nao foi possivel abrir o PDF selecionado.",
                        inspection.message
                    )
                }
            }
        }
    }

    fun convert(outputDocxUri: Uri, scope: CoroutineScope) {
        val pdf = selectedPdf
        if (pdf == null) {
            state = ConversionState.error("Selecione um PDF antes de converter.")
            return
        }
        if (isProtected && pdfPassword.isBlank()) {
            showPasswordDialog = true
            state = ConversionState.error("Informe a senha do PDF antes de converter.")
            return
        }

        scope.launch {
            state = ConversionState.running("Extraindo texto do PDF e aplicando OCR quando necessario...")
            completedConversion = null
            logger.conversionStarted("PDF_TO_DOCX_ANDROID", pageCount ?: 1)
            val result = withContext(Dispatchers.IO) {
                converter.convert(
                    pdfUri = pdf.uri,
                    outputDocxUri = outputDocxUri,
                    password = pdfPassword.takeIf { isProtected },
                    ocrEnabled = settings.ocrEnabled,
                    ocrRenderDpi = settings.ocrDpi
                )
            }

            state = if (result.success) {
                logger.conversionCompleted(
                    operation = "PDF_TO_DOCX_ANDROID",
                    elapsedMs = result.elapsedMs ?: 0,
                    outputCount = result.outputUris.size
                )
                completedConversion = CompletedConversion(
                    title = "PDF para DOCX",
                    message = result.details,
                    outputUris = result.outputUris,
                    mimeType = DOCX_MIME_TYPE
                )
                ConversionState.success(result.message, result.elapsedMs ?: 0, result.details)
            } else {
                logger.conversionFailed("PDF_TO_DOCX_ANDROID", result.message, result.details)
                if (isProtected) {
                    showPasswordDialog = true
                }
                ConversionState.error(result.message, result.details)
            }
        }
    }

    fun clear() {
        selectedPdf = null
        pageCount = null
        isProtected = false
        showPasswordDialog = false
        pdfPassword = ""
        completedConversion = null
        state = ConversionState.idle("Selecione um PDF para converter em DOCX.")
    }

    companion object {
        const val DOCX_MIME_TYPE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    }
}
