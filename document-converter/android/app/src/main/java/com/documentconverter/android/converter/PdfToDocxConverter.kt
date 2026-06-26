package com.documentconverter.android.converter

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.android.gms.tasks.Task
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.rendering.ImageType
import com.tom_roush.pdfbox.rendering.PDFRenderer
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.InputStream
import java.util.concurrent.CancellationException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class PdfToDocxConverter(private val context: Context) {
    private val resolver = context.contentResolver

    init {
        PDFBoxResourceLoader.init(context)
    }

    fun inspect(pdfUri: Uri): PdfInspectionResult {
        return try {
            resolver.openInputStream(pdfUri).use { input ->
                requireNotNull(input) { "Nao foi possivel abrir o PDF." }
                PDDocument.load(input).use { document ->
                    if (document.isEncrypted) {
                        PdfInspectionResult.PasswordProtected
                    } else {
                        PdfInspectionResult.Readable(document.numberOfPages)
                    }
                }
            }
        } catch (error: Throwable) {
            Log.w(TAG, "Falha ao inspecionar PDF para DOCX.", error)
            if (isPasswordProtectedError(error)) {
                PdfInspectionResult.PasswordProtected
            } else {
                PdfInspectionResult.Error(error.message ?: error::class.java.simpleName)
            }
        }
    }

    suspend fun convert(
        pdfUri: Uri,
        outputDocxUri: Uri,
        password: String? = null,
        ocrEnabled: Boolean = true,
        ocrRenderDpi: Int = DEFAULT_OCR_RENDER_DPI
    ): ConversionResult {
        val start = System.currentTimeMillis()
        return try {
            val pages = resolver.openInputStream(pdfUri).use { input ->
                requireNotNull(input) { "Nao foi possivel abrir o PDF." }
                loadPdf(input, password).use { document ->
                    require(document.numberOfPages > 0) { "PDF vazio." }
                    extractPagesWithOcr(document, ocrEnabled, ocrRenderDpi.toFloat())
                }
            }

            val textCharacters = pages.sumOf { it.text.length }
            if (textCharacters == 0) {
                return ConversionResult.failure(
                    message = "PDF sem texto extraivel.",
                    details = if (ocrEnabled) OCR_EMPTY_MESSAGE else OCR_DISABLED_MESSAGE
                )
            }

            resolver.openOutputStream(outputDocxUri, "w").use { output ->
                requireNotNull(output) { "Nao foi possivel abrir o arquivo DOCX de destino." }
                writeDocx(outputStream = ZipOutputStream(output), pages = pages)
            }

            val paragraphCount = pages.sumOf { splitParagraphs(it.text).size }
            val ocrPageCount = pages.count { it.source == TextSource.Ocr }
            ConversionResult.success(
                message = "DOCX criado com sucesso.",
                elapsedMs = System.currentTimeMillis() - start,
                details = buildString {
                    append("${pages.size} pagina(s), $paragraphCount paragrafo(s) e $textCharacters caractere(s) extraido(s).")
                    if (ocrPageCount > 0) {
                        append(" OCR aplicado em $ocrPageCount pagina(s).")
                    }
                    append(" Layout complexo pode perder formatacao.")
                },
                outputUris = listOf(outputDocxUri)
            )
        } catch (error: Throwable) {
            Log.e(TAG, "Falha em PDF para DOCX.", error)
            if (isPasswordProtectedError(error)) {
                ConversionResult.failure(
                    message = "Senha incorreta ou PDF protegido nao pode ser aberto.",
                    details = error.message ?: error::class.java.simpleName
                )
            } else {
                ConversionResult.failure(
                    message = "Falha ao converter PDF para DOCX.",
                    details = error.message ?: error::class.java.simpleName
                )
            }
        }
    }

    private fun loadPdf(input: InputStream, password: String?): PDDocument {
        return if (password.isNullOrBlank()) {
            PDDocument.load(input)
        } else {
            PDDocument.load(input, password)
        }
    }

    private suspend fun extractPagesWithOcr(
        document: PDDocument,
        ocrEnabled: Boolean,
        ocrRenderDpi: Float
    ): List<ExtractedPage> {
        val stripper = PDFTextStripper()
        val renderer = PDFRenderer(document)
        val recognizer = if (ocrEnabled) {
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        } else {
            null
        }

        return try {
            (1..document.numberOfPages).map { pageNumber ->
                stripper.startPage = pageNumber
                stripper.endPage = pageNumber
                val text = stripper.getText(document).trim()
                if (text.isNotBlank()) {
                    ExtractedPage(
                        pageNumber = pageNumber,
                        text = text,
                        source = TextSource.PdfText
                    )
                } else if (recognizer == null) {
                    ExtractedPage(
                        pageNumber = pageNumber,
                        text = "",
                        source = TextSource.None
                    )
                } else {
                    val ocrText = recognizePageText(renderer, recognizer, pageNumber - 1, ocrRenderDpi)
                    ExtractedPage(
                        pageNumber = pageNumber,
                        text = ocrText.trim(),
                        source = TextSource.Ocr
                    )
                }
            }
        } finally {
            recognizer?.close()
        }
    }

    private suspend fun recognizePageText(
        renderer: PDFRenderer,
        recognizer: TextRecognizer,
        pageIndex: Int,
        ocrRenderDpi: Float
    ): String {
        val bitmap = renderer.renderImageWithDPI(pageIndex, ocrRenderDpi, ImageType.RGB)
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            recognizer.process(image).awaitText().text
        } finally {
            bitmap.recycle()
        }
    }

    private suspend fun Task<Text>.awaitText(): Text {
        return suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { result ->
                continuation.resume(result)
            }
            addOnFailureListener { error ->
                continuation.resumeWithException(error)
            }
            addOnCanceledListener {
                continuation.resumeWithException(CancellationException("OCR cancelado."))
            }
        }
    }

    private fun writeDocx(outputStream: ZipOutputStream, pages: List<ExtractedPage>) {
        outputStream.use { zip ->
            zip.putTextEntry("[Content_Types].xml", contentTypesXml())
            zip.putTextEntry("_rels/.rels", rootRelsXml())
            zip.putTextEntry("word/document.xml", documentXml(pages))
        }
    }

    private fun ZipOutputStream.putTextEntry(name: String, content: String) {
        putNextEntry(ZipEntry(name))
        write(content.toByteArray(Charsets.UTF_8))
        closeEntry()
    }

    private fun documentXml(pages: List<ExtractedPage>): String {
        val pageXml = pages.mapIndexed { index, page ->
            val paragraphs = splitParagraphs(page.text)
            val content = if (paragraphs.isEmpty()) {
                paragraphXml("Pagina ${page.pageNumber}: nenhum texto extraido mesmo apos OCR.", italic = true)
            } else {
                val sourceNote = if (page.source == TextSource.Ocr) {
                    paragraphXml("Pagina ${page.pageNumber} - texto reconhecido por OCR", italic = true)
                } else {
                    ""
                }
                sourceNote + textContentXml(page.text)
            }

            if (index < pages.lastIndex) {
                content + pageBreakXml()
            } else {
                content
            }
        }.joinToString("")

        return """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
              <w:body>
                $pageXml
                <w:sectPr><w:pgSz w:w="12240" w:h="15840"/><w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440"/></w:sectPr>
              </w:body>
            </w:document>
        """.trimIndent()
    }

    private fun paragraphXml(text: String, heading: Boolean = false, italic: Boolean = false): String {
        val paragraphProperties = if (heading) {
            "<w:pPr><w:pStyle w:val=\"Heading1\"/></w:pPr>"
        } else {
            ""
        }
        val runProperties = if (italic) "<w:rPr><w:i/></w:rPr>" else ""
        return "<w:p>$paragraphProperties<w:r>$runProperties<w:t xml:space=\"preserve\">${escapeXml(text)}</w:t></w:r></w:p>"
    }

    private fun textContentXml(text: String): String {
        return text.split(Regex("\\n\\s*\\n"))
            .joinToString("") { block ->
                if (looksLikeTableBlock(block)) {
                    tableXml(block)
                } else {
                    block.lineSequence()
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .joinToString("") { paragraph ->
                            paragraphXml(paragraph, heading = looksLikeHeading(paragraph))
                        }
                }
            }
    }

    private fun looksLikeTableBlock(block: String): Boolean {
        val rows = parseTableRows(block)
        return rows.size >= 2 && rows.count { it.size >= 2 } >= 2
    }

    private fun parseTableRows(block: String): List<List<String>> {
        return block.lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val cells = if ("|" in line) {
                    line.trim('|').split(Regex("\\s*\\|\\s*"))
                } else {
                    line.split(Regex("\\s{2,}"))
                }.map { it.trim() }.filter { it.isNotBlank() }

                cells.takeIf { it.size >= 2 }
            }
            .toList()
    }

    private fun tableXml(block: String): String {
        val rows = parseTableRows(block)
        if (rows.isEmpty()) {
            return ""
        }
        val columnCount = rows.maxOf { it.size }
        val rowsXml = rows.mapIndexed { rowIndex, row ->
            val cellsXml = (0 until columnCount).joinToString("") { columnIndex ->
                val cellText = row.getOrNull(columnIndex).orEmpty()
                val boldStart = if (rowIndex == 0) "<w:rPr><w:b/></w:rPr>" else ""
                """
                <w:tc>
                  <w:tcPr><w:tcW w:w="2400" w:type="dxa"/></w:tcPr>
                  <w:p><w:r>$boldStart<w:t xml:space="preserve">${escapeXml(cellText)}</w:t></w:r></w:p>
                </w:tc>
                """.trimIndent()
            }
            "<w:tr>$cellsXml</w:tr>"
        }.joinToString("")

        return """
            <w:tbl>
              <w:tblPr>
                <w:tblBorders>
                  <w:top w:val="single" w:sz="4" w:space="0" w:color="A0A0A0"/>
                  <w:left w:val="single" w:sz="4" w:space="0" w:color="A0A0A0"/>
                  <w:bottom w:val="single" w:sz="4" w:space="0" w:color="A0A0A0"/>
                  <w:right w:val="single" w:sz="4" w:space="0" w:color="A0A0A0"/>
                  <w:insideH w:val="single" w:sz="4" w:space="0" w:color="A0A0A0"/>
                  <w:insideV w:val="single" w:sz="4" w:space="0" w:color="A0A0A0"/>
                </w:tblBorders>
              </w:tblPr>
              $rowsXml
            </w:tbl>
        """.trimIndent()
    }

    private fun pageBreakXml(): String {
        return "<w:p><w:r><w:br w:type=\"page\"/></w:r></w:p>"
    }

    private fun contentTypesXml() = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
          <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
          <Default Extension="xml" ContentType="application/xml"/>
          <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
        </Types>
    """.trimIndent()

    private fun rootRelsXml() = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
          <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
        </Relationships>
    """.trimIndent()

    private fun splitParagraphs(text: String): List<String> {
        return text.lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toList()
    }

    private fun looksLikeHeading(paragraph: String): Boolean {
        return paragraph.length <= 80 &&
            paragraph.any { it.isLetter() } &&
            paragraph.uppercase() == paragraph
    }

    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
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

    private data class ExtractedPage(
        val pageNumber: Int,
        val text: String,
        val source: TextSource
    )

    private enum class TextSource {
        PdfText,
        Ocr,
        None
    }

    companion object {
        const val PROTECTED_PDF_MESSAGE =
            "Este PDF esta protegido por senha. Digite a senha para tentar converter."
        const val OCR_EMPTY_MESSAGE =
            "Nao foi possivel reconhecer texto no PDF. Verifique se as paginas estao legiveis e em boa resolucao."
        const val OCR_DISABLED_MESSAGE =
            "Este PDF nao possui texto selecionavel e o OCR esta desativado nas configuracoes."
        private const val TAG = "PdfToDocxConverter"
        private const val DEFAULT_OCR_RENDER_DPI = 200
    }
}
