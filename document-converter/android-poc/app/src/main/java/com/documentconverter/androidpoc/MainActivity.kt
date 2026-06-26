package com.documentconverter.androidpoc

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.documentconverter.androidpoc.poc.AndroidPocConverters
import com.documentconverter.androidpoc.poc.PocResult
import com.documentconverter.androidpoc.poc.AndroidPocConverters.ImageFormat
import com.documentconverter.androidpoc.poc.AndroidPocConverters.PdfInspectionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PocScreen(converters = AndroidPocConverters(this))
                }
            }
        }
    }
}

@Composable
private fun PocScreen(converters: AndroidPocConverters) {
    val scope = rememberCoroutineScope()
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedImageNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedPdf by remember { mutableStateOf<Uri?>(null) }
    var selectedPdfIsProtected by remember { mutableStateOf(false) }
    var showProtectedPdfDialog by remember { mutableStateOf(false) }
    var protectedPdfPassword by remember { mutableStateOf("") }
    var lastResult by remember {
        mutableStateOf(
            PocResult.info(
                "POC pronta. Selecione um teste para validar compatibilidade Android."
            )
        )
    }

    fun runTest(block: suspend (progress: (String) -> Unit) -> PocResult) {
        scope.launch {
            lastResult = PocResult.info("Executando teste...")
            val progress: (String) -> Unit = { message ->
                scope.launch {
                    lastResult = PocResult.info(message)
                }
            }
            lastResult = withContext(Dispatchers.IO) {
                try {
                    block(progress)
                } catch (error: Throwable) {
                    PocResult.failure(error.message ?: error::class.java.simpleName)
                }
            }
        }
    }

    val supportedImageTypes = arrayOf("image/jpeg", "image/png", "image/webp")

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        selectedImages = uris
        selectedImageNames = uris.map { uri -> converters.displayName(uri) }
        lastResult = if (uris.isNotEmpty()) {
            PocResult.info("${uris.size} imagem(ns) selecionada(s). Agora escolha onde salvar o PDF.")
        } else {
            PocResult.info("Selecao de imagens cancelada.")
        }
    }

    val pdfCreator = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        val imageUris = selectedImages
        if (imageUris.isNotEmpty() && uri != null) {
            runTest { progress ->
                converters.imagesToPdf(imageUris, uri) { currentImage, totalImages ->
                    progress("Imagem $currentImage de $totalImages")
                }
            }
        } else {
            lastResult = PocResult.info("Destino PDF nao selecionado.")
        }
    }

    val pdfPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        selectedPdf = uri
        selectedPdfIsProtected = false
        protectedPdfPassword = ""
        if (uri != null) {
            lastResult = PocResult.info("PDF selecionado. Verificando protecao e quantidade de paginas...")
            scope.launch {
                val inspection = withContext(Dispatchers.IO) {
                    converters.inspectPdf(uri)
                }
                when (inspection) {
                    is PdfInspectionResult.Readable -> {
                        lastResult = PocResult.info(
                            "PDF selecionado com ${inspection.pageCount} pagina(s). Agora escolha a pasta de destino."
                        )
                    }

                    PdfInspectionResult.PasswordProtected -> {
                        selectedPdfIsProtected = true
                        showProtectedPdfDialog = true
                        lastResult = PocResult.failure(
                            "Este PDF esta protegido por senha."
                        )
                    }

                    is PdfInspectionResult.Error -> {
                        selectedPdf = null
                        lastResult = PocResult.failure(
                            "Nao foi possivel abrir o PDF selecionado.",
                            details = inspection.message
                        )
                    }
                }
            }
        } else {
            lastResult = PocResult.info("Selecao de PDF cancelada.")
        }
    }

    val outputFolderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        val pdfUri = selectedPdf
        if (pdfUri != null && uri != null) {
            runTest { progress ->
                converters.pdfToImages(
                    pdfUri = pdfUri,
                    outputTreeUri = uri,
                    imageFormat = ImageFormat.PNG
                ) { currentPage, totalPages ->
                    progress("Pagina $currentPage de $totalPages")
                }
            }
        } else {
            lastResult = PocResult.info("Pasta de destino nao selecionada.")
        }
    }

    val docxPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runTest { _ -> converters.probeDocx(uri) }
        } else {
            lastResult = PocResult.info("Selecao de DOCX cancelada.")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Document Converter Android POC",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Valida conversoes Android sem JavaFX, AWT, ImageIO, File direto ou LibreOffice.",
            style = MaterialTheme.typography.bodyMedium
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { imagePicker.launch(supportedImageTypes) }
        ) {
            Text("1. Selecionar imagens")
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedImages.isNotEmpty(),
            onClick = { pdfCreator.launch("imagem_android_poc.pdf") }
        ) {
            Text("Gerar PDF multipagina com imagens")
        }
        if (selectedImageNames.isNotEmpty()) {
            Text(
                text = "Selecionadas: ${selectedImageNames.size}",
                style = MaterialTheme.typography.titleSmall
            )
            selectedImageNames.take(8).forEachIndexed { index, name ->
                Text(
                    text = "${index + 1}. $name",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (selectedImageNames.size > 8) {
                Text(
                    text = "... e mais ${selectedImageNames.size - 8}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { pdfPicker.launch(arrayOf("application/pdf")) }
        ) {
            Text("2. Selecionar PDF")
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedPdf != null && !selectedPdfIsProtected,
            onClick = { outputFolderPicker.launch(null) }
        ) {
            Text("Renderizar todas as paginas em PNG")
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                docxPicker.launch(
                    arrayOf(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                    )
                )
            }
        ) {
            Text("3. Testar abertura DOCX com Apache POI")
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                lastResult = PocResult.info(
                    "PDF para DOCX no Android marcado como experimental/desktop-only nesta POC."
                )
            }
        ) {
            Text("4. Avaliar PDF para DOCX")
        }

        ResultPanel(result = lastResult)
    }

    if (showProtectedPdfDialog) {
        AlertDialog(
            onDismissRequest = { showProtectedPdfDialog = false },
            title = { Text("PDF protegido") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Este PDF esta protegido por senha. Digite a senha para tentar abrir.")
                    TextField(
                        value = protectedPdfPassword,
                        onValueChange = { protectedPdfPassword = it },
                        label = { Text("Senha") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                    Text(
                        text = "Limitacao: PdfRenderer nativo do Android nao oferece API para desbloquear PDFs protegidos por senha. Nesta POC, a senha ainda nao pode ser usada para converter.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showProtectedPdfDialog = false
                        lastResult = PocResult.failure(
                            "Este PDF esta protegido por senha. A versao Android ainda nao suporta desbloqueio de PDF protegido."
                        )
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProtectedPdfDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ResultPanel(result: PocResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Status:", style = MaterialTheme.typography.titleMedium)
            Text(result.status)
        }
        Text(result.message, style = MaterialTheme.typography.bodyMedium)
        if (result.elapsedMs != null) {
            Text("Tempo: ${result.elapsedMs} ms")
        }
        if (result.details.isNotBlank()) {
            Text(result.details, style = MaterialTheme.typography.bodySmall)
        }
    }
}
