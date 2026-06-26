package com.documentconverter.androidpoc.poc

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.provider.OpenableColumns
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import org.apache.poi.xwpf.usermodel.XWPFDocument
import kotlin.math.min
import kotlin.math.roundToInt

class AndroidPocConverters(private val context: Context) {
    private companion object {
        private const val TAG = "AndroidPocConverters"
        private const val MAX_RENDER_SIDE = 2400
    }

    private val resolver = context.contentResolver

    fun imagesToPdf(
        imageUris: List<Uri>,
        outputPdfUri: Uri,
        progressCallback: (currentImage: Int, totalImages: Int) -> Unit = { _, _ -> }
    ): PocResult {
        val start = System.currentTimeMillis()
        if (imageUris.isEmpty()) {
            return PocResult.failure("Nenhuma imagem selecionada.")
        }

        val document = PdfDocument()
        var pageNumber = 0
        return try {
            imageUris.forEachIndexed { index, imageUri ->
                val currentImage = index + 1
                progressCallback(currentImage, imageUris.size)
                Log.i(TAG, "Adicionando imagem $currentImage de ${imageUris.size}: $imageUri")

                val bitmap = decodeImageForPdf(imageUri)
                try {
                    pageNumber++
                    val pageInfo = PdfDocument.PageInfo.Builder(
                        bitmap.width,
                        bitmap.height,
                        pageNumber
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

            val elapsed = System.currentTimeMillis() - start
            PocResult.success(
                message = "PDF criado com sucesso com ${imageUris.size} imagem(ns).",
                elapsedMs = elapsed,
                details = "Uma imagem por pagina. Ordem mantida conforme retorno do seletor SAF."
            )
        } catch (error: OutOfMemoryError) {
            Log.e(TAG, "Memoria insuficiente ao criar PDF de imagens.", error)
            PocResult.failure(
                message = "Imagem grande demais para a memoria disponivel.",
                details = "A POC processa uma imagem por vez e limita o bitmap, mas a imagem ainda excedeu a memoria."
            )
        } catch (error: Throwable) {
            Log.e(TAG, "Falha em Imagem -> PDF.", error)
            PocResult.failure(
                message = "Falha ao criar PDF com imagens.",
                details = error.message ?: error::class.java.simpleName
            )
        } finally {
            document.close()
        }
    }

    fun imageToPdf(imageUri: Uri, outputPdfUri: Uri): PocResult {
        return imagesToPdf(listOf(imageUri), outputPdfUri)
    }

    fun inspectPdf(pdfUri: Uri): PdfInspectionResult {
        try {
            val descriptor = resolver.openFileDescriptor(pdfUri, "r")
                ?: return PdfInspectionResult.Error("Nao foi possivel abrir o PDF.")
            descriptor.use { safeDescriptor ->
                PdfRenderer(safeDescriptor).use { renderer ->
                    return PdfInspectionResult.Readable(renderer.pageCount)
                }
            }
        } catch (error: Throwable) {
            Log.w(TAG, "Falha ao inspecionar PDF.", error)
            return if (isPasswordProtectedError(error)) {
                PdfInspectionResult.PasswordProtected
            } else {
                PdfInspectionResult.Error(error.message ?: error::class.java.simpleName)
            }
        }
    }

    fun pdfToImages(
        pdfUri: Uri,
        outputTreeUri: Uri,
        imageFormat: ImageFormat = ImageFormat.PNG,
        progressCallback: (currentPage: Int, totalPages: Int) -> Unit = { _, _ -> }
    ): PocResult {
        val start = System.currentTimeMillis()
        val outputDir = DocumentFile.fromTreeUri(context, outputTreeUri)
            ?: return PocResult.failure("Nao foi possivel abrir a pasta de destino.")
        if (!outputDir.canWrite()) {
            return PocResult.failure("Sem permissao de escrita na pasta selecionada.")
        }

        return try {
            val descriptor = resolver.openFileDescriptor(pdfUri, "r")
                ?: return PocResult.failure("Nao foi possivel abrir o PDF.")

            var generatedCount = 0
            descriptor.usePdfRenderer { renderer ->
                val totalPages = renderer.pageCount
                require(totalPages > 0) { "PDF vazio." }
                Log.i(TAG, "Iniciando PDF -> Imagem. Paginas: $totalPages")

                for (pageIndex in 0 until totalPages) {
                    val currentPage = pageIndex + 1
                    progressCallback(currentPage, totalPages)
                    renderer.openPage(pageIndex).use { page ->
                        val bitmap = createPageBitmap(page)
                        try {
                            bitmap.eraseColor(Color.WHITE)
                            page.render(
                                bitmap,
                                null,
                                null,
                                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                            )
                            val outputFile = createOutputImage(outputDir, currentPage, imageFormat)
                            resolver.openOutputStream(outputFile.uri, "w").use { output ->
                                requireNotNull(output) {
                                    "Nao foi possivel abrir destino da pagina $currentPage."
                                }
                                bitmap.compress(imageFormat.compressFormat, imageFormat.quality, output)
                            }
                            generatedCount++
                            Log.i(TAG, "Pagina $currentPage de $totalPages salva: ${outputFile.name}")
                        } finally {
                            bitmap.recycle()
                        }
                    }
                }
            }

            val elapsed = System.currentTimeMillis() - start
            PocResult.success(
                message = "Conversao concluida: $generatedCount pagina(s) salvas.",
                elapsedMs = elapsed,
                details = "Arquivos gerados como pagina_1.${imageFormat.extension}, pagina_2.${imageFormat.extension}..."
            )
        } catch (error: OutOfMemoryError) {
            Log.e(TAG, "Memoria insuficiente ao renderizar PDF.", error)
            PocResult.failure(
                message = "Arquivo grande demais para a memoria disponivel no emulador/dispositivo.",
                details = "A POC processa uma pagina por vez, mas esta pagina exigiu bitmap grande demais."
            )
        } catch (error: Throwable) {
            Log.e(TAG, "Falha em PDF -> Imagem.", error)
            if (isPasswordProtectedError(error)) {
                PocResult.failure(
                    message = "Este PDF esta protegido por senha. A versao Android ainda nao suporta PDFs protegidos.",
                    details = error.message ?: error::class.java.simpleName
                )
            } else {
                PocResult.failure(
                    message = "Falha ao converter PDF para imagens.",
                    details = error.message ?: error::class.java.simpleName
                )
            }
        }
    }

    fun probeDocx(docxUri: Uri): PocResult {
        val start = System.currentTimeMillis()
        val stats = resolver.openInputStream(docxUri).use { input ->
            requireNotNull(input) { "Nao foi possivel abrir o DOCX." }
            XWPFDocument(input).use { document ->
                val paragraphCount = document.paragraphs.size
                val tableCount = document.tables.size
                val pictureCount = document.allPictures.size
                "Paragrafos: $paragraphCount | Tabelas: $tableCount | Imagens: $pictureCount"
            }
        }

        val elapsed = System.currentTimeMillis() - start
        return PocResult.success(
            message = "DOCX simples aberto com Apache POI.",
            elapsedMs = elapsed,
            details = "$stats. Esta POC nao converte DOCX para PDF."
        )
    }

    fun displayName(uri: Uri): String {
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null).use { cursor ->
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    return cursor.getString(index)
                }
            }
        }
        return uri.lastPathSegment ?: uri.toString()
    }

