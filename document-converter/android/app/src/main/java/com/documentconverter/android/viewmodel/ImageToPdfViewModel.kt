package com.documentconverter.android.viewmodel

import android.content.Context
import android.net.Uri
import com.documentconverter.android.converter.ImageToPdfConverter
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

class ImageToPdfViewModel(context: Context) {
    private val appContext = context.applicationContext
    private val converter = ImageToPdfConverter(appContext)
    private val nameResolver = DocumentNameResolver(appContext)
    private val logger = ConversionLogger(appContext)

    var selectedImages by mutableStateOf<List<SelectedDocument>>(emptyList())
        private set

    var state by mutableStateOf(ConversionState.idle("Selecione imagens para criar um PDF."))
        private set

    var completedConversion by mutableStateOf<CompletedConversion?>(null)
        private set

    fun selectImages(uris: List<Uri>) {
        selectedImages = uris.map { uri ->
            SelectedDocument(uri = uri, displayName = nameResolver.displayName(uri))
        }
        completedConversion = null
        state = if (selectedImages.isEmpty()) {
            ConversionState.idle("Nenhuma imagem selecionada.")
        } else {
            ConversionState.idle("${selectedImages.size} imagem(ns) selecionada(s).")
        }
    }

    fun convert(outputPdfUri: Uri, scope: CoroutineScope) {
        val imageUris = selectedImages.map { it.uri }
        if (imageUris.isEmpty()) {
            state = ConversionState.error("Selecione uma ou mais imagens antes de converter.")
            return
        }

        scope.launch {
            state = ConversionState.running("Criando PDF...")
            completedConversion = null
            logger.conversionStarted("IMAGE_TO_PDF", imageUris.size)
            val result = withContext(Dispatchers.IO) {
                converter.convert(imageUris, outputPdfUri) { currentImage, totalImages ->
                    scope.launch {
                        state = ConversionState.running(
                            message = "Criando PDF...",
                            progressText = "Imagem $currentImage de $totalImages"
                        )
                    }
                }
            }
            state = if (result.success) {
                logger.conversionCompleted(
                    operation = "IMAGE_TO_PDF",
                    elapsedMs = result.elapsedMs ?: 0,
                    outputCount = result.outputUris.size
                )
                completedConversion = CompletedConversion(
                    title = "Imagem para PDF",
                    message = result.details,
                    outputUris = result.outputUris,
                    mimeType = "application/pdf"
                )
                ConversionState.success(result.message, result.elapsedMs ?: 0, result.details)
            } else {
                logger.conversionFailed("IMAGE_TO_PDF", result.message, result.details)
                ConversionState.error(result.message, result.details)
            }
        }
    }

    fun clear() {
        selectedImages = emptyList()
        completedConversion = null
        state = ConversionState.idle("Selecione imagens para criar um PDF.")
    }
}
