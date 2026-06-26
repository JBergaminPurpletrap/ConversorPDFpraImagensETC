package com.documentconverter.android.converter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log

class ImageToPdfConverter(private val context: Context) {
    private val resolver = context.contentResolver

    fun convert(
        imageUris: List<Uri>,
        outputPdfUri: Uri,
        onProgress: (currentImage: Int, totalImages: Int) -> Unit
    ): ConversionResult {
        val start = System.currentTimeMillis()
        if (imageUris.isEmpty()) {
            return ConversionResult.failure("Nenhuma imagem selecionada.")
        }

        val document = PdfDocument()
        return try {
            imageUris.forEachIndexed { index, imageUri ->
                val currentImage = index + 1
                onProgress(currentImage, imageUris.size)
                Log.i(TAG, "Adicionando imagem $currentImage de ${imageUris.size}: $imageUri")

                val bitmap = decodeImageForPdf(imageUri)
                try {
                    val pageInfo = PdfDocument.PageInfo.Builder(
                        bitmap.width,
                        bitmap.height,
                        currentImage
                    ).create()
                    val page = document.startPage(pageInfo)
                    page.canvas.drawColor(Color.WHITE)
                    page.canvas.drawBitmap(bitmap, 0f, 0f, null)
                    document.finishPage(page)
                } finally {
                    bitmap.recycle()
                }
            }

            resolver.openOutputStream(outputPdfUri, "w").use { output ->
                requireNotNull(output) { "Nao foi possivel abrir o destino PDF." }
                document.writeTo(output)
            }

            ConversionResult.success(
                message = "PDF criado com sucesso.",
                elapsedMs = System.currentTimeMillis() - start,
                details = "${imageUris.size} imagem(ns) em um PDF multipagina.",
                outputUris = listOf(outputPdfUri)
            )
        } catch (error: OutOfMemoryError) {
            Log.e(TAG, "Memoria insuficiente ao criar PDF.", error)
            ConversionResult.failure(
                message = "Imagem grande demais para a memoria disponivel.",
                details = "O app processa uma imagem por vez, mas uma imagem excedeu a memoria."
            )
        } catch (error: Throwable) {
            Log.e(TAG, "Falha em Imagem para PDF.", error)
            ConversionResult.failure(
                message = "Falha ao criar PDF.",
                details = error.message ?: error::class.java.simpleName
            )
        } finally {
            document.close()
        }
    }

    private fun decodeImageForPdf(uri: Uri): Bitmap {
        val bounds = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        resolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Nao foi possivel abrir a imagem." }
            BitmapFactory.decodeStream(input, null, bounds)
        }

        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw IllegalArgumentException("Imagem invalida ou nao suportada.")
        }

        val largestSide = maxOf(bounds.outWidth, bounds.outHeight)
        var sampleSize = 1
        while (largestSide / sampleSize > MAX_RENDER_SIDE) {
            sampleSize *= 2
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return resolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Nao foi possivel reabrir a imagem." }
            BitmapFactory.decodeStream(input, null, options)
                ?: throw IllegalArgumentException("Falha ao decodificar imagem.")
        }
    }

    private companion object {
        private const val TAG = "ImageToPdfConverter"
        private const val MAX_RENDER_SIDE = 2400
    }
}