    private inline fun ParcelFileDescriptor.usePdfRenderer(block: (PdfRenderer) -> Unit) {
        use { descriptor ->
            PdfRenderer(descriptor).use(block)
        }
    }

    private fun createPageBitmap(page: PdfRenderer.Page): Bitmap {
        val largestSide = maxOf(page.width, page.height).coerceAtLeast(1)
        val scale = min(2f, MAX_RENDER_SIDE.toFloat() / largestSide).coerceAtLeast(0.25f)
        val width = (page.width * scale).roundToInt().coerceAtLeast(1)
        val height = (page.height * scale).roundToInt().coerceAtLeast(1)
        Log.d(TAG, "Renderizando pagina em bitmap ${width}x$height")
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
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
            throw IllegalArgumentException("Imagem invalida ou nao suportada: $uri")
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
                ?: throw IllegalArgumentException("Falha ao decodificar imagem: $uri")
        }
    }

    private fun createOutputImage(
        outputDir: DocumentFile,
        pageNumber: Int,
        imageFormat: ImageFormat
    ): DocumentFile {
        val fileName = "pagina_$pageNumber.${imageFormat.extension}"
        outputDir.findFile(fileName)?.delete()
        return outputDir.createFile(imageFormat.mimeType, fileName)
            ?: throw IllegalStateException("Nao foi possivel criar $fileName.")
    }

    private fun isPasswordProtectedError(error: Throwable): Boolean {
        val text = generateSequence(error) { it.cause }
            .joinToString(" ") { "${it::class.java.name} ${it.message.orEmpty()}" }
            .lowercase()
        return listOf("password", "senha", "encrypted", "decrypt", "protected", "security")
            .any { token -> token in text }
    }

    enum class ImageFormat(
        val extension: String,
        val mimeType: String,
        val compressFormat: Bitmap.CompressFormat,
        val quality: Int
    ) {
        PNG("png", "image/png", Bitmap.CompressFormat.PNG, 100),
        JPG("jpg", "image/jpeg", Bitmap.CompressFormat.JPEG, 92)
    }

    sealed class PdfInspectionResult {
        data class Readable(val pageCount: Int) : PdfInspectionResult()
        data object PasswordProtected : PdfInspectionResult()
        data class Error(val message: String) : PdfInspectionResult()
    }

    @Suppress("unused")
    private fun fitCenter(sourceWidth: Int, sourceHeight: Int, destWidth: Int, destHeight: Int): Rect {
        val scale = min(destWidth.toFloat() / sourceWidth, destHeight.toFloat() / sourceHeight)
        val width = (sourceWidth * scale).toInt()
        val height = (sourceHeight * scale).toInt()
        val left = (destWidth - width) / 2
        val top = (destHeight - height) / 2
        return Rect(left, top, left + width, top + height)
    }
}
