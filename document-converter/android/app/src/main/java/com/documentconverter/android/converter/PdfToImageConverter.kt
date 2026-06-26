package com.documentconverter.android.converter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer as AndroidPdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.rendering.ImageType
import com.tom_roush.pdfbox.rendering.PDFRenderer as PdfBoxRenderer
import java.io.InputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.min
import kotlin.math.roundToInt

class PdfToImageConverter(private val context: Context) {
    private val resolver = context.contentResolver

    init {
        PDFBoxResourceLoader.init(context)
    }

    fun inspect(pdfUri: Uri): PdfInspectionResult {
        return try {
            val descriptor = resolver.openFileDescriptor(pdfUri, "r")
                ?: return PdfInspectionResult.Error("Nao foi possivel abrir o PDF.")
            descriptor.use { safeDescriptor ->
                AndroidPdfRenderer(safeDescriptor).use { renderer ->
                    PdfInspectionResult.Readable(renderer.pageCount)
                }
            }
        } catch (error: Throwable) {
            Log.w(TAG, "Falha ao inspecionar PDF.", error)
            if (isPasswordProtectedError(error)) {
                PdfInspectionResult.PasswordProtected
            } else {
                PdfInspectionResult.Error(error.message ?: error::class.java.simpleName)
            }
        }
    }

    fun convert(
        pdfUri: Uri,
        outputTreeUri: Uri,
        outputFormat: ImageOutputFormat = ImageOutputFormat.JPEG,
        password: String? = null,
        onProgress: (currentPage: Int, totalPages: Int) -> Unit
    ): ConversionResult {
        val start = System.currentTimeMillis()
        val outputDir = DocumentFile.fromTreeUri(context, outputTreeUri)
            ?: return ConversionResult.failure("Nao foi possivel abrir a pasta de destino.")
        if (!outputDir.canWrite()) {
            return ConversionResult.failure("Sem permissao de escrita na pasta selecionada.")
        }

        if (!password.isNullOrBlank()) {
            return convertWithPdfBox(
                pdfUri = pdfUri,
                outputDir = outputDir,
                outputFormat = outputFormat,
                password = password,
                onProgress = onProgress,
                start = start
            )
        }

        return try {
            val conversionDir = createConversionDirectory(outputDir)
            val descriptor = resolver.openFileDescriptor(pdfUri, "r")
                ?: return ConversionResult.failure("Nao foi possivel abrir o PDF.")

            val generatedUris = mutableListOf<Uri>()
            descriptor.usePdfRenderer { renderer ->
                val totalPages = renderer.pageCount
                require(totalPages > 0) { "PDF vazio." }

                for (pageIndex in 0 until totalPages) {
                    val currentPage = pageIndex + 1
                    onProgress(currentPage, totalPages)
                    renderer.openPage(pageIndex).use { page ->
                        val bitmap = createPageBitmap(page)
                        try {
                            bitmap.eraseColor(Color.WHITE)
                            page.render(bitmap, null, null, AndroidPdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            val outputFile = createOutputImage(conversionDir, currentPage, outputFormat)
                            resolver.openOutputStream(outputFile.uri, "w").use { output ->
                                requireNotNull(output) {
                                    "Nao foi possivel salvar a pagina $currentPage."
                                }
                                bitmap.compress(
                                    outputFormat.compressFormat,
                                    outputFormat.quality,
                                    output
                                )
                            }
                            generatedUris += outputFile.uri
                        } finally {
                            bitmap.recycle()
                        }
                    }
                }
            }

            ConversionResult.success(
                message = "Conversao concluida.",
                elapsedMs = System.currentTimeMillis() - start,
                details = "${generatedUris.size} pagina(s) salvas em ${conversionDir.name} como pagina_1.${outputFormat.extension}, pagina_2.${outputFormat.extension}...",
                outputUris = generatedUris
            )
        } catch (error: OutOfMemoryError) {
            Log.e(TAG, "Memoria insuficiente ao renderizar PDF.", error)
            ConversionResult.failure(
                message = "Arquivo grande demais para a memoria disponivel.",
                details = "O app processa uma pagina por vez, mas uma pagina exigiu bitmap grande demais."
            )
        } catch (error: Throwable) {
            Log.e(TAG, "Falha em PDF para Imagem.", error)
            if (isPasswordProtectedError(error)) {
                ConversionResult.failure(
                    message = PROTECTED_PDF_MESSAGE,
                    details = error.message ?: error::class.java.simpleName
                )
            } else {
                ConversionResult.failure(
                    message = "Falha ao converter PDF para imagens.",
                    details = error.message ?: error::class.java.simpleName
                )
            }
        }
    }

    private fun convertWithPdfBox(
        pdfUri: Uri,
        outputDir: DocumentFile,
        outputFormat: ImageOutputFormat,
        password: String,
        onProgress: (currentPage: Int, totalPages: Int) -> Unit,
        start: Long
    ): ConversionResult {
        return try {
            val conversionDir = createConversionDirectory(outputDir)
            val generatedUris = mutableListOf<Uri>()
            resolver.openInputStream(pdfUri).use { input ->
                requireNotNull(input) { "Nao foi possivel abrir o PDF." }
                loadPdfWithPassword(input, password).use { document ->
                    val totalPages = document.numberOfPages
                    require(totalPages > 0) { "PDF vazio." }
                    val renderer = PdfBoxRenderer(document)

                    for (pageIndex in 0 until totalPages) {
                        val currentPage = pageIndex + 1
                        onProgress(currentPage, totalPages)
                        val bitmap = renderer.renderImageWithDPI(
                            pageIndex,
                            PDFBOX_RENDER_DPI,
                            ImageType.RGB
                        )
                        try {
                            val outputFile = createOutputImage(conversionDir, currentPage, outputFormat)
                            resolver.openOutputStream(outputFile.uri, "w").use { output ->
                                requireNotNull(output) {
                                    "Nao foi possivel salvar a pagina $currentPage."
                                }
                                bitmap.compress(
                                    outputFormat.compressFormat,
                                    outputFormat.quality,
                                    output
                                )
                            }
                            generatedUris += outputFile.uri
                        } finally {
                            bitmap.recycle()
                        }
                    }
                }
            }

            ConversionResult.success(
                message = "Conversao concluida.",
                elapsedMs = System.currentTimeMillis() - start,
                details = "${generatedUris.size} pagina(s) salvas em ${conversionDir.name} usando senha do PDF.",
                outputUris = generatedUris
            )
        } catch (error: Throwable) {
            Log.e(TAG, "Falha em PDF protegido para Imagem.", error)
            val message = if (isPasswordProtectedError(error)) {
                "Senha incorreta ou PDF protegido nao pode ser aberto."
            } else {
                "Falha ao converter PDF protegido para imagens."
            }
            ConversionResult.failure(message, error.message ?: error::class.java.simpleName)
        }
    }

    private fun loadPdfWithPassword(input: InputStream, password: String): PDDocument {
        return PDDocument.load(input, password)
    }

    private inline fun ParcelFileDescriptor.usePdfRenderer(block: (AndroidPdfRenderer) -> Unit) {
        use { descriptor ->
            AndroidPdfRenderer(descriptor).use(block)
        }
    }

    private fun createPageBitmap(page: AndroidPdfRenderer.Page): Bitmap {
        val largestSide = maxOf(page.width, page.height).coerceAtLeast(1)
        val scale = min(2f, MAX_RENDER_SIDE.toFloat() / largestSide).coerceAtLeast(0.25f)
        val width = (page.width * scale).roundToInt().coerceAtLeast(1)
        val height = (page.height * scale).roundToInt().coerceAtLeast(1)
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }

    private fun createOutputImage(
        outputDir: DocumentFile,
        pageNumber: Int,
        outputFormat: ImageOutputFormat
    ): DocumentFile {
        val fileName = "pagina_$pageNumber.${outputFormat.extension}"
        return outputDir.createFile(outputFormat.mimeType, fileName)
            ?: throw IllegalStateException("Nao foi possivel criar $fileName.")
    }

    private fun createConversionDirectory(outputDir: DocumentFile): DocumentFile {
        val baseName = "document_converter_${
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        }"
        var folderName = baseName
        var suffix = 2
        while (outputDir.findFile(folderName) != null) {
            folderName = "${baseName}_$suffix"
            suffix++
        }
        return outputDir.createDirectory(folderName)
            ?: throw IllegalStateException("Nao foi possivel criar a pasta $folderName.")
    }

