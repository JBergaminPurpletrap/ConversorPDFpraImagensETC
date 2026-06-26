package com.documentconverter.android.viewmodel

import android.content.Context
import android.net.Uri
import com.documentconverter.android.converter.PdfToImageConverter
import com.documentconverter.android.converter.PdfToImageConverter.ImageOutputFormat
import com.documentconverter.android.converter.PdfToImageConverter.PdfInspectionResult
import com.documentconverter.android.ui.model.CompletedConversion
import com.documentconverter.android.ui.state.ConversionState
import com.documentconverter.android.ui.model.SelectedDocument
import com.documentconverter.android.util.ConversionLogger
import com.documentconverter.android.util.DocumentNameResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class PdfToImageViewModel(context: Context) {
    private val appContext = context.applicationContext
    private val converter = PdfToImageConverter(appContext)
    private val nameResolver = DocumentNameResolver(appContext)
    private val logger = ConversionLogger(appContext)

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

    var state by mutableStateOf(ConversionState.idle("Selecione um PDF para converter."))
        private set

    var completedConversion by mutableStateOf<CompletedConversion?>(null)
        private set

    var selectedOutputFormat by mutableStateOf(ImageOutputFormat.JPEG)
        private set

    fun selectOutputFormat(format: ImageOutputFormat) {
        selectedOutputFormat = format
        completedConversion = null
        state = ConversionState.idle(
            "Formato selecionado: ${format.label}. Escolha a pasta de destino para converter."
        )
    }

    fun updatePassword(password: String) {
        pdfPassword = password
    }

    fun dismissPasswordDialog() {
        showPasswordDialog = false
    }

    fun setSelectedPdf(uri: Uri?, scope: CoroutineScope) {
        if (uri == null) {
            selectedPdf = null
            pageCount = null
            isProtected = false
            showPasswordDialog = false
            pdfPassword = ""
            completedConversion = null
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
                    state = ConversionState.idle(PdfToImageConverter.PROTECTED_PDF_MESSAGE)
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

    fun convert(outputTreeUri: Uri, scope: CoroutineScope) {
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
            state = ConversionState.running("Convertendo PDF em imagens...")
            completedConversion = null
            logger.conversionStarted("PDF_TO_IMAGE", 1)
            val result = withContext(Dispatchers.IO) {
                converter.convert(
                    pdfUri = pdf.uri,
                    outputTreeUri = outputTreeUri,
                    outputFormat = selectedOutputFormat,
                    password = pdfPassword.takeIf { isProtected }
                ) { currentPage, totalPages ->
                    scope.launch {
                        state = ConversionState.running(
                            message = "Convertendo PDF em imagens...",
                            progressText = "Pagina $currentPage de $totalPages"
                        )
                    }
                }
            }
            state = if (result.success) {
                logger.conversionCompleted(
                    operation = "PDF_TO_IMAGE",
                    elapsedMs = result.elapsedMs ?: 0,
                    outputCount = result.outputUris.size
                )
                completedConversion = CompletedConversion(
                    title = "PDF para Imagem",
                    message = result.details,
                    outputUris = result.outputUris,
                    mimeType = selectedOutputFormat.mimeType
                )
                ConversionState.success(result.message, result.elapsedMs ?: 0, result.details)
            } else {
                logger.conversionFailed("PDF_TO_IMAGE", result.message, result.details)
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
        selectedOutputFormat = ImageOutputFormat.JPEG
        state = ConversionState.idle("Selecione um PDF para converter.")
    }
}
