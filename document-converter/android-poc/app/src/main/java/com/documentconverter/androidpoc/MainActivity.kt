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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.documentconverter.androidpoc.poc.AndroidPocConverters
import com.documentconverter.androidpoc.poc.PocResult
import com.documentconverter.androidpoc.poc.AndroidPocConverters.ImageFormat
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
    var selectedImage by remember { mutableStateOf<Uri?>(null) }
    var selectedPdf by remember { mutableStateOf<Uri?>(null) }
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

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        selectedImage = uri
        lastResult = if (uri != null) {
            PocResult.info("Imagem selecionada. Agora escolha onde salvar o PDF.")
        } else {
            PocResult.info("Selecao de imagem cancelada.")
        }
    }

    val pdfCreator = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        val imageUri = selectedImage
        if (imageUri != null && uri != null) {
            runTest { _ -> converters.imageToPdf(imageUri, uri) }
        } else {
            lastResult = PocResult.info("Destino PDF nao selecionado.")
        }
    }

    val pdfPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        selectedPdf = uri
        lastResult = if (uri != null) {
            PocResult.info("PDF selecionado. Agora escolha a pasta para salvar todas as paginas.")
        } else {
            PocResult.info("Selecao de PDF cancelada.")
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
            onClick = { imagePicker.launch(arrayOf("image/*")) }
        ) {
            Text("1. Selecionar imagem")
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedImage != null,
            onClick = { pdfCreator.launch("imagem_android_poc.pdf") }
        ) {
            Text("Gerar PDF da imagem")
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { pdfPicker.launch(arrayOf("application/pdf")) }
        ) {
            Text("2. Selecionar PDF")
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedPdf != null,
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