    private fun isPasswordProtectedError(error: Throwable): Boolean {
        val text = generateSequence(error) { it.cause }
            .joinToString(" ") { "${it::class.java.name} ${it.message.orEmpty()}" }
            .lowercase()
        return listOf("password", "senha", "encrypted", "decrypt", "protected", "security")
            .any { token -> token in text }
    }

    sealed class PdfInspectionResult {
        data class Readable(val pageCount: Int) : PdfInspectionResult()
        data object PasswordProtected : PdfInspectionResult()
        data class Error(val message: String) : PdfInspectionResult()
    }

    enum class ImageOutputFormat(
        val label: String,
        val extension: String,
        val mimeType: String,
        val compressFormat: Bitmap.CompressFormat,
        val quality: Int
    ) {
        JPEG("JPEG", "jpg", "image/jpeg", Bitmap.CompressFormat.JPEG, 92),
        PNG("PNG", "png", "image/png", Bitmap.CompressFormat.PNG, 100)
    }

    companion object {
        const val PROTECTED_PDF_MESSAGE =
            "Este PDF esta protegido por senha. Digite a senha para tentar converter."

        private const val TAG = "PdfToImageConverter"
        private const val MAX_RENDER_SIDE = 2400
        private const val PDFBOX_RENDER_DPI = 144f
    }
}
