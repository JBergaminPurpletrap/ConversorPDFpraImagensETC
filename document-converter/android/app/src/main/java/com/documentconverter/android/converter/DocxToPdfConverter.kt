package com.documentconverter.android.converter

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class DocxToPdfConverter(private val context: Context) {
    private val resolver = context.contentResolver

    fun convert(inputDocxUri: Uri, outputPdfUri: Uri): ConversionResult {
        val start = System.currentTimeMillis()
        val document = PdfDocument()
        return try {
            val text = resolver.openInputStream(inputDocxUri).use { input ->
                requireNotNull(input) { "Nao foi possivel abrir o DOCX." }
                extractDocumentText(input)
            }

            if (text.isBlank()) {
                return ConversionResult.failure(
                    message = "DOCX sem texto extraivel.",
                    details = "O arquivo pode conter apenas imagens, objetos ou formato nao suportado."
                )
            }

            writeTextPdf(document, text)
            resolver.openOutputStream(outputPdfUri, "w").use { output ->
                requireNotNull(output) { "Nao foi possivel abrir o PDF de destino." }
                document.writeTo(output)
            }

            ConversionResult.success(
                message = "PDF criado com sucesso.",
                elapsedMs = System.currentTimeMillis() - start,
                details = "Texto do DOCX convertido para PDF simples.",
                outputUris = listOf(outputPdfUri)
            )
        } catch (error: Throwable) {
            Log.e(TAG, "Falha em DOCX para PDF.", error)
            ConversionResult.failure(
                message = "Falha ao converter DOCX para PDF.",
                details = error.message ?: error::class.java.simpleName
            )
        } finally {
            document.close()
        }
    }

    private fun extractDocumentText(input: InputStream): String {
        val documentXml = findDocumentXml(input)
            ?: throw IllegalArgumentException("DOCX invalido: word/document.xml nao encontrado.")
        return parseWordDocumentXml(documentXml)
    }

    private fun findDocumentXml(input: InputStream): ByteArray? {
        ZipInputStream(input).use { zip ->
            var entry: ZipEntry?
            while (true) {
                entry = zip.nextEntry ?: break
                if (entry?.name == "word/document.xml") {
                    return zip.readBytes()
                }
                zip.closeEntry()
            }
        }
        return null
    }

    private fun parseWordDocumentXml(xml: ByteArray): String {
        val parser = XmlPullParserFactory.newInstance().newPullParser()
        parser.setInput(ByteArrayInputStream(xml), "UTF-8")
        val output = StringBuilder()
        var event = parser.eventType

        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                when (parser.name.localName()) {
                    "t" -> output.append(parser.nextText())
                    "tab" -> output.append("    ")
                    "br" -> output.append('\n')
                    "p" -> {
                        if (output.isNotEmpty() && !output.endsWith("\n\n")) {
                            output.append("\n\n")
                        }
                    }
                }
            }
            event = parser.next()
        }

        return output.toString().trim()
    }

    private fun String.localName(): String = substringAfter(':')

    private fun writeTextPdf(document: PdfDocument, text: String) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = BODY_TEXT_SIZE
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        val titlePaint = Paint(paint).apply {
            textSize = TITLE_TEXT_SIZE
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        var pageNumber = 1
        var page = startPage(document, pageNumber)
        var canvas = page.canvas
        canvas.drawColor(Color.WHITE)
        var y = PAGE_MARGIN
        canvas.drawText("Documento convertido", PAGE_MARGIN, y, titlePaint)
        y += TITLE_SPACING

        text.split("\n").forEach { paragraph ->
            val lines = wrapText(paragraph, paint, PAGE_WIDTH - PAGE_MARGIN * 2)
            if (lines.isEmpty()) {
                y += LINE_HEIGHT
            } else {
                lines.forEach { line ->
                    if (y > PAGE_HEIGHT - PAGE_MARGIN) {
                        document.finishPage(page)
                        pageNumber += 1
                        page = startPage(document, pageNumber)
                        canvas = page.canvas
                        canvas.drawColor(Color.WHITE)
                        y = PAGE_MARGIN
                    }
                    canvas.drawText(line, PAGE_MARGIN, y, paint)
                    y += LINE_HEIGHT
                }
                y += PARAGRAPH_SPACING
            }
        }

        document.finishPage(page)
    }

    private fun startPage(document: PdfDocument, pageNumber: Int): PdfDocument.Page {
        val info = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNumber).create()
        return document.startPage(info)
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        if (text.isBlank()) {
            return emptyList()
        }

        val lines = mutableListOf<String>()
        var current = ""
        text.split(Regex("\\s+")).forEach { word ->
            val candidate = if (current.isEmpty()) word else "$current $word"
            if (paint.measureText(candidate) <= maxWidth) {
                current = candidate
            } else {
                if (current.isNotEmpty()) {
                    lines += current
                }
                current = word
            }
        }
        if (current.isNotEmpty()) {
            lines += current
        }
        return lines
    }

    private companion object {
        private const val TAG = "DocxToPdfConverter"
        private const val PAGE_WIDTH = 595f
        private const val PAGE_HEIGHT = 842f
        private const val PAGE_MARGIN = 48f
        private const val BODY_TEXT_SIZE = 14f
        private const val TITLE_TEXT_SIZE = 18f
        private const val LINE_HEIGHT = 20f
        private const val TITLE_SPACING = 32f
        private const val PARAGRAPH_SPACING = 8f
    }
}
